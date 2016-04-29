package saberapplications.pawpads.ui.profile;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.listeners.QBPrivacyListListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;

public class ProfileActivity extends BaseActivity {
    private QBDialog dialog;
    private QBUser currentQbUser;

    private AdView adView, largeAdView;
    private InterstitialAd interAd;
    private TextView profileInfo;
    private TextView isBlockedView;
    private ImageView profileAvatar;

    private Privacy privacy;
    private boolean isUserBlocked;
    private Button chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilepage);
        profileAvatar = (ImageView) findViewById(R.id.profilepic);
        profileInfo = (TextView) findViewById(R.id.profileinfo);
        isBlockedView = (TextView) findViewById(R.id.is_blocked);
        privacy = new Privacy();

        interAd = new InterstitialAd(this);
        interAd.setAdUnitId(Util.AD_UNIT_ID);
        //String DEVICE_ID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        adView = (AdView) this.findViewById(R.id.profileAdView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("3064B67C1862D04332D90B97D7E7F360")//)AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);

        interAd.loadAd(adRequest);
        interAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                displayInterAd();
            }
        });

        dialog = (QBDialog) getIntent().getSerializableExtra(ChatActivity.DIALOG);
        chatButton = (Button) findViewById(R.id.chatBtn);
        QBUsers.getUser(getIntent().getExtras().getInt(Util.QB_USERID, -1), new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                currentQbUser = qbUser;
                if (currentQbUser.getCustomData() != null) {
                    String info = String.valueOf(currentQbUser.getCustomData());
                    if (!info.equals("null")) {
                        profileInfo.setText(String.valueOf(currentQbUser.getCustomData()));
                    }
                }
                if (qbUser.getFullName() != null) {
                    setTitle("PawPads | " + qbUser.getFullName());
                } else {
                    setTitle("PawPads | " + qbUser.getLogin());
                }

                if (currentQbUser.getFileId() != null) {
                    int userProfilePictureID = currentQbUser.getFileId(); // user - an instance of QBUser class
                    float d = getResources().getDisplayMetrics().density;
                    int size = Math.round(150 * d);
                    AvatarLoaderHelper.loadImage(userProfilePictureID, profileAvatar, size, size);

                }
                // Check if user is blocked

                QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();
                try {
                    QBPrivacyList list = privacyListsManager.getPrivacyList("public");
                    if (list != null) {
                        for (QBPrivacyListItem item : list.getItems()) {
                            String id = currentQbUser.getId().toString();
                            if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                                            item.getValueForType().contains(id)
                                    ) {
                                setBlockedUI(!item.isAllow());
                            }
                        }

                    }else{
                        setBlockedUI(false);
                    }
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                    setBlockedUI(false);
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                    setBlockedUI(false);
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                    setBlockedUI(false);
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

        View.OnClickListener clickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, ChatActivity.class);
                i.putExtra(ChatActivity.DIALOG, dialog);
                i.putExtra(ChatActivity.RECIPIENT, currentQbUser);
                startActivity(i);
                finish();
            }
        };
        chatButton.setOnClickListener(clickHandler);
    }

    public void displayInterAd() {
        int interval = (int) Math.floor(Math.random() * 101) % 3;
        if (interAd.isLoaded() && interval == 0) {
            interAd.show();
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem unblock, block;
        block = menu.findItem(R.id.action_blockUser);
        unblock = menu.findItem(R.id.action_unblock);
        if (isUserBlocked) {
            block.setVisible(false);
            unblock.setVisible(true);
        } else {
            block.setVisible(true);
            unblock.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_blockUser:
                addUserToBlockList();
                return true;

            case R.id.action_unblock:
                removeUserFromBlockList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addUserToBlockList() {

        QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();


        QBPrivacyList list = new QBPrivacyList();
        list.setName("public");
        ArrayList<QBPrivacyListItem> items = new ArrayList<>();

        QBPrivacyListItem item1 = new QBPrivacyListItem();
        item1.setAllow(false);
        item1.setType(QBPrivacyListItem.Type.USER_ID);
        item1.setValueForType(String.valueOf(currentQbUser.getId()));
        items.add(item1);
        list.setItems(items);
        try {
            privacyListsManager.setPrivacyList(list);
            list.setActiveList(true);
            list.setDefaultList(true);
            Toast.makeText(ProfileActivity.this, R.string.user_added_to_block_list, Toast.LENGTH_LONG).show();
            setBlockedUI(true);
            saveBlockListToPreferences(list);

        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage(e.getLocalizedMessage())
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });

        }

    }

    private void removeUserFromBlockList() {
        QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();
        try {
            QBPrivacyList list = privacyListsManager.getPrivacyList("public");
            List<QBPrivacyListItem> items = list.getItems();
            for (QBPrivacyListItem item : items) {
                String id = currentQbUser.getId().toString();

                if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                        item.getValueForType().contains(id)) {
                    item.setAllow(true);
                }
            }
            list.setItems(items);
            privacyListsManager.setPrivacyList(list);
            setBlockedUI(false);
            saveBlockListToPreferences(list);
            Toast.makeText(ProfileActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    private void setBlockedUI(boolean isBlocked) {
        isUserBlocked=isBlocked;
        if (isBlocked) {
            isBlockedView.setVisibility(View.VISIBLE);
            chatButton.setVisibility(View.GONE);
        } else {
            isBlockedView.setVisibility(View.GONE);
            chatButton.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }
    private void saveBlockListToPreferences(QBPrivacyList list){
        JSONArray ids=new JSONArray();
        for (QBPrivacyListItem item : list.getItems()) {
            String id = currentQbUser.getId().toString();

            if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                    !item.isAllow()) {
                Pattern p = Pattern.compile("\\d*_");
                Matcher m = p.matcher(item.getValueForType());
                if(m.matches()) {
                    ids.put(m.group(1));
                }
            }
        }




    }
}
