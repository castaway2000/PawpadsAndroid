package saberapplications.pawpads.ui.profile;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;

public class ProfileActivity extends BaseActivity {
    private QBDialog dialog;
    private QBUser currentQbUser;

    private AdView adView, largeAdView;
    private TextView profileInfo;
    private ImageView profileAvatar;
    MenuItem unblock, block;
    private Privacy privacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);
        profileAvatar = (ImageView) findViewById(R.id.profilepic);
        profileInfo = (TextView) findViewById(R.id.profileinfo);
        block = (MenuItem) findViewById(R.id.action_blockUser);
        unblock = (MenuItem) findViewById(R.id.action_unblock);
        privacy = new Privacy();

        //String DEVICE_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
//        adView = (AdView) this.findViewById(R.id.profileAdView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice("3064B67C1862D04332D90B97D7E7F360")//)AdRequest.DEVICE_ID_EMULATOR)
//                .build();
//        adView.loadAd(adRequest);

        dialog = (QBDialog) getIntent().getSerializableExtra(ChatActivity.DIALOG);
        QBUsers.getUser(getIntent().getExtras().getInt(Util.QB_USERID, -1), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                currentQbUser = qbUser;
                if(currentQbUser.getCustomData()!=null) {
                    String info=String.valueOf(currentQbUser.getCustomData());
                    if (!info.equals("null")){
                        profileInfo.setText(String.valueOf(currentQbUser.getCustomData()));
                    }
                }
                if (qbUser.getFullName()!=null){
                    setTitle( "PawPads | " + qbUser.getFullName());
                }else{
                    setTitle( "PawPads | " + qbUser.getLogin());
                }

                if (currentQbUser.getFileId() != null) {
                    int userProfilePictureID = currentQbUser.getFileId(); // user - an instance of QBUser class
                    float d=getResources().getDisplayMetrics().density;
                    int size=Math.round(150* d);
                    AvatarLoaderHelper.loadImage(userProfilePictureID, profileAvatar, size, size);

                }

//                if(privacy.items.contains(currentQbUser.getId())){
//                   block.setVisible(false);
//                    unblock.setVisible(true);
//                } else {
//                    unblock.setVisible(false);
//                    block.setVisible(true);
//                }
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {
                Util.onError(list, ProfileActivity.this);
            }
        });
        Button button = (Button) findViewById(R.id.chatBtn);
        View.OnClickListener clickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, ChatActivity.class);
                i.putExtra(ChatActivity.DIALOG, dialog);
                i.putExtra(ChatActivity.RECIPIENT,currentQbUser);
                startActivity(i);
                finish();
            }
        };
        button.setOnClickListener(clickHandler);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profilepage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_blockUser:
                  privacy.addToBlockList(currentQbUser.getId());
//                  block.setEnabled(false);
//                block.setVisible(false);
//                unblock.setVisible(true);
                finish();
                return true;

            case R.id.action_unblock:
                privacy.removeFromBlockList(currentQbUser.getId());
//                block.setVisible(true);
//                unblock.setVisible(false);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
