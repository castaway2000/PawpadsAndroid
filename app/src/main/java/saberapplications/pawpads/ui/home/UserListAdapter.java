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
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.users.model.QBUser;

import java.io.InputStream;
import java.util.List;

import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;

/**
 * Created by Stas on 28.12.15.
 */
public class UserListAdapter extends ArrayAdapter<QBLocation> {

    public UserListAdapter(Context context, int resource, List<QBLocation> objects) {
        super(context, resource, objects);
    }

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
        String locationGPSProvider = LocationManager.GPS_PROVIDER;
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationGPSProvider);

        if (lastKnownLocation == null) {
            String locationNetworkProvider = LocationManager.NETWORK_PROVIDER;
            lastKnownLocation = locationManager.getLastKnownLocation(locationNetworkProvider);
        }
        Location userLocation = new Location("");
        userLocation.setLatitude(qbLocation.getLatitude());
        userLocation.setLongitude(qbLocation.getLongitude());
        int distanceTo = Math.round(lastKnownLocation.distanceTo(userLocation)/3.2808f);
        //gps coordinates
        TextView gps = (TextView) customView.findViewById(R.id.geoloc);
        gps.setText(String.valueOf(distanceTo) + " feet");

        //set image sequence;
        ImageLoader imageloader = ImageLoader.getInstance();
        imageloader.init(ImageLoaderConfiguration.createDefault(getContext()));
        final ImageView blazeImage = (ImageView) customView.findViewById(R.id.blazeimageView);
        if (qbLocation.getUser().getFileId() != null) {
            int userProfilePictureID = qbLocation.getUser().getFileId(); // user - an instance of QBUser class

            QBContent.downloadFileTask(userProfilePictureID, new QBEntityCallback<InputStream>() {
                @Override
                public void onSuccess(InputStream inputStream, Bundle params) {
                    new AsyncTask<InputStream, Void, Bitmap>() {

                        @Override
                        protected Bitmap doInBackground(InputStream... params) {
                            BitmapFactory.Options o = new BitmapFactory.Options();
                            int width_tmp = o.outWidth, height_tmp = o.outHeight;
                            int scale = 1;

                            while (true) {
                                if (width_tmp / 2 < 80 || height_tmp / 2 < 80)
                                    break;
                                width_tmp /= 2;
                                height_tmp /= 2;
                                scale *= 2;
                            }

                            BitmapFactory.Options o2 = new BitmapFactory.Options();
                            o2.inSampleSize = scale;
                            return BitmapFactory.decodeStream(params[0], null, o2);
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            super.onPostExecute(bitmap);
                            blazeImage.setImageBitmap(bitmap);

                        }
                    }.execute(inputStream);
                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(List<String> list) {
                    Util.onError(list, getContext());
                }


            }, new QBProgressCallback() {
                @Override
                public void onProgressUpdate(int progress) {

                }
            });
        }
        return customView;
    }
}
