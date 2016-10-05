package saberapplications.pawpads.ui.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.json.JSONArray;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;


public class ChatActivity extends BaseActivity {
    public static final String DIALOG = "dialog";
    public static final String RECIPIENT = "recipient";
    public static final String DIALOG_ID = "dialog_id";
    public static final String RECIPIENT_ID = "user_id";
    public static final String CURRENT_USER_ID = "current user id";
    private static final int PICKFILE_REQUEST_CODE = 2;
    //EditText editText_mail_id;
    EditText editText_chat_message;
    ListView listView_chat_messages;
    Button button_send_chat;

//    BroadcastReceiver recieve_chat;
    private QBDialog dialog;
    private QBUser recipient;
    private ChatAdapter chatAdapter;
    private FrameLayout blockedContainer;
    private LinearLayout messageContainer;
    private Button unblock;
    private TextView blockStatus;
    private ImageView sendFile;
    private ArrayList<QBChatMessage> chatMessages;
    Bundle savedInstanceState;

    private  QBMessageListener<QBPrivateChat> messageListener = new QBMessageListener<QBPrivateChat>() {
        @Override
        public void processMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (qbChatMessage.getProperties().containsKey("blocked")){
                        if (qbChatMessage.getProperty("blocked").equals("1")){
                            onBlocked();
                        }else if (qbChatMessage.getProperty("blocked").equals("0")){
                            onUnBlocked();
                        }
                    }else {
                        displayChatMessage( qbChatMessage);
                    }

                }
            });
        }

        @Override
        public void processError(QBPrivateChat qbPrivateChat, QBChatException e, QBChatMessage qbChatMessage) {
            Util.onError(e,ChatActivity.this);
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
        this.savedInstanceState=savedInstanceState;

        editText_chat_message = (EditText) findViewById(R.id.editText_chat_message);
        listView_chat_messages = (ListView) findViewById(R.id.listView_chat_messages);
        button_send_chat = (Button) findViewById(R.id.button_send_chat);
        blockedContainer = (FrameLayout) findViewById(R.id.block_container);
        messageContainer = (LinearLayout) findViewById(R.id.message_container);
        unblock = (Button) findViewById(R.id.button_unblock);
        sendFile = (ImageView) findViewById(R.id.imageViewSendFile);
        blockStatus = (TextView) findViewById(R.id.text_view_block_status);


    }

    private void loadDataById() {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("_id", getIntent().getStringExtra(DIALOG_ID));
                    //requestBuilder.eq("date_sent", getIntent().getStringExtra(DIALOG_ID));

                    Bundle bundle = new Bundle();
                    ArrayList<QBDialog> dialogs = QBChatService.getChatDialogs(QBDialogType.PRIVATE, requestBuilder, bundle);
                    dialog = dialogs.get(0);
                    Integer recipientId=0;
                    for (Integer uid:dialog.getOccupants()){
                        if (uid!=currentUserId){
                            recipientId=uid;
                        }

                    }

                    recipient = QBUsers.getUser(recipientId);

                } catch (QBResponseException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (progressDialog!=null) progressDialog.dismiss();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isExternalDialogOpened=false;
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == PICKFILE_REQUEST_CODE) {

                Uri uri = data.getData();
                // Get the path
                try {
                    final String path = getPath(this, uri);
                    final File filePhoto = new File(path);
                    QBContent.uploadFileTask(filePhoto, false, null, new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile file, Bundle params) {

                            // create a message
                            QBChatMessage chatMessage = new QBChatMessage();
                            chatMessage.setProperty("save_to_history", "1"); // Save a message to history

                            // attach a photo
                            QBAttachment attachment = new QBAttachment("photo");
                            attachment.setId(file.getId().toString());
                            attachment.setName(filePhoto.getName());
                            chatMessage.addAttachment(attachment);
                            chatMessage.setBody(filePhoto.getName());
                            try {
                                privateChat.sendMessage(chatMessage);
                            }  catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            // send a message
                            // ...
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }



                    });
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    @Override
    public void onQBConnect() {
        // init recipient and dialog if intent contains only their ids
        currentUserId = getUserId();
        if (getIntent().hasExtra(DIALOG_ID) && dialog == null) {
            loadDataById();
            return;
        }



        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(privateChatManagerListener);
        if (dialog == null) return;

        QBChatService.getInstance().getPrivateChatManager().addPrivateChatManagerListener(privateChatManagerListener);
        QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();

        privateChat = privateChatManager.getChat(sendTo);

        if (privateChat == null) {
            privateChat = privateChatManager.createChat(sendTo, messageListener);
        } else {
            privateChat.addMessageListener(messageListener);
        }

        if (chatMessages!=null) return;

        // Detect blocked state
        new AsyncTask<Void, Void, Void>() {

            String error;
            boolean isBlockedByMe;
            boolean isIBlockedByOther;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    currentUser = QBUsers.getUser(PreferenceManager.getDefaultSharedPreferences(ChatActivity.this).getInt(Util.QB_USERID, -1));
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", recipient.getId());
                    requestBuilder.eq("blocked_user", currentUser.getId());

                    ArrayList<QBCustomObject> blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    isIBlockedByOther = blocks.size() > 0;

                    requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentUser.getId());
                    requestBuilder.eq("blocked_user", recipient.getId());

                    blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    isBlockedByMe = blocks.size() > 0;


                    requestBuilder = new QBRequestGetBuilder();
                    requestBuilder. setLimit(100);
                    if (savedInstanceState!=null && savedInstanceState.containsKey("chat")){
                        String json=savedInstanceState.getString("chat");
                        Gson gson=new Gson();
                        Type listType = new TypeToken<ArrayList<QBChatMessage>>() {}.getType();
                        chatMessages= gson.fromJson(json,listType);
                    }else {
                        chatMessages = QBChatService.getDialogMessages(dialog, requestBuilder, new Bundle());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    error = e.getLocalizedMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                if (error != null) {
                    progressDialog.dismiss();
                    new AlertDialog.Builder(ChatActivity.this)
                            .setMessage(error)
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                if (isIBlockedByOther) {
                    messageContainer.setVisibility(View.GONE);
                    blockedContainer.setVisibility(View.VISIBLE);
                } else {
                    blockedContainer.setVisibility(View.GONE);
                    messageContainer.setVisibility(View.VISIBLE);
                }
                if (isBlockedByMe) {
                    unblock.setVisibility(View.VISIBLE);
                    unblock.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeUserFromBlockList();
                            unblock.setVisibility(View.GONE);
                            if (!isIBlockedByOther) {
                                messageContainer.setVisibility(View.VISIBLE);
                                blockedContainer.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    unblock.setVisibility(View.GONE);
                }

                chatAdapter = new ChatAdapter(ChatActivity.this, R.layout.chat_view, chatMessages,currentUser);
                listView_chat_messages.setAdapter(chatAdapter);
                progressDialog.dismiss();

            }
        }.execute();

    }

    private void displayChatMessage(QBChatMessage message) {
        Date dt=new Date(message.getDateSent());
        chatAdapter.add(message);
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
        Gson gson=new Gson();
        outState.putSerializable("chat",gson.toJson(chatMessages));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(chatMessages==null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Loading data");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    private void removeUserFromBlockList() {

        new AsyncTask<Void, Void, Boolean>() {
            String error;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentUser.getId());
                    requestBuilder.eq("blocked_user", recipient.getId());

                    ArrayList<QBCustomObject> blockedList = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    for (QBCustomObject item : blockedList) {
                        QBCustomObjects.deleteObject("BlockList", item.getCustomObjectId());
                    }

                    QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();

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

                } catch (Exception e) {
                    e.printStackTrace();
                    error = e.getLocalizedMessage();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Toast.makeText(ChatActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();
                } else {

                }
            }
        }.execute();
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
        editor.putString(C.BLOCKED_USERS_IDS, ids.toString());
        editor.apply();
        currentUser.setCustomData(ids.toString());
        QBUsers.updateUser(currentUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {

            }

            @Override
            public void onError(QBResponseException e) {

            }


        });

    }




    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    private void onBlocked(){
        Toast.makeText(this,getString(R.string.text_you_blocked),Toast.LENGTH_LONG).show();
        messageContainer.setVisibility(View.GONE);
        blockedContainer.setVisibility(View.VISIBLE);
    }
    private void onUnBlocked(){
        Toast.makeText(this,getString(R.string.text_you_unblocked),Toast.LENGTH_LONG).show();
        messageContainer.setVisibility(View.VISIBLE);
        blockedContainer.setVisibility(View.GONE);
    }

    public void sendChatMessage(View v) {
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
                displayChatMessage( msg);
            } catch (SmackException.NotConnectedException e) {
                Util.onError(e, ChatActivity.this);
            } catch (Exception e) {
                Util.onError(e, ChatActivity.this);
            }
            editText_chat_message.setText("");
        }
    }
    public void uploadFile(View view) {
        isExternalDialogOpened=true;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);

    }

}
