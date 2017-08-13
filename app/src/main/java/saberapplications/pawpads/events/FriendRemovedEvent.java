package saberapplications.pawpads.events;

import com.quickblox.users.model.QBUser;

/**
 * Created by Stanislav Volnjanskij on 6/15/17.
 */

public class FriendRemovedEvent {

    QBUser user;

    public FriendRemovedEvent(QBUser user) {
        this.user = user;
    }

    public QBUser getUser() {
        return user;
    }
}
