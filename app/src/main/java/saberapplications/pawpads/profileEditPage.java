package saberapplications.pawpads;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;


/**
 * Created by blaze on 10/17/2015.
 */
public class profileEditPage extends AppCompatActivity implements View.OnClickListener{
    //TODO: send profile info to the database.
    private final static int SELECT_IMAGE = 1;
    String selectedImagePath;
    ImageView img;
    EditText textOut, proDescr;
    Button saveBtn, getimgbtn;
    Uri path;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: retrieve and set all user info for the user in propper spots.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editpage);
        setTitle("PawPads | Edit Profile");

        img = (ImageView) findViewById(R.id.editImageView);

        getimgbtn = (Button) findViewById(R.id.newPicButton);
        saveBtn = (Button) findViewById(R.id.profileSave);
        proDescr = (EditText) findViewById(R.id.editProfileText);
        //proDescr.setText(user.userInfo);

        getimgbtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //TODO:// on click send username, image, and profile description to UpdateUser.php
        switch(v.getId()){
            case R.id.newPicButton:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, SELECT_IMAGE);
                break;

            case R.id.profileSave:
                //saved image state passed to database
                Bitmap image = ((BitmapDrawable)img.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

                user.image = encodedImage;
                //saved description updated to database
                user.userInfo = proDescr.getText().toString();

                UpdateUser();

                //back to main activity
                Intent i = new Intent(profileEditPage.this, MainActivity.class);
                startActivity(i);
                break;
        }
    }


    public void UpdateUser(){
        ServerRequests serverRequests = new ServerRequests(this, user.lat, user.lng, user.username);
        serverRequests.updateUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                finish();
            }
        });
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
                    img.setImageBitmap(decodeUri(getApplicationContext(), selectedImageUri, 80));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }
}
