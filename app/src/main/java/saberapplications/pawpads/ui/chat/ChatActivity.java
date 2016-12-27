package saberapplications.pawpads.ui.chat;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jivesoftware.smack.SmackException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.imoji.sdk.grid.HalfScreenWidget;
import io.imoji.sdk.grid.components.SearchResultAdapter;
import io.imoji.sdk.grid.components.WidgetDisplayOptions;
import io.imoji.sdk.grid.components.WidgetListener;
import io.imoji.sdk.objects.Imoji;
import io.imoji.sdk.objects.RenderingOptions;
import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityChatBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableInteger;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.profile.ProfileActivity;
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
    private static final int IMAGE_CAPTURE_REQUEST_CODE = 33;
    private static final int READ_STORAGE_PERMISSION_REQUEST = 200;
    private static final int WRITE_ST_CAMERA_PERMISSION_REQUEST = 205;
    public final BindableBoolean isSendingMessage = new BindableBoolean();
    public final BindableInteger uploadProgress = new BindableInteger(0);
    public final BindableBoolean isBusy = new BindableBoolean(true);
    //EditText editText_mail_id;
    EditText editText_chat_message;
    Button button_send_chat;
    Bundle savedInstanceState;
    ActivityChatBinding binding;
    int currentPage = 0;
    int messagesPerPage = 15;
    long paused;
    boolean gotMessagesInOffline = false;
    private Uri mPhotoUri;
    //    BroadcastReceiver recieve_chat;
    private QBDialog dialog;
    private HalfScreenWidget mStickersWidget;
    private FrameLayout mStickersContainer;
    BroadcastReceiver updateChatReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (dialog.getDialogId().equals(intent.getStringExtra(DIALOG_ID))) {
                gotMessagesInOffline = true;
            }
        }
    };
    private QBUser recipient;
    private ChatMessagesAdapter chatAdapter;
    private FrameLayout blockedContainer;
    private LinearLayout messageContainer;
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


    private QBPrivateChat privateChat;

    private boolean isBlocked;
    private boolean userDeleted;

    public static boolean isImage(File file) {
        String fileName = file.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return ext.equals("jpeg") || ext.equals("jpg") || ext.equals("png") || ext.equals("bmp");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        binding.setActivity(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        chatAdapter = new ChatMessagesAdapter(ChatActivity.this, currentUserId);

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

        initStickersWidget();
    }

    private void init() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recipient == null || userDeleted) return;
                if (recipient.getFileId() != null) {
                    float d = getResources().getDisplayMetrics().density;
                    int size = Math.round(80 * d);
                    AvatarLoaderHelper.loadImage(recipient.getFileId(), binding.recipientAvatar, size, size);
                }
                binding.setUsername(Util.getUserName(recipient));
            }
        });

    }

    private void initStickersWidget() {
        mStickersContainer = binding.stickersContainer;
        mStickersWidget = new HalfScreenWidget(
                this,
                new WidgetDisplayOptions(new RenderingOptions(
                        RenderingOptions.BorderStyle.Sticker,
                        RenderingOptions.ImageFormat.Png,
                        RenderingOptions.Size.Resolution320
                )),
                new SearchResultAdapter.ImageLoader() {
                    @Override
                    public void loadImage(@NonNull ImageView target, @NonNull Uri uri,
                                          @NonNull final SearchResultAdapter.ImageLoaderCallback callback) {
                        Picasso.with(getApplicationContext())
                                .load(uri.toString())
                                .into(target, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        callback.updateImageView();
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
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
                Picasso.with(ChatActivity.this)
                        .load(imoji.getStandardThumbnailUri())
                        .into(picassoImageTarget(getApplicationContext(), "stickerDir", "sticker.jpg"));
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
            if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
                sendAttachment(mPhotoUri);
            }

        }
    }

    @Override
    public void onQBConnect(final boolean isActivityReopened) {
        // init recipient and dialog if intent contains only their ids
        isBusy.set(true);


        final QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();


        // Detect blocked state
        new AsyncTask<Void, Void, Void>() {

            Exception error;
            private ArrayList<QBChatMessage> chatMessages;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (currentQBUser == null) {
                        currentQBUser = QBUsers.getUser(preferences.getInt(C.QB_USERID, 0));
                    }
                    currentUserId = currentQBUser.getId();
                    if (recipient == null) {
                        if (getIntent().hasExtra(RECIPIENT)) {
                            recipient = (QBUser) getIntent().getSerializableExtra(RECIPIENT);
                            isBlocked = getIntent().getBooleanExtra(Util.IS_BLOCKED, false);
                        }
                        if (getIntent().hasExtra(RECIPIENT_ID)) {
                            try {
                                recipient = QBUsers.getUser(getIntent().getIntExtra(RECIPIENT_ID, 0));
                            } catch (QBResponseException e) {
                                if (e.getHttpStatusCode() == 404) {
                                    binding.setIsDeleted(true);
                                    userDeleted = true;
                                    hideSoftKeyboard();
                                } else {
                                    throw e;
                                }
                            }
                        }
                    }
                    if (!userDeleted) {
                        privateChat = privateChatManager.getChat(recipient.getId());
                        if (privateChat == null) {
                            privateChat = privateChatManager.createChat(recipient.getId(), messageListener);
                        } else {
                            privateChat.addMessageListener(messageListener);
                        }

                        init();
                    }
                    if (dialog == null) {
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
                    QBRequestGetBuilder requestBuilder;
                    if (!userDeleted) {
                        requestBuilder = new QBRequestGetBuilder();
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
                Log.d("CHAT", "finished");
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
            msg.setRecipientId(recipient.getId());
            msg.setDialogId(dialog.getDialogId());
            msg.setProperty("send_to_chat", "1");


            try {
                privateChat.sendMessage(msg);
                displayChatMessage(msg);
            } catch (SmackException.NotConnectedException e) {
                if (!isNetworkAvailable()){
                    Util.onError(getString(R.string.verify_internet_connection),this);
                }else {
                    isReopened=true;
                    isBusy.set(true);
                    loginToChat();
                    Toast.makeText(this, R.string.reconnect_message,Toast.LENGTH_LONG).show();
                }
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

                    privateChat.sendMessage(chatMessage);

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
                    Util.onError(exception, ChatActivity.this);
                    return;
                }
                displayChatMessage(qbChatMessage);
            }

        }.execute();
    }

    public void selectImoji() {
        if(mStickersContainer != null && mStickersContainer.getChildCount() == 0) {
            hideSoftKeyboard();
            mStickersContainer.addView(mStickersWidget);
        } else {
            hideStickersContainer();
        }
    }

    private void hideStickersContainer() {
        if(mStickersContainer != null && mStickersContainer.getChildCount() > 0) {
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
                    getImgFromGallery();
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
                    new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE },
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

    private void getImgFromGallery() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST);
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
            case READ_STORAGE_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImgFromGallery();
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
    public void onChatMessage(QBPrivateChat qbPrivateChat, final QBChatMessage qbChatMessage) {
        if (dialog == null) return;
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

    public void openProfile() {
        hideSoftKeyboard();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(recipient != null) {
                    Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
                    intent.putExtra(C.QB_USERID, recipient.getId());
                    intent.putExtra(C.QB_USER, recipient);
                    startActivity(intent);
                }
            }
        }, 50);
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        if(mStickersContainer.getChildCount() != 0) {
            hideStickersContainer();
            hideSoftKeyboard();
        } else {
            super.onBackPressed();
        }
    }

    private Target picassoImageTarget(Context context, final String imageDir, final String imageName) {
        Log.d("picassoImageTarget", " picassoImageTarget");
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_imageDir
        final File directory = cw.getDir(imageDir, Context.MODE_PRIVATE);
        return new Target() {
            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Create image file
                        final File savedStickerImgFile = new File(directory, imageName);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(savedStickerImgFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("image", "image saved to >>>" + savedStickerImgFile.getAbsolutePath());
                        sendAttachment(Uri.fromFile(savedStickerImgFile));

                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d("picassoImageTarget", "onBitmapFailed");
            }
            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                if (placeHolderDrawable != null) {}
            }
        };
    }
}