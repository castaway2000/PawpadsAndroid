package saberapplications.pawpads;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.ui.home.MainActivity;

public class profilepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);

//        //receiving data from listview
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(profilepage.this);
        String userName = defaultSharedPreferences.getString(Util.USER_NAME, "");
        String userInfo = defaultSharedPreferences.getString(Util.USER_INFO, "");
        final String imgVal = defaultSharedPreferences.getString(Util.USER_AVATAR_PATH, "");
        String loc = defaultSharedPreferences.getString(Util.USER_LOCATION, "");

//        //setting new data into profile
        String newTitle = "PawPads | " + userName;
        ImageView iv = (ImageView) findViewById(R.id.profilepic);
        TextView tv = (TextView) findViewById(R.id.profileinfo);
//
//        //this is getting an async task that is setting the image.
//        //TODO: make this from local stored variable.
        if(!imgVal.isEmpty()) {
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
                Intent i = new Intent(profilepage.this, MainActivity.class);
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
