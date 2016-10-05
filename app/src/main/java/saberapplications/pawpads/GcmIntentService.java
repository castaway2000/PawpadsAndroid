package saberapplications.pawpads;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import saberapplications.pawpads.ui.chat.ChatActivity;
import saberapplications.pawpads.ui.home.MainActivity;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (Util.PUSH_NOTIFICIATIONS == true) {
            if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
                if (GoogleCloudMessaging.
                        MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.
                        MESSAGE_TYPE_DELETED.equals(messageType)) {
                    sendNotification("Deleted messages on server: " +
                            extras.toString());
                    // If it's a regular GCM message, do some work.
                } else if (GoogleCloudMessaging.
                        MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                    String recieved_message = intent.getStringExtra("message");
                    Intent sendIntent = new Intent("message_recieved");
                    sendIntent.putExtra("message", recieved_message);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
                    if (intent.hasExtra("dialog_id") && intent.hasExtra("user_id")) {
                        sendNotificationChat(intent);
                    } else {
                        sendNotification(recieved_message);
                    }

                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(final String msg) {
        mNotificationManager = (NotificationManager)
                GcmIntentService.this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(GcmIntentService.this, 0,
                new Intent(GcmIntentService.this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(GcmIntentService.this)
                        .setSmallIcon(R.drawable.pplogo)
                        .setAutoCancel(true)
                        .setContentTitle("PawPads")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void sendNotificationChat(final Intent intent) {
        Bundle extras = intent.getExtras();

        String msg = extras.getString("message");
        String userId = extras.getString("user_id");
        //Check blocked users
        boolean isBlocked = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String stringJSON = sharedPreferences.getString(C.BLOCKED_USERS_IDS, "");
        if (!stringJSON.isEmpty()) {
            JsonParser parser = new JsonParser();
            JsonArray array  = parser.parse(stringJSON).getAsJsonArray();
            ArrayList<String> stringsIds = new ArrayList<>();
            for (JsonElement jsonElement : array) {
                stringsIds.add(jsonElement.getAsString());
            }
            isBlocked = stringsIds.contains(userId);
        }

        if (!isBlocked) {
            mNotificationManager = (NotificationManager)
                    GcmIntentService.this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent chatIntent = new Intent(GcmIntentService.this, ChatActivity.class);
            chatIntent.putExtra(ChatActivity.RECIPIENT_ID, userId);
            chatIntent.putExtra(ChatActivity.DIALOG_ID, extras.getString("dialog_id"));
            PendingIntent contentIntent = PendingIntent.getActivity(GcmIntentService.this, 0,
                    chatIntent, 0);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pplogo);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(GcmIntentService.this)
                            .setSmallIcon(R.drawable.pplogo)
                            .setLargeIcon(bitmap)
                            .setAutoCancel(true)
                            .setContentTitle("PawPads")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(msg))
                            .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            //10672731
        }
    }
}
