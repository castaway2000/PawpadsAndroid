package saberapplications.pawpads;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.quickblox.core.QBSettings;
import com.twitter.sdk.android.core.Twitter;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.imoji.sdk.ImojiSDK;
import saberapplications.pawpads.views.FontManager;


/**
 * Created by Stas on 28.12.15.
 */
public class PawPadsApplication extends Application {
    static PawPadsApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;

        QBSettings.getInstance().init(this,Util.QB_APPID, Util.QB_AUTH_KEY, Util.QB_AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Util.QB_ACCOUNT_KEY);
        FontManager.init(getAssets());
//      StickersManager.initialize(Util.STICKERS_API_KEY, this);

        Fabric.with(this, new Crashlytics());
        Twitter.initialize(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        ImojiSDK.getInstance().setCredentials(UUID.fromString(Util.IMOJI_SDK_CLIENT_ID), Util.IMOJI_SDK_API_TOKEN);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static PawPadsApplication getInstance() {
        return instance;
    }
}
