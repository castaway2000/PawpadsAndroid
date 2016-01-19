package saberapplications.pawpads;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;

import saberapplications.pawpads.ui.chat.ChatActivity;

public class profilepage extends AppCompatActivity {
    private QBDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);
//        //receiving data from listview
        String userName = getIntent().getExtras().getString(Util.USER_NAME, "");
        String userInfo = getIntent().getExtras().getString(Util.USER_INFO, "");
        final String imgVal = getIntent().getExtras().getString(Util.USER_AVATAR_PATH, "");
        String loc = getIntent().getExtras().getString(Util.USER_LOCATION, "");
        dialog = (QBDialog) getIntent().getSerializableExtra(ChatActivity.EXTRA_DIALOG);
//        //setting new data into profile
        String newTitle = "PawPads | " + userName;
        ImageView iv = (ImageView) findViewById(R.id.profilepic);
        TextView tv = (TextView) findViewById(R.id.profileinfo);
//
//        //this is getting an async task that is setting the image.
//        //TODO: make this from local stored variable.
        if (!imgVal.isEmpty()) {
            ImageLoader imageloader = ImageLoader.getInstance();
            imageloader.displayImage(imgVal, iv);
        }

        tv.setText(userInfo);
        setTitle(newTitle);

        //button click event
        Button button = (Button) findViewById(R.id.chatBtn);
        View.OnClickListener clickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(profilepage.this, ChatActivity.class);
                i.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
                startActivity(i);
                finish();
            }
        };
        button.setOnClickListener(clickHandler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profilepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
