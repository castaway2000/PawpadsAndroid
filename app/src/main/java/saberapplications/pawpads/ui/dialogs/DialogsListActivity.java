package saberapplications.pawpads.ui.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;

import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.ui.chat.ChatActivity;

/**
 * Class {@link DialogsListActivity
 *
 * @author RomanMosiienko
 * @version 1.0
 * @since 15.01.16
 */
public class DialogsListActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{

    private ArrayList<QBDialog> qbDialogArrayList = new ArrayList<>();
    private ListView listView;
    private DialogsAdapter dialogsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs_list);
        initViews();
    }

    private void initViews() {
        listView = (ListView) findViewById(R.id.dialog_listview);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        dialogsAdapter = new DialogsAdapter(qbDialogArrayList, this);
        listView.setAdapter(dialogsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DialogsListActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_DIALOG, qbDialogArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDialogsFromServer();
    }

    private void getDialogsFromServer() {
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setPagesLimit(100);
        QBChatService.getChatDialogs(null, requestBuilder, new QBEntityCallbackImpl<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle args) {
                mSwipeRefreshLayout.setRefreshing(false);
                qbDialogArrayList.clear();
                qbDialogArrayList.addAll(dialogs);
                dialogsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(List<String> errors) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onRefresh() {
        getDialogsFromServer();
    }
}
