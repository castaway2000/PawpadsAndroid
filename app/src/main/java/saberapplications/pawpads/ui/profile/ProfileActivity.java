package saberapplications.pawpads.ui.profile;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

    private TextView profileInfo;
    private ImageView profileAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);
        profileAvatar = (ImageView) findViewById(R.id.profilepic);
        profileInfo = (TextView) findViewById(R.id.profileinfo);
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
                    AvatarLoaderHelper.loadImage(userProfilePictureID, profileAvatar,size,size);

                }

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

}
