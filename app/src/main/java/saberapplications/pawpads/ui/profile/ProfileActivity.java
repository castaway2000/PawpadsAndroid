package saberapplications.pawpads.ui.profile;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import saberapplications.pawpads.util.ChatRosterHelper;

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
    QBRoster chatRoster;

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
        binding.setUser(qbUser);

        initChatRoster();
        setFriendsUI();

        if(qbUser != null && qbUser.getId() == preferences.getInt(C.QB_USERID, 0)) {
            binding.blockUserView.setVisibility(View.GONE);
            binding.blockUserView.setText("");
            binding.blockUserView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
            binding.openChatButton.setVisibility(View.GONE);
            binding.addToFriendsButton.setVisibility(View.GONE);
        }

        //banner ad
        AdView adView = (AdView)findViewById(R.id.profileAdView);
        adView.loadAd(requestNewAd());

    }

    @Override
    public void onQBConnect(boolean isActivityReopened) throws Exception {
        if (isActivityReopened) return;
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
                    float density = getResources().getDisplayMetrics().density;

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
                    Calendar calendar= GregorianCalendar.getInstance();
                    binding.age.setText(String.format(getString(R.string.age),calendar.get(Calendar.YEAR)-profile.getAge()));
                }
                float density=getResources().getDisplayMetrics().density;
                if (profile.getGender().equals("M")){
                    Drawable drawable= ContextCompat.getDrawable(ProfileActivity.this,R.drawable.male_icon);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    binding.age.setCompoundDrawables(drawable
                            ,null,null,null);
                }else if(profile.getGender().equals("F")){
                    Drawable drawable= ContextCompat.getDrawable(ProfileActivity.this,R.drawable.female_icon);
                    drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());
                    binding.age.setCompoundDrawables(drawable
                            ,null,null,null);
                }

                if (qbUser.getFileId()!=null){
                    AvatarLoaderHelper.loadImage(qbUser.getFileId(), binding.userAvatar,
                            Math.round(density*100), Math.round(density*100));

                    AvatarLoaderHelper.loadImage(qbUser.getFileId(), binding.avatarHolder,
                            Math.round(density*60), Math.round(density*60));
                }


                if (profile.getBackgroundId()>0){
                    int height=Math.round(density*147);
                    int width=getResources().getDisplayMetrics().widthPixels;
                    AvatarLoaderHelper.loadImage(profile.getBackgroundId(), binding.userBackground,
                            width,height);
                }

                binding.age.invalidate();

                setBlockedUI(isBlockedByMe.get());

                setFriendsUI();
            }
        }.execute();
    }

    public AdRequest requestNewAd(){
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        return adRequest;
    }

//    public String getNewAdID(){
//        String ID;
//        Random rand = new Random();
//        int n = rand.nextInt(3)+1;
//        if(n == 1){ ID = String.valueOf(R.string.profile_activity_ad_unit_id); }
//        else if(n == 2){ ID = String.valueOf(R.string.profile_activity_ad_unit_id2); }
//        else{ ID = String.valueOf(R.string.profile_activity_ad_unit_id3); }
//        return ID;
//    }


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
                    new AlertDialog.Builder(ProfileActivity.this,R.style.AppAlertDialogTheme)
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
                    requestBuilder.eq("source_user", currentQBUser.getId());
                    requestBuilder.eq("blocked_user", qbUser.getId());

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

    private void initChatRoster() {
        if(chatRoster == null) {
            chatRoster = ChatRosterHelper.getChatRoster(new QBSubscriptionListener() {
                @Override
                public void subscriptionRequested(int userId) {
                    // nothing to do
                }
            });
        }
    }

    private void setFriendsUI() {
        if(chatRoster == null) return;
        if (chatRoster.contains(qbUser.getId())) {
            binding.addToFriendsButton.setImageResource(R.drawable.added_to_friend);
            binding.deleteFromFriends.setVisibility(View.VISIBLE);
        } else {
            binding.addToFriendsButton.setImageResource(R.drawable.add_to_friend);
            binding.deleteFromFriends.setVisibility(View.GONE);
        }
    }

    public void addUserToFriends() {
        if(qbUser.getId() == null || qbUser.getId() == preferences.getInt(C.QB_USERID, 0)) return;
        if(chatRoster.contains(qbUser.getId())) return;

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
                .setCancelable(true);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_to_friends, null);
        ImageView dialogAvatar = (ImageView) view.findViewById(R.id.dialog_avatar);
        dialogAvatar.setImageDrawable(binding.avatarHolder.getDrawable());

        TextView messageText = (TextView) view.findViewById(R.id.message_text);
        String sourceString = getString(R.string.dialog_add_friend_add) + " <b>" + qbUser.getFullName() + "</b> " + getString(R.string.dialog_add_to_friend);
        messageText.setText(Html.fromHtml(sourceString));

        builder.setView(view);

        final android.app.AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        TextView sendButton = (TextView) view.findViewById(R.id.send_friend_request);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                addToFriends();
            }
        });

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void addToFriends() {
        int userId = qbUser.getId();
        if (chatRoster.contains(userId)) {
            try {
                chatRoster.subscribe(userId);
                setFriendsUI();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                chatRoster.createEntry(userId, null);
                setFriendsUI();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeUserFromFriends() {
        if(qbUser.getId() == null || qbUser.getId() == preferences.getInt(C.QB_USERID, 0)) return;
        if( !chatRoster.contains(qbUser.getId())) return;

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
                .setCancelable(true);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_to_friends, null);
        ImageView dialogAvatar = (ImageView) view.findViewById(R.id.dialog_avatar);
        dialogAvatar.setImageDrawable(binding.avatarHolder.getDrawable());

        TextView messageText = (TextView) view.findViewById(R.id.message_text);
        String sourceString = getString(R.string.dialog_remove_friend) + " <b>" + qbUser.getFullName() + "</b> " + getString(R.string.dialog_remove_from_friend);
        messageText.setText(Html.fromHtml(sourceString));

        builder.setView(view);

        final android.app.AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        TextView sendButton = (TextView) view.findViewById(R.id.send_friend_request);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                removeFromFriends();
            }
        });

        TextView cancelButton = (TextView) view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void removeFromFriends() {
        int userId = qbUser.getId();
        try {
            chatRoster.unsubscribe(userId);
            setFriendsUI();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }
}
