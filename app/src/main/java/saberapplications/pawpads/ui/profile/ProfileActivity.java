package saberapplications.pawpads.ui.profile;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.model.QBEntity;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
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
import saberapplications.pawpads.util.Constants;

public class ProfileActivity extends BaseActivity {
    private QBDialog dialog;
    private QBUser currentQbUser;

    private AdView adView, largeAdView;
    private InterstitialAd interAd;
    private TextView profileInfo;
    private TextView isBlockedView;
    private ImageView profileAvatar;
    private Privacy privacy;
    private Button chatButton;
    String name;
    private QBUser currentUser;
    private boolean isBlockedByOther;
    private boolean isBlockedByMe;
    private ProgressDialog progressDialog;

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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);
        progressDialog.show();
        new AsyncTask<Void, Void, Void>() {
            String error = null;

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    currentQbUser = QBUsers.getUser(getIntent().getExtras().getInt(Util.QB_USERID, -1));
                    currentUser = QBUsers.getUser(PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this).getInt(Util.QB_USERID, -1));

                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentQbUser.getId());
                    requestBuilder.eq("blocked_user", currentUser.getId());
                    ArrayList<QBCustomObject> blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    isBlockedByOther = blocks.size() > 0;
                    requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentUser.getId());
                    requestBuilder.eq("blocked_user", currentQbUser.getId());
                    blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    isBlockedByMe = blocks.size() > 0;
                } catch (QBResponseException e) {
                    e.printStackTrace();
                }
                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
                if (error != null) {
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(error)
                            .setPositiveButton("OK",null)
                            .show();
                    return;
                }

                if (currentQbUser.getFullName() != null) {
                    name = currentQbUser.getFullName();
                    setTitle("PawPads | " + name);
                } else {
                    name = currentQbUser.getLogin();
                    setTitle("PawPads | " + name);
                }

                if (currentQbUser.getCustomData() != null) {
                    String info = String.valueOf(currentQbUser.getCustomData());
                    if (!info.equals("null")) {
                        profileInfo.setText(String.valueOf(currentQbUser.getCustomData()));
                    }
                } else {
                    profileInfo.setText(name + " has not set up their profile info yet.");
                }

                if (currentQbUser.getFileId() != null) {
                    int userProfilePictureID = currentQbUser.getFileId(); // user - an instance of QBUser class
                    float d = getResources().getDisplayMetrics().density;
                    int size = Math.round(150 * d);
                    AvatarLoaderHelper.loadImage(userProfilePictureID, profileAvatar, size, size);

                }
                setBlockedUI(isBlockedByMe);


            }
        }.execute();


        View.OnClickListener clickHandler = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ProfileActivity.this, ChatActivity.class);
                i.putExtra(ChatActivity.DIALOG, dialog);
                i.putExtra(ChatActivity.RECIPIENT, currentQbUser);
                i.putExtra(Util.IS_BLOCKED, isBlockedByMe);
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
        if (isBlockedByMe) {
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

        new AsyncTask<Void, Void, Void>() {
            private String error;

            @Override
            protected Void doInBackground(Void... params) {
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

                    saveBlockListToPreferences(list);

                    QBCustomObject object = new QBCustomObject();
                    object.putInteger("source_user", currentUser.getId());
                    object.putInteger("blocked_user", currentQbUser.getId());

// set the class name
                    object.setClassName("BlockList");
                    QBCustomObjects.createObject(object);

                    QBPrivateChat privatChat = QBChatService.getInstance().getPrivateChatManager().getChat(currentQbUser.getId());
                    if (privatChat==null){
                       privatChat=QBChatService.getInstance().getPrivateChatManager().createChat(currentQbUser.getId(),null);
                    }
                    QBChatMessage message=new QBChatMessage();
                    message.setProperty("blocked","1");
                    privatChat.sendMessage(message);

                } catch (Exception e) {
                    error = e.getLocalizedMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (error != null) {
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage(error)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).show();
                } else {
                    setBlockedUI(true);
                    Toast.makeText(ProfileActivity.this, R.string.user_added_to_block_list, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();


    }

    private void removeUserFromBlockList() {

        new AsyncTask<Void, Void, Boolean>() {
            String error;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentUser.getId());
                    requestBuilder.eq("blocked_user", currentQbUser.getId());

                    ArrayList<QBCustomObject> blockedList = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    for (QBCustomObject item : blockedList) {
                        QBCustomObjects.deleteObject("BlockList", item.getCustomObjectId());
                    }

                    QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();

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

                    saveBlockListToPreferences(list);
                    QBPrivateChat privatChat = QBChatService.getInstance().getPrivateChatManager().getChat(currentQbUser.getId());
                    if (privatChat==null){
                        privatChat=QBChatService.getInstance().getPrivateChatManager().createChat(currentQbUser.getId(),null);
                    }
                    QBChatMessage message=new QBChatMessage();
                    message.setProperty("blocked","0");
                    privatChat.sendMessage(message);


                } catch (Exception e) {
                    e.printStackTrace();
                    error = e.getLocalizedMessage();
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    setBlockedUI(false);
                    Toast.makeText(ProfileActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();
                } else {

                }
            }
        }.execute();


    }

    private void setBlockedUI(boolean isBlocked) {
        isBlockedByMe = isBlocked;
        if (isBlockedByMe) {
            chatButton.setVisibility(View.VISIBLE);
            isBlockedView.setVisibility(View.VISIBLE);

        } else {
            chatButton.setVisibility(View.VISIBLE);
            isBlockedView.setVisibility(View.GONE);
        }
        if (isBlockedByOther || isBlockedByMe) {
            chatButton.setText(R.string.text_chat_history);
        }else{
            chatButton.setText(R.string.text_chat);
        }


        if (isBlockedByOther) {
            isBlockedView.setVisibility(View.VISIBLE);
            isBlockedView.setText(R.string.text_you_blocked);
        } else {
            isBlockedView.setVisibility(View.GONE);
        }


        invalidateOptionsMenu();

    }

    private void saveBlockListToPreferences(QBPrivacyList list) {

        final JSONArray ids = new JSONArray();
        for (QBPrivacyListItem item : list.getItems()) {
            if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                    !item.isAllow()) {
                Pattern p = Pattern.compile("(\\d*)");
                Matcher m = p.matcher(item.getValueForType());
                if (m.find()) {
                    ids.put(m.group(1));
                }
            }
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.BLOCKED_USERS_IDS, ids.toString());
        editor.apply();

    }
}
