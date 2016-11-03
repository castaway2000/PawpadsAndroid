package saberapplications.pawpads.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityChatBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableInteger;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.util.FileUtil;
import saberapplications.pawpads.views.BaseListAdapter;


public class ChatActivity extends BaseActivity {
    public static final String DIALOG = "dialog";
    public static final String RECIPIENT = "recipient";
    public static final String DIALOG_ID = "dialog_id";
    public static final String RECIPIENT_ID = "user_id";
    public static final String CURRENT_USER_ID = "current user id";
    private static final int PICKFILE_REQUEST_CODE = 2;
    //EditText editText_mail_id;
    EditText editText_chat_message;

    Button button_send_chat;

    //    BroadcastReceiver recieve_chat;
    private QBDialog dialog;
    private QBUser recipient;
    private ChatMessagesAdapter chatAdapter;
    private FrameLayout blockedContainer;
    private LinearLayout messageContainer;

    Bundle savedInstanceState;
    ActivityChatBinding binding;
    public final BindableBoolean isSendingMessage = new BindableBoolean();
    public final BindableInteger uploadProgress = new BindableInteger(0);
    public final BindableBoolean isBusy = new BindableBoolean(false);
    int currentPage = 0;
    int messagesPerPage = 15;
    long paused;
    boolean gotMessagesInOffline = false;

    BroadcastReceiver updateChatReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (dialog.getDialogId().equals(intent.getStringExtra(DIALOG_ID))) {
                gotMessagesInOffline = true;
            }
        }
    };

    private QBMessageListener messageListener = new QBMessageListener() {
        @Override
        public void processMessage(QBChat qbChat, final QBChatMessage qbChatMessage) {
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (qbChatMessage.getProperties().containsKey("blocked")) {
                        if (qbChatMessage.getProperty("blocked").equals("1")) {
                            onBlocked();
                        } else if (qbChatMessage.getProperty("blocked").equals("0")) {
                            onUnBlocked();
                        }
                    } else {
                        displayChatMessage(qbChatMessage);
                    }

                }
            });
        }

        @Override
        public void processError(QBChat qbChat, QBChatException e, QBChatMessage qbChatMessage) {
            Util.onError(e, ChatActivity.this);
        }
    };


    //    private QBPrivateChat chat;
    private Integer currentUserId;

    private QBPrivateChat privateChat;

    private boolean isBlocked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        binding.setActivity(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        chatAdapter = new ChatMessagesAdapter(ChatActivity.this, currentQBUser.getId());
        binding.listViewChatMessages.setAdapter(chatAdapter);
        chatAdapter.setCallback(new BaseListAdapter.Callback<QBChatMessage>() {
            @Override
            public void onLoadMore() {
                loadData();
            }

            @Override
            public void onItemClick(QBChatMessage item) {

            }
        });

        if (savedInstanceState != null) {
            dialog = (QBDialog) savedInstanceState.get(DIALOG);
            recipient = (QBUser) savedInstanceState.get(RECIPIENT);
            currentUserId = savedInstanceState.getInt(CURRENT_USER_ID, 0);
            if (savedInstanceState.containsKey("chat")) {
                String json = savedInstanceState.getString("chat");
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<QBChatMessage>>() {
                }.getType();
            }
        }


        if (recipient != null) {
            init();
        }
        this.savedInstanceState = savedInstanceState;

        editText_chat_message = (EditText) findViewById(R.id.editText_chat_message);

        button_send_chat = (Button) findViewById(R.id.button_send_chat);
        blockedContainer = (FrameLayout) findViewById(R.id.block_container);
        messageContainer = (LinearLayout) findViewById(R.id.message_container);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateChatReciever, new IntentFilter(C.UPDATE_CHAT));

    }


    private void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recipient.getFileId() != null) {
                    float d = getResources().getDisplayMetrics().density;
                    int size = Math.round(80 * d);
                    AvatarLoaderHelper.loadImage(recipient.getFileId(), binding.recipientAvatar, size, size);
                }
                binding.setUsername(Util.getUserName(recipient));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isExternalDialogOpened = false;
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == PICKFILE_REQUEST_CODE) {
                sendAttachment(data.getData());
            }


        }
    }

    @Override
    public void onQBConnect(final boolean isActivityReopened) {
        // init recipient and dialog if intent contains only their ids
        isBusy.set(true);
        currentUserId = currentQBUser.getId();

        final QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();


        // Detect blocked state
        new AsyncTask<Void, Void, Void>() {

            Exception error;
            private ArrayList<QBChatMessage> chatMessages;

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    if (getIntent() != null && !isActivityReopened) {
                        if (getIntent().hasExtra(RECIPIENT)) {
                            recipient = (QBUser) getIntent().getSerializableExtra(RECIPIENT);
                            isBlocked = getIntent().getBooleanExtra(Util.IS_BLOCKED, false);
                        }
                        if (recipient == null && getIntent().hasExtra(RECIPIENT_ID)) {
                            recipient = QBUsers.getUser(getIntent().getIntExtra(RECIPIENT_ID, 0));
                        }
                        privateChat = privateChatManager.getChat(recipient.getId());
                        if (privateChat == null) {
                            privateChat = privateChatManager.createChat(recipient.getId(), messageListener);
                        } else {
                            privateChat.addMessageListener(messageListener);
                        }
                        init();

                        if (getIntent().hasExtra(DIALOG)) {
                            dialog = (QBDialog) getIntent().getSerializableExtra(DIALOG);
                        }
                        if (dialog == null && getIntent().hasExtra(DIALOG_ID)) {
                            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                            requestBuilder.eq("_id", getIntent().getStringExtra(DIALOG_ID));
                            //requestBuilder.eq("date_sent", getIntent().getStringExtra(DIALOG_ID));

                            Bundle bundle = new Bundle();
                            ArrayList<QBDialog> dialogs = QBChatService.getChatDialogs(QBDialogType.PRIVATE, requestBuilder, bundle);
                            dialog = dialogs.get(0);
                        }
                    }


                    if (dialog == null) {
                        dialog = privateChatManager.createDialog(recipient.getId());
                    }

                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", recipient.getId());
                    requestBuilder.eq("blocked_user", currentQBUser.getId());
                    ArrayList<QBCustomObject> blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    binding.setIsBlockedByOther(blocks.size() > 0);

                    if (!isActivityReopened) {
                        requestBuilder = new QBRequestGetBuilder();
                        requestBuilder.eq("source_user", currentQBUser.getId());
                        requestBuilder.eq("blocked_user", recipient.getId());

                        blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                        binding.setIsBlockedByMe(blocks.size() > 0);
                    }


                    if (!isActivityReopened) {
                        requestBuilder = new QBRequestGetBuilder();
                        requestBuilder.setLimit(messagesPerPage);
                        requestBuilder.sortDesc("date_sent");
                        chatMessages = QBChatService.getDialogMessages(dialog, requestBuilder, new Bundle());
                        currentPage++;
                    } else if (gotMessagesInOffline) {
                        requestBuilder = new QBRequestGetBuilder();
                        requestBuilder.addRule("date_sent", ">", String.valueOf(paused));
                        requestBuilder.sortDesc("date_sent");
                        chatMessages = QBChatService.getDialogMessages(dialog, requestBuilder, new Bundle());
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    error = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                isBusy.set(false);
                if (error != null) {
                    Util.onError(error, ChatActivity.this);
                    return;
                }
                if (!isActivityReopened) {
                    chatAdapter.addItems(chatMessages);
                    if (chatMessages.size() < messagesPerPage) {
                        chatAdapter.disableLoadMore();
                    }
                } else if (gotMessagesInOffline) {
                    chatAdapter.addItemsToStart(chatMessages);
                    chatAdapter.alignToPageSize(messagesPerPage, currentPage);
                    gotMessagesInOffline = false;
                }


            }
        }.execute();

    }

    private void displayChatMessage(QBChatMessage message) {
        Date dt = new Date();
        if (message.getSenderId() == null) {
            message.setSenderId(currentQBUser.getId());
        }
        if (message.getDateSent() == 0) {
            message.setDateSent(dt.getTime() / 1000);
        }
        if (message.getAttachments() == null) {
            message.setAttachments(new ArrayList<QBAttachment>());
        }
        chatAdapter.addItem(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateChatReciever);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DIALOG, dialog);
        outState.putSerializable(RECIPIENT, recipient);
        outState.putInt(CURRENT_USER_ID, currentUserId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        paused = System.currentTimeMillis();
        isBusy.set(true);
    }



    private void onBlocked() {
        Toast.makeText(this, getString(R.string.text_you_blocked), Toast.LENGTH_LONG).show();
        messageContainer.setVisibility(View.GONE);
        blockedContainer.setVisibility(View.VISIBLE);
    }

    private void onUnBlocked() {
        Toast.makeText(this, getString(R.string.text_you_unblocked), Toast.LENGTH_LONG).show();
        binding.setIsBlockedByOther(false);
    }

    public void sendChatMessage() {
        if (isBusy.get()) {
            Toast.makeText(this,"Please wait until connection to server restored",Toast.LENGTH_LONG).show();
            return;
        }
        // send chat message to server
        if (!editText_chat_message.getText().toString().equals("")) {
            QBChatMessage msg = new QBChatMessage();
            msg.setBody(editText_chat_message.getText().toString());
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy/MM/dd", Locale.US);
            //msg.setProperty("date_sent",String.valueOf(sdf.format(new Date()))+"");

            msg.setProperty("save_to_history", "1");
            msg.setRecipientId(recipient.getId());
            msg.setDialogId(dialog.getDialogId());
            msg.setProperty("send_to_chat", "1");


            try {
                privateChat.sendMessage(msg);
                displayChatMessage(msg);
            } catch (SmackException.NotConnectedException e) {
                Util.onError(e, ChatActivity.this);
            } catch (Exception e) {
                Util.onError(e, ChatActivity.this);
            }
            editText_chat_message.setText("");
        }
    }

    public void sendAttachment(Uri uri) {
        if (isSendingMessage.get()) return;
        isSendingMessage.set(true);

        // Get the path

            final String path = FileUtil.getPath(this, uri);
            final File filePhoto = new File(path);
            new AsyncTask<Void, Integer, QBChatMessage>() {
                Exception exception;

                @Override
                protected QBChatMessage doInBackground(Void... params) {

                    try {
                        QBFile qbFile = QBContent.uploadFileTask(filePhoto, false, null, new QBProgressCallback() {
                            @Override
                            public void onProgressUpdate(int i) {
                                uploadProgress.set(i);
                            }
                        });

                        // create a message
                        QBChatMessage chatMessage = new QBChatMessage();
                        chatMessage.setProperty("save_to_history", "1"); // Save a message to history

                        // attach a photo
                        QBAttachment attachment = new QBAttachment("photo");
                        attachment.setId(qbFile.getId().toString());
                        attachment.setName(filePhoto.getName());
                        chatMessage.addAttachment(attachment);
                        chatMessage.setBody(filePhoto.getName());
                        if (isImage(filePhoto)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(filePhoto.getAbsolutePath());
                            Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);
                            File tmp = File.createTempFile("thumb", ".jpg");
                            FileOutputStream stream = new FileOutputStream(tmp);
                            thumb.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                            stream.close();

                            QBFile qbFileThumb = QBContent.uploadFileTask(tmp, false, null);
                            QBAttachment attachmentThumb = new QBAttachment("thumb");
                            attachmentThumb.setId(qbFileThumb.getId().toString());
                            attachmentThumb.setName(qbFileThumb.getName());
                            chatMessage.addAttachment(attachmentThumb);
                            bitmap.recycle();
                        }

                        privateChat.sendMessage(chatMessage);
                        return chatMessage;
                    } catch (Exception e) {
                        e.printStackTrace();
                        exception = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(QBChatMessage qbChatMessage) {
                    isSendingMessage.set(false);
                    if (exception != null) {
                        Util.onError(exception, ChatActivity.this);
                        return;
                    }
                    displayChatMessage(qbChatMessage);
                }

            }.execute();




    }

    public void selectFile() {
        isExternalDialogOpened = true;
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT);
            return;
        }
        isExternalDialogOpened = true;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);


    }

    public void loadData() {
        new AsyncTask<Void, Void, List<QBChatMessage>>() {
            Exception e;

            @Override
            protected List<QBChatMessage> doInBackground(Void... params) {
                ArrayList<QBChatMessage> chatMessages = null;
                QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                requestBuilder.setLimit(messagesPerPage);
                requestBuilder.setSkip(messagesPerPage * currentPage);
                requestBuilder.sortDesc("date_sent");
                try {
                    chatMessages = QBChatService.getDialogMessages(dialog, requestBuilder, new Bundle());
                    currentPage++;
                } catch (QBResponseException exc) {
                    exc.printStackTrace();
                    this.e = exc;
                }
                return chatMessages;
            }

            @Override
            protected void onPostExecute(List<QBChatMessage> chatMessages) {
                if (e != null) {
                    Util.onError(e, ChatActivity.this);
                    return;
                }
                if (chatMessages != null && chatMessages.size() > 0) {
                    chatAdapter.addItems(chatMessages);
                }

                if (chatMessages == null || chatMessages.size() < messagesPerPage) {
                    chatAdapter.disableLoadMore();
                }

            }
        }.execute();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void unblockUser() {
        binding.unblock.setEnabled(false);
        binding.unblock.setText(R.string.unblocking);
        new AsyncTask<Void, Void, Boolean>() {
            Exception error;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentQBUser.getId());
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

                } catch (Exception e) {
                    e.printStackTrace();
                    error = e;
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (error != null) {
                    Util.onError(error, ChatActivity.this);
                    return;
                }
                binding.setIsBlockedByMe(false);
                Toast.makeText(ChatActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();

            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectFile();
                }
            }
        }
    }

    public static boolean isImage(File file) {
        String fileName = file.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return ext.equals("jpeg") || ext.equals("jpg") || ext.equals("png") || ext.equals("bmp");
    }

    @Override
    public void onChatMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
        if (!qbChatMessage.getDialogId().equals(dialog.getDialogId())) return;
        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (qbChatMessage.getProperties().containsKey("blocked")) {
                    if (qbChatMessage.getProperty("blocked").equals("1")) {
                        onBlocked();
                    } else if (qbChatMessage.getProperty("blocked").equals("0")) {
                        onUnBlocked();
                    }
                } else {
                    displayChatMessage(qbChatMessage);
                }

            }
        });
    }


}
