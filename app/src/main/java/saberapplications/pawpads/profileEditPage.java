package saberapplications.pawpads;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


/**
 * Created by blaze on 10/17/2015.
 */
public class profileEditPage extends AppCompatActivity {
    //TODO: save button
    //TODO: image handling
    //TODO: text handling
    private final static int SELECT_IMAGE = 1;
    private String selectedImagePath;
    private ImageView img;
    private EditText proDescr;
    private EditText textOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editpage);
        img = (ImageView) findViewById(R.id.editImageView);
        proDescr = (EditText) findViewById(R.id.editProfileText);
        textOut = (EditText) findViewById(R.id.editProfileText);
        imageHandler();

        Button saveBtn = (Button) findViewById(R.id.profileSave);
        View.OnClickListener clickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saved image state passed to database

                //saved description updated to database
                String test = proDescr.getText().toString();
                textOut.setText(test);
                //back to main activity
                Intent i = new Intent(profileEditPage.this, MainActivity.class);
                startActivity(i);
            }
        };
        saveBtn.setOnClickListener(clickHandler);
    }

    private void imageHandler() {
        Button getimgbtn = (Button) findViewById(R.id.newPicButton);
        View.OnClickListener clickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(intent, SELECT_IMAGE);
            }
        };
        getimgbtn.setOnClickListener(clickHandler);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                System.out.println("Image Path : " + selectedImagePath);
                img.setImageURI(selectedImageUri);
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
}
