package saberapplications.pawpads.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
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
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;


/**
 * Created by blaze on 10/17/2015.
 */
public class ProfileEditActivity extends BaseActivity implements View.OnClickListener {
    //TODO: send profile info to the database.
    private final static int SELECT_IMAGE = 1;
    private static File avatarFile;
    String selectedImagePath;
    ImageView img;
    EditText proDescr;
    Button saveBtn, getimgbtn;
    Uri avatarImagePath;
    private QBUser currentQbUser;
    private SharedPreferences defaultSharedPreferences;
    private Bitmap bitmap;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editpage);
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
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileEditActivity.this);
        QBUsers.getUser(defaultSharedPreferences.getInt(Util.QB_USERID, -1), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                currentQbUser = qbUser;
                if (currentQbUser.getCustomData() != null) {
                    proDescr.setText(String.valueOf(currentQbUser.getCustomData()));
                }
                if (currentQbUser.getFileId() != null) {
                    int userProfilePictureID = currentQbUser.getFileId(); // user - an instance of QBUser class

                    QBContent.downloadFileTask(userProfilePictureID, new QBEntityCallback<InputStream>() {
                        @Override
                        public void onSuccess(InputStream inputStream, Bundle params) {
                            new BitmapDownloader().execute(inputStream);
                        }

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(List<String> list) {
                            Util.onError(list, ProfileEditActivity.this);
                        }


                    }, new QBProgressCallback() {
                        @Override
                        public void onProgressUpdate(int progress) {

                        }
                    });
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {
                Util.onError(list, ProfileEditActivity.this);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.newPicButton:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, SELECT_IMAGE);
                break;

            case R.id.profileSave:
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                if (avatarImagePath != null) {
                    updateAvatar();
                }

                currentQbUser.setCustomData(proDescr.getText().toString());
                QBUsers.updateUser(currentQbUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle args) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(List<String> list) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Util.onError(list, ProfileEditActivity.this);
                    }
                });
                Toast.makeText(getApplicationContext(), "profile saved", Toast.LENGTH_SHORT).show();
                break;
        }
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
                    final File file = File.createTempFile("avatar",".jpg",getCacheDir());
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.close();

                    mSwipeRefreshLayout.setRefreshing(true);
                    QBContent.uploadFileTask(file, false, file.getAbsolutePath(), new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle params) {
                            file.delete();
                            int uploadedFileID = qbFile.getId();
                            currentQbUser.setFileId(uploadedFileID);
                            currentQbUser.setCustomData(proDescr.getText().toString());
                            QBUsers.updateUser(currentQbUser, new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser user, Bundle args) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }

                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(List<String> list) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    Util.onError(list, ProfileEditActivity.this);
                                }
                            });
                        }

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(List<String> list) {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Util.onError(list, ProfileEditActivity.this);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (avatarFile != null) {
            avatarFile.delete();
        }
    }

    private class BitmapDownloader extends AsyncTask<InputStream, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(InputStream... params) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            while (true) {
                if (width_tmp / 2 < 80 || height_tmp / 2 < 80)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(params[0], null, o2);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            img.setImageBitmap(bitmap);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
