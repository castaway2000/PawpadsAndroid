package saberapplications.pawpads.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.databinding.ActivitySearchBinding;
import saberapplications.pawpads.ui.BaseActivity;
import saberapplications.pawpads.ui.profile.ProfileActivity;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by developer on 05.06.17.
 */

public class SearchActivity extends BaseActivity implements BaseListAdapter.Callback<QBUser> {
    ActivitySearchBinding binding;
    SearchAdapter adapter;
    private int currentUserId;
    private Set<QBUser> filteredUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setActivity(this);
        adapter = new SearchAdapter();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentUserId = prefs.getInt(C.QB_USERID, 0);
        adapter.setCurrentUserId(currentUserId);
        binding.searchListView.setAdapter(adapter);
        adapter.setCallback(this);
        binding.swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                adapter.disableLoadMore();
                binding.swipelayout.setRefreshing(false);
                binding.emptySearchStateLayout.setVisibility(View.VISIBLE);
            }
        });
        adapter.setShowInitialLoad(false);
        adapter.disableLoadMore();
        initSearchPanel();
    }

    private void initSearchPanel() {
        binding.toolbarSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = v.getText().toString();
                    if(query.length() > 0) {
                        hideSoftKeyboard();
                        loadDataBySearchQuery(query);
                    }
                    return true;
                }
                return false;
            }
        });

        binding.toolbarSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0) {
                    binding.clearSearchText.setVisibility(View.INVISIBLE);
                    adapter.clear();
                    adapter.disableLoadMore();
                    binding.emptySearchStateLayout.setVisibility(View.VISIBLE);
                } else {
                    if(binding.clearSearchText.getVisibility() == View.INVISIBLE)
                        binding.clearSearchText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadDataBySearchQuery(final String searchQuery) {
        binding.emptySearchStateLayout.setVisibility(View.GONE);
        adapter.clear();
        adapter.setShowInitialLoad(true);
        filteredUsers = new HashSet<>();

        new AsyncTask<Void, View, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {

                QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
                Bundle bundle = new Bundle();
                try {
                    ArrayList<QBUser> resultByFullNameUsers = QBUsers.getUsersByFullName(searchQuery, pagedRequestBuilder, bundle);
                    if (resultByFullNameUsers.size() > 0) {
                        for (QBUser user : resultByFullNameUsers) {
                            if (user.getFullName() != null && user.getFullName().contains(searchQuery))
                                filteredUsers.add(user);
                        }
                    }
                } catch (QBResponseException e) {
                    e.printStackTrace();
                }

                ArrayList<String> usersLogins = new ArrayList<>();
                usersLogins.add(searchQuery);
                try {
                    ArrayList<QBUser> resultByLoginUsers = QBUsers.getUsersByLogins(usersLogins, pagedRequestBuilder, bundle);
                    if (resultByLoginUsers.size() > 0) {
                        for (QBUser user : resultByLoginUsers) {
                            if (user.getLogin() != null && user.getLogin().contains(searchQuery))
                                filteredUsers.add(user);
                        }
                    }
                } catch (QBResponseException e) {
                    e.printStackTrace();
                }

                ArrayList<String> usersEmails = new ArrayList<>();
                usersEmails.add(searchQuery);
                try {
                    ArrayList<QBUser> resultByEmailUsers = QBUsers.getUsersByEmails(usersEmails, pagedRequestBuilder, bundle);
                    if (resultByEmailUsers.size() > 0) {
                        for (QBUser user : resultByEmailUsers) {
                            if (user.getEmail() != null && user.getEmail().contains(searchQuery))
                                filteredUsers.add(user);
                        }
                    }
                } catch (QBResponseException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                processSearchComplete(filteredUsers);
            }
        }.execute();
    }

    private void processSearchComplete(Set<QBUser> users) {
        if (users.size() > 0) {
            adapter.addItems(new ArrayList<>(users));
        }

        if (users.size() == 0 || users.size() < 10) {
            adapter.disableLoadMore();
        }
        binding.swipelayout.setRefreshing(false);
    }

    public void clearSearchQuery() {
        binding.toolbarSearchText.setText("");
    }

    @Override
    public void onLoadMore() {
        //nothing to do
    }

    @Override
    public void onItemClick(final QBUser user) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user != null) {
                    Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                    intent.putExtra(C.QB_USERID, user.getId());
                    intent.putExtra(C.QB_USER, user);
                    startActivity(intent);
                }
            }
        }, 50);
    }
}