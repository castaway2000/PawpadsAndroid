package saberapplications.pawpads.ui.profile;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.greenrobot.eventbus.EventBus;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.packet.RosterPacket;
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
import saberapplications.pawpads.events.FriendRemovedEvent;
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

        if(isOwnProfile()) {
            isBlockedByMe.set(true);
            hideFieldsInOwnProfile();
        }

        //banner ad
        AdView adView = (AdView)findViewById(R.id.profileAdView);
        adView.loadAd(requestNewAd());

    }

    private boolean isOwnProfile() {
        if(qbUser != null && qbUser.getId() == preferences.getInt(C.QB_USERID, 0)) {
            return true;
        }
        return false;
    }

    private void hideFieldsInOwnProfile() {
        binding.blockUserView.setVisibility(View.GONE);
        binding.blockUserView.setText("");
        binding.blockUserView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        binding.openChatButton.setVisibility(View.GONE);
        binding.addToFriendsButton.setVisibility(View.GONE);
        binding.openChatButtonBg.setVisibility(View.GONE);
        binding.addToFriendsButtonBg.setVisibility(View.GONE);
        binding.unblockButton.setVisibility(View.GONE);
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
                    if(!isOwnProfile()) isBlockedByMe.set( blocks.size() > 0);
                    requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", qbUser.getId());
                    requestBuilder.eq("blocked_user", currentQBUser.getId());
                    blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    if(!isOwnProfile()) isBlockedByOther.set(blocks.size() > 0);



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
                if(isBlockedByMe.get() && !isOwnProfile()) {
                    binding.userBlockedHeaderInfo.setVisibility(View.VISIBLE);
                    binding.userBackground.setImageResource(R.color.blocked_red);
                }

                setFriendsUI();
                if(isOwnProfile()) hideFieldsInOwnProfile();
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
                    binding.userBackground.setImageResource(R.color.blocked_red);
                    binding.userStatusInfo.setVisibility(View.GONE);
                    binding.userBlockedHeaderInfo.setVisibility(View.VISIBLE);
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
                    binding.userBlockedHeaderInfo.setVisibility(View.GONE);
                    float density=getResources().getDisplayMetrics().density;
                    if (profile.getBackgroundId()>0){
                        int height=Math.round(density*147);
                        int width=getResources().getDisplayMetrics().widthPixels;
                        AvatarLoaderHelper.loadImage(profile.getBackgroundId(), binding.userBackground, width,height);
                    } else {
                        binding.userBackground.setImageResource(R.drawable.app_bar_bg);
                    }
                    Toast.makeText(ProfileActivity.this, R.string.user_removed_from_block_list, Toast.LENGTH_LONG).show();
                } else {

                }
            }
        }.execute();


    }

    private void initChatRoster() {
        if(chatRoster == null) chatRoster = ChatRosterHelper.getChatRoster();
    }

    private void setFriendsUI() {
        if(chatRoster == null) return;
        if (chatRoster.contains(qbUser.getId())) {
            int userId = qbUser.getId();
            if(chatRoster != null && chatRoster.getEntry(userId) != null &&
                    chatRoster.getEntry(userId).getType() == RosterPacket.ItemType.none &&
                    chatRoster.getEntry(userId).getStatus() == RosterPacket.ItemStatus.subscribe) {
                binding.addToFriendsButton.setVisibility(View.GONE);
                binding.deleteFromFriends.setVisibility(View.GONE);
                binding.userStatusInfo.setVisibility(isBlockedByMe.get() ? View.GONE : View.VISIBLE);
            } else {
                binding.addToFriendsButton.setImageResource(R.drawable.added_to_friend);
                binding.deleteFromFriends.setVisibility(View.VISIBLE);
                binding.userStatusInfo.setVisibility(View.GONE);
            }
        } else {
            binding.addToFriendsButton.setImageResource(R.drawable.add_to_friend);
            binding.deleteFromFriends.setVisibility(View.GONE);
            binding.userStatusInfo.setVisibility(View.GONE);
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
                addToFriends();
                dialog.dismiss();
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
                Util.addFriendOutInviteToList(userId);
                binding.addToFriendsButton.setVisibility(View.GONE);
                binding.deleteFromFriends.setVisibility(View.GONE);
                binding.userStatusInfo.setVisibility(isBlockedByMe.get() ? View.GONE : View.VISIBLE);
            } catch (SmackException.NotConnectedException e) {
                handleOnError(ProfileActivity.this, e, getString(R.string.reconnect_message));
            }
        } else {
            try {
                chatRoster.createEntry(userId, null);
                Util.addFriendOutInviteToList(userId);
                binding.addToFriendsButton.setVisibility(View.GONE);
                binding.deleteFromFriends.setVisibility(View.GONE);
                binding.userStatusInfo.setVisibility(isBlockedByMe.get() ? View.GONE : View.VISIBLE);
            } catch (SmackException.NotConnectedException e) {
                handleOnError(ProfileActivity.this, e, getString(R.string.reconnect_message));
            } catch (SmackException.NotLoggedInException e) {
                handleOnError(ProfileActivity.this, e, getString(R.string.you_are_not_logged_in));
            } catch (XMPPException e) {
                handleOnError(ProfileActivity.this, e, getString(R.string.something_wrong_try_again_later));
            } catch (SmackException.NoResponseException e) {
                handleOnError(ProfileActivity.this, e, getString(R.string.something_wrong_try_again_later));
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
        sendButton.setText(getString(R.string.remove));
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
            if (chatRoster.getEntry(userId) != null && chatRoster.contains(userId)) {
                chatRoster.removeEntry(chatRoster.getEntry(userId));
                Util.removeFriendOutInviteFromList(userId);
            }
            setFriendsUI();
            EventBus.getDefault().post(new FriendRemovedEvent(qbUser));
        } catch (SmackException.NotConnectedException e) {
            handleOnError(ProfileActivity.this, e, getString(R.string.reconnect_message));
        } catch (SmackException.NotLoggedInException e) {
            handleOnError(ProfileActivity.this, e, getString(R.string.you_are_not_logged_in));
        } catch (XMPPException e) {
            handleOnError(ProfileActivity.this, e, getString(R.string.something_wrong_try_again_later));
        } catch (SmackException.NoResponseException e) {
            handleOnError(ProfileActivity.this, e, getString(R.string.something_wrong_try_again_later));
        }
    }

    public static void handleOnError(Context context, Exception e, String message) {
        e.printStackTrace();
        Crashlytics.logException(e);
        Util.showAlert(context, message);
    }
}
