package saberapplications.pawpads.ui.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import saberapplications.pawpads.R;
import saberapplications.pawpads.UserStatusHelper;
import saberapplications.pawpads.databinding.RowChatsBinding;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by Stanislav Volnjanskij on 12.10.16.
 */

public class ChatsAdapter extends BaseListAdapter<QBDialog> {

    int currentUserId;
    ArrayMap<Integer,QBUser> userCache=new ArrayMap<>();
    public static class ChatDialogHolder extends DataHolder<QBDialog>{

        private final int size;
        RowChatsBinding binding;
        ChatsAdapter adapter;

        public ChatDialogHolder(View v, BaseListAdapter<QBDialog> adapter) {
            super(v, adapter);
            binding= DataBindingUtil.bind(v);
            this.adapter= (ChatsAdapter) adapter;
            float d= view.getResources().getDisplayMetrics().density;
            size=Math.round(60 * d);
        }

        @Override
        public void showData(DataItem<QBDialog> data,int position) {
            QBDialog dialog=data.model.get();
            String lastMessage = data.model.get().getLastMessage();
            if(lastMessage != null && lastMessage.length() > 25) {
                lastMessage = lastMessage.substring(0, 22) + "...";
            }
            binding.setLastMessage(lastMessage);
            binding.setUsername(data.model.get().getName());
            int userId=0;
            for(int uid:dialog.getOccupants()){
                if (uid!=adapter.currentUserId){
                    userId=uid;
                }
            }

            binding.avatar.setImageResource(R.drawable.user_placeholder);
            binding.avatarLastMessage.setImageResource(R.drawable.user_placeholder);
            binding.avatarLastMessage.setVisibility(View.GONE);

            if(dialog.getType() == QBDialogType.GROUP || dialog.getType() == QBDialogType.PUBLIC_GROUP) {
                try {
                    int lastMsgUserId = data.model.get().getLastMessageUserId();
                    float d= view.getResources().getDisplayMetrics().density;
                    binding.avatarLastMessage.setVisibility(View.VISIBLE);
                    loadUserAvatar(lastMsgUserId, binding.avatarLastMessage, Math.round(25 * d));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    AvatarLoaderHelper.loadImage(Integer.parseInt(dialog.getPhoto()), binding.avatar, size, size, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                binding.setBindStatusVisibility(false);

            } else {
                loadUserAvatar(userId, binding.avatar, size);

                QBUsers.getUser(userId, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        if (qbUser != null) {
                            binding.setBindStatusVisibility(true);
                            binding.setOnlineStatus(UserStatusHelper.getUserStatus(qbUser));
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
        }

        private void loadUserAvatar(int userId, final ImageView avatar, final int avatarSize) {
            if (!adapter.userCache.containsKey(userId)) {
                QBUsers.getUser(userId, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        if (qbUser.getFileId() != null) {
                            AvatarLoaderHelper.loadImage(qbUser.getFileId(), avatar, avatarSize, avatarSize);
                            adapter.userCache.put(qbUser.getId(), qbUser);
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            } else {
                AvatarLoaderHelper.loadImage(adapter.userCache.get(userId).getFileId(), avatar, avatarSize, avatarSize);
            }
        }
    }

    @Override
    public DataHolder<QBDialog> getItemHolder(ViewGroup parent) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chats,parent,false);
        return new ChatsAdapter.ChatDialogHolder(v,this);
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    protected int getEmptyStateResId() {
        return R.layout.empty_state_chats;
    }
}
