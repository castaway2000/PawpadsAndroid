package saberapplications.pawpads.ui.search;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import saberapplications.pawpads.R;
import saberapplications.pawpads.UserStatusHelper;
import saberapplications.pawpads.databinding.RowSearchUserItemBinding;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by developer on 05.06.17.
 */

public class SearchAdapter extends BaseListAdapter<QBUser> {

    private int currentUserId;
    private ArrayMap<Integer,QBUser> userCache = new ArrayMap<>();

    public static class SearchAdapterHolder extends DataHolder<QBUser>{

        private final int size;
        RowSearchUserItemBinding binding;
        SearchAdapter adapter;

        public SearchAdapterHolder(View v, BaseListAdapter<QBUser> adapter) {
            super(v, adapter);
            binding= DataBindingUtil.bind(v);
            this.adapter= (SearchAdapter) adapter;
            float d= view.getResources().getDisplayMetrics().density;
            size=Math.round(60 * d);
        }

        @Override
        public void showData(final DataItem<QBUser> data, int position) {
            final QBUser user = data.model.get();
            String userName = data.model.get().getFullName() == null ? data.model.get().getLogin() : data.model.get().getFullName();
            binding.setUsername(userName);
            int userId=user.getId();

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
        }
    }

    @Override
    public DataHolder<QBUser> getItemHolder(ViewGroup parent) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_search_user_item,parent,false);
        return new SearchAdapter.SearchAdapterHolder(v,this);
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    @Override
    protected int getEmptyStateResId() {
        return R.layout.empty_state_create_chat;
    }
}