package saberapplications.pawpads.ui.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

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
import saberapplications.pawpads.ui.login.LoginActivity;


/**
 * Created by blaze on 10/17/2015.
 */
public class ProfileEditActivity extends AppCompatActivity implements View.OnClickListener {
    //TODO: send profile info to the database.
    private final static int SELECT_IMAGE = 1;
    private static File avatarFile;
    String selectedImagePath;
    ImageView img;
    EditText textOut, proDescr;
    Button saveBtn, getimgbtn, removeProfile;
    Uri path;
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
        removeProfile = (Button) findViewById(R.id.removeProfile);
        proDescr = (EditText) findViewById(R.id.editProfileText);
//        textOut = (EditText) findViewById(R.id.editProfileText);
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
                if(currentQbUser.getCustomData()!=null) {
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
        removeProfile.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 buildRemoveAlertDialog();
                                             }
                                         }
        );
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
                if (avatarFile != null) {
                    updateAvatar(avatarFile);
                } else {
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

                //saved image state passed to database
//                Bitmap image = ((BitmapDrawable) img.getDrawable()).getBitmap();
//                new UploadImage(image, proDescr.getText().toString());
//
//                //saved description updated to database
//                String descr = proDescr.getText().toString();
//                textOut.setText(descr);
//
//                //back to main activity
//                Intent i = new Intent(profileEditPage.this, profilepage.class);
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(profileEditPage.this).edit();
//                editor.putString(Util.USER_INFO, textOut.getText().toString());
//                editor.putString(Util.USER_AVATAR_PATH, selectedImagePath);
//                editor.apply();
//                startActivity(i);
//                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                path = selectedImageUri;

                System.out.println("Image Path : " + selectedImagePath);
                try {
                    bitmap = decodeUri(getApplicationContext(), selectedImageUri, 80);
                    img.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        return uri.getPath();
    }


    public Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        InputStream inputStream = c.getContentResolver().openInputStream(uri);
        avatarFile = getAvatarFile(inputStream);
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    private void buildRemoveAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileEditActivity.this);
        alertDialog.setTitle("Remove profile");
        alertDialog.setMessage("Enter Password");
        final EditText input = new EditText(ProfileEditActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (!input.getText().toString().isEmpty() && input.getText().toString().equals(defaultSharedPreferences.getString(Util.QB_PASSWORD, ""))) {
                            QBUsers.deleteUser(defaultSharedPreferences.getInt(Util.QB_USERID, -1), new QBEntityCallbackImpl() {

                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ProfileEditActivity.this, "Remove profile successfully", Toast.LENGTH_LONG).show();
                                    Intent myIntent1 = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(myIntent1);
                                    finish();

                                }

                                @Override
                                public void onError(List errors) {
                                    Util.onError(errors, ProfileEditActivity.this);
                                }
                            });
                        } else {
                            Toast.makeText(ProfileEditActivity.this, "Wrong password", Toast.LENGTH_LONG).show();
                        }


                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    private void updateAvatar(File file) {

        QBContent.uploadFileTask(file, false, file.getAbsolutePath(), new QBEntityCallback<QBFile>() {
            @Override
            public void onSuccess(QBFile qbFile, Bundle params) {

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
    }

    private File getAvatarFile(InputStream inputStream) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "tempAvatar.jpg");
        try {
            OutputStream os = new FileOutputStream(file);
            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 10, bos);
            InputStream in = new ByteArrayInputStream(bos.toByteArray());
            ContentBody photo = new InputStreamBody(in, "image/jpeg", "filename");
            photo.writeTo(os);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(avatarFile!=null){
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
