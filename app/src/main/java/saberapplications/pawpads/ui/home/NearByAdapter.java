package saberapplications.pawpads.ui.home;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.location.model.QBLocation;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.Date;

import saberapplications.pawpads.R;
import saberapplications.pawpads.UserStatusHelper;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.RowNearByBinding;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by Stanislav Volnjanskij on 11.10.16.
 */
public class NearByAdapter extends BaseListAdapter<NearByAdapter.NearByItem> {
    public static class NearByItem {
        private QBLocation location;
        private Date lastMessageDate;

        public QBLocation getLocation() {
            return location;
        }

        public void setLocation(QBLocation location) {
            this.location = location;

        }

        public void setLastMessageDate(long lastMessageDate) {
            this.lastMessageDate =new Date(lastMessageDate*1000);
        }
        public String getLastMessageDateFmt(){
            if (lastMessageDate!=null) {
                SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
                return sdf.format(lastMessageDate);
            }else {
                return "";
            }
        }

    }

    private Location location;

    public void setLocation(Location location) {
        this.location = location;
        try {
            notifyDataSetChanged();
        }catch (Exception e){

        }
    }

    public Location getLocation() {
        return location;
    }

    public static class LocationHolder extends DataHolder<NearByAdapter.NearByItem>{

        RowNearByBinding binding;
        Context context;
        NearByAdapter adapter;
        public LocationHolder(View v, BaseListAdapter<NearByAdapter.NearByItem> adapter) {
            super(v, adapter);
            binding= DataBindingUtil.bind(v);
            context=v.getContext();
            this.adapter= (NearByAdapter) adapter;
        }

        @Override
        public void showData(DataItem<NearByAdapter.NearByItem> data,int position) {

            binding.setLocation(data.model.get().getLocation());
            binding.setLastMessage(data.model.get().getLastMessageDateFmt());
            binding.setUsername(Util.getUserName(data.model.get().location.getUser()));
            binding.setDistance(getDistance(data.model.get().getLocation()));
            binding.setOnlineStatus(UserStatusHelper.getUserStatus(data.model.get().getLocation().getUser()));

            QBUser user=data.model.get().getLocation().getUser();
            if (user.getFileId() != null) {
                int userProfilePictureID = user.getFileId();
                float d=context.getResources().getDisplayMetrics().density;
                int size=Math.round(60 * d);
                AvatarLoaderHelper.loadImage(userProfilePictureID,binding.avatar,size,size);
            }else {
                binding.avatar.setImageResource(R.drawable.user_placeholder);
            }
        }

        public String getDistance(QBLocation qbLocation){
            if (qbLocation==null) return "";
            Location userLocation = new Location("");
            userLocation.setLatitude(qbLocation.getLatitude());
            userLocation.setLongitude(qbLocation.getLongitude());
            return Util.formatDistance(userLocation.distanceTo(adapter.getLocation()));

        }
    }


    @Override
    public DataHolder<NearByAdapter.NearByItem> getItemHolder(ViewGroup parent) {

        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.row_near_by,parent,false);
        return new LocationHolder(v,this);
    }

    @Override
    protected int getEmptyStateResId() {
        return R.layout.empty_state_nearby;
    }
}
