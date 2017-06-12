package saberapplications.pawpads.util;

import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRoster;
import com.quickblox.chat.listeners.QBRosterListener;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBPresence;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.Collection;

/**
 * Created by developer on 22.05.17.
 */

public class ChatRosterHelper {
    public static final String TAG = ChatRosterHelper.class.getSimpleName();

    public static QBRoster getChatRoster() {
        QBSubscriptionListener subscriptionListener = new QBSubscriptionListener() {
            @Override
            public void subscriptionRequested(int userId) {
                Log.d(TAG, "subscriptionRequested " + userId);
            }
        };

        QBRosterListener rosterListener = new QBRosterListener() {
            @Override
            public void entriesDeleted(Collection<Integer> userIds) {
                Log.d(TAG, "entriesDeleted " + userIds.toString());
            }

            @Override
            public void entriesAdded(Collection<Integer> userIds) {
                Log.d(TAG, "entriesAdded " + userIds.toString());
            }

            @Override
            public void entriesUpdated(Collection<Integer> userIds) {
                Log.d(TAG, "entriesUpdated " + userIds.toString());
            }

            @Override
            public void presenceChanged(QBPresence presence) {
                Log.d(TAG, "presenceChanged " + presence.toString());
            }
        };

        // Do this after success Chat login
        QBRoster chatRoster = QBChatService.getInstance().getRoster(QBRoster.SubscriptionMode.mutual, subscriptionListener);
        if(chatRoster == null) return null;
        chatRoster.addRosterListener(rosterListener);
        return chatRoster;
    }
}
