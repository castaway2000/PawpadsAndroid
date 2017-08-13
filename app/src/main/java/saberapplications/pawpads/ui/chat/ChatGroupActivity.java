package saberapplications.pawpads.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.imoji.sdk.editor.ImojiCreateService;
import io.imoji.sdk.editor.ImojiEditorActivity;
import io.imoji.sdk.editor.util.EditorBitmapCache;
import io.imoji.sdk.grid.QuarterScreenWidget;
import io.imoji.sdk.grid.components.SearchResultAdapter;
import io.imoji.sdk.grid.components.WidgetDisplayOptions;
import io.imoji.sdk.grid.components.WidgetListener;
import io.imoji.sdk.objects.Imoji;
import io.imoji.sdk.objects.RenderingOptions;
import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityChatGroupBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableInteger;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.GroupEditActivity;
import saberapplications.pawpads.util.FileUtil;
import saberapplications.pawpads.views.BaseListAdapter;
import saberapplications.pawpads.views.giphyselector.Giphy;
import saberapplications.pawpads.views.giphyselector.GiphySelector;


public class ChatGroupActivity extends BaseActivity {
    public static final String DIALOG = "dialog";
    public static final String DIALOG_ID = "dialog_id";
    public static final String CURRENT_USER_ID = "current user id";
    public static final String RECIPIENT_IDS_LIST = "recipient_ids_list";
    public static final String NEW_ADDED_USERS_LIST = "NEW_ADDED_USERS_LIST";
    public static final String CHANGED_GROUP_NAME = "CHANGED_GROUP_NAME";
    public static final String DIALOG_GROUP_TYPE = "DIALOG_GROUP_TYPE";
    public static final String IS_FIRST_OPENED = "IS_FIRST_OPENED";
    public static final int ADD_NEW_GROUP_MEMBER = 25;
    public static final int GROUP_SETTINGS_CHANGE = 26;
    private static final int PICKFILE_REQUEST_CODE = 2;
    private static final int IMAGE_CAPTURE_REQUEST_CODE = 33;
    private static final int READ_STORAGE_PERMISSION_REQUEST = 200;
    private static final int WRITE_ST_CAMERA_PERMISSION_REQUEST = 205;
    private static final int PICK_FILE_FOR_EDIT_REQUEST = 3;
    public final BindableBoolean isSendingMessage = new BindableBoolean();
    public final BindableInteger uploadProgress = new BindableInteger(0);
    public final BindableBoolean isBusy = new BindableBoolean(true);
    //EditText editText_mail_id;
    EditText editText_chat_message;
    Button button_send_chat;
    Bundle savedInstanceState;
    ActivityChatGroupBinding binding;
    int currentPage = 0;
    int messagesPerPage = 15;
    long paused;
    boolean gotMessagesInOffline = false;
    private Uri mPhotoUri;
    //    BroadcastReceiver recieve_chat;
    private QBDialog dialog;
    private QuarterScreenWidget mStickersWidget;
    private FrameLayout mStickersContainer;
    BroadcastReceiver updateChatReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (dialog.getDialogId().equals(intent.getStringExtra(DIALOG_ID))) {
                gotMessagesInOffline = true;
            }
        }
    };

    private ArrayList<Integer> userIdsList;
    private ArrayList<QBUser> recipientList;
    private ArrayList<Integer> selectedNewGroupUserList;
    private ChatGroupMessagesAdapter chatAdapter;
    private FrameLayout blockedContainer;
    private ViewGroup messageContainer;

    private QBMessageListener<QBGroupChat> groupChatMessageListener = new QBMessageListener<QBGroupChat>() {
        @Override
        public void processMessage(final QBGroupChat groupChat, final QBChatMessage qbChatMessage) {
            ChatGroupActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentUserId != null && qbChatMessage.getSenderId().intValue() != currentUserId.intValue()) {
                        displayChatMessage(qbChatMessage);
                    }
                }
            });
        }

        @Override
        public void processError(final QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage) {
            Util.onError(error, ChatGroupActivity.this);
        }
    };

    private QBSystemMessagesManager systemMessagesManager;

    private QBSystemMessageListener systemMessageListener = new QBSystemMessageListener() {
        @Override
        public void processMessage(QBChatMessage qbChatMessage) {
            displayChatMessage(qbChatMessage);
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
            Util.onError(e, ChatGroupActivity.this);
        }
    };

    private QBGroupChat groupChat;

    private boolean isBlocked;
    private boolean userDeleted;
    private int currentCode;

    public static boolean isImage(File file) {
        String fileName = file.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return ext.equals("jpeg") || ext.equals("jpg") || ext.equals("png") || ext.equals("bmp");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat_group);
        binding.setActivity(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        chatAdapter = new ChatGroupMessagesAdapter(ChatGroupActivity.this, currentUserId);

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
            userIdsList = savedInstanceState.getIntegerArrayList(RECIPIENT_IDS_LIST);
            currentUserId = savedInstanceState.getInt(CURRENT_USER_ID, 0);
        }
        if (dialog != null && dialog.getType() != null && dialog.getType() == QBDialogType.GROUP) {
            initForGroupChat();
        }

        this.savedInstanceState = savedInstanceState;

        editText_chat_message = (EditText) findViewById(R.id.editText_chat_message);

        button_send_chat = (Button) findViewById(R.id.button_send_chat);
        blockedContainer = (FrameLayout) findViewById(R.id.block_container);
        messageContainer = (ViewGroup) findViewById(R.id.message_container);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateChatReciever, new IntentFilter(C.UPDATE_CHAT));
        binding.giphySelector.setCallback(new GiphySelector.Callback() {
            @Override
            public void onSelected(Giphy giphy) {
                sendSticker(Uri.parse(giphy.getFull().getUrl()));
            }
        });
        initStickersWidget();


    }

    private void initForGroupChat() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.setGroupName(dialog.getName());
                binding.addGroupMember.setVisibility(dialog.getType() == QBDialogType.GROUP ? View.VISIBLE : View.GONE);
            }
        });

    }

    private void initStickersWidget() {
        mStickersContainer = binding.stickersContainer;
        RenderingOptions renderingOptions = new RenderingOptions(
                RenderingOptions.BorderStyle.Sticker,
                RenderingOptions.ImageFormat.Png,
                RenderingOptions.Size.Thumbnail
        );
        WidgetDisplayOptions options = new WidgetDisplayOptions(renderingOptions);
        mStickersWidget = new QuarterScreenWidget(
                this,
                options,
                new SearchResultAdapter.ImageLoader() {
                    @Override
                    public void loadImage(@NonNull ImageView target, @NonNull Uri uri,
                                          @NonNull final SearchResultAdapter.ImageLoaderCallback callback) {
                        Glide.with(getApplicationContext())
                                .load(uri.toString())
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        callback.updateImageView();
                                        return false;
                                    }
                                })
                                .into(target);
                    }
                }
        );
        mStickersWidget.setWidgetListener(new WidgetListener() {
            @Override
            public void onCloseButtonTapped() {
                // not needed
            }

            @Override
            public void onStickerTapped(Imoji imoji) {
                sendSticker(imoji.getStandardFullSizeUri());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        isExternalDialogOpened = false;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICKFILE_REQUEST_CODE:
                    sendAttachment(data.getData());
                    break;
                case IMAGE_CAPTURE_REQUEST_CODE:
                    sendAttachment(mPhotoUri);
                    break;
                case PICK_FILE_FOR_EDIT_REQUEST:
                    openStickerEditor(data.getData());
                    break;
                case ImojiEditorActivity.START_EDITOR_REQUEST_CODE:
                    if (data.hasExtra(ImojiCreateService.IMOJI_MODEL_BUNDLE_ARG_KEY)) {
                        Imoji imoji = data.getParcelableExtra(ImojiCreateService.IMOJI_MODEL_BUNDLE_ARG_KEY);
                        sendSticker(imoji.getStandardFullSizeUri());
                    }
                    break;
                case ADD_NEW_GROUP_MEMBER:
                    if (data.hasExtra(NEW_ADDED_USERS_LIST)) {
                        selectedNewGroupUserList = data.getIntegerArrayListExtra(NEW_ADDED_USERS_LIST);
                        addNewMembers();
                    }
                    break;
                case GROUP_SETTINGS_CHANGE:
                    if (data.hasExtra(CHANGED_GROUP_NAME)) {
                        binding.setGroupName(data.getStringExtra(CHANGED_GROUP_NAME));
                        dialog.setName(data.getStringExtra(CHANGED_GROUP_NAME));
                    }
                    break;
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            isBusy.set(false);
        }
    }

    private void addNewMembers() {
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
        if (selectedNewGroupUserList != null) {
            for (Integer id : selectedNewGroupUserList) {
                requestBuilder.addUsers(id);
            }

            try {
                dialog = groupChatManager.updateDialog(dialog, requestBuilder);
            } catch (QBResponseException e) {
                e.printStackTrace();
            }
            notifyGroupUsers(selectedNewGroupUserList);
            selectedNewGroupUserList = null;
        }
    }

    @Override
    public void onQBConnect(final boolean isActivityReopened) {
        // init recipient and dialog if intent contains only their ids
        reloadData(isActivityReopened);
    }

    private void reloadData(final boolean isActivityReopened) {
        isBusy.set(true);

        final QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
        systemMessagesManager.addSystemMessageListener(systemMessageListener);

        if (getIntent().hasExtra(RECIPIENT_IDS_LIST)) {
            userIdsList = getIntent().getIntegerArrayListExtra(RECIPIENT_IDS_LIST);
            getUsers(userIdsList);
        }

        new AsyncTask<Void, Void, Void>() {

            Exception error;
            private ArrayList<QBChatMessage> chatMessages;

            @Override
            protected Void doInBackground(Void... params) {
                Log.d("CHAT", "doInBackground");
                try {
                    if (currentQBUser == null) {
                        currentQBUser = QBUsers.getUser(preferences.getInt(C.QB_USERID, 0));
                    }
                    currentUserId = currentQBUser.getId();

                    if (dialog == null) {
                        if (getIntent().hasExtra(DIALOG)) {
                            dialog = (QBDialog) getIntent().getSerializableExtra(DIALOG);
                        }
                        if (dialog == null && getIntent().hasExtra(DIALOG_ID)) {
                            QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                            requestBuilder.eq("_id", getIntent().getStringExtra(DIALOG_ID));

                            Bundle bundle = new Bundle();
                            ArrayList<QBDialog> dialogs = QBChatService.getChatDialogs(QBDialogType.GROUP, requestBuilder, bundle);
                            if (dialogs.size() == 0) return null;
                            dialog = dialogs.get(0);

                            if (userIdsList == null && dialog != null && dialog.getOccupants() != null) {
                                userIdsList = (ArrayList<Integer>) dialog.getOccupants();
                            }
                        }
                    }

                    if (dialog == null) {
                        QBDialog groupDialog = new QBDialog();
                        groupDialog.setName(getString(R.string.default_channel_name));
                        if (getIntent().hasExtra(DIALOG_GROUP_TYPE) &&
                                getIntent().getSerializableExtra(DIALOG_GROUP_TYPE) == QBDialogType.PUBLIC_GROUP) {
                            groupDialog.setType(QBDialogType.PUBLIC_GROUP);
                        } else {
                            groupDialog.setType(QBDialogType.GROUP);
                            groupDialog.setOccupantsIds(userIdsList);
                        }
                        dialog = groupChatManager.createDialog(groupDialog);
                        notifyGroupUsers(groupDialog.getOccupants());
                    }

                    try {
                        groupChat = groupChatManager.getGroupChat(dialog.getRoomJid());
                    } catch (NullPointerException e) {
                        Log.e("QBPrivateChatManager", e.getMessage());
                        e.printStackTrace();
                    }
                    if (groupChat == null) {
                        groupChat = groupChatManager.createGroupChat(dialog.getRoomJid());
                    }
                    groupChat.addMessageListener(groupChatMessageListener);

                    DiscussionHistory history = new DiscussionHistory();
                    history.setMaxStanzas(0);
                    groupChat.join(history);

                    QBRequestGetBuilder requestBuilder;
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

                    initForGroupChat();


                } catch (Exception e) {
                    e.printStackTrace();
                    error = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d("CHAT", "finished");
                isBusy.set(false);
                if (error != null) {
                    Util.onError(error, ChatGroupActivity.this);
                    return;
                }
                if (!isActivityReopened) {
                    if (chatMessages == null) chatMessages = new ArrayList<>();
                    chatAdapter.addItems(chatMessages);
                    if (chatMessages.size() < messagesPerPage) {
                        chatAdapter.disableLoadMore();
                    }
                } else if (gotMessagesInOffline) {
                    chatAdapter.addItemsToStart(chatMessages);
                    chatAdapter.alignToPageSize(messagesPerPage, currentPage);
                    gotMessagesInOffline = false;
                }

                checkIsOpenedFirst();

            }
        }.execute();
    }

    private void checkIsOpenedFirst() {
        if (getIntent().hasExtra(IS_FIRST_OPENED) &&
                getIntent().getBooleanExtra(IS_FIRST_OPENED, false)) {
            Intent intent = new Intent(ChatGroupActivity.this, GroupEditActivity.class);
            intent.putExtra(GroupEditActivity.DIALOG, dialog);
            startActivityForResult(intent, GROUP_SETTINGS_CHANGE);
        }
    }

    private void notifyGroupUsers(List<Integer> list) {
        if (list == null || list.size() == 0) return;
        for (Integer userID : list) {
            QBChatMessage chatMessage = createChatNotificationForGroupChatCreation(dialog);
            chatMessage.setRecipientId(userID);
            try {
                systemMessagesManager.sendSystemMessage(chatMessage);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (IllegalStateException ee) {
                ee.printStackTrace();
            }
        }
    }

    public static QBChatMessage createChatNotificationForGroupChatCreation(QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = TextUtils.join(",", dialog.getOccupants());
        String dialogName = dialog.getName();
        String dialogTypeCode = String.valueOf(dialog.getType().ordinal());

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody("You were added to group");

        // Add notification_type=1 to extra params when you created a group chat
        chatMessage.setProperty("notification_type", "1");

        chatMessage.setProperty("_id", dialogId);
        if (!TextUtils.isEmpty(roomJid)) {
            chatMessage.setProperty("room_jid", roomJid);
        }
        chatMessage.setProperty("occupants_ids", occupantsIds);
        if (!TextUtils.isEmpty(dialogName)) {
            chatMessage.setProperty("name", dialogName);
        }
        chatMessage.setProperty("type", dialogTypeCode);

        return chatMessage;
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
        outState.putIntegerArrayList(RECIPIENT_IDS_LIST, userIdsList);
        outState.putInt(CURRENT_USER_ID, currentUserId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mStickersContainer.getChildCount() != 0) {
            hideSoftKeyboard();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        paused = System.currentTimeMillis();
        if (!isExternalDialogOpened) {
            isBusy.set(true);
        }
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
            Toast.makeText(this, "Please wait until connection to server restored", Toast.LENGTH_LONG).show();
            return;
        }
        // send chat message to server
        if (!editText_chat_message.getText().toString().equals("")) {
            QBChatMessage msg = new QBChatMessage();
            msg.setBody(editText_chat_message.getText().toString());
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy/MM/dd", Locale.US);
            //msg.setProperty("date_sent",String.valueOf(sdf.format(new Date()))+"");

            msg.setProperty("save_to_history", "1");
            msg.setProperty("send_to_chat", "1");
            msg.setDialogId(dialog.getDialogId());
            Location location = UserLocationService.getLastLocation();
            if (location != null) {
                msg.setProperty(C.LATITUDE, String.valueOf(location.getLatitude()));
                msg.setProperty(C.LONGITUDE, String.valueOf(location.getLongitude()));
            }

            try {
                groupChat.sendMessage(msg);
                displayChatMessage(msg);
                EventBus.getDefault().post(msg);
            } catch (SmackException.NotConnectedException e) {
                if (!isNetworkAvailable()) {
                    Util.onError(getString(R.string.verify_internet_connection), this);
                } else {
                    isReopened = true;
                    isBusy.set(true);
                    loginToChat();
                    Toast.makeText(this, R.string.reconnect_message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Util.onError(e, ChatGroupActivity.this);
            }
            editText_chat_message.setText("");
        }
    }

    public void sendAttachment(Uri uri) {
        if (isSendingMessage.get()) return;
        isSendingMessage.set(true);

        // Get the path

        final String path = FileUtil.getPath(this, uri);
        if (path == null) {
            Util.onError(getString(R.string.unable_to_get_file), this);
            isSendingMessage.set(false);
            return;
        }
        final File filePhoto = new File(path);

        new AsyncTask<Void, Integer, QBChatMessage>() {
            Exception exception;

            @Override
            protected QBChatMessage doInBackground(Void... params) {

                try {
                    UploadFileTask task = new UploadFileTask(filePhoto, false, null, new QBProgressCallback() {
                        @Override
                        public void onProgressUpdate(int i) {
                            uploadProgress.set(i);
                        }
                    });

                    QBFile qbFile = task.execute();

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

                    groupChat.sendMessage(chatMessage);

                    if (filePhoto.getAbsolutePath().contains("cache")) {
                        filePhoto.delete();
                    }
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
                isBusy.set(false);
                if (exception != null) {
                    Util.onError(exception, ChatGroupActivity.this);
                    return;
                }
                displayChatMessage(qbChatMessage);
            }

        }.execute();
    }

    public void sendSticker(Uri uri) {
        if (isBusy.get()) {
            Toast.makeText(this, "Please wait until connection to server restored", Toast.LENGTH_LONG).show();
            return;
        }
        if (isSendingMessage.get()) return;
        isSendingMessage.set(true);

        // send sticker to chat server
        if (uri.toString().contains("http")) {
            QBChatMessage msg = new QBChatMessage();
            msg.setBody("sticker");

            msg.setProperty("save_to_history", "1");
            msg.setDialogId(dialog.getDialogId());
            //msg.setProperty("send_to_chat", "1");
            msg.setProperty(C.CHAT_MSG_STICKER_PROPERTY, uri.toString());

            try {
                groupChat.sendMessage(msg);
                displayChatMessage(msg);
            } catch (SmackException.NotConnectedException e) {
                if (!isNetworkAvailable()) {
                    Util.onError(getString(R.string.verify_internet_connection), this);
                } else {
                    isReopened = true;
                    isBusy.set(true);
                    loginToChat();
                    Toast.makeText(this, R.string.reconnect_message, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Util.onError(e, ChatGroupActivity.this);
            }
        }
        isSendingMessage.set(false);
    }

    public void onClickImoji() {
        if (binding.getShowGiphy()) onClickGiphy();
        if (mStickersContainer != null && mStickersContainer.getChildCount() == 0) {
            hideSoftKeyboard();
            mStickersContainer.addView(mStickersWidget);
        } else {
            hideStickersContainer();
        }
    }

    private void hideStickersContainer() {
        if (mStickersContainer != null && mStickersContainer.getChildCount() > 0) {
            mStickersContainer.removeAllViews();
        }
    }

    public void selectFile() {
        final CharSequence[] items = {
                getString(R.string.dialog_take_from_camera),
                getString(R.string.dialog_get_from_gallery),
                getString(R.string.cancel)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppAlertDialogTheme);
        builder.setTitle(getString(R.string.dialog_send_file_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.dialog_take_from_camera))) {
                    getImgFromCamera();
                } else if (items[item].equals(getString(R.string.dialog_get_from_gallery))) {
                    getImgFromGallery(PICKFILE_REQUEST_CODE);
                } else if (items[item].equals(getString(R.string.cancel))) {
                    dialog.dismiss();
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void getImgFromCamera() {
        int permCameraCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int permWriteCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permCameraCheck != PackageManager.PERMISSION_GRANTED || permWriteCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_ST_CAMERA_PERMISSION_REQUEST);
            return;
        }
        isExternalDialogOpened = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mPhotoUri = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
        startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PawPads");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");
    }

    private void getImgFromGallery(int code) {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            currentCode = code;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST);
            return;
        }
        isExternalDialogOpened = true;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), code);
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
                    Util.onError(e, ChatGroupActivity.this);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_STORAGE_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImgFromGallery(currentCode);
                }
                break;
            }
            case WRITE_ST_CAMERA_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImgFromCamera();
                }
                break;
            }
        }
    }

    @Override
    public void onGroupChatMessage(QBGroupChat qbGroupChat, final QBChatMessage qbChatMessage) {
        if (dialog == null) return;
        if (!qbChatMessage.getDialogId().equals(dialog.getDialogId())) return;
        ChatGroupActivity.this.runOnUiThread(new Runnable() {
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

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }, 50);
        }
    }

    @Override
    public void onBackPressed() {
        if (mStickersContainer.getChildCount() != 0) {
            hideStickersContainer();
            hideSoftKeyboard();
        } else {
            super.onBackPressed();
        }
    }

    public void onClickGiphy() {
        hideSoftKeyboard();
        hideStickersContainer();
        binding.setShowGiphy(!binding.getShowGiphy());
    }

    public void onCLickCreateSticker() {
        getImgFromGallery(PICK_FILE_FOR_EDIT_REQUEST);

    }

    public void openStickerEditor(Uri uri) {
        isExternalDialogOpened = true;
        final String path = FileUtil.getPath(this, uri);
        Bitmap bitmap = null;
        if (path == null) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bitmap = BitmapFactory.decodeFile(path);
        }

        if (bitmap == null) {
            Util.onError(getString(R.string.unable_to_get_file), this);
            isSendingMessage.set(false);
        }
        EditorBitmapCache.getInstance().put(EditorBitmapCache.Keys.INPUT_BITMAP, bitmap);
        Intent intent = new Intent(this, ImojiEditorActivity.class);
        startActivityForResult(intent, ImojiEditorActivity.START_EDITOR_REQUEST_CODE);
    }

    private void getUsers(List<Integer> userIdsList) {
        if (userIdsList == null || userIdsList.size() == 0) return;

        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        QBUsers.getUsersByIDs(userIdsList, pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                if (users.size() > 0) {
                    recipientList = users;
                }
            }

            @Override
            public void onError(QBResponseException errors) {
                if (getApplicationContext() == null) return;
                Util.onError(errors, getApplicationContext());
            }
        });
    }

    public void editGroupSettings() {
        Intent intent = new Intent(ChatGroupActivity.this, GroupEditActivity.class);
        intent.putExtra(GroupEditActivity.DIALOG, dialog);
        startActivityForResult(intent, GROUP_SETTINGS_CHANGE);
    }

    public void addGroupMember() {
        Intent intentAddGroupMember = new Intent(this, CreateChatActivity.class);
        intentAddGroupMember.putIntegerArrayListExtra(CreateChatActivity.DIALOG_USERS_LIST, ((ArrayList<Integer>) dialog.getOccupants()));
        startActivityForResult(intentAddGroupMember, ADD_NEW_GROUP_MEMBER);
    }

}