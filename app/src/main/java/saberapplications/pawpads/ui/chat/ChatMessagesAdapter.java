package saberapplications.pawpads.ui.chat;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import saberapplications.pawpads.R;
import saberapplications.pawpads.databinding.ChatMessageLeftBinding;
import saberapplications.pawpads.databinding.ChatMessageRightBinding;
import saberapplications.pawpads.views.BaseChatAdapter;

/**
 * Created by Stanislav Volnjanskij on 26.10.16.
 */

public class ChatMessagesAdapter extends BaseChatAdapter<QBChatMessage> {
    private Context mContext;
    private LayoutInflater mInflater;
    private int currentUserId;

    public ChatMessagesAdapter(Context context,int userId) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        this.currentUserId=userId;
    }

    @Override
    public DataHolder<QBChatMessage> getChatItemHolderRight(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.chat_message_right, parent, false);
        return new HolderRight(v, this);
    }

    @Override
    public DataHolder<QBChatMessage> getChatItemHolderLeft(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.chat_message_left, parent, false);
        return new HolderLeft(v, this);
    }

    @Override
    public boolean getMessageSelf(int position) {
        QBChatMessage message=items.get(position).model.get();
        return message.getSenderId()==currentUserId;
    }

    private static class HolderRight extends DataHolder<QBChatMessage> {

        ChatMessageRightBinding binding;
        ChatMessagesAdapter adapter;
        protected HolderRight(View v, ChatMessagesAdapter adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
            this.adapter=adapter;
        }

        @Override
        public void showData(DataItem<QBChatMessage> model,int position) {
            QBChatMessage item = model.model.get();
            Date date=new Date(item.getDateSent()*1000);
            binding.setMessage(item.getBody());
            binding.setDate(adapter.formatDate(date));
            if (position>0){
                if (adapter.getMessageSelf(position-1)){
                    binding.text.setBackgroundResource(R.drawable.message_right);
                    binding.setIsLast(false);
                }else {
                    binding.text.setBackgroundResource(R.drawable.message_right_last);
                    binding.setIsLast(true);
                }
            }else {
                binding.text.setBackgroundResource(R.drawable.message_right_last);
                binding.setIsLast(true);
            }

            if (item.getAttachments().size()>0){
                final QBAttachment attachment=item.getAttachments().iterator().next();
                binding.setMessage(attachment.getName());
            }
        }
    }

    private static class HolderLeft extends DataHolder<QBChatMessage> {

        ChatMessageLeftBinding binding;
        ChatMessagesAdapter adapter;
        protected HolderLeft(View v, ChatMessagesAdapter adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
            this.adapter=adapter;
        }

        @Override
        public void showData(DataItem<QBChatMessage> model,int position) {
            QBChatMessage item = model.model.get();
            Date date=new Date(item.getDateSent()*1000);
            binding.setDate(adapter.formatDate(date));
            binding.setMessage(item.getBody());
            if (position>0){
                if (!adapter.getMessageSelf(position-1)){
                    binding.text.setBackgroundResource(R.drawable.message_left);
                    binding.setIsLast(false);
                }else {
                    binding.text.setBackgroundResource(R.drawable.message_left_last);
                    binding.setIsLast(true);
                }
            }else {
                binding.text.setBackgroundResource(R.drawable.message_left_last);
                binding.setIsLast(true);
            }

            if (item.getAttachments().size()>0){
                final QBAttachment attachment=item.getAttachments().iterator().next();
                binding.setMessage(attachment.getName());
            }

        }
    }
    public String formatDate(Date date){

        Calendar today= GregorianCalendar.getInstance();
        today.set(Calendar.HOUR,0);
        today.set(Calendar.MINUTE,0);
        today.set(Calendar.SECOND,0);

        if (today.getTime().compareTo(date)<0){
            SimpleDateFormat dateFormat=new SimpleDateFormat("h:mm a");
            return  dateFormat.format(date);
        }else {
            SimpleDateFormat dateFormat=new SimpleDateFormat("d MMM h:mm a");
            return  dateFormat.format(date);
        }


    }
}