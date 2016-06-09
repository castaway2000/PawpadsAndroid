package saberapplications.pawpads.ui.chat;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saberapplications.pawpads.ChatObject;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.util.Constants;


public class ChatActivity extends BaseActivity {
    public static final String DIALOG = "dialog";
    public static final String RECIPIENT = "recipient";
    public static final String DIALOG_ID = "dialog_id";
    public static final String RECIPIENT_ID = "user_id";
    public static final String CURRENT_USER_ID = "current user id";
    //EditText editText_mail_id;
    EditText editText_chat_message;
    ListView listView_chat_messages;
    Button button_send_chat;
    private List<ChatObject> chat_list;
    //BroadcastReceiver recieve_chat;
    private QBDialog dialog;
    private QBUser recipient;
    private ChatAdapter chatAdapter;
    private FrameLayout blockedContainer;
    private LinearLayout messageContainer;
    private Button unblock;
    private TextView blockStatus;

    private QBMessageListener messageListener = new QBMessageListener() {
        @Override
        public void processMessage(QBChat qbChat, final QBChatMessage qbChatMessage) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (chatAdapter != null) {
                        showChat(ChatObject.RECEIVED, qbChatMessage.getBody());
                        //chatAdapter.add(new ChatObject(String.valueOf(qbChatMessage.getDateSent()), ChatObject.RECEIVED));
                    }
                }
            });
        }

        @Override
        public void processError(QBChat qbChat, QBChatException e, QBChatMessage qbChatMessage) {

        }

    };

    QBPrivateChatManagerListener privateChatManagerListener = new QBPrivateChatManagerListener() {
        @Override
        public void chatCreated(final QBPrivateChat privateChat, final boolean createdLocally) {
            if (!createdLocally) {
                privateChat.addMessageListener(messageListener);
            }
        }
    };

    //    private QBPrivateChat chat;
    private Integer currentUserId;
    private int sendTo;
    private ProgressDialog progressDialog;
    private QBPrivateChat privateChat;
    private QBMessageListener msgListener;
    private boolean isBlocked;
    public QBUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("PawPads | Chat");

        if (getIntent() != null) {
            if (getIntent().hasExtra(DIALOG)) {
                dialog = (QBDialog) getIntent().getSerializableExtra(DIALOG);
                recipient = (QBUser) getIntent().getSerializableExtra(RECIPIENT);
                isBlocked = getIntent().getBooleanExtra(Util.IS_BLOCKED, false);
            }
        } else if (savedInstanceState != null) {
            dialog = (QBDialog) savedInstanceState.get(DIALOG);
            recipient = (QBUser) savedInstanceState.get(RECIPIENT);
            currentUserId = savedInstanceState.getInt(CURRENT_USER_ID, 0);
        }

        if (recipient != null && dialog != null) {
            init();
        }
    }

    private void loadDataById() {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    recipient = QBUsers.getUser(Integer.parseInt(getIntent().getStringExtra(RECIPIENT_ID)));
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("_id", getIntent().getStringExtra(DIALOG_ID));
                    //requestBuilder.eq("date_sent", getIntent().getStringExtra(DIALOG_ID));

                    Bundle bundle = new Bundle();
                    ArrayList<QBDialog> dialogs = QBChatService.getChatDialogs(QBDialogType.PRIVATE, requestBuilder, bundle);
                    dialog = dialogs.get(0);
                } catch (QBResponseException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                progressDialog.dismiss();
                if (result) {
                    init();
                    onQBConnect();
                } else {
                    finish();
                }

            }
        };
        task.execute();

    }

    private void init() {
        sendTo = recipient.getId();

        TextView header = (TextView) findViewById(R.id.chat_header_recipient_name);
        if (recipient.getFullName() != null) {
            header.setText(recipient.getFullName());
        } else {
            header.setText(recipient.getLogin());
        }
        ImageView iv = (ImageView) findViewById(R.id.chat_header_profile_image);
        if (recipient.getFileId() != null) {
            float d = getResources().getDisplayMetrics().density;
            int size = Math.round(80 * d);
            AvatarLoaderHelper.loadImage(recipient.getFileId(), iv, size, size);
        }
    }


    @Override
    public void onQBConnect() {
        // init recipient and dialog if intent contains only their ids
        if (getIntent().hasExtra(DIALOG_ID) && dialog == null) {
            loadDataById();
            return;
        }
        currentUserId = getUserId();
        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(privateChatManagerListener);
        if (dialog == null) return;
        editText_chat_message = (EditText) findViewById(R.id.editText_chat_message);
        listView_chat_messages = (ListView) findViewById(R.id.listView_chat_messages);
        button_send_chat = (Button) findViewById(R.id.button_send_chat);
        blockedContainer = (FrameLayout) findViewById(R.id.block_container);
        messageContainer = (LinearLayout) findViewById(R.id.message_container);
        unblock = (Button) findViewById(R.id.button_unblock);
        blockStatus = (TextView) findViewById(R.id.text_view_block_status);
        button_send_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send chat message to server
                if (!editText_chat_message.getText().toString().equals("")) {
                    QBChatMessage msg = new QBChatMessage();
                    msg.setBody(editText_chat_message.getText().toString());
                    //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy/MM/dd", Locale.US);
                    //msg.setProperty("date_sent",String.valueOf(sdf.format(new Date()))+"");

                    msg.setProperty("save_to_history", "1");
                    msg.setRecipientId(sendTo);
                    msg.setDialogId(dialog.getDialogId());
                    msg.setProperty("send_to_chat", "1");


                    try {
                        privateChat.sendMessage(msg);
                        showChat(ChatObject.SENT, msg.getBody());
                    } catch (XMPPException e) {
                        Util.onError(e, ChatActivity.this);
                    } catch (SmackException.NotConnectedException e) {
                        Util.onError(e, ChatActivity.this);
                    }
                    editText_chat_message.setText("");
                }
            }
        });
        QBUsers.getUser(PreferenceManager.getDefaultSharedPreferences(ChatActivity.this).getInt(Util.QB_USERID, -1), new QBEntityCallback<QBUser>() {


            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                currentUser = qbUser;

                if (recipient.getCustomData()!=null) {
                    if (!recipient.getCustomData().contains(String.valueOf(currentUser.getId()))) {
                        unblock.setVisibility(View.VISIBLE);
                        unblock.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeUserFromBlockList();
                                messageContainer.setVisibility(View.VISIBLE);
                                blockedContainer.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        unblock.setVisibility(View.GONE);
                    }
                }else {
                    unblock.setVisibility(View.VISIBLE);
                    unblock.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeUserFromBlockList();
                            messageContainer.setVisibility(View.VISIBLE);
                            blockedContainer.setVisibility(View.GONE);
                        }
                    });
                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {
                Util.onError(list, ChatActivity.this);
            }
        });
        if (isBlocked) {
            messageContainer.setVisibility(View.GONE);
            blockedContainer.setVisibility(View.VISIBLE);

        } else {
            blockedContainer.setVisibility(View.GONE);
            messageContainer.setVisibility(View.VISIBLE);
        }
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setPagesLimit(100);
        QBChatService.getDialogMessages(dialog, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> result, Bundle params) {
                progressDialog.dismiss();
                chat_list = new ArrayList<>();
                for (QBChatMessage qbChatMessage : result) {
                    String type = currentUserId.equals(qbChatMessage.getRecipientId()) ? ChatObject.RECEIVED : ChatObject.SENT;
                    chat_list.add(new ChatObject(qbChatMessage.getBody(), type));
                    //chat_list.add(new ChatObject(String.valueOf(qbChatMessage.getDateSent()),type));
                }
                chatAdapter = new ChatAdapter(ChatActivity.this, R.layout.chat_view, chat_list);
                listView_chat_messages.setAdapter(chatAdapter);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Util.onError(errors, ChatActivity.this);
            }
        });


        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(privateChatManagerListener);
        QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();

        privateChat = privateChatManager.getChat(sendTo);
        msgListener = new QBMessageListener() {

            @Override
            public void processMessage(QBChat qbChat, QBChatMessage qbChatMessage) {

            }

            @Override
            public void processError(QBChat qbChat, final QBChatException e, QBChatMessage qbChatMessage) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Util.onError(e, ChatActivity.this);
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

            }
        };
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(sendTo, msgListener);
        } else {
            privateChat.addMessageListener(msgListener);
        }


    }

    private void showChat(String type, String message) {
        chatAdapter.add(new ChatObject(message, type));
        chatAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DIALOG, dialog);
        outState.putSerializable(RECIPIENT, recipient);
        outState.putInt(CURRENT_USER_ID, currentUserId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data");
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    private void removeUserFromBlockList() {
        currentUser.setCustomData(currentUser.getCustomData().replace(recipient.getId().toString(),""));
        QBUsers.updateUser(currentUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {
                Util.onError(list, ChatActivity.this);

            }
        });
        QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();
        try {
            QBPrivacyList list = privacyListsManager.getPrivacyList("public");
            List<QBPrivacyListItem> items = list.getItems();
            for (QBPrivacyListItem item : items) {
                String id = recipient.getId().toString();

                if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                        item.getValueForType().contains(id)) {
                    item.setAllow(true);
                }
            }
            list.setItems(items);
            privacyListsManager.setPrivacyList(list);
            saveBlockListToPreferences(list);
            Toast.makeText(ChatActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    private void saveBlockListToPreferences(QBPrivacyList list) {
        final JSONArray ids = new JSONArray();
        for (QBPrivacyListItem item : list.getItems()) {
            if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                    !item.isAllow()) {
                Pattern p = Pattern.compile("(\\d*)");
                Matcher m = p.matcher(item.getValueForType());
                if (m.find()) {
                    ids.put(m.group(1));
                }
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.BLOCKED_USERS_IDS, ids.toString());
        editor.apply();
        currentUser.setCustomData(ids.toString());
        QBUsers.updateUser(currentUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {
                Util.onError(list, ChatActivity.this);

            }
        });

    }
}
