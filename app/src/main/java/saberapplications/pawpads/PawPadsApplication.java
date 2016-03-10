package saberapplications.pawpads;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.quickblox.core.QBSettings;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Stas on 28.12.15.
 */
public class PawPadsApplication extends Application {
    static PawPadsApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        Fabric.with(this, new Crashlytics());
        QBSettings.getInstance().fastConfigInit(Util.QB_APPID, Util.QB_AUTH_KEY, Util.QB_AUTH_SECRET);
//        StickersManager.initialize(Util.STICKERS_API_KEY, this);
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
