package saberapplications.pawpads.ui.profile;

import android.os.Bundle;

import com.quickblox.chat.QBPrivacyListsManager;
import com.quickblox.chat.listeners.QBPrivacyListListener;
import com.quickblox.chat.model.QBPrivacyList;
import com.quickblox.chat.model.QBPrivacyListItem;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

import saberapplications.pawpads.ui.BaseActivity;

/**
 * Created by blaze on 4/10/2016.
 */
public class Privacy extends BaseActivity {
    QBPrivacyListsManager privacyListsManager;
    QBPrivacyList list;
    QBPrivacyListItem item1;
    ArrayList<QBPrivacyListItem> items;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new QBPrivacyList();
        list.setName("public");
        item1 = new QBPrivacyListItem();

        items = new ArrayList<>();

    }


    public void addToBlockList(int userID) {
//        item1.setType(QBPrivacyListItem.Type.USER_ID);

        item1.setValueForType(String.valueOf(userID));
        item1.setAllow(false);

        items.add(item1);
        list.setItems(items);

        try {
            privacyListsManager.setPrivacyList(list);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

        // set listener
        QBPrivacyListListener privacyListListener = new QBPrivacyListListener() {
            @Override
            public void setPrivacyList(String listName, List<QBPrivacyListItem> listItem) {
            }

            @Override
            public void updatedPrivacyList(String listName) {

            }
        };
    }

    public void removeFromBlockList(int userID) {
        list = new QBPrivacyList();
        items = new ArrayList<>();
        item1 = new QBPrivacyListItem();
        item1.setAllow(false);
        item1.setType(QBPrivacyListItem.Type.USER_ID);
        item1.setValueForType(String.valueOf(userID));

        items.remove(items.indexOf(item1));
        list.setItems(items);

        try {
            privacyListsManager.setPrivacyList(list);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

        // set listener
        QBPrivacyListListener privacyListListener = new QBPrivacyListListener() {
            @Override
            public void setPrivacyList(String listName, List<QBPrivacyListItem> listItem) {
            }

            @Override
            public void updatedPrivacyList(String listName) {

            }
        };
    }

}
