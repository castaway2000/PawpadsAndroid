package saberapplications.pawpads.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.FragmentChannelsBinding;
import saberapplications.pawpads.ui.chat.ChatGroupActivity;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by developer on 08.06.17.
 */

public class ChannelsFragment extends Fragment implements BaseListAdapter.Callback<QBDialog> {

    private static final int MAX_AMOUNT_OF_CREATED_CHANNELS = 3;
    FragmentChannelsBinding binding;
    ChatsAdapter adapter;
    int currentPage = 0;
    private int currentUserId;
    private boolean isLoading;

    public ChannelsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_channels, container, false);
        binding = DataBindingUtil.bind(view);
        binding.setFragment(this);
        adapter = new ChatsAdapter();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        currentUserId = prefs.getInt(C.QB_USERID, 0);
        adapter.setCurrentUserId(currentUserId);
        binding.listView.setAdapter(adapter);
        adapter.setCallback(this);
        binding.swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isLoading) {
                    binding.swipelayout.setRefreshing(false);
                    return;
                }
                adapter.setShowInitialLoad(true);
                adapter.clear();
                currentPage = 0;
                loadData();
                binding.swipelayout.setRefreshing(false);
            }
        });
        checkAmountOfCreatedChannels();
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount()<=1){
                    adapter.clear();
                    currentPage = 0;
                    loadData();
                }
            }
        }, 700);
        adapter.notifyDataSetChanged();
    }

    public void loadData() {
        isLoading = true;
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(10);
        requestBuilder.setSkip(currentPage * 10);
        requestBuilder.sortDesc("last_message_date_sent");
        QBChatService.getChatDialogs(QBDialogType.PUBLIC_GROUP, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                if (adapter==null) return;
                if (dialogs.size() > 0) {
                    adapter.addItems(dialogs);
                    currentPage++;
                }

                if (dialogs.size() == 0 || dialogs.size() < 10) {
                    adapter.disableLoadMore();
                }
                binding.swipelayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public void onError(QBResponseException e) {
                adapter.disableLoadMore();
                binding.swipelayout.setRefreshing(false);
                isLoading = false;
                if (getContext()==null) return;
                Util.onError(e, getContext());
            }
        });
    }

    public void reloadData() {
        if(isLoading) return;
        adapter.setShowInitialLoad(true);
        adapter.clear();
        currentPage = 0;
        loadData();
        binding.swipelayout.setRefreshing(false);
    }

    private void checkAmountOfCreatedChannels() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(100);
        requestBuilder.sortDesc("last_message_date_sent");
        QBChatService.getChatDialogs(QBDialogType.PUBLIC_GROUP, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                if(dialogs.size() == 0) return;
                int amountOfCreatedChannels = 0;
                for (QBDialog dialog : dialogs) {
                    if(dialog.getUserId() == currentUserId) {
                        amountOfCreatedChannels++;
                    }
                }
                Util.setCreatedChannelsCount(amountOfCreatedChannels);
            }

            @Override
            public void onError(QBResponseException errors) {
                errors.printStackTrace();
            }
        });
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(final QBDialog dialog) {
        ArrayList<Integer> occupansts = (ArrayList<Integer>) dialog.getOccupants();
        Intent intent = new Intent(getContext(), ChatGroupActivity.class);
        intent.putExtra(ChatGroupActivity.DIALOG, dialog);
        intent.putExtra(ChatGroupActivity.RECIPIENT_IDS_LIST, occupansts);
        startActivity(intent);
    }

    public void createNewPublicGroup() {
        if(Util.getCreatedChannelsCount() < MAX_AMOUNT_OF_CREATED_CHANNELS) {
            QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();
            if (groupChatManager == null) return;
            Intent i = new Intent(getActivity(), ChatGroupActivity.class);
            i.putExtra(ChatGroupActivity.DIALOG_GROUP_TYPE, QBDialogType.PUBLIC_GROUP);
            i.putExtra(ChatGroupActivity.IS_FIRST_OPENED, true);
            startActivity(i);
            Util.setCreatedChannelsCount(Util.getCreatedChannelsCount()+1);
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.sorry_cannot_create_more_three_channels), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(QBChatMessage msg) {
        ArrayList<BaseListAdapter.DataItem<QBDialog>> items = adapter.getItems();
        for(int i=0;i<items.size();i++){
            BaseListAdapter.DataItem<QBDialog> item=items.get(i);
            QBDialog dlg=item.model.get();
            if (dlg.getDialogId().equals(msg.getDialogId())){
                dlg.setLastMessageDateSent(msg.getDateSent());
                dlg.setLastMessage(msg.getBody());
                items.remove(i);
                items.add(0,item);
                adapter.notifyItemMoved(i,0);
                binding.listView.scrollToPosition(0);
            }
        }
    }
}