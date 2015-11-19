package saberapplications.pawpads;

/**
 * Created by blaze on 9/5/2015.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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


//gets layout info and passes text data in.
class CustomAdapter extends ArrayAdapter<String> {
    final String[] pics;
    final String[] user;
    final String[] geoloc;
    final String[] descrip;

    public CustomAdapter(Context context, String[] user, String[] pics, String[] descrip, String[] geoloc) {
        super(context, R.layout.custom_row, user);
        this.pics = pics;
        this.geoloc = geoloc;
        this.descrip = descrip;
        this.user = user;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater blazeinfaltor = LayoutInflater.from(getContext());
        View customView = blazeinfaltor.inflate(R.layout.custom_row, parent, false);

        //set username info
        String username = getItem(position);
        TextView blazetext = (TextView) customView.findViewById(R.id.blazeText);
        blazetext.setText(username);

//        //profile info
//        TextView profile = (TextView) customView.findViewById(R.id.info);
//        profile.setText(descrip[position]);

        //gps coordinates
        TextView gps = (TextView) customView.findViewById(R.id.geoloc);
        gps.setText(geoloc[position]);

        //set image sequence;
        ImageLoader imageloader = ImageLoader.getInstance();
        imageloader.init(ImageLoaderConfiguration.createDefault(getContext()));

        final ImageView blazeImage = (ImageView) customView.findViewById(R.id.blazeimageView);
        imageloader.displayImage(String.valueOf(Uri.parse(pics[position])), blazeImage);
        imageloader.loadImage(String.valueOf(Uri.parse(pics[position])), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    blazeImage.setImageBitmap(loadedImage);
                }
        });
        return customView;
    }
}