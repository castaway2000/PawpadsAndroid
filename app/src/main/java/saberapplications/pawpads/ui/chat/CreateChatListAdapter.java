package saberapplications.pawpads.ui.chat;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import saberapplications.pawpads.R;
import saberapplications.pawpads.UserStatusHelper;
import saberapplications.pawpads.databinding.RowCreateChatItemBinding;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by developer on 26.05.17.
 */

public class CreateChatListAdapter extends BaseListAdapter<QBUser> {

    private int currentUserId;
    private ArrayMap<Integer,QBUser> userCache = new ArrayMap<>();
    private static OnUserSelectedListener mSelectedListener;
    private static Set<Integer> selectedUsers = new HashSet<>();

    public interface OnUserSelectedListener {
        public void userSelected(QBUser user);
    }

    public void setUserSelectedListener(OnUserSelectedListener selectedListener) {
        mSelectedListener = selectedListener;
    }

    public static class CreateChatListHolder extends DataHolder<QBUser>{

        private final int size;
        RowCreateChatItemBinding binding;
        CreateChatListAdapter adapter;

        public CreateChatListHolder(View v, BaseListAdapter<QBUser> adapter) {
            super(v, adapter);
            binding= DataBindingUtil.bind(v);
            this.adapter= (CreateChatListAdapter) adapter;
            float d= view.getResources().getDisplayMetrics().density;
            size=Math.round(60 * d);
        }

        @Override
        public void showData(final DataItem<QBUser> data, int position) {
            final QBUser user = data.model.get();
            String userName = data.model.get().getFullName() == null ? data.model.get().getLogin() : data.model.get().getFullName();
            binding.setUsername(userName);
            final int userId=user.getId();

            binding.checkbox.setChecked(false);
            if(selectedUsers.contains(userId)) binding.checkbox.setChecked(true);
            binding.avatar.setImageResource(R.drawable.user_placeholder);
            if(!adapter.userCache.containsKey(userId)) {
                QBUsers.getUser(userId, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        if (qbUser.getFileId() != null) {
                            AvatarLoaderHelper.loadImage(qbUser.getFileId(), binding.avatar, size, size);
                            adapter.userCache.put(qbUser.getId(), qbUser);
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }else {
                AvatarLoaderHelper.loadImage(adapter.userCache.get(userId).getFileId(), binding.avatar, size, size);
            }

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

            binding.checkboxLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(binding.checkbox.isChecked()) {
                        binding.checkbox.setChecked(false);
                        if(selectedUsers.contains(userId)) selectedUsers.remove(userId);
                    } else {
                        binding.checkbox.setChecked(true);
                        if(!selectedUsers.contains(userId)) selectedUsers.add(userId);
                    }
                    mSelectedListener.userSelected(user);
                }
            });
        }
    }

    @Override
    public DataHolder<QBUser> getItemHolder(ViewGroup parent) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_create_chat_item,parent,false);
        return new CreateChatListAdapter.CreateChatListHolder(v,this);
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    protected int getEmptyStateResId() {
        return R.layout.empty_state_create_chat;
    }
}