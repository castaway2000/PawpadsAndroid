package saberapplications.pawpads.ui.chat;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.ChatObject;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;


public class ChatActivity extends BaseActivity {
    //TODO: 
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

    private QBMessageListener messageListener = new QBMessageListener() {
        @Override
        public void processMessage(QBChat qbChat, final QBChatMessage qbChatMessage) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (chatAdapter != null) {
                        chatAdapter.add(new ChatObject(qbChatMessage.getBody(), ChatObject.RECEIVED));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("PawPads | Chat");

        if (getIntent() != null) {
            if (getIntent().hasExtra(DIALOG)) {
                dialog = (QBDialog) getIntent().getSerializableExtra(DIALOG);
                recipient = (QBUser) getIntent().getSerializableExtra(RECIPIENT);
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
//        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(chatListener);
        if (dialog == null) return;
        editText_chat_message = (EditText) findViewById(R.id.editText_chat_message);
        listView_chat_messages = (ListView) findViewById(R.id.listView_chat_messages);
        button_send_chat = (Button) findViewById(R.id.button_send_chat);
        button_send_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send chat message to server
                if(!editText_chat_message.getText().toString().equals("")){
                    QBChatMessage msg = new QBChatMessage();
                msg.setBody(editText_chat_message.getText().toString());
                msg.setProperty("save_to_history", "1");
                msg.setRecipientId(sendTo);
                msg.setDialogId(dialog.getDialogId());
                msg.setProperty("send_to_chat", "1");
                QBChatService.createMessage(msg, new QBEntityCallbackImpl<QBChatMessage>() {
                    @Override
                    public void onSuccess(QBChatMessage result, Bundle params) {
                        showChat(ChatObject.SENT, result.getBody());
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Util.onError(errors, ChatActivity.this);
                    }
                });
                editText_chat_message.setText("");
                }
            }
        });

        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setPagesLimit(100);
        QBChatService.getDialogMessages(dialog, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> result, Bundle params) {

                progressDialog.dismiss();
                chat_list = new ArrayList<>();

                for (QBChatMessage qbChatMessage : result) {
                    String type = currentUserId.equals(qbChatMessage.getRecipientId()) ? ChatObject.RECEIVED : ChatObject.SENT;
                    chat_list.add(new
                            ChatObject(qbChatMessage.getBody(), type));

                }
                chatAdapter = new ChatAdapter(ChatActivity.this, R.layout.chat_view, chat_list);
                listView_chat_messages.setAdapter(chatAdapter);

                //chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(List<String> errors) {
                progressDialog.dismiss();
                Util.onError(errors, ChatActivity.this);
            }
        });


        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(privateChatManagerListener);
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
}
