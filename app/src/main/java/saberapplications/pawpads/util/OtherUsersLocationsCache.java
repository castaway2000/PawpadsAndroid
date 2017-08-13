package saberapplications.pawpads.util;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.SparseArrayCompat;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.location.QBLocations;
import com.quickblox.location.model.QBLocation;
import com.quickblox.location.request.QBLocationRequestBuilder;

import java.util.ArrayList;

/**
 * Created by Stanislav Volnjanskij on 7/21/17.
 */

public class OtherUsersLocationsCache {
    private static class CacheEntry{
        public Location location;
        public long time;

        public CacheEntry(Location location) {
            this.location = location;
            time=System.currentTimeMillis();
        }
    }
    private static SparseArrayCompat<CacheEntry> cache=new SparseArrayCompat<>();
    public static void put(Integer id,Location location){
        cache.put(id,new CacheEntry(location));
    }
    public static Adapter get(final Integer id){
        final Adapter adapter=new Adapter();
        long now=System.currentTimeMillis();
        if (cache.indexOfKey(id)>0 && (now-cache.get(id).time)<600000 ){
            new Handler().postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            adapter.setLocation(cache.get(id).location);
                        }
                    },50
            );

            return adapter;
        }

        QBLocationRequestBuilder getLocationsBuilder = new QBLocationRequestBuilder();
        getLocationsBuilder.setLastOnly();
        // radius in kilometers
        getLocationsBuilder.setUserId(id);
        QBLocations.getLocations(getLocationsBuilder, new QBEntityCallback<ArrayList<QBLocation>>() {
            @Override
            public void onSuccess(ArrayList<QBLocation> qbLocations, Bundle bundle) {
                Location location=new Location("");
                location.setLongitude(qbLocations.get(0).getLongitude());
                location.setLatitude(qbLocations.get(0).getLatitude());
                cache.put(id,new CacheEntry(location));
                adapter.setLocation(location);
            }
            @Override
            public void onError(QBResponseException e) {}
        });
        return adapter;
    }

    public static class Adapter{
        Callback callback;
        public void callback(Callback c){
            this.callback=c;
        }
        public void setLocation(Location location){
            callback.location(location);
        }
    }

    public interface Callback{
        public void location(Location location);
    }
}
