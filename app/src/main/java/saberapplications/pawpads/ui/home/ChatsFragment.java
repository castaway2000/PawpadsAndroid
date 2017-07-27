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

import com.quickblox.chat.QBChatService;
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
import java.util.List;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.FragmentChatsBinding;
import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.ui.chat.ChatGroupActivity;
import saberapplications.pawpads.ui.chat.CreateChatActivity;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements BaseListAdapter.Callback<QBDialog> {

    FragmentChatsBinding binding;
    ChatsAdapter adapter;
    int currentPage = 0;
    private int currentUserId;
    private boolean isLoading;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
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
                if (isLoading) {
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
                if (adapter.getItemCount() <= 1) {
                    currentPage = 0;
                    adapter.clear();
                    loadData();
                }
            }
        }, 500);
    }

    public void loadData() {
        isLoading = true;
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(10);
        requestBuilder.setSkip(currentPage * 10);
        requestBuilder.in("type","2","3");
        requestBuilder.sortDesc("last_message_date_sent");

        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                if (adapter == null) return;
                if (dialogs.size() == 0 || dialogs.size() < 10) {
                    adapter.disableLoadMore();
                }
                if (dialogs.size() > 0) {
                    ArrayList<QBDialog> privateDialogs = new ArrayList<>();
                    for (QBDialog dialog : dialogs) {
                        if (!dialog.getType().equals(QBDialogType.PUBLIC_GROUP))
                            privateDialogs.add(dialog);
                    }
                    adapter.addItems(privateDialogs);
                    currentPage++;
                }


                binding.swipelayout.setRefreshing(false);
                isLoading = false;
            }

            @Override
            public void onError(QBResponseException e) {
                adapter.disableLoadMore();
                binding.swipelayout.setRefreshing(false);
                isLoading = false;
                if (getContext() == null) return;
                Util.onError(e, getContext());

            }
        });
    }

    public void reloadData() {
        if (isLoading) return;
        adapter.setShowInitialLoad(true);
        adapter.clear();
        currentPage = 0;
        loadData();
        binding.swipelayout.setRefreshing(false);
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(final QBDialog dialog) {
        if (dialog.getType() == QBDialogType.GROUP) {
            ArrayList<Integer> occupansts = (ArrayList<Integer>) dialog.getOccupants();
            Intent intent = new Intent(getContext(), ChatGroupActivity.class);
            intent.putExtra(ChatGroupActivity.DIALOG, dialog);
            intent.putExtra(ChatGroupActivity.RECIPIENT_IDS_LIST, occupansts);
            startActivity(intent);
        } else {
            List<Integer> occupansts = dialog.getOccupants();
            Integer recipientId = occupansts.get(0) == currentUserId ? occupansts.get(1) : occupansts.get(0);

            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.DIALOG, dialog);
            intent.putExtra(ChatActivity.RECIPIENT_ID, recipientId);
            startActivity(intent);
        }
    }

    public void createNewChatOrGroup() {
        Intent intent = new Intent(getContext(), CreateChatActivity.class);
        startActivity(intent);
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
                dlg.setLastMessageUserId(msg.getSenderId());
                item.model.set(dlg);
                items.remove(i);
                items.add(0,item);
                adapter.notifyItemChanged(i);
                adapter.notifyItemChanged(0);
                binding.listView.scrollToPosition(0);
            }
        }
    }
}
