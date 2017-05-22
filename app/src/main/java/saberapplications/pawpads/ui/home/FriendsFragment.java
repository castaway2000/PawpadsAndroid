package saberapplications.pawpads.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.FragmentFriendsBinding;
import saberapplications.pawpads.util.ChatRosterHelper;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment implements BaseListAdapter.Callback<QBUser> {
    public static final String TAG = FriendsFragment.class.getSimpleName();
    FragmentFriendsBinding binding;
    FriendsAdapter adapter;
    int currentPage = 1;
    private int currentUserId;
    QBRoster chatRoster;
    AlertDialog requestDialog;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        binding = DataBindingUtil.bind(view);
        adapter = new FriendsAdapter();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        currentUserId = prefs.getInt(C.QB_USERID, 0);
        adapter.setCurrentUserId(currentUserId);
        binding.friendsListView.setAdapter(adapter);
        adapter.setCallback(this);
        binding.swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                currentPage = 0;
                loadData();
                binding.swipelayout.setRefreshing(false);
            }
        });
        initChatRoster();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(adapter.isShowInitialLoad()) {
                    adapter.clear();
                    currentPage = 0;
                    loadData();
                }
            }
        }, 5000);
        adapter.notifyDataSetChanged();
    }

    private void initChatRoster() {
        if(chatRoster == null) {
            chatRoster = ChatRosterHelper.getChatRoster(new QBSubscriptionListener() {
                @Override
                public void subscriptionRequested(int userId) {
                    Log.d(TAG, "subscriptionRequested " + userId);
                    showSubscriptionRequestDialog(userId);
                }
            });
        }
    }

    private void showSubscriptionRequestDialog(final int userId) {
        if(getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if(requestDialog != null && requestDialog.isShowing()) return;
                    requestDialog = new AlertDialog.Builder(getActivity())
                            .setTitle("Subscription Request")
                            .setMessage("User " + userId + " wants to add your in friends list")
                            .setPositiveButton("Reject", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    rejectRequest(userId);
                                }
                            })
                            .setNegativeButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    confirmRequest(userId);
                                }
                            })
                            .create();
                    requestDialog.show();
                }
            });
        }
    }

    private void confirmRequest(int userId) {
        try {
            chatRoster.confirmSubscription(userId);
            loadData();
        } catch (SmackException.NotConnectedException e) {

        } catch (SmackException.NotLoggedInException e) {

        } catch (XMPPException e) {

        } catch (SmackException.NoResponseException e) {

        }
    }

    private void rejectRequest(int userId) {
        try {
            chatRoster.reject(userId);
        } catch (SmackException.NotConnectedException e) {

        }
    }

    public void loadData() {
        ArrayList<Integer> usersIds = new ArrayList<>();
        if(chatRoster != null) {
            Collection<QBRosterEntry> entries = chatRoster.getEntries();
            Log.d(TAG, "Collection<QBRosterEntry> entries " + entries.toString());
            for(QBRosterEntry entry : entries) {
                if(entry.getRosterEntry().getType() == RosterPacket.ItemType.none) {
                    showSubscriptionRequestDialog(entry.getUserId());
                } else {
                    usersIds.add(entry.getUserId());
                }
            }
            //addUserToFriendsList(28003081);
        }
        if(usersIds.size() > 0) {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(currentPage);
            pagedRequestBuilder.setPerPage(10);
            QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    if (adapter==null) return;
                    if (users.size() > 0) {
                        adapter.addItems(users);
                        currentPage++;
                    }

                    if (users.size() == 0 || users.size() < 10) {
                        adapter.disableLoadMore();
                    }
                    binding.swipelayout.setRefreshing(false);
                }

                @Override
                public void onError(QBResponseException errors) {
                    if (getContext()==null) return;
                    Util.onError(errors, getContext());
                }
            });
        } else {
            adapter.disableLoadMore();
            binding.swipelayout.setRefreshing(false);
        }
    }

    private void addUserToFriendsList(int userId) {
        if (chatRoster.contains(userId)) {
            try {
                chatRoster.subscribe(userId);
            } catch (SmackException.NotConnectedException e) {

            }
        } else {
            try {
                chatRoster.createEntry(userId, null);
            } catch (XMPPException e) {

            } catch (SmackException.NotLoggedInException e) {

            } catch (SmackException.NotConnectedException e) {

            } catch (SmackException.NoResponseException e) {

            }
        }
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(final QBUser user) {

    }
}
