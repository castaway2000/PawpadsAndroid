package saberapplications.pawpads.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import saberapplications.pawpads.ChatObject;
import saberapplications.pawpads.R;


public class ChatAdapter extends ArrayAdapter<ChatObject> {

    List<ChatObject> chat_data;
    Context context;
    int resource;


    public ChatAdapter(Context context, int resource, List<ChatObject> chat_data) {
        super(context, resource, chat_data);

        this.chat_data = chat_data;
        this.context = context;
        this.resource = resource;
    }


    private class ViewHolder {
        TextView textView_left_chat;
        TextView textView_right_chat;
        TextView CVtime_left;
        TextView CVtime_right;
        View relative_layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.chat_view, null);
            holder = new ViewHolder();
            holder.textView_left_chat = (TextView) convertView.findViewById(R.id.textView_left_chat);
            holder.textView_right_chat = (TextView) convertView.findViewById(R.id.textView_right_chat);
            holder.CVtime_left = (TextView) convertView.findViewById(R.id.CVtime_left);
            holder.CVtime_right = (TextView) convertView.findViewById(R.id.CVtime_right);
            holder.relative_layout = convertView.findViewById(R.id.rChatLayout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ChatObject data = chat_data.get(position);
        if (data.getType().equals("sent")) {
            holder.textView_left_chat.setText(data.getMessage());
//                holder.CVtime_left.setText(String.valueOf(chat_data.get(position).getType().equals("date_sent")));

            holder.CVtime_right.setVisibility(View.GONE);
            holder.textView_right_chat.setVisibility(View.GONE);
            holder.textView_left_chat.setVisibility(View.VISIBLE);
            holder.relative_layout.setBackgroundColor(Color.parseColor("#97159cc6"));

        } else {
            holder.textView_right_chat.setText(data.getMessage());
//            holder.CVtime_right.setText(String.valueOf(chat_data.get(position).getType().equals("date_sent")));
            holder.CVtime_left.setVisibility(View.GONE);

            holder.textView_left_chat.setVisibility(View.GONE);
            holder.textView_right_chat.setVisibility(View.VISIBLE);
            holder.relative_layout.setBackgroundColor(000000);
        }

        return convertView;
    }
}