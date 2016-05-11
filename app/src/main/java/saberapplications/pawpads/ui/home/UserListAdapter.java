package saberapplications.pawpads.ui.home;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.location.model.QBLocation;

import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.util.AvatarLoaderHelper;

/**
 * Created by Stas on 28.12.15.
 */
public class UserListAdapter extends ArrayAdapter<QBLocation> {


    public UserListAdapter(Context context, int resource, List<QBLocation> objects) {
        super(context, resource, objects);
        if(objects.size() <= 1){
            Toast.makeText(context, "make sure your range is wide enough to see others near you. \n\n check your settings.", Toast.LENGTH_LONG).show();
            return;
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    private  Location location;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View customView;
        if (convertView == null) {
            LayoutInflater blazeinfaltor = LayoutInflater.from(getContext());
            customView = blazeinfaltor.inflate(R.layout.custom_row, parent, false);
        } else {
            customView = convertView;
        }

        //set username info
        QBLocation qbLocation = getItem(position);
        TextView blazetext = (TextView) customView.findViewById(R.id.blazeText);
        blazetext.setText(qbLocation.getUser().getLogin());


        Location userLocation = new Location("");
        //,
        userLocation.setLatitude(qbLocation.getLatitude());
        userLocation.setLongitude(qbLocation.getLongitude());
        if(Util.UNIT_OF_MEASURE.equals("MI")) {
            float distanceTo = location.distanceTo(userLocation) * 3.2808f;
            //gps coordinates
            TextView gps = (TextView) customView.findViewById(R.id.geoloc);
            if (distanceTo > 5280) {
                gps.setText(String.format("%.2f miles", distanceTo / 5280));
            } else {
                distanceTo = Math.round(distanceTo);
                gps.setText(String.format("%.0f feet", distanceTo));
            }
        }
        else{
            float distanceTo = location.distanceTo(userLocation);
            //gps coordinates
            TextView gps = (TextView) customView.findViewById(R.id.geoloc);
            if (distanceTo > 1000) {
                gps.setText(String.format("%.2f km", distanceTo / 1000));
            } else {
                distanceTo = Math.round(distanceTo);
                gps.setText(String.format("%.0f meters", distanceTo));
            }
        }

        final ImageView blazeImage = (ImageView) customView.findViewById(R.id.blazeimageView);
        if (qbLocation.getUser().getFileId() != null) {
            int userProfilePictureID = qbLocation.getUser().getFileId(); // user - an instance of QBUser class
            float d=getContext().getResources().getDisplayMetrics().density;
            int size=Math.round(80 * d);
            AvatarLoaderHelper.loadImage(userProfilePictureID,blazeImage,size,size);
        }

        return customView;
    }

    public Location getLocation() {
        return location;
    }
}
