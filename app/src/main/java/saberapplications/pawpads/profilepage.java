package saberapplications.pawpads;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.QBContent;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.InputStream;
import java.util.List;

import saberapplications.pawpads.ui.chat.ChatActivity;

public class profilepage extends AppCompatActivity {
    private QBDialog dialog;
    private QBUser currentQbUser;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView profileInfo;
    private ImageView profileAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);
        profileAvatar = (ImageView) findViewById(R.id.profilepic);
        profileInfo = (TextView) findViewById(R.id.profileinfo);
        dialog = (QBDialog) getIntent().getSerializableExtra(ChatActivity.EXTRA_DIALOG);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });
        QBUsers.getUser(getIntent().getExtras().getInt(Util.QB_USERID, -1), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                currentQbUser = qbUser;
                if(currentQbUser.getCustomData()!=null) {
                    profileInfo.setText(String.valueOf(currentQbUser.getCustomData()));
                }
                String newTitle = "PawPads | " + qbUser.getFullName();
                setTitle(newTitle);
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
                            Util.onError(list, profilepage.this);
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
                Util.onError(list, profilepage.this);
            }
        });
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
            profileAvatar.setImageBitmap(bitmap);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
