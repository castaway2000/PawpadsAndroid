package saberapplications.pawpads.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.users.model.QBUser;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
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
        userLocation.setLatitude(qbLocation.getLatitude());
        userLocation.setLongitude(qbLocation.getLongitude());
        int distanceTo = Math.round(location.distanceTo(userLocation)*3.2808f);
        //gps coordinates
        TextView gps = (TextView) customView.findViewById(R.id.geoloc);
        gps.setText(String.valueOf(distanceTo) + " feet");

        final ImageView blazeImage = (ImageView) customView.findViewById(R.id.blazeimageView);
        if (qbLocation.getUser().getFileId() != null) {
            int userProfilePictureID = qbLocation.getUser().getFileId(); // user - an instance of QBUser class
            AvatarLoaderHelper.loadImage(userProfilePictureID,blazeImage);

        }
        return customView;
    }

    public Location getLocation() {
        return location;
    }
}
