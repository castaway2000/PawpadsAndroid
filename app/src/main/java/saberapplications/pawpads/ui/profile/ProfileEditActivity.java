package saberapplications.pawpads.ui.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.ProfileEditpageBinding;
import saberapplications.pawpads.model.UserProfile;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;


/**
 * Created by blaze on 10/17/2015.
 */
public class ProfileEditActivity extends BaseActivity implements View.OnClickListener {
    private final static int SELECT_IMAGE = 1;
    String selectedImagePath;
    ImageView img;
    EditText proDescr;
    Button saveBtn, getimgbtn;
    Uri avatarImagePath;
    private QBUser currentQbUser;
    private UserProfile profile;
    private SharedPreferences defaultSharedPreferences;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Runnable timeOutRunnable;
    public final BindableBoolean isBusy = new BindableBoolean();

    private ProfileEditpageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.profile_editpage);
        binding.setActivity(this);
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.password_recovery);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("PawPads | Edit Profile");

        img = (ImageView) findViewById(R.id.editImageView);

        getimgbtn = (Button) findViewById(R.id.newPicButton);
        saveBtn = (Button) findViewById(R.id.profileSave);
        proDescr = (EditText) findViewById(R.id.editProfileText);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        getimgbtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
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
                if (currentQbUser.getFileId() != null) {
                    AvatarLoaderHelper.loadImage(currentQbUser.getFileId(), img,
                            img.getWidth(), img.getHeight()
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
                Util.onError(e, ProfileEditActivity.this);
            }

        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.newPicButton:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE);
                break;

            case R.id.profileSave:
                if (mSwipeRefreshLayout.isRefreshing()) return;

                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                timeOutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getApplicationContext(), "Connection timeout", Toast.LENGTH_SHORT).show();
                    }
                };
                //edit second parameter to set timeout period
                mSwipeRefreshLayout.postDelayed(timeOutRunnable, 8000);

                currentQbUser.setCustomData(proDescr.getText().toString());
                QBUsers.updateUser(currentQbUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {
                        if (avatarImagePath != null) {
                            updateAvatar();
                        } else {
                            onProfileSaved("profile saved");
                        }

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Util.onError(e, ProfileEditActivity.this);
                        onProfileSaved(null);
                    }

                });

                break;
        }
    }

    private void onProfileSaved(String message) {
        if (message != null) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.removeCallbacks(timeOutRunnable);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                avatarImagePath = selectedImageUri;
                //  try {
                //    bitmap = decodeUri(getApplicationContext(), selectedImageUri, img.getWidth());
                Picasso.with(this).load(selectedImageUri)
                        .resize(img.getWidth(), img.getHeight())
                        .centerCrop()
                        .into(img);
            /*
            } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
              //  img.setImageBitmap(bitmap);
            }*/
            }
        }
    }

    public String getPath(Uri uri) {
        return uri.getPath();
    }


    private void updateAvatar() {
// Crop bitmap to 500x500, save it file ,and then upload it to server
        Picasso.with(this).load(avatarImagePath).resize(500, 500).centerCrop().into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                FileOutputStream out = null;
                try {
                    final File file = File.createTempFile("avatar", ".jpg", getCacheDir());
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();

                    mSwipeRefreshLayout.setRefreshing(true);
                    QBContent.uploadFileTask(file, false, file.getAbsolutePath(), new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle params) {
                            int uploadedFileID = qbFile.getId();
                            currentQbUser.setFileId(uploadedFileID);
                            currentQbUser.setCustomData(proDescr.getText().toString());
                            QBUsers.updateUser(currentQbUser, new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser user, Bundle args) {
                                    file.delete();
                                    avatarImagePath = null;
                                    onProfileSaved("profile saved");
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    Util.onError(e, ProfileEditActivity.this);
                                    file.delete();
                                    avatarImagePath = null;
                                    onProfileSaved("profile saved");
                                }

                            });
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            onProfileSaved(null);
                            Util.onError(e, ProfileEditActivity.this);

                        }


                    }, new QBProgressCallback() {
                        @Override
                        public void onProgressUpdate(int progress) {

                        }
                    });


                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (Exception e2) {

                    }
                }


            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });


    }

    public void changeProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
