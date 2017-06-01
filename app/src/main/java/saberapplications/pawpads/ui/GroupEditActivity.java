package saberapplications.pawpads.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityEditGroupBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.databinding.RowParticipantsBinding;
import saberapplications.pawpads.ui.chat.CreateChatActivity;
import saberapplications.pawpads.ui.home.MainActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.util.FileUtil;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by developer on 30.05.17.
 */

public class GroupEditActivity extends BaseActivity {
    private final static int SELECT_IMAGE = 1;
    private static final int PERMISSION_REQUEST = 200;
    private static final int CHANGE_PROFILE_PICTURE = 1;
    public static final String DIALOG = "dialog";
    public static final int ADD_NEW_GROUP_MEMBER = 25;
    public static final String NEW_ADDED_USERS_LIST = "NEW_ADDED_USERS_LIST";
    String selectedImagePath;

    Uri avatarImagePath;

    private SharedPreferences defaultSharedPreferences;

    private Runnable timeOutRunnable;
    public final BindableBoolean isBusy = new BindableBoolean();
    public final BindableString progressMessage = new BindableString();

    private ActivityEditGroupBinding binding;
    public final BindableString groupName = new BindableString();
    public final BindableString groupType = new BindableString();
    public final BindableString groupParticipants = new BindableString();
    public final BindableString adminName = new BindableString();

    private int imageAction;
    private QBDialog dialog;
    private ArrayList<Integer> selectedNewGroupUserList;
    private ParticipantsAdapter adapter;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_group);
        binding.setActivity(this);
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentUserId = prefs.getInt(C.QB_USERID, 0);

        progressMessage.set(getString(R.string.loading));

        adapter = new ParticipantsAdapter();
        binding.participantsListView.setAdapter(adapter);

        if (getIntent().hasExtra(DIALOG)) {
            dialog = (QBDialog) getIntent().getSerializableExtra(DIALOG);
        }
        if(dialog != null && dialog.getType() == QBDialogType.PUBLIC_GROUP) binding.addGroupMemberTv.setVisibility(View.GONE);
        loadData();
    }

    private void loadData() {
        if(dialog == null) return;
        isBusy.set(true);
        if(adapter != null) adapter.clear();
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(GroupEditActivity.this);

        groupName.set(dialog.getName());
        groupType.set(dialog.getType() == QBDialogType.GROUP ? "Private group" : "Public group");
        int ocupantsSize = dialog.getOccupants().size();
        groupParticipants.set("Participants (" + (ocupantsSize == 0 ? ocupantsSize : ocupantsSize-1) + ")");
        binding.groupAvatar.setImageResource(R.drawable.user_placeholder);

        QBUsers.getUser(dialog.getUserId(), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {

                String username = qbUser.getFullName() == null ? qbUser.getLogin() : qbUser.getFullName();
                if(currentUserId == qbUser.getId()) username = "You (" + username + ")";
                adminName.set(username);
                if (qbUser.getFileId() != null) {
                    AvatarLoaderHelper.loadImage(qbUser.getFileId(), binding.adminAvatar,
                            binding.adminAvatar.getWidth(), binding.adminAvatar.getHeight()
                            , new AvatarLoaderHelper.Callback() {
                                @Override
                                public void imageLoaded() {
                                    isBusy.set(false);
                                }
                            });

                } else {
                    isBusy.set(false);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                isBusy.set(false);
                Util.onError(e, GroupEditActivity.this);
            }

        });

        getUsers(dialog.getOccupants());

        if(dialog.getPhoto() != null) {
            float d = getResources().getDisplayMetrics().density;
            int size = Math.round(100 * d);
            try {
                AvatarLoaderHelper.loadImage(Integer.parseInt(dialog.getPhoto()), binding.groupAvatar, size, size, null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    private void onGroupSettingsSaved(String message) {
        if (message != null) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = FileUtil.getPath(this, selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                avatarImagePath = selectedImageUri;

                displayBitmap(avatarImagePath, binding.groupAvatar);
            }
            if (requestCode == ADD_NEW_GROUP_MEMBER) {
                selectedNewGroupUserList = data.getIntegerArrayListExtra(NEW_ADDED_USERS_LIST);
                System.out.println("ADD_NEW_GROUP_MEMBER RESULT OK");
                save();
            }
        }
    }

    private void displayBitmap(final Uri uri, final ImageView view) {

        final int width = view.getWidth();
        final int height = view.getHeight();
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, width, height);
                    bitmap.recycle();
                    return thumb;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null)
                    view.setImageBitmap(bitmap);
            }
        }.execute();
    }

    public String getPath(Uri uri) {
        return uri.getPath();
    }


    public void changeGroupPicture() {
        imageAction = CHANGE_PROFILE_PICTURE;
        if (!permissionCheck()) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }

    public void save() {
        if (isBusy.get()) return;
        if (groupName.get().length() < 3) {
            Toast.makeText(this, "Group name is too short", Toast.LENGTH_LONG).show();
            return;
        }
        if (groupName.get().length() > 50) {
            Toast.makeText(this, "Group name can not be more than 50 characters", Toast.LENGTH_LONG).show();
            return;
        }

        progressMessage.set(getString(R.string.saving));
        isBusy.set(true);

        timeOutRunnable = new Runnable() {
            @Override
            public void run() {
                isBusy.set(false);
                Toast.makeText(getApplicationContext(), "Connection timeout", Toast.LENGTH_SHORT).show();
            }
        };
        //edit second parameter to set timeout period
        final Handler handler = new Handler();
        handler.postDelayed(timeOutRunnable, 30000);

        AsyncTask<Void, View, Boolean> saveTask = new AsyncTask<Void, View, Boolean>() {
            Exception e;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {

                    if (avatarImagePath != null) {
                        Bitmap avatar = MediaStore.Images.Media.getBitmap(getContentResolver(), avatarImagePath);
                        avatar = ThumbnailUtils.extractThumbnail(avatar, 500, 500);
                        final File file = File.createTempFile("group_photo", ".jpg", getCacheDir());
                        FileOutputStream out = new FileOutputStream(file);
                        avatar.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        QBFile qbFile = QBContent.uploadFileTask(file, false, file.getAbsolutePath());
                        dialog.setPhoto(qbFile.getId().toString());
                        file.delete();
                        avatar.recycle();
                        avatarImagePath = null;
                    }

                    QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
                    QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                    if(selectedNewGroupUserList != null) {
                        for(Integer id : selectedNewGroupUserList) {
                            requestBuilder.addUsers(id);
                        }
                    }
                    if( !groupName.get().equals(dialog.getName())) {
                        dialog.setName(groupName.get());
                    }
                    dialog = groupChatManager.updateDialog(dialog, requestBuilder);
                    selectedNewGroupUserList = null;

                    return true;
                } catch (Exception e) {
                    this.e = e;
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                isBusy.set(false);
                handler.removeCallbacks(timeOutRunnable);
                if (aBoolean) {
                    onGroupSettingsSaved("group settings saved");
                } else {
                    Util.onError(e, GroupEditActivity.this);
                }
            }
        };
        saveTask.execute();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    public boolean permissionCheck() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switch (imageAction) {
                        case CHANGE_PROFILE_PICTURE:
                            changeGroupPicture();
                            break;
                    }
                }
            }
        }
    }

    private void getUsers(List<Integer> userIdsList) {
        if(userIdsList == null || userIdsList.size() == 0) {
            adapter.disableLoadMore();
            return;
        }
        if(userIdsList.contains(dialog.getUserId())) userIdsList.remove(dialog.getUserId());
        isBusy.set(true);

        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        QBUsers.getUsersByIDs(userIdsList, pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                if (users.size() > 0) {
                    adapter.addItems(users);
                }
                adapter.disableLoadMore();
                isBusy.set(false);
            }

            @Override
            public void onError(QBResponseException errors) {
                if (getApplicationContext()==null) return;
                Util.onError(errors, getApplicationContext());
                adapter.disableLoadMore();
                isBusy.set(false);
            }
        });
    }

    private class ParticipantsAdapter extends BaseListAdapter<QBUser> {

        int currentUserId;
        ArrayMap<Integer,QBUser> userCache=new ArrayMap<>();

        class ParticipantsHolder extends DataHolder<QBUser>{

            private final int size;
            RowParticipantsBinding binding;
            ParticipantsAdapter adapter;

            ParticipantsHolder(View v, BaseListAdapter<QBUser> adapter) {
                super(v, adapter);
                binding= DataBindingUtil.bind(v);
                this.adapter= (ParticipantsAdapter) adapter;
                float d= view.getResources().getDisplayMetrics().density;
                size=Math.round(35 * d);
            }

            @Override
            public void showData(DataItem<QBUser> data,int position) {
                QBUser user = data.model.get();
                String userName = data.model.get().getFullName() == null ? data.model.get().getLogin() : data.model.get().getFullName();
                if(currentUserId == user.getId()) userName = "You (" + userName + ")";
                binding.setUsername(userName);
                int userId=user.getId();

                binding.userAvatar.setImageResource(R.drawable.user_placeholder);
                if(!adapter.userCache.containsKey(userId)) {
                    QBUsers.getUser(userId, new QBEntityCallback<QBUser>() {
                        @Override
                        public void onSuccess(QBUser qbUser, Bundle bundle) {
                            if (qbUser.getFileId() != null) {
                                AvatarLoaderHelper.loadImage(qbUser.getFileId(), binding.userAvatar, size, size);
                                adapter.userCache.put(qbUser.getId(), qbUser);
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            e.printStackTrace();
                        }
                    });
                }else {
                    AvatarLoaderHelper.loadImage(adapter.userCache.get(userId).getFileId(), binding.userAvatar, size, size);
                }
            }
        }

        @Override
        public DataHolder<QBUser> getItemHolder(ViewGroup parent) {
            View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_participants,parent,false);
            return new ParticipantsAdapter.ParticipantsHolder(v,this);
        }

        public void setCurrentUserId(int currentUserId) {
            this.currentUserId = currentUserId;
        }

        @Override
        protected int getEmptyStateResId() {
            return R.layout.empty_state_participants;
        }
    }

    public void addGroupMember() {
        Intent intentAddGroupMember = new Intent(this, CreateChatActivity.class);
        intentAddGroupMember.putIntegerArrayListExtra(CreateChatActivity.DIALOG_USERS_LIST, ((ArrayList<Integer>) dialog.getOccupants()));
        startActivityForResult(intentAddGroupMember, ADD_NEW_GROUP_MEMBER);
    }

    public void leaveAndDeleteGroup() {
        isBusy.set(true);
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
        QBGroupChat currentChatRoom = groupChatManager.getGroupChat(dialog.getRoomJid());
        try {
            currentChatRoom.leave();
            currentChatRoom = null;
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        boolean forceDelete = false;
        if(currentUserId == dialog.getUserId()) {
            forceDelete = true;
        }

        groupChatManager.deleteDialog(dialog.getDialogId(), forceDelete, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                isBusy.set(false);
                Intent intent = new Intent(GroupEditActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                isBusy.set(false);
                if (getApplicationContext()==null) return;
                Util.onError(e, getApplicationContext());
            }
        });
    }
}