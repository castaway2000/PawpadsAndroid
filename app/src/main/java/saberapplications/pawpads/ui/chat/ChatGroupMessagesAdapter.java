package saberapplications.pawpads.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.customobjects.QBCustomObjects;
import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import saberapplications.pawpads.C;
import saberapplications.pawpads.R;
import saberapplications.pawpads.Util;
import saberapplications.pawpads.databinding.ChatGroupMessageLeftBinding;
import saberapplications.pawpads.databinding.ChatGroupMessageRightBinding;
import saberapplications.pawpads.service.FileDownloadService;
import saberapplications.pawpads.service.UserLocationService;
import saberapplications.pawpads.ui.profile.ProfileActivity;
import saberapplications.pawpads.util.AvatarLoaderHelper;
import saberapplications.pawpads.util.OtherUsersLocationsCache;
import saberapplications.pawpads.views.BaseChatAdapter;
import saberapplications.pawpads.views.BaseListAdapter;


/**
 * Created by developer on 31.05.17.
 */

public class ChatGroupMessagesAdapter extends BaseChatAdapter<QBChatMessage> {
    private Context mContext;
    private LayoutInflater mInflater;
    private static int currentUserId;
    private ArrayMap<Integer, QBUser> userCache = new ArrayMap<>();

    public ChatGroupMessagesAdapter(Context context, int userId) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        currentUserId = userId;
    }

    @Override
    public DataHolder<QBChatMessage> getChatItemHolderRight(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.chat_group_message_right, parent, false);
        return new ChatGroupMessagesAdapter.HolderRight(v, this);
    }

    @Override
    public DataHolder<QBChatMessage> getChatItemHolderLeft(ViewGroup parent) {
        View v = mInflater.inflate(R.layout.chat_group_message_left, parent, false);
        return new ChatGroupMessagesAdapter.HolderLeft(v, this);
    }

    @Override
    public boolean getMessageSelf(int position) {
        QBChatMessage message = items.get(position).model.get();
        return message.getSenderId() == currentUserId;
    }
    public boolean groupWithPrevMessage(int position,QBChatMessage currentMessage){
        QBChatMessage prevMessage = items.get(position).model.get();
        long timeDiff=currentMessage.getDateSent()-prevMessage.getDateSent();
        return prevMessage.getSenderId().equals(currentMessage.getSenderId()) && timeDiff<C.DAY;
    }

    public static class MessageHolder extends DataHolder<QBChatMessage> {
        protected ChatGroupMessagesAdapter adapter;
        QBAttachment attachment;

        public MessageHolder(View v, BaseListAdapter<QBChatMessage> adapter) {
            super(v, adapter);
            this.adapter = (ChatGroupMessagesAdapter) adapter;
        }

        @Override
        public void showData(DataItem<QBChatMessage> model, int position) {

        }

        public void downloadAttachment() {
            if (attachment != null) {
                FileDownloadService.startService(adapter.mContext, attachment);
            }
        }

        protected void loadUserAvatar(final int userId, final ImageView avatar, final int avatarSize) {
            Glide.clear(avatar);
            Glide.with(avatar.getContext()).load(R.drawable.user_placeholder).into(avatar);
            avatar.setTag(R.id.user_id,userId);
            if (!adapter.userCache.containsKey(userId)) {
                QBUsers.getUser(userId, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        int storedId= (int) avatar.getTag(R.id.user_id);
                        if (qbUser.getFileId() != null && storedId==userId) {
                            adapter.userCache.put(qbUser.getId(), qbUser);
                        }
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            } else {
                AvatarLoaderHelper.loadImage(adapter.userCache.get(userId).getFileId(), avatar, avatarSize, avatarSize);
            }
        }



        protected void loadUsername(final int userId, final TextView username){
            QBUsers.getUser(userId, new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    if (qbUser.getFullName() != null) {
                        username.setText(qbUser.getFullName());
                    }
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }

        protected void checkIsUserBlocked(final int recipientId, final View view) {
            if (view == null) return;
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected Boolean doInBackground(Void... voids) {
                    QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
                    requestBuilder.eq("source_user", currentUserId);
                    requestBuilder.eq("blocked_user", recipientId);

                    ArrayList<QBCustomObject> blocks = null;
                    try {
                        blocks = QBCustomObjects.getObjects("BlockList", requestBuilder, new Bundle());
                    } catch (QBResponseException e) {
                        e.printStackTrace();
                    }
                    return (blocks != null && blocks.size() > 0);
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    if (result) view.setVisibility(View.VISIBLE);
                }
            }.execute();
        }

    }

    public static class HolderRight extends ChatGroupMessagesAdapter.MessageHolder {

        ChatGroupMessageRightBinding binding;

        protected HolderRight(View v, ChatGroupMessagesAdapter adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
            binding.setHolder(this);
        }

        @Override
        public void showData(DataItem<QBChatMessage> model, int position) {
            QBChatMessage item = model.model.get();
            Date date = new Date(item.getDateSent() * 1000);
            binding.setMessage(item.getBody());
            binding.setDate(adapter.formatDate(date));
            binding.avatarGroupUser.setImageResource(R.drawable.user_placeholder);
            binding.avatarGroupUser.setVisibility(View.INVISIBLE);
            binding.blockedStatus.setVisibility(View.GONE);
            if (position > 0) {
                if (adapter.groupWithPrevMessage(position - 1,item)) {
                    binding.text.setBackgroundResource(R.drawable.message_right);
                    binding.setIsLast(false);
                } else {
                    binding.text.setBackgroundResource(R.drawable.message_right_last);
                    binding.setIsLast(true);
                }
            } else {
                binding.text.setBackgroundResource(R.drawable.message_right_last);
                binding.setIsLast(true);
            }
            if (binding.getIsLast()) {
                int userId = item.getSenderId();
                float d = view.getResources().getDisplayMetrics().density;
                binding.avatarGroupUser.setVisibility(View.VISIBLE);
                loadUserAvatar(userId, binding.avatarGroupUser, Math.round(25 * d));
                checkIsUserBlocked(item.getSenderId(), binding.blockedStatus);
            }

            binding.setShowThumbNail(false);
            if (item.getAttachments().size() > 0) {
                Iterator<QBAttachment> iterator = item.getAttachments().iterator();
                attachment = iterator.next();
                if (attachment.getName() != null) {
                    binding.setMessage(attachment.getName());
                } else {
                    binding.setMessage(item.getBody());
                }


                if (iterator.hasNext()) {
                    QBAttachment thumbAttachment = iterator.next();
                    if (thumbAttachment.getType().equals("thumb")) {
                        binding.setMessage("");
                        binding.thumb.setImageBitmap(null);
                        binding.setShowThumbNail(true);
                        AvatarLoaderHelper.loadImage(Integer.parseInt(thumbAttachment.getId()), binding.thumb, 300, 300);
                    }
                }
            } else {
                attachment = null;
            }
            if (item.getProperty(C.CHAT_MSG_STICKER_PROPERTY) != null) {
                binding.text.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                binding.setShowThumbNail(true);
                binding.stickerProgressBar.setVisibility(View.VISIBLE);

                String url = item.getProperty(C.CHAT_MSG_STICKER_PROPERTY).toString();
                Glide.clear(binding.thumb);
                if (url.matches("\\.gif")) {
                    Glide.with(itemView.getContext())
                            .load(Uri.parse(url)).asGif()
                            .listener(new RequestListener<Uri, GifDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GifDrawable> target, boolean isFirstResource) {
                                    if (binding.stickerProgressBar != null) {
                                        binding.stickerProgressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GifDrawable resource, Uri model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    if (binding.stickerProgressBar != null) {
                                        binding.stickerProgressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }
                            })
                            .into(binding.thumb);
                } else {
                    Glide.with(itemView.getContext())
                            .load(Uri.parse(url))
                            .listener(new RequestListener<Uri, GlideDrawable>() {
                                @Override
                                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                    if (binding.stickerProgressBar != null) {
                                        binding.stickerProgressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    if (binding.stickerProgressBar != null) {
                                        binding.stickerProgressBar.setVisibility(View.GONE);
                                    }
                                    return false;
                                }
                            })
                            .into(binding.thumb);
                }

            }
        }
    }

    public static class HolderLeft extends ChatGroupMessagesAdapter.MessageHolder {

        ChatGroupMessageLeftBinding binding;
        private QBChatMessage item;

        protected HolderLeft(View v, ChatGroupMessagesAdapter adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
            binding.setHolder(this);
        }

        @Override
        public void showData(DataItem<QBChatMessage> model, int position) {
            item = model.model.get();
            Date date = new Date(item.getDateSent() * 1000);
            binding.setDate(adapter.formatDate(date));
            binding.setMessage(item.getBody());

            binding.avatarGroupUser.setImageResource(R.drawable.user_placeholder);
            binding.avatarGroupUser.setVisibility(View.INVISIBLE);
            binding.blockedStatus.setVisibility(View.GONE);
            if (position > 0) {
                if (adapter.groupWithPrevMessage(position - 1,item)) {
                    binding.text.setBackgroundResource(R.drawable.message_left);
                    binding.setIsLast(false);
                } else {
                    binding.text.setBackgroundResource(R.drawable.message_left_last);
                    binding.setIsLast(true);
                }
            } else {
                binding.text.setBackgroundResource(R.drawable.message_left_last);
                binding.setIsLast(true);

            }
            if (binding.getIsLast()) {
                int userId = item.getSenderId();
                float d = view.getResources().getDisplayMetrics().density;
                binding.avatarGroupUser.setVisibility(View.VISIBLE);
                loadUserAvatar(userId, binding.avatarGroupUser, Math.round(25 * d));
                loadUsername(userId, binding.leftUsername);
                checkIsUserBlocked(item.getSenderId(), binding.blockedStatus);
            }

            if (item.getAttachments().size() > 0) {
                if (item.getAttachments().size() > 0) {
                    Iterator<QBAttachment> iterator = item.getAttachments().iterator();
                    attachment = iterator.next();
                    binding.setMessage(attachment.getName());
                    if (iterator.hasNext()) {
                        QBAttachment thumbAttachment = iterator.next();
                        if (thumbAttachment.getType().equals("thumb")) {
                            binding.setMessage("");
                            binding.setShowThumbNail(true);
                            AvatarLoaderHelper.loadImage(Integer.parseInt(thumbAttachment.getId()), binding.thumb, 300, 300);
                        }
                    }
                } else {
                    attachment = null;
                }

            } else {
                attachment = null;
                binding.setShowThumbNail(false);
            }
            final Location currentLocation= UserLocationService.getLastLocation();
            if(item.getProperty(C.LATITUDE)!=null && currentLocation!=null){
                Location messageLocation=new Location("");
                messageLocation.setLatitude(Double.parseDouble((String) item.getProperty(C.LATITUDE)));
                messageLocation.setLongitude(Double.parseDouble((String) item.getProperty(C.LONGITUDE)));
                binding.setDistance(
                        Util.formatDistance(currentLocation.distanceTo(messageLocation))
                );
            }else if(currentLocation!=null){
                OtherUsersLocationsCache.get(item.getSenderId()).callback(new OtherUsersLocationsCache.Callback() {
                    @Override
                    public void location(Location location) {
                        binding.setDistance(
                            Util.formatDistance(currentLocation.distanceTo(location))
                        );
                    }
                });
            }

            if (item.getProperty(C.CHAT_MSG_STICKER_PROPERTY) != null) {
                binding.text.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                binding.setShowThumbNail(true);
                binding.stickerProgressBar.setVisibility(View.VISIBLE);

                String url = item.getProperty(C.CHAT_MSG_STICKER_PROPERTY).toString();

                Glide.with(itemView.getContext())
                        .load(Uri.parse(url))
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                                if (binding.stickerProgressBar != null) {
                                    binding.stickerProgressBar.setVisibility(View.GONE);
                                }
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                if (binding.stickerProgressBar != null) {
                                    binding.stickerProgressBar.setVisibility(View.GONE);
                                }
                                return false;
                            }
                        })
                        .into(binding.thumb);
            }
        }

        public void openUserProfile() {
            final int userId = item.getSenderId();
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(1);
            QBUsers.getUsersByIDs(new ArrayList<Integer>() {{
                add(userId);
            }}, pagedRequestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                    Intent intent=new Intent(itemView.getContext(), ProfileActivity.class);
                    intent.putExtra(C.QB_USER,qbUsers.get(0));
                    itemView.getContext().startActivity(intent);
                }
                @Override
                public void onError(QBResponseException e) {
                    Util.onError(e, itemView.getContext());
                }
            });
        }
    }

    public String formatDate(Date date) {

        Calendar today = GregorianCalendar.getInstance();
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        if (today.getTime().compareTo(date) < 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            return dateFormat.format(date);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM h:mm a");
            return dateFormat.format(date);
        }


    }
}
