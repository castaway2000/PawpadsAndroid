//package saberapplications.pawpads;
//
///**
// * Created by blaze on 11/27/2015.
// */
//
//import android.annotation.SuppressLint;
//import android.annotation.TargetApi;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Build;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.support.v4.content.LocalBroadcastManager;
//import android.util.Log;
//import android.widget.Toast;
//
//import org.jivesoftware.smack.AndroidConnectionConfiguration;
//import org.jivesoftware.smack.Chat;
//import org.jivesoftware.smack.ChatManagerListener;
//import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.ConnectionListener;
//import org.jivesoftware.smack.MessageListener;
//import org.jivesoftware.smack.PacketListener;
//import org.jivesoftware.smack.ReconnectionManager;
//import org.jivesoftware.smack.Roster;
//import org.jivesoftware.smack.RosterListener;
//import org.jivesoftware.smack.SmackAndroid;
//import org.jivesoftware.smack.SmackConfiguration;
//import org.jivesoftware.smack.XMPPConnection;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.packet.Message;
//import org.jivesoftware.smack.packet.Packet;
//import org.jivesoftware.smack.packet.Presence;
//import org.jivesoftware.smack.provider.PrivacyProvider;
//import org.jivesoftware.smack.provider.ProviderManager;
//import org.jivesoftware.smackx.Form;
//import org.jivesoftware.smackx.FormField;
//import org.jivesoftware.smackx.GroupChatInvitation;
//import org.jivesoftware.smackx.PrivateDataManager;
//import org.jivesoftware.smackx.ServiceDiscoveryManager;
//import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
//import org.jivesoftware.smackx.muc.DiscussionHistory;
//import org.jivesoftware.smackx.muc.MultiUserChat;
//import org.jivesoftware.smackx.packet.ChatStateExtension;
//import org.jivesoftware.smackx.packet.LastActivity;
//import org.jivesoftware.smackx.packet.OfflineMessageInfo;
//import org.jivesoftware.smackx.packet.OfflineMessageRequest;
//import org.jivesoftware.smackx.packet.SharedGroupsInfo;
//import org.jivesoftware.smackx.ping.PingFailedListener;
//import org.jivesoftware.smackx.ping.PingManager;
//import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
//import org.jivesoftware.smackx.provider.DataFormProvider;
//import org.jivesoftware.smackx.provider.DelayInformationProvider;
//import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
//import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
//import org.jivesoftware.smackx.provider.MUCAdminProvider;
//import org.jivesoftware.smackx.provider.MUCOwnerProvider;
//import org.jivesoftware.smackx.provider.MUCUserProvider;
//import org.jivesoftware.smackx.provider.MessageEventProvider;
//import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
//import org.jivesoftware.smackx.provider.RosterExchangeProvider;
//import org.jivesoftware.smackx.provider.StreamInitiationProvider;
//import org.jivesoftware.smackx.provider.VCardProvider;
//import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
//import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;
//import org.jivesoftware.smackx.pubsub.provider.SubscriptionProvider;
//import org.jivesoftware.smackx.search.UserSearch;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//
//
//@TargetApi(Build.VERSION_CODES.KITKAT)
//public class XmppService extends Service implements Handler.Callback,
//        ChatManagerListener, MessageListener, PacketListener,
//        ConnectionListener {
//    private static XMPPConnection connection;
//    private static PingManager mPingManager;
//    private static String username;
//    private static String password;
//    private static String domain;
//    private static String ip;
//
//    private static final String KEY_METHOD = "key_method";
//    private static final String KEY_DOMAIN = "key_domain";
//    private static final String KEY_IP = "key_ip";
//    private static final String KEY_USERNAME = "key_username";
//    private static final String KEY_PASSWORD = "key_password";
//
//    private static final String KEY_SEND_TO = "key_send_to";
//    private static final String KEY_SEND_TYPE = "key_send_type";
//    private static final String KEY_SEND_MESSAGE = "key_send_message";
//
//    private static final String KEY_ROOM_NAME = "key_room_name";
//    private static final String KEY_ROOM_NICKNAME = "key_room_nickname";
//
//    private static final int METHOD_SETUP = 1;
//    private static final int METHOD_SETUP_CONNECT = 2;
//    private static final int METHOD_SEND_CHAT = 3;
//    private static final int METHOD_AUTO_START = 100;
//    private static final int METHOD_START_ROOM = 101;
//    private static final int METHOD_GROUP_ROOM_CHAT = 102;
//
//
//    private static Handler mHandler;
//
//    private static PowerManager.WakeLock mWakeLock;
//
//    private static volatile boolean isConnected = false;
//
//    @Override
//    public void onCreate() {
//        HandlerThread mThread = new HandlerThread("XmppServiceThread",
//                android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//        mThread.start();
//        mHandler = new Handler(mThread.getLooper(), this);
//
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        int method = intent.getIntExtra(KEY_METHOD, -1);
//        if (method != -1) {
//            mHandler.sendMessage(mHandler.obtainMessage(method, intent));
//        }
//        return START_NOT_STICKY;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    // STATIC PUBLIC METHODS
//
//    public static void setup(final Context context, String domain, String ip,
//                             String username, String password) {
//        Intent xmppServiceIntent = addInfoToSetupIntent(context, METHOD_SETUP,
//                domain, ip, username, password);
//        context.startService(xmppServiceIntent);
//    }
//
//    public static void setupAndConnect(final Context context, String domain,
//                                       String ip, String username, String password) {
//        Intent xmppServiceIntent = addInfoToSetupIntent(context,
//                METHOD_SETUP_CONNECT, domain, ip, username, password);
//        context.startService(xmppServiceIntent);
//    }
//
//
//    public static void disconnect() {
//        if (connection != null) {
//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        connection.disconnect();
//                        connection = null;
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            new Thread(runnable).start();
//        }
//
//        if (mWakeLock != null) {
//            if (mWakeLock.isHeld()) {
//                mWakeLock.release();
//            }
//        }
//
//    }
//
//    public static XMPPConnection getConnection() {
//        return connection;
//    }
//
//    public static void sendMessage(final Context context, String to,
//                                   Message.Type type, String message) {
//        Intent xmppServiceIntent = addInfoToSendIntent(context, to, type,
//                message);
//        context.startService(xmppServiceIntent);
//    }
//
//
//    public static void sendGroupMessage(final Context context, String group_id,
//                                        String message, String user_id) {
//        Intent xmppServiceIntent = addInfoToSendGroupMessageIntent(context, group_id,
//                message, user_id);
//        context.startService(xmppServiceIntent);
//    }
//
//
//    // PRIVATE METHODS
//
//    private static Intent addInfoToSendIntent(final Context context, String to,
//                                              Message.Type type, String message) {
//        Intent intent = getXmppServiceIntent(context);
//        intent.putExtra(KEY_METHOD, METHOD_SEND_CHAT);
//        intent.putExtra(KEY_SEND_TO, to);
//        intent.putExtra(KEY_SEND_TYPE, type);
//        intent.putExtra(KEY_SEND_MESSAGE, message);
//        return intent;
//    }
//
//    private static Intent addInfoToSendGroupMessageIntent(final Context context, String group_id,
//                                                          String message, String user_id) {
//        Intent intent = getXmppServiceIntent(context);
//        intent.putExtra(KEY_METHOD, METHOD_GROUP_ROOM_CHAT);
//        intent.putExtra(KEY_SEND_TO, group_id);
//        intent.putExtra(KEY_SEND_MESSAGE, message);
//        intent.putExtra("user_id", user_id);
//        return intent;
//    }
//
//    private static Intent addInfoToSetupIntent(final Context context,
//                                               int method, String domain, String ip, String username,
//                                               String password) {
//        Intent intent = getXmppServiceIntent(context);
//        intent.putExtra(KEY_METHOD, method);
//        intent.putExtra(KEY_DOMAIN, domain);
//        intent.putExtra(KEY_IP, ip);
//        intent.putExtra(KEY_USERNAME, username);
//        intent.putExtra(KEY_PASSWORD, password);
//        return intent;
//    }
//
//    private static Intent addInfoToRoomIntent(final Context context,
//                                              int method, String roomName, String nickname) {
//        Intent intent = getXmppServiceIntent(context);
//        intent.putExtra(KEY_METHOD, method);
//        intent.putExtra(KEY_ROOM_NAME, roomName);
//        intent.putExtra(KEY_ROOM_NICKNAME, nickname);
//        return intent;
//    }
//
//    private static Intent addInfoToJoinIntent(final Context context,
//                                              int method, String roomName, String username) {
//        Intent intent = getXmppServiceIntent(context);
//        intent.putExtra(KEY_METHOD, method);
//        intent.putExtra(KEY_ROOM_NAME, roomName);
//        intent.putExtra(KEY_USERNAME, username);
//        return intent;
//    }
//
//    private static Intent addInfoToGroupMessageIntent(final Context context,
//                                                      int method, String roomName, String message) {
//        Intent intent = getXmppServiceIntent(context);
//        intent.putExtra(KEY_METHOD, method);
//        intent.putExtra(KEY_ROOM_NAME, roomName);
//        intent.putExtra(KEY_SEND_MESSAGE, message);
//        return intent;
//    }
//
//    private static Intent getXmppServiceIntent(final Context context) {
//        return new Intent(context, XmppService.class);
//    }
//
//    private void _connect() {
//        if (connection != null) {
//            try {
//                connection.disconnect();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            mPingManager = null;
//            connection = null;
//        }
//        if (username != null && password != null && domain != null) {
//            try {
//                SmackAndroid.init(this);
//                configure(ProviderManager.getInstance());
//
//                ConnectionConfiguration config = new AndroidConnectionConfiguration(
//                        domain, 5222);
//                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//                config.setReconnectionAllowed(false);
//                connection = new XMPPConnection(config);
//                connection.addConnectionListener(this);
//
//                connection.connect();
//
//                // LogUtils.printLogW("Connection, Username, password "
//                // + connection + ", " + username + "," + password);
//
//                connection.login(username, password);
//
//                mPingManager = PingManager.getInstanceFor(connection);
//                mPingManager.setPingIntervall(5);
//                mPingManager
//                        .registerPingFailedListener(new PingFailedListener() {
//                            @Override
//                            public void pingFailed() {
//                                // LogUtils.printLogD("Ping failed");
//                            }
//                        });
//
//                // ChatManager chatManager = ChatManager
//                // .getInstanceFor(connection);
//                connection.getChatManager().addChatListener(XmppService.this);
//
//                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
//                        "XmppService");
//                mWakeLock.acquire();
//
//
////				try {
////					handler = new Handler();
////					runnable.run();
////				} catch (Exception e) {
////					System.out.println(e);
////				}
//
//                // throw an event saying that log in is successful
//                // EventBus.getDefault().postSticky(new LoginEvent(true));
//            } catch (XMPPException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private void _sendMessage(String to, Message.Type type, String message) {
//        //
//        // MultiUserChat chatRoom = new MultiUserChat(connection,
//        // to);
//
//        Message msg = new Message(to, type);
//        msg.setBody(message);
//        if (connection != null) {
//            try {
//
//                Log.d("pavan", "to " + to);
//                Log.d("pavan", "message " + message);
//                Log.d("pavan", "message in send message");
//                Log.d("pavan", "message in msg " + msg.getBody());
//
//                connection.getChatManager().createChat(to, this)
//                        .sendMessage(msg);
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public boolean handleMessage(android.os.Message msg) {
//        int method = msg.what;
//        switch (method) {
//            case METHOD_SETUP:
//            case METHOD_SETUP_CONNECT:
//                if (!isConnected) {
//                    Intent setupIntent = (Intent) msg.obj;
//                    username = setupIntent.getStringExtra(KEY_USERNAME);
//                    password = setupIntent.getStringExtra(KEY_PASSWORD);
//                    domain = setupIntent.getStringExtra(KEY_DOMAIN);
//                    ip = setupIntent.getStringExtra(KEY_IP);
//                    if (method == METHOD_SETUP_CONNECT) {
//                        _connect();
//                    }
//                }
//                break;
//            case METHOD_SEND_CHAT:
//                Intent sendIntent = (Intent) msg.obj;
//                String to = sendIntent.getStringExtra(KEY_SEND_TO);
//                Message.Type type = (Message.Type) sendIntent
//                        .getSerializableExtra(KEY_SEND_TYPE);
//                String message = sendIntent.getStringExtra(KEY_SEND_MESSAGE);
//                _sendMessage(to, type, message);
//                break;
//            case METHOD_AUTO_START:
//                checkAndStart(this);
//                break;
//
//
//            case METHOD_START_ROOM:
//                Intent AddXmppIntent = (Intent) msg.obj;
//                RegisterChat(AddXmppIntent.getStringExtra(KEY_ROOM_NAME), AddXmppIntent.getStringExtra(KEY_ROOM_NICKNAME));
//                break;
//            default:
//                break;
//        }
//        return false;
//    }
//
//
//    @Override
//    public void chatCreated(Chat chat, boolean createdLocally) {
//        if (!createdLocally) {
//            chat.addMessageListener(XmppService.this);
//        }
//    }
//
//    @Override
//    public void processMessage(Chat chat, Message message) {
//        Intent sendIntent = new Intent("message_recieved");
//        sendIntent.putExtra("message", message.getBody());
//        LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
//
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        return super.onUnbind(intent);
//    }
//
//    @SuppressLint("NewApi")
//    @Override
//    public void onRebind(Intent intent) {
//        super.onRebind(intent);
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        isConnected = false;
//        mPingManager = null;
//        connection = null;
//        //	scheduleRestart(this);
//    }
//
//    // multi user chat stuff
//
//    static {
//        try {
//            Class.forName(ReconnectionManager.class.getName());
//            Class.forName(ServiceDiscoveryManager.class.getName());
//        } catch (ClassNotFoundException ex) {
//            // LogUtils.printLogD("Class not initialized!");
//        }
//    }
//
//    @Override
//    public void processPacket(Packet packet) {
//        Message message = (Message) packet;
//
//        Log.d("pavan", "in process packert ");
//
//        Intent sendIntent = new Intent("message_recieved");
//        sendIntent.putExtra("message", "pavan");
//        LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
//    }
//
//    public void checkAndStart(final Context context) {
//        // boolean shouldStart = Util.getBoolean(context,
//        // Util.PREFERENCE_SERVICE_SHOULD_START, true);
//        boolean shouldStart = true;
//
//
//        XmppService.setupAndConnect(context, Util.SERVER, "",
//                "pavan", Util.XMPP_PASSWORD);
//
//
//    }
//
//    public static boolean isConnected() {
//        return isConnected;
//    }
//
//    public void connected(XMPPConnection xmppConnection) {
//        connection = xmppConnection;
//        isConnected = true;
//
//        Roster roster = connection.getRoster();
//        roster.addRosterListener(new RosterListener() {
//            // Ignored events public void entriesAdded(Collection<String>
//            // addresses) {}
//
//            public void entriesDeleted(Collection<String> addresses) {
//            }
//
//            public void entriesUpdated(Collection<String> addresses) {
//            }
//
//            public void presenceChanged(Presence presence) {
//
//                // LogUtils.printLogW("Roster changed " + presence.getFrom() +
//                // " "
//                // + presence);
//
//            }
//
//            @Override
//            public void entriesAdded(Collection<String> arg0) {
//                // TODO Auto-generated method stub
//
//            }
//        });
//
//
//    }
//
//    public void authenticated(XMPPConnection xmppConnection) {
//
//    }
//
//    @Override
//    public void connectionClosed() {
////		isConnected = false;
////		try {
////			//connection.disconnect();
////		} catch (Exception e1) {
////			e1.printStackTrace();
////		}
////		mPingManager = null;
////		connection = null;
//    }
//
//    @Override
//    public void connectionClosedOnError(Exception e) {
//        isConnected = false;
//        try {
//            connection.disconnect();
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
//        mPingManager = null;
//        connection = null;
//
//    }
//
//    @Override
//    public void reconnectingIn(int i) {
//
//    }
//
//    @Override
//    public void reconnectionSuccessful() {
//        isConnected = true;
//    }
//
//    @Override
//    public void reconnectionFailed(Exception e) {
//        isConnected = false;
//        mPingManager = null;
//        connection = null;
//    }
//
//    public static boolean isOnline(Context context) {
//        ConnectivityManager cm = (ConnectivityManager) context
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        // should check null because in air plan mode it will be null
//        if (netInfo != null && netInfo.isConnected()) {
//            return true;
//        }
//        return false;
//    }
//
//
//    public void configure(ProviderManager pm) {
//
//        // Private Data Storage
//        pm.addIQProvider("query", Util.SERVER + ":iq:private",
//                new PrivateDataManager.PrivateDataIQProvider());
//
//        // Time
//        try {
//            pm.addIQProvider("query", Util.SERVER + ":iq:time",
//                    Class.forName("org.jivesoftware.smackx.packet.Time"));
//        } catch (ClassNotFoundException e) {
//            Log.w("TestClient",
//                    "Can't load class for org.jivesoftware.smackx.packet.Time");
//        }
//
//        // Roster Exchange
//        pm.addExtensionProvider("x", Util.SERVER + ":x:roster",
//                new RosterExchangeProvider());
//
//        // Message Events
//        pm.addExtensionProvider("x", Util.SERVER + ":x:event",
//                new MessageEventProvider());
//
//        // Chat State
//        pm.addExtensionProvider("active", "http://" + Util.SERVER
//                + "/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("composing", "http://" + Util.SERVER
//                + "/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("paused", "http://" + Util.SERVER
//                + "/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("inactive", "http://" + Util.SERVER
//                + "/protocol/chatstates", new ChatStateExtension.Provider());
//        pm.addExtensionProvider("gone", "http://" + Util.SERVER
//                + "/protocol/chatstates", new ChatStateExtension.Provider());
//
//        // XHTML
//        pm.addExtensionProvider("html", "http://" + Util.SERVER
//                + "/protocol/xhtml-im", new XHTMLExtensionProvider());
//
//        // Group Chat Invitations
//        pm.addExtensionProvider("x", Util.SERVER + ":x:conference",
//                new GroupChatInvitation.Provider());
//
//        // Service Discovery # Items
//        pm.addIQProvider("query", "http://" + Util.SERVER
//                + "/protocol/disco#items", new DiscoverItemsProvider());
//
//        // Service Discovery # Info
//        pm.addIQProvider("query", "http://" + Util.SERVER
//                + "/protocol/disco#info", new DiscoverInfoProvider());
//
//        // Data Forms
//        pm.addExtensionProvider("x", Util.SERVER + ":x:data",
//                new DataFormProvider());
//
//        // MUC User
//        pm.addExtensionProvider("x", "http://" + Util.SERVER
//                + "/protocol/muc#user", new MUCUserProvider());
//
//        // MUC Admin
//        pm.addIQProvider("query", "http://" + Util.SERVER
//                + "/protocol/muc#admin", new MUCAdminProvider());
//
//        // MUC Owner
//        pm.addIQProvider("query", "http://" + Util.SERVER
//                + "/protocol/muc#owner", new MUCOwnerProvider());
//
//        // Delayed Delivery
//        pm.addExtensionProvider("x", Util.SERVER + ":x:delay",
//                new DelayInformationProvider());
//
//        // Version
//        try {
//            pm.addIQProvider("query", Util.SERVER + ":iq:version",
//                    Class.forName("org.jivesoftware.smackx.packet.Version"));
//        } catch (ClassNotFoundException e) {
//            // Not sure what's happening here.
//        }
//
//        // VCard
//        pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
//
//        // Offline Message Requests
//        pm.addIQProvider("offline", "http://" + Util.SERVER
//                + "/protocol/offline", new OfflineMessageRequest.Provider());
//
//        // Offline Message Indicator
//        pm.addExtensionProvider("offline", "http://" + Util.SERVER
//                + "/protocol/offline", new OfflineMessageInfo.Provider());
//
//        // Last Activity
//        pm.addIQProvider("query", Util.SERVER + ":iq:last",
//                new LastActivity.Provider());
//
//        // User Search
//        pm.addIQProvider("query", Util.SERVER + ":iq:search",
//                new UserSearch.Provider());
//
//        // SharedGroupsInfo
//        pm.addIQProvider("sharedgroup",
//                "http://www.jivesoftware.org/protocol/sharedgroup",
//                new SharedGroupsInfo.Provider());
//
//        // JEP-33: Extended Stanza Addressing
//        pm.addExtensionProvider("addresses", "http://" + Util.SERVER
//                + "/protocol/address", new MultipleAddressesProvider());
//
//        // FileTransfer
//        pm.addIQProvider("si", "http://" + Util.SERVER + "/protocol/si",
//                new StreamInitiationProvider());
//
//        pm.addIQProvider("query", "http://" + Util.SERVER
//                + "/protocol/bytestreams", new BytestreamsProvider());
//
//        // Privacy
//        pm.addIQProvider("query", Util.SERVER + ":iq:privacy",
//                new PrivacyProvider());
//        pm.addIQProvider("command", "http://" + Util.SERVER
//                + "/protocol/commands", new AdHocCommandDataProvider());
//        pm.addExtensionProvider("malformed-action", "http://"
//                        + Util.SERVER + "/protocol/commands",
//                new AdHocCommandDataProvider.MalformedActionError());
//        pm.addExtensionProvider("bad-locale", "http://" + Util.SERVER
//                        + "/protocol/commands",
//                new AdHocCommandDataProvider.BadLocaleError());
//        pm.addExtensionProvider("bad-payload", "http://" + Util.SERVER
//                        + "/protocol/commands",
//                new AdHocCommandDataProvider.BadPayloadError());
//        pm.addExtensionProvider("bad-sessionid", "http://" + Util.SERVER
//                        + "/protocol/commands",
//                new AdHocCommandDataProvider.BadSessionIDError());
//        pm.addExtensionProvider("session-expired", "http://" + Util.SERVER
//                        + "/protocol/commands",
//                new AdHocCommandDataProvider.SessionExpiredError());
//
//        // ******************************Registratiion for
//        // PUBSUB******************
//        pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.PubSubProvider());
//
//        pm.addExtensionProvider("subscription",
//                PubSubNamespace.BASIC.getXmlns(), new SubscriptionProvider());
//
//        pm.addExtensionProvider(
//                "create",
//                "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.SimpleNodeProvider());
//
//        pm.addExtensionProvider("items", "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.ItemsProvider());
//
//        pm.addExtensionProvider("item", "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.ItemProvider());
//
//        pm.addExtensionProvider("item", "",
//                new org.jivesoftware.smackx.pubsub.provider.ItemProvider());
//
//        pm.addExtensionProvider(
//                "subscriptions",
//                "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.SubscriptionsProvider());
//
//        pm.addExtensionProvider(
//                "subscriptions",
//                "http://jabber.org/protocol/pubsub#owner",
//                new org.jivesoftware.smackx.pubsub.provider.SubscriptionsProvider());
//
//        pm.addExtensionProvider(
//                "affiliations",
//                "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.AffiliationsProvider());
//
//        pm.addExtensionProvider(
//                "affiliation",
//                "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.AffiliationProvider());
//
//        pm.addExtensionProvider("options", "http://jabber.org/protocol/pubsub",
//                new org.jivesoftware.smackx.pubsub.provider.FormNodeProvider());
//
//        pm.addIQProvider("pubsub", "http://jabber.org/protocol/pubsub#owner",
//                new org.jivesoftware.smackx.pubsub.provider.PubSubProvider());
//
//        pm.addExtensionProvider("configure",
//                "http://jabber.org/protocol/pubsub#owner",
//                new org.jivesoftware.smackx.pubsub.provider.FormNodeProvider());
//
//        pm.addExtensionProvider("default",
//                "http://jabber.org/protocol/pubsub#owner",
//                new org.jivesoftware.smackx.pubsub.provider.FormNodeProvider());
//
//        pm.addExtensionProvider("event",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.EventProvider());
//
//        pm.addExtensionProvider(
//                "configuration",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.ConfigEventProvider());
//
//        pm.addExtensionProvider(
//                "delete",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.SimpleNodeProvider());
//
//        pm.addExtensionProvider("options",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.FormNodeProvider());
//
//        pm.addExtensionProvider("items",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.ItemsProvider());
//
//        pm.addExtensionProvider("item",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.ItemProvider());
//
//        pm.addExtensionProvider("headers", "http://jabber.org/protocol/shim",
//                new org.jivesoftware.smackx.provider.HeaderProvider());
//
//        pm.addExtensionProvider("header", "http://jabber.org/protocol/shim",
//                new org.jivesoftware.smackx.provider.HeadersProvider());
//
//        pm.addExtensionProvider(
//                "retract",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.RetractEventProvider());
//
//        pm.addExtensionProvider(
//                "purge",
//                "http://jabber.org/protocol/pubsub#event",
//                new org.jivesoftware.smackx.pubsub.provider.SimpleNodeProvider());
//
//        pm.addExtensionProvider("x", "jabber:x:data",
//                new org.jivesoftware.smackx.provider.DataFormProvider());
//
//    }
//
//    public static MultiUserChat muc;
//
//    public static void createXmppGroup(String group_id, String group_name,
//                                       String admin_id) {
//
//        muc = new MultiUserChat(connection, group_id + "@conference."
//                + Util.SERVER);
//        if (muc != null) {
//            try {
//                muc.create(group_id);
//
//                muc.join(group_id);
//                // Send From with description
//                Form form = muc.getConfigurationForm();
//                Form submitForm = form.createAnswerForm();
//                // submitForm.setAnswer("muc#roomconfig_moderatedroom", "1");
//
//                for (Iterator<FormField> fields = form.getFields(); fields
//                        .hasNext(); ) {
//                    FormField field = (FormField) fields.next();
//
//                    if (!FormField.TYPE_HIDDEN.equals(field.getType())
//                            && field.getVariable() != null) {
//                        submitForm.setDefaultAnswer(field.getVariable());
//                    }
//
//                }
//
//                List<String> owners = new ArrayList<String>();
//                owners.add(connection.getUser().toString());
//                submitForm.setAnswer("muc#roomconfig_roomowners", owners);
//                submitForm.setAnswer("muc#roomconfig_persistentroom", true);
//                submitForm.setAnswer("muc#roomconfig_roomdesc", admin_id
//                        + "###" + group_name + "###");
//
//                // submitForm.setAnswer("muc#roomconfig_roomname",
//                // group_name);
//                // submitForm.setAnswer("muc#roomconfig_roomdesc",
//                // admin_id);
//
//                muc.sendConfigurationForm(submitForm);
//
//
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//
//    }
//
//
//    public void RegisterChat(String group_id, String user_id) {
//
//        Log.d("pavan", "chat register _id " + group_id);
//        MultiUserChat chat = new MultiUserChat(getConnection(),
//                group_id + "@conference." + Util.SERVER);
//
//        try {
//
//            DiscussionHistory history = new DiscussionHistory();
//            history.setSince(new Date());
//            history.setMaxStanzas(0);
//            chat.join(user_id, Util.XMPP_PASSWORD, history, SmackConfiguration.getPacketReplyTimeout());
//            // /chat1.join(userId);
//
//            chat.addMessageListener(XmppService.this);
//
//            muc = chat;
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        //	return chat;
//
//    }
//
//
//    private Handler handler;
//    private Runnable runnable = new Runnable() {
//        public void run() {
//            try {
//                Presence presence = new Presence(Presence.Type.available);
//                presence.setStatus("available");
//                presence.setPriority(24);
//                presence.setMode(Presence.Mode.available);
//                connection.sendPacket(presence);
//                handler.postDelayed(this, 10000);
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                Toast.makeText(getApplicationContext(), "Check the internet connection",
//                        Toast.LENGTH_SHORT).show();
//                //ConnectionSetup();
//                e.printStackTrace();
//            }
//        }
//    };
//
//}
