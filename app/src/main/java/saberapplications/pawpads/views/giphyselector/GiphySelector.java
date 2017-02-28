package saberapplications.pawpads.views.giphyselector;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;
import saberapplications.pawpads.R;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by Stanislav Volnjanskij on 2/10/17.
 */

public class GiphySelector extends FrameLayout {
    public interface Callback{
        void onSelected(Giphy giphy);
    }


    private View rootView;
    private RecyclerView recyclerView;
    private GiphyListAdapter adapter;
    private EditText searcEditText;
    private Call<JsonObject> task;
    private int limit=25;
    private int offset=0;
    private Callback callback;

    public GiphySelector(Context context) {
        super(context);
        init(context);
    }

    public GiphySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GiphySelector(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GiphySelector(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = inflater.inflate(R.layout.giphy_selector, this, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GiphyListAdapter();
        adapter.setCallback(new BaseListAdapter.Callback<Giphy>() {
            @Override
            public void onLoadMore() {
                search(searcEditText.getText().toString());
            }

            @Override
            public void onItemClick(Giphy item) {
                if(callback!=null){
                    callback.onSelected(item);
                }

            }
        });
        adapter.setShowInitialLoad(false);


        recyclerView.setAdapter(adapter);

        searcEditText = (EditText) rootView.findViewById(R.id.search);

        searcEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return false;
            }
        });
        searcEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (task!=null && task.isExecuted()) task.cancel();
                if (s.length() > 1) {
                    adapter.clear();
                    adapter.setShowInitialLoad(true);
                    search(s.toString());
                }else{
                    adapter.clear();
                    adapter.setShowInitialLoad(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        this.addView(rootView);

    }

    private void search(String q) {

        task = GiphyApiClient.getService().getGifs(q,limit,offset);
        task.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                offset=offset+limit;
                JsonArray gifs = response.body().getAsJsonArray("data");
                adapter.setShowInitialLoad(true);
                ArrayList<Giphy> items = new ArrayList<Giphy>();
                for (JsonElement gif : gifs) {
                    JsonObject images = gif.getAsJsonObject().getAsJsonObject("images");
                    JsonObject thumb = images.getAsJsonObject("preview_gif");

                    if (thumb==null) continue;

                    Giphy giphy = new Giphy(gif.getAsJsonObject().get("slug").getAsString());

                    giphy.thumb = new Giphy.Image(thumb.get("url").getAsString(),
                            thumb.get("width").getAsInt(),
                            thumb.get("height").getAsInt());

                    JsonObject full = images.getAsJsonObject("fixed_width_downsampled");
                    giphy.full = new Giphy.Image(full.get("url").getAsString(),
                            full.get("width").getAsInt(),
                            full.get("height").getAsInt());

                    items.add(giphy);
                }
                if (items.size()<limit) adapter.disableLoadMore();
                adapter.addItems(items);
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }

        });
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
