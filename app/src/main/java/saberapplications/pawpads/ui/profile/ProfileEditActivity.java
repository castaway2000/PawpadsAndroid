package saberapplications.pawpads.ui.profile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
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
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.databinding.ProfileEditpageBinding;
import saberapplications.pawpads.model.UserProfile;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.util.FileUtil;


/**
 * Created by blaze on 10/17/2015.
 */
public class ProfileEditActivity extends BaseActivity {
    private final static int SELECT_IMAGE = 1;
    private final static int SELECT_BACKGOUND = 2;
    private static final int PERMISSION_REQUEST = 200;
    private static final int CHANGE_PROFILE_PICTURE = 1;
    private static final int CHANGE_PROFILE_BACKGROUND = 2;
    String selectedImagePath;

    EditText proDescr;

    Uri avatarImagePath;
    Uri backgoundImagePath;
    private QBUser currentQbUser;
    private UserProfile profile;
    private SharedPreferences defaultSharedPreferences;

    private Runnable timeOutRunnable;
    public final BindableBoolean isBusy = new BindableBoolean();
    public final BindableString progressMessage = new BindableString();

    private ProfileEditpageBinding binding;
    public final BindableString fullName = new BindableString();
    public final BindableString age = new BindableString();
    public final BindableString hobby = new BindableString();
    public final BindableString about = new BindableString();
    public final BindableString gender = new BindableString();
    String[] genders;
    private int imageAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.profile_editpage);
        binding.setActivity(this);
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        genders = getResources().getStringArray(R.array.genders);
        gender.set(getString(R.string.gender));
        gender.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable observable, int i) {
                binding.gender.setTextColor(ContextCompat.getColor(ProfileEditActivity.this, R.color.primary));
            }
        });
        progressMessage.set(getString(R.string.loading));
        loadUserData();
    }

    private void loadUserData() {
        isBusy.set(true);
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileEditActivity.this);
        QBUsers.getUser(defaultSharedPreferences.getInt(C.QB_USERID, -1), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {

                currentQbUser = qbUser;
                if (currentQbUser.getCustomData() != null) {
//                    proDescr.setText(String.valueOf(currentQbUser.getCustomData()));
                    profile = UserProfile.createFromJson(currentQbUser.getCustomData());
                } else {
                    profile = new UserProfile();
                }
                fullName.set(qbUser.getFullName());
                if (profile.getAge() > 0)
                    age.set(String.valueOf(profile.getAge()));


                if (profile.getGender() != null) {
                    if (profile.getGender().equals("M")) {
                        gender.set(genders[0]);
                    } else if (profile.getGender().equals("F")) {
                        gender.set(genders[1]);
                    }
                }
                hobby.set(profile.getHobby());
                about.set(profile.getAbout());


                if (currentQbUser.getFileId() != null) {
                    AvatarLoaderHelper.loadImage(currentQbUser.getFileId(), binding.userAvatar,
                            binding.userAvatar.getWidth(), binding.userAvatar.getHeight()
                            , new AvatarLoaderHelper.Callback() {
                                @Override
                                public void imageLoaded() {
                                    isBusy.set(false);
                                }
                            });

                } else {
                    isBusy.set(false);
                }
                if (profile.getBackgroundId() > 0) {
                    AvatarLoaderHelper.loadImage(profile.getBackgroundId(), binding.userBackground,
                            binding.userBackground.getWidth(), binding.userBackground.getHeight()
                            , new AvatarLoaderHelper.Callback() {
                                @Override
                                public void imageLoaded() {
                                    isBusy.set(false);
                                }
                            });
                }

            }

            @Override
            public void onError(QBResponseException e) {
                isBusy.set(false);
                Util.onError(e, ProfileEditActivity.this);
            }

        });
    }


    private void onProfileSaved(String message) {
        if (message != null) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

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

                displayBitmap(avatarImagePath, binding.userAvatar);
            }
            if (requestCode == SELECT_BACKGOUND) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = FileUtil.getPath(this, selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                backgoundImagePath = selectedImageUri;
                //  try {
                //    bitmap = decodeUri(getApplicationContext(), selectedImageUri, img.getWidth());
                displayBitmap(backgoundImagePath, binding.userBackground);
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


    public void changeProfilePicture() {
        imageAction = CHANGE_PROFILE_PICTURE;
        if (!permissionCheck()) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);
    }

    public void changeProfileBackgound() {
        imageAction = CHANGE_PROFILE_BACKGROUND;
        if (!permissionCheck()) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_BACKGOUND);

    }

    public void save() {
        if (isBusy.get()) return;
        if (fullName.get().length() < 3) {
            Toast.makeText(this, R.string.full_name_too_short, Toast.LENGTH_LONG).show();
            return;
        }
        if (fullName.get().length() > 50) {
            Toast.makeText(this, R.string.full_name_max_len, Toast.LENGTH_LONG).show();
            return;
        }
        if (age.get() != null && !age.get().equals("")) {
            Calendar calendar = GregorianCalendar.getInstance();
            int ageInt = calendar.get(Calendar.YEAR) - Integer.parseInt(age.get());
            if (ageInt <= 14 || ageInt > 100) {
                Toast.makeText(this, R.string.age_range_check, Toast.LENGTH_LONG).show();
                return;
            }
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

                    if (age.get() != null && !age.get().equals("")) {
                        profile.setAge(Integer.parseInt(age.get()));
                    }else{
                        profile.setAge(0);
                    }
                    profile.setAbout(about.get());
                    profile.setHobby(hobby.get());
                    currentQbUser.setFullName(fullName.get());
                    Gson gson = new Gson();
                    if (gender.get().equals(genders[0])) {
                        profile.setGender("M");
                    } else if (gender.get().equals(genders[1])) {
                        profile.setGender("F");
                    }else{
                        profile.setGender("");
                    }

                    boolean userImagesChanged = false;

                    if (backgoundImagePath != null) {
                        if (profile.getBackgroundId() > 0) {
                            try {
                                QBContent.deleteFile(profile.getBackgroundId());
                            } catch (Exception e) {

                            }
                        }
                        Bitmap bg = MediaStore.Images.Media.getBitmap(getContentResolver(), backgoundImagePath);
                        bg = ThumbnailUtils.extractThumbnail(bg, 1080, 540);
                        final File file = File.createTempFile("avatar_bg", ".jpg", getCacheDir());
                        FileOutputStream out = new FileOutputStream(file);
                        bg.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        QBFile qbFile = QBContent.uploadFileTask(file, false, file.getAbsolutePath());

                        profile.setBackgroundId(qbFile.getId());
                        file.delete();
                        bg.recycle();
                        backgoundImagePath = null;

                    }

                    if (avatarImagePath != null) {
                        if (currentQbUser.getFileId() != null && currentQbUser.getFileId() > 0) {
                            try {
                                QBContent.deleteFile(currentQbUser.getFileId());
                            } catch (Exception e) {

                            }
                        }
                        Bitmap avatar = MediaStore.Images.Media.getBitmap(getContentResolver(), avatarImagePath);
                        avatar = ThumbnailUtils.extractThumbnail(avatar, 500, 500);
                        final File file = File.createTempFile("avatar", ".jpg", getCacheDir());
                        FileOutputStream out = new FileOutputStream(file);
                        avatar.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        QBFile qbFile = QBContent.uploadFileTask(file, false, file.getAbsolutePath());
                        currentQbUser.setFileId(qbFile.getId());
                        file.delete();
                        avatar.recycle();
                        avatarImagePath = null;


                    }
                    currentQbUser.setCustomData(gson.toJson(profile));
                    QBUsers.updateUser(currentQbUser);

                    LocalBroadcastManager.getInstance(ProfileEditActivity.this).sendBroadcast(new Intent(C.USER_DATA_CHANGED));

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
                    onProfileSaved("profile saved");
                } else {
                    Util.onError(e, ProfileEditActivity.this);
                }
            }
        };
        saveTask.execute();


    }

    public void selectGender() {
        new AlertDialog.Builder(this, R.style.AppAlertDialogTheme)
                .setTitle(R.string.select_gender)
                .setItems(R.array.genders, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (which<2) {
                            gender.set(genders[which]);
                        }else {
                            gender.set(getString(R.string.gender));
                            binding.gender.setTextColor(ContextCompat.getColor(ProfileEditActivity.this,R.color.hint));
                        }

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
                            changeProfilePicture();
                            break;
                        case CHANGE_PROFILE_BACKGROUND:
                            changeProfileBackgound();
                            break;
                    }
                }
            }
        }
    }
}
