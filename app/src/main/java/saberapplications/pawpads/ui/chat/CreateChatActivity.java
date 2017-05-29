package saberapplications.pawpads.ui.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityCreateChatBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.ChatRosterHelper;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by developer on 26.05.17.
 */

public class CreateChatActivity extends BaseActivity implements BaseListAdapter.Callback<QBUser> {
    ActivityCreateChatBinding binding;
    CreateChatListAdapter adapter;
    int dialogsLoadCurrentPage = 0;
    int usersLoadCurrentPage = 0;
    private int currentUserId;
    public final BindableBoolean isBlockedByMe=new BindableBoolean();
    public final BindableBoolean isBusy=new BindableBoolean();
    public final BindableString progressMessage=new BindableString();
    Set<Integer> userIdsSet = new HashSet<>();
    QBRoster chatRoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_chat);
        binding.setActivity(this);
        adapter = new CreateChatListAdapter();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentUserId = prefs.getInt(C.QB_USERID, 0);
        adapter.setCurrentUserId(currentUserId);
        binding.listView.setAdapter(adapter);
        adapter.setCallback(this);
        binding.swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                dialogsLoadCurrentPage = 0;
                usersLoadCurrentPage = 0;
                loadData();
                binding.swipelayout.setRefreshing(false);
            }
        });
        initChatRoster();
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
                    dialogsLoadCurrentPage = 0;
                    usersLoadCurrentPage = 0;
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
            }
        });
    }

    public void loadData() {
        if(chatRoster != null) {
            Collection<QBRosterEntry> entries = chatRoster.getEntries();
            for(QBRosterEntry entry : entries) {
                userIdsSet.add(entry.getUserId());
            }
        }

        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(10);
        requestBuilder.setSkip(dialogsLoadCurrentPage * 10);
        requestBuilder.sortDesc("last_message_date_sent");
        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                if (dialogs.size() > 0) {
                    for(QBDialog dialog : dialogs) {
                        List<Integer> occupansts = dialog.getOccupants();
                        Integer recipientId = occupansts.get(0) == currentUserId ? occupansts.get(1) : occupansts.get(0);
                        userIdsSet.add(recipientId);
                    }
                    getUsers();
                    dialogsLoadCurrentPage++;
                }
            }

            @Override
            public void onError(QBResponseException e) {
                if (getApplicationContext()==null) return;
                Util.onError(e, getApplicationContext());
                adapter.disableLoadMore();
                binding.swipelayout.setRefreshing(false);
            }
        });

    }

    private void getUsers() {
        if(userIdsSet.size() > 0) {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(usersLoadCurrentPage);
            pagedRequestBuilder.setPerPage(10);
            QBUsers.getUsersByIDs(userIdsSet, pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    if (adapter==null) return;
                    if (users.size() > 0) {
                        adapter.addItems(users);
                        usersLoadCurrentPage++;
                    }

                    if (users.size() == 0 || users.size() < 10) {
                        adapter.disableLoadMore();
                    }
                    binding.swipelayout.setRefreshing(false);
                }

                @Override
                public void onError(QBResponseException errors) {
                    if (getApplicationContext()==null) return;
                    Util.onError(errors, getApplicationContext());
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
    }

    public void createChat() {
        if(adapter == null || adapter.getSelectedUsers().size() == 0) return;
        List<QBUser> usersList = adapter.getSelectedUsers();
        if(usersList.size() > 1) {
            ArrayList<Integer> occupantIdsList = new ArrayList<>();
            for (QBUser user : usersList) {
                occupantIdsList.add(user.getId());
            }

            QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
            if (groupChatManager == null) return;
            Intent i = new Intent(CreateChatActivity.this, ChatGroupActivity.class);

            i.putExtra(ChatGroupActivity.RECIPIENT_IDS_LIST, occupantIdsList);
            startActivity(i);
            finish();
        } else if(usersList.size() == 1) {
            QBUser user = usersList.get(0);
            QBPrivateChatManager chatManager = QBChatService.getInstance().getPrivateChatManager();
            if (chatManager == null) return;
            Intent i = new Intent(CreateChatActivity.this, ChatActivity.class);

            i.putExtra(ChatActivity.RECIPIENT, user);
            i.putExtra(Util.IS_BLOCKED, isBlockedByMe.get());
            startActivity(i);
            finish();
        }
    }
}
