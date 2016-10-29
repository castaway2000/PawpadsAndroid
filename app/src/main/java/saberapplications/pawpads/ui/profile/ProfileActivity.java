package saberapplications.pawpads.ui.profile;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.InterstitialAd;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ActivityProfilepageBinding;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.model.UserProfile;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;

public class ProfileActivity extends BaseActivity {



    private InterstitialAd interAd;

    private TextView isBlockedView;

    private QBUser qbUser;
    public final BindableBoolean isBlockedByOther=new BindableBoolean();
    public final BindableBoolean isBlockedByMe=new BindableBoolean();
    public final BindableBoolean isBusy=new BindableBoolean();
    public final BindableString progressMessage=new BindableString();

    UserProfile profile;
    ActivityProfilepageBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=DataBindingUtil.setContentView(this,R.layout.activity_profilepage);
        binding.setActivity(this);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);


        progressMessage.set(getString(R.string.loading));
        isBlockedView = (TextView) findViewById(R.id.is_blocked);

        qbUser= (QBUser) getIntent().getSerializableExtra(C.QB_USER);
        interAd = new InterstitialAd(this);
        interAd.setAdUnitId(Util.AD_UNIT_ID);

        isBusy.set(true);
        new AsyncTask<Void, Void, Void>() {
            Exception e;
            @Override
            protected Void doInBackground(Void... params) {
                try {
                 //   qbUser = QBUsers.getUser(getIntent().getExtras().getInt(C.QB_USERID, -1));
                    if(currentQBUser==null) {
                        currentQBUser = QBUsers.getUser(PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this).getInt(C.QB_USERID, -1));
                    }
                    if (qbUser.getFullName()==null){
                        qbUser.setFullName(qbUser.getLogin());
                    }
                    binding.setUser(qbUser);


                    profile = UserProfile.createFromJson(qbUser.getCustomData());
                    float density=getResources().getDisplayMetrics().density;

                    if (qbUser.getFileId()!=null){
                        AvatarLoaderHelper.loadImageSync(qbUser.getFileId(), binding.userAvatar,
                                Math.round(density*100), Math.round(density*100));
                    }

                    int width=getResources().getDisplayMetrics().widthPixels;
                    if (profile.getBackgroundId()>0){
                        int height=Math.round(density*147);
                        AvatarLoaderHelper.loadImageSync(profile.getBackgroundId(), binding.userBackground,
                                width,height);
                    }
                    binding.setProfile(profile);

                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentQBUser.getId());
                    requestBuilder.eq("blocked_user", qbUser.getId());
                    ArrayList<QBCustomObject> blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    isBlockedByMe.set( blocks.size() > 0);
                    requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", qbUser.getId());
                    requestBuilder.eq("blocked_user", currentQBUser.getId());
                    blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    isBlockedByOther.set(blocks.size() > 0);



                } catch (QBResponseException e) {
                    e.printStackTrace();
                    this.e=e;

                }
                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                isBusy.set(false);
                if (e!=null) {
                    Util.onError(e, ProfileActivity.this);
                    return;
                }
                if (profile.getAge()>0){
                    binding.age.setText(String.format(getString(R.string.age),profile.getAge()));
                }
                float density=getResources().getDisplayMetrics().density;
                if (profile.getGender().equals("M")){
                    Drawable drawable=ContextCompat.getDrawable(ProfileActivity.this,R.drawable.female_icon);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    binding.age.setCompoundDrawables(drawable
                            ,null,null,null);
                }else if(profile.getGender().equals("F")){
                    binding.age.setCompoundDrawables(
                            ContextCompat.getDrawable(ProfileActivity.this,R.drawable.female_icon),null,null,null);
                }
                binding.age.invalidate();


                setBlockedUI(isBlockedByMe.get());
            }
        }.execute();


    }

    public void displayInterAd() {
        int interval = (int) Math.floor(Math.random() * 101) % 3;
        if (interAd.isLoaded() && interval == 0) {
            interAd.show();
        }
    }

    public void openChat(){
        isBusy.set(true);
        progressMessage.set(getString(R.string.loading));
        QBPrivateChatManager chatManager = QBChatService.getInstance().getPrivateChatManager();
        if (chatManager == null) return;
        Intent i = new Intent(ProfileActivity.this, ChatActivity.class);

        i.putExtra(ChatActivity.RECIPIENT, qbUser);
        i.putExtra(Util.IS_BLOCKED, isBlockedByMe.get());
        startActivity(i);
        finish();
        isBusy.set(false);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }



    private void setBlockedUI(boolean isBlocked) {
        isBlockedByMe.set(isBlocked);


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
        editor.putString(C.BLOCKED_USERS_IDS, ids.toString());
        editor.apply();

    }
    public void blockUser(){
        isBusy.set(true);
        progressMessage.set(getString(R.string.blocking_user));
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
                item1.setValueForType(String.valueOf(currentQBUser.getId()));
                items.add(item1);
                list.setItems(items);
                try {
                    privacyListsManager.setPrivacyList(list);
                    list.setActiveList(true);
                    list.setDefaultList(true);

                    saveBlockListToPreferences(list);

                    QBCustomObject object = new QBCustomObject();
                    object.putInteger("source_user", currentQBUser.getId());
                    object.putInteger("blocked_user", qbUser.getId());

// set the class name
                    object.setClassName("BlockList");
                    QBCustomObjects.createObject(object);

                    QBPrivateChat privatChat = QBChatService.getInstance().getPrivateChatManager().getChat(currentQBUser.getId());
                    if (privatChat==null){
                        privatChat=QBChatService.getInstance().getPrivateChatManager().createChat(currentQBUser.getId(),null);
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
                isBusy.set(false);
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
    public void unblockUser(){
        isBusy.set(true);
        progressMessage.set(getString(R.string.unblocking_user));
        new AsyncTask<Void, Void, Boolean>() {
            String error;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", qbUser.getId());
                    requestBuilder.eq("blocked_user", currentQBUser.getId());

                    ArrayList<QBCustomObject> blockedList = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    for (QBCustomObject item : blockedList) {
                        QBCustomObjects.deleteObject("BlockList", item.getCustomObjectId());
                    }

                    QBPrivacyListsManager privacyListsManager = QBChatService.getInstance().getPrivacyListsManager();

                    QBPrivacyList list = privacyListsManager.getPrivacyList("public");
                    List<QBPrivacyListItem> items = list.getItems();
                    for (QBPrivacyListItem item : items) {
                        String id = currentQBUser.getId().toString();

                        if (item.getType() == QBPrivacyListItem.Type.USER_ID &&
                                item.getValueForType().contains(id)) {
                            item.setAllow(true);
                        }
                    }
                    list.setItems(items);
                    privacyListsManager.setPrivacyList(list);

                    saveBlockListToPreferences(list);
                    QBPrivateChat privatChat = QBChatService.getInstance().getPrivateChatManager().getChat(currentQBUser.getId());
                    if (privatChat==null){
                        privatChat=QBChatService.getInstance().getPrivateChatManager().createChat(currentQBUser.getId(),null);
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
                isBusy.set(false);
                if (result) {
                    setBlockedUI(false);
                    Toast.makeText(ProfileActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();
                } else {

                }
            }
        }.execute();


    }
}
