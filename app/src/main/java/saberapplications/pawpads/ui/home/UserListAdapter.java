package saberapplications.pawpads.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.quickblox.users.model.QBUser;

import java.util.List;

import saberapplications.pawpads.R;

/**
 * Created by Stas on 28.12.15.
 */
public class UserListAdapter extends ArrayAdapter<QBUser> {

    public UserListAdapter(Context context, int resource, List<QBUser> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View customView;
        if (convertView==null){
            LayoutInflater blazeinfaltor = LayoutInflater.from(getContext());
            customView = blazeinfaltor.inflate(R.layout.custom_row, parent, false);
        }else {
            customView=convertView;
        }

        //set username info
        QBUser user=getItem(position);
        TextView blazetext = (TextView) customView.findViewById(R.id.blazeText);
        blazetext.setText(user.getLogin());

        //gps coordinates
        TextView gps = (TextView) customView.findViewById(R.id.geoloc);
        //gps.setText(geoloc[position]);

        //set image sequence;
        ImageLoader imageloader = ImageLoader.getInstance();
        imageloader.init(ImageLoaderConfiguration.createDefault(getContext()));
/*
        final ImageView blazeImage = (ImageView) customView.findViewById(R.id.blazeimageView);
        imageloader.displayImage(String.valueOf(Uri.parse(pics[position])), blazeImage);
        imageloader.loadImage(String.valueOf(Uri.parse(pics[position])), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                blazeImage.setImageBitmap(loadedImage);
            }
        });
        */
        return customView;
    }
}
