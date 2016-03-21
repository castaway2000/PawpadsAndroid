package saberapplications.pawpads.ui.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import saberapplications.pawpads.R;

/**
 * Class {@link DialogsAdapter
 *
 * @author RomanMosiienko
 * @version 1.0
 * @since 15.01.16
 */
public class DialogsAdapter extends BaseAdapter {

    private ArrayList<QBDialog> dialogs;
    private Context context;

    public DialogsAdapter(ArrayList<QBDialog> dialogs, Context context) {
        this.dialogs = dialogs;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dialogs.size();
    }

    @Override
    public QBDialog getItem(int position) {
        return dialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.row_dialog, parent, false);
        }else {
            view=convertView;
        }
        QBDialog dialog = getItem(position);
        TextView lastMessage = (TextView) view.findViewById(R.id.dialog_last_message);
        TextView lastDate = (TextView) view.findViewById(R.id.dialog_date_last_message);
        String lastDateFormat = new SimpleDateFormat("dd MMM yyyy ", Locale.getDefault()).format(dialog.getUpdatedAt());
        lastDate.setText(lastDateFormat);
        lastMessage.setText(dialog.getLastMessage());
        if (dialog.getUnreadMessageCount() > 0) {
            view.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
        }
        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
