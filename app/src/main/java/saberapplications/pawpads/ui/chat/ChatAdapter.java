package saberapplications.pawpads.ui.chat;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Date;

import saberapplications.pawpads.R;


public class ChatAdapter extends ArrayAdapter<QBChatMessage> {

    ArrayList<QBChatMessage> chat_data;
    QBUser currentUser;
    Context context;
    int resource;


    public ChatAdapter(Context context, int resource, ArrayList<QBChatMessage> chat_data,QBUser currentUser) {
        super(context, resource, chat_data);

        this.chat_data = chat_data;
        this.context = context;
        this.resource = resource;
        this.currentUser=currentUser;
    }


    private class ViewHolder {
        TextView textView_left_chat;
        TextView textView_right_chat;
        TextView CVtime_left;
        TextView CVtime_right;
        View relative_layout;
        public TextView attachment;
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
            holder.attachment=(TextView) convertView.findViewById(R.id.attachment);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatMessage message = chat_data.get(position);
        Date sentDate=new Date(message.getDateSent()*1000);
        String type = currentUser.getId().equals(message.getRecipientId()) ?"received": "sent";
        if (type.equals("sent")) {
            holder.textView_left_chat.setText(message.getBody());

            if (DateUtils.isToday(sentDate.getTime())) {
                String time = DateFormat.format("HH:mm", sentDate) + " today";
                holder.CVtime_left.setText(time);
            }else  {
                holder.CVtime_left.setText(DateFormat.format("HH:mm dd MMM yyyy", sentDate));
            }
//                holder.CVtime_left.setText(String.valueOf(chat_data.get(position).getType().equals("date_sent")));

            holder.CVtime_right.setVisibility(View.GONE);
            holder.textView_right_chat.setVisibility(View.GONE);
            holder.textView_left_chat.setVisibility(View.VISIBLE);
            holder.relative_layout.setBackgroundColor(Color.parseColor("#97159cc6"));

        } else {
            if (DateUtils.isToday(sentDate.getTime())) {
                String time = DateFormat.format("HH:mm", sentDate) + " today";
                holder.CVtime_right.setText(time);
            }else {
                holder.CVtime_right.setText(DateFormat.format("HH:mm dd MMM yyyy", sentDate));
            }
            holder.textView_right_chat.setText(message.getBody());
//            holder.CVtime_right.setText(String.valueOf(chat_data.get(position).getType().equals("date_sent")));
            holder.CVtime_left.setVisibility(View.GONE);

            holder.textView_left_chat.setVisibility(View.GONE);
            holder.textView_right_chat.setVisibility(View.VISIBLE);
            holder.relative_layout.setBackgroundColor(000000);
        }

        if (message.getAttachments().size()>0){
            final QBAttachment attachment=message.getAttachments().iterator().next();
            holder.attachment.setText(attachment.getName());
            holder.attachment.setVisibility(View.VISIBLE);
            holder.attachment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }else {
            holder.attachment.setVisibility(View.GONE);
        }


        return convertView;
    }
    public ArrayList<QBChatMessage> getChatItems() {
        return chat_data;
    }


}