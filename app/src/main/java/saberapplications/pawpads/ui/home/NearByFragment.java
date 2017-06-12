package saberapplications.pawpads.ui.home;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;
import com.quickblox.location.request.SortField;
import com.quickblox.location.request.SortOrder;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.BindableBoolean;
import saberapplications.pawpads.databinding.BindableString;
import saberapplications.pawpads.databinding.FragmentNearByBinding;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.profile.ProfileActivity;
import saberapplications.pawpads.views.BaseListAdapter.Callback;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearByFragment extends Fragment implements Callback<NearByAdapter.NearByItem> {
    FragmentNearByBinding binding;
    NearByAdapter adapter;
    BindableBoolean isBusy = new BindableBoolean(false);
    BindableString progressMessage=new BindableString();
    private Location lastListUpdatedLocation;
    private ArrayMap<Integer, Long> lastMessages;
    int currentPage = 1;
    int itemsPerPage=12;
    BroadcastReceiver locationChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(UserLocationService.LOCATION);
            onLocationChanged(location);
        }
    };

    public NearByFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                locationChanged, new IntentFilter(UserLocationService.LOCATION_CHANGED)
        );
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(locationChanged);
    }

    private void onLocationChanged(Location location) {
        if (lastListUpdatedLocation == null) {
            loadData();
        }
        if (location == null) return;
        if (adapter == null) return;
        if (lastListUpdatedLocation.distanceTo(location) > 20 && lastListUpdatedLocation.distanceTo(location) < 100) {
            adapter.setLocation(location);
        } else if (lastListUpdatedLocation.distanceTo(location) > 100) {
            adapter.clear();
            currentPage=1;
            loadData();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_near_by, container, false);
        binding = DataBindingUtil.bind(view);
        adapter = new NearByAdapter();
        binding.listView.setAdapter(adapter);
        binding.swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                adapter.setShowInitialLoad(true);
                currentPage=1;
                loadData();
                binding.swipelayout.setRefreshing(false);
            }
        });
        adapter.setCallback(this);
        progressMessage.set(getString(R.string.obtaining_location));
        if (UserLocationService.getLastLocation()!=null){
            loadData();
        } else {
            adapter.disableLoadMore();
            isBusy.set(false);
        }
        return view;
    }

    public void loadData() {

        if (UserLocationService.getLastLocation() == null) return;
        if (isBusy.get()) return;
        progressMessage.set(getString(R.string.loading));
        lastListUpdatedLocation = UserLocationService.getLastLocation();
        adapter.setLocation(lastListUpdatedLocation);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final int currentUserId = prefs.getInt(C.QB_USERID, 0);
        final ArrayList<QBLocation> nearLocations = new ArrayList<>();

        isBusy.set(true);

        AsyncTask<Void, Void, List<NearByAdapter.NearByItem>> task = new AsyncTask<Void, Void, List<NearByAdapter.NearByItem>>() {
            @Override
            protected List<NearByAdapter.NearByItem> doInBackground(Void... params) {
                ArrayList<NearByAdapter.NearByItem> result = new ArrayList<>();

                try {
                    Bundle out;
                    // cache last messages for last half of year
                    if (lastMessages == null) {
                        lastMessages = new ArrayMap<>();
                        out = new Bundle();
                        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                        Calendar calendar = GregorianCalendar.getInstance();
                        calendar.add(GregorianCalendar.MONTH, -6);
                        requestBuilder.addRule("last_message_date_sent", "gt", calendar.getTimeInMillis() / 1000);
                        ArrayList<QBDialog> dialogList = QBChatService.getChatDialogs(null, requestBuilder, out);
                        if (dialogList != null && dialogList.size() > 0) {
                            for (QBDialog dialog : dialogList) {
                                try {
                                    if (dialog.getOccupants()==null || dialog.getOccupants().size()==0) continue;
                                    int userId = dialog.getOccupants().get(0) == currentUserId ? dialog.getOccupants().get(1) : dialog.getOccupants().get(0);
                                    if (dialog.getLastMessageDateSent() > 0) {
                                        lastMessages.put(userId, dialog.getLastMessageDateSent());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    out = new Bundle();
                    QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
                    getLocationsBuilder.setLastOnly();
                    // radius in kilometers
                    getLocationsBuilder.setRadius(lastListUpdatedLocation.getLatitude(), lastListUpdatedLocation.getLongitude(), Util.getRange() );
                    getLocationsBuilder.setPerPage(itemsPerPage);
                    getLocationsBuilder.setPage(currentPage);
                    getLocationsBuilder.setSort(SortField.DISTANCE, SortOrder.ASCENDING);
                    ArrayList<QBLocation> locations = QBLocations.getLocations(getLocationsBuilder, out);
                    if (locations.size() > 0) {
                        currentPage++;
                    }
                    for (QBLocation qbLocation : locations) {

                        if (qbLocation.getUser().getId() != currentUserId) {
                            nearLocations.add(qbLocation);
                        }
                    }


                    for (QBLocation qbLocation : nearLocations) {
                        NearByAdapter.NearByItem item = new NearByAdapter.NearByItem();
                        item.setLocation(qbLocation);
                        if (lastMessages.containsKey(qbLocation.getUserId())) {
                            item.setLastMessageDate(lastMessages.get(qbLocation.getUserId()));
                        }
                        Location userLocation = new Location("");
                        userLocation.setLatitude(item.getLocation().getLatitude());
                        userLocation.setLongitude(item.getLocation().getLongitude());

                        if (lastListUpdatedLocation.distanceTo(userLocation)<=Util.getRange()*1000){
                            result.add(item);
                        }

                    }
                    return result;

                } catch (
                        QBResponseException e
                        )

                {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<NearByAdapter.NearByItem> nearByItems) {
                if (nearByItems != null && nearByItems.size()>0) {
                    adapter.addItems(nearByItems);
                }
                if (nearByItems==null || nearByItems.size()==0 || nearByItems.size()<(itemsPerPage-1)){
                    adapter.disableLoadMore();
                }

                binding.swipelayout.setRefreshing(false);

                isBusy.set(false);
            }
        };
        task.execute();

    }



    @Override
    public void onLoadMore() {
        loadData();
    }

    @Override
    public void onItemClick(NearByAdapter.NearByItem item) {

        QBRequestGetBuilder builder = new QBRequestGetBuilder();
        final QBLocation qbLocation=item.getLocation();
        builder.eq("occupants_ids", qbLocation.getUser().getId());

        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(C.QB_USERID, qbLocation.getUser().getId());
        intent.putExtra(C.QB_USER, qbLocation.getUser());
        startActivity(intent);


/*
        QBPrivateChatManager chatManager = QBChatService.getInstance().getPrivateChatManager();
        if (chatManager == null) return;
        chatManager.createDialog(qbLocation.getUser().getId(), new QBEntityCallback<QBDialog>() {

            @Override
            public void onSuccess(QBDialog result, Bundle params) {
                openProfile( qbLocation.getUser());
            }

            @Override
            public void onError(QBResponseException e) {
                Util.onError(e, getContext());
            }

        });
*/
    }


    private void openProfile(QBUser user) {

        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(C.QB_USERID, user.getId());
        startActivity(intent);
    }

}
