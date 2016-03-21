package saberapplications.pawpads.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.utils.Utils;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.chat.ChatActivity;

/**
 * Class {@link DialogsListActivity
 *
 * @author RomanMosiienko
 * @version 1.0
 * @since 15.01.16
 */
public class DialogsListActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    private QBPrivateChatManagerListener chatListener = new QBPrivateChatManagerListener() {
        @Override
        public void chatCreated(QBPrivateChat qbPrivateChat, final boolean createdLocally) {
            if (!createdLocally) {
                qbPrivateChat.addMessageListener(new QBMessageListener() {
                    @Override
                    public void processMessage(final QBChat qbChat, final QBChatMessage qbChatMessage) {
                        DialogsListActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               getDialogsFromServer();
                            }
                        });
                    }

                    @Override
                    public void processError(QBChat qbChat, QBChatException e, QBChatMessage qbChatMessage) {

                    }
                });
            }
        }
    };
    private ArrayList<QBDialog> qbDialogArrayList = new ArrayList<>();
    private ListView listView;
    private DialogsAdapter dialogsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);
        initViews();
    }

    private void initViews() {
        listView = (ListView) findViewById(R.id.dialog_listview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        dialogsAdapter = new DialogsAdapter(qbDialogArrayList, this);
        listView.setAdapter(dialogsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final QBDialog dialog = qbDialogArrayList.get(position);
                QBUsers.getUser(dialog.getRecipientId(), new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Intent intent = new Intent(DialogsListActivity.this, ChatActivity.class);
                        intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
                        intent.putExtra(ChatActivity.RECIPIENT,result);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Util.onError(errors, DialogsListActivity.this);
                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDialogsFromServer();
    }

    private void getDialogsFromServer() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setPagesLimit(100);
        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                mSwipeRefreshLayout.setRefreshing(false);
                qbDialogArrayList.clear();
                qbDialogArrayList.addAll(dialogs);
                dialogsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(List<String> errors) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onStop() {
        if (QBChatService.isInitialized()) {
            QBChatService.getInstance().getPrivateChatManager().removePrivateChatManagerListener(chatListener);
        }
        super.onStop();
    }

    @Override
    public void onRefresh() {
        getDialogsFromServer();
    }

    @Override
    public void onQBConnect() {
        try {
        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(chatListener);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
