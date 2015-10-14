package saberapplications.pawpads;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;


/**
 * Created by blaze on 9/10/2015.
 * <p/>
 * THIS MAY NOT BE NEEDED IF CHAT HTTP REQUEST CODE WORKS FOR JSON CALLS
 */
public class RequestQueue extends Application {
    //private InputStream inputStream = null;

    private int _id;
    private String _name;
    private String _description;
    private float _lat;
    private float _lon;
    private byte[] _image;

    public static final String TAG = "VolleyPatterns";
    private com.android.volley.RequestQueue mRequestQueue;
    private static RequestQueue sInstance;

    public RequestQueue(int id, String name, String description, float lat, float lon, byte[] image) {
        _id = id;
        _name = name;
        _description = description;
        _lat = lat;
        _lon = lon;
        _image = image;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // initialize the singleton
        sInstance = this;
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized RequestQueue getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public com.android.volley.RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        VolleyLog.d("Adding request to queue: %s", req.getUrl());
        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
