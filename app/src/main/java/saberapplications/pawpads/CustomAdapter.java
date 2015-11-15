package saberapplications.pawpads;

/**
 * Created by blaze on 9/5/2015.
 */

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        ImageView blazeImage = (ImageView) customView.findViewById(R.id.blazeimageView);
        blazeImage.setImageURI(Uri.parse(pics[position]));

        return customView;
    }
}