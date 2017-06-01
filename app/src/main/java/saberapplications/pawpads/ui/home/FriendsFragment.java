package saberapplications.pawpads.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBSubscriptionListener;
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
import saberapplications.pawpads.ui.profile.ProfileActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;
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
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initChatRoster();
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
        chatRoster = ChatRosterHelper.getChatRoster(new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int userId) {
                Log.d(TAG, "subscriptionRequested " + userId);
                loadDataAfterChanges();
            }
        });
    }

    private void showAddToFriendsRequestDialog(final QBUser user) {
        if(user == null) return;

        if(requestDialog != null && requestDialog.isShowing()) return;
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity())
                .setCancelable(true);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_to_friends, null);
        ImageView dialogAvatar = (ImageView) view.findViewById(R.id.dialog_avatar);
        if(user.getFileId() != null) {
            float density = getResources().getDisplayMetrics().density;
            AvatarLoaderHelper.loadImage(user.getFileId(), dialogAvatar,
                    Math.round(density * 60), Math.round(density * 60));
        }

        TextView messageText = (TextView) view.findViewById(R.id.message_text);
        String userName = user.getFullName() == null ? user.getLogin() : user.getFullName();
        String sourceString = getString(R.string.user) + " <b>" + userName + "</b> " + getString(R.string.wants_to_add_you_to_friendlist);
        messageText.setText(Html.fromHtml(sourceString));

        builder.setView(view);

        requestDialog = builder.create();
        requestDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        requestDialog.show();

        TextView acceptButton = (TextView) view.findViewById(R.id.send_friend_request);
        acceptButton.setText(getString(R.string.accept_invite));
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRequest(user.getId());
                requestDialog.dismiss();
            }
        });

        TextView rejectButton = (TextView) view.findViewById(R.id.cancel_button);
        rejectButton.setText(getString(R.string.reject_invite));
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectRequest(user.getId());
                requestDialog.dismiss();
            }
        });
    }

    private void loadDataAfterChanges() {
        initChatRoster();
        adapter.clear();
        currentPage = 0;
        loadData();
        binding.swipelayout.setRefreshing(false);
    }

    private void acceptRequest(int userId) {
        try {
            chatRoster.confirmSubscription(userId);
            loadDataAfterChanges();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    private void rejectRequest(int userId) {
        try {
            chatRoster.reject(userId);
            chatRoster.unsubscribe(userId);
            if (chatRoster.getEntry(userId) != null && chatRoster.contains(userId)) {
                chatRoster.removeEntry(chatRoster.getEntry(userId));
            }
            loadDataAfterChanges();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        ArrayList<Integer> usersIds = new ArrayList<>();
        if(chatRoster != null) {
            Collection<QBRosterEntry> entries = chatRoster.getEntries();
            Log.d(TAG, "Collection<QBRosterEntry> entries " + entries.toString());
            for(QBRosterEntry entry : entries) {
                usersIds.add(entry.getUserId());
            }
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
                    adapter.disableLoadMore();
                    Util.onError(errors, getContext());
                }
            });
        } else {
            adapter.disableLoadMore();
            binding.swipelayout.setRefreshing(false);
        }
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(final QBUser user) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    if(chatRoster != null && chatRoster.getEntry(user.getId()) != null &&
                            chatRoster.getEntry(user.getId()).getType() == RosterPacket.ItemType.none &&
                            chatRoster.getEntry(user.getId()).getStatus() == null) {
                        showAddToFriendsRequestDialog(user);
                    } else {
                        Intent intent = new Intent(getActivity(), ProfileActivity.class);
                        intent.putExtra(C.QB_USERID, user.getId());
                        intent.putExtra(C.QB_USER, user);
                        startActivity(intent);
                    }
                }
            }
        }, 50);
    }
}
