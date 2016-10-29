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
import java.util.Iterator;

import saberapplications.pawpads.R;
import saberapplications.pawpads.databinding.ChatMessageLeftBinding;
import saberapplications.pawpads.databinding.ChatMessageRightBinding;
import saberapplications.pawpads.service.FileDownloadService;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.views.BaseChatAdapter;
import saberapplications.pawpads.views.BaseListAdapter;

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

    public static class MessageHolder extends DataHolder<QBChatMessage>{
        protected ChatMessagesAdapter adapter;
        QBAttachment attachment;

        public MessageHolder(View v, BaseListAdapter<QBChatMessage> adapter) {
            super(v, adapter);
            this.adapter= (ChatMessagesAdapter) adapter;
        }

        @Override
        public void showData(DataItem<QBChatMessage> model, int position) {

        }
        public void downloadAttachment(){
            if (attachment!=null) {
                FileDownloadService.startService(adapter.mContext, attachment);
            }
        }

    }



    public static class HolderRight extends MessageHolder {

        ChatMessageRightBinding binding;

        protected HolderRight(View v, ChatMessagesAdapter adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
            binding.setHolder(this);
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
            binding.setShowThumbNail(false);
            if (item.getAttachments().size()>0){
                Iterator<QBAttachment> iterator = item.getAttachments().iterator();
                attachment=iterator.next();
                binding.setMessage(attachment.getName());
                if (iterator.hasNext()) {
                    QBAttachment thumbAttachment = iterator.next();
                    if ( thumbAttachment.getType().equals("thumb")){
                        binding.setMessage("");
                        binding.setShowThumbNail(true);
                        AvatarLoaderHelper.loadImage(Integer.parseInt(thumbAttachment.getId()),binding.thumb,300,300);
                    }
                }
            }else {
                attachment=null;
            }
        }
    }

    public static class HolderLeft extends  MessageHolder {

        ChatMessageLeftBinding binding;

        protected HolderLeft(View v, ChatMessagesAdapter adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
            binding.setHolder(this);
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
                if (item.getAttachments().size()>0){
                    Iterator<QBAttachment> iterator = item.getAttachments().iterator();
                    attachment=iterator.next();
                    binding.setMessage(attachment.getName());
                    if (iterator.hasNext()) {
                        QBAttachment thumbAttachment = iterator.next();
                        if ( thumbAttachment.getType().equals("thumb")){
                            binding.setMessage("");
                            binding.setShowThumbNail(true);
                            AvatarLoaderHelper.loadImage(Integer.parseInt(thumbAttachment.getId()),binding.thumb,300,300);
                        }
                    }
                }else {
                    attachment=null;
                }

            }else {
                attachment=null;
                binding.setShowThumbNail(false);
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