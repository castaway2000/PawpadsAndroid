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
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;

import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.FragmentChatsBinding;
import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment implements BaseListAdapter.Callback<QBDialog> {

    FragmentChatsBinding binding;
    ChatsAdapter adapter;
    int currentPage = 0;
    private int currentUserId;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        binding = DataBindingUtil.bind(view);
        adapter = new ChatsAdapter();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        currentUserId = prefs.getInt(C.QB_USERID, 0);
        adapter.setCurrentUserId(currentUserId);
        binding.listView.setAdapter(adapter);
        adapter.setCallback(this);
        binding.swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                currentPage = 0;
                loadData();
                binding.swipelayout.setRefreshing(false);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                    if(adapter.isShowInitialLoad()) {
                        adapter.clear();
                        currentPage = 0;
                        loadData();
                    }
            }
        }, 5000);
    }

    public void loadData() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(10);
        requestBuilder.setSkip(currentPage * 10);
        requestBuilder.sortDesc("last_message_date_sent");
        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
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

            }

            @Override
            public void onError(QBResponseException e) {
                if (getContext()==null) return;
                Util.onError(e, getContext());

            }


        });
    }

    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(final QBDialog dialog) {


        List<Integer> occupansts = dialog.getOccupants();
        Integer recipientId = occupansts.get(0) == currentUserId ? occupansts.get(1) : occupansts.get(0);

        Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.DIALOG, dialog);
        intent.putExtra(ChatActivity.RECIPIENT_ID, recipientId);
        startActivity(intent);

    }
}
