package saberapplications.pawpads.views.giphyselector;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import saberapplications.pawpads.R;
import saberapplications.pawpads.databinding.GiphyItemBinding;
import saberapplications.pawpads.views.BaseListAdapter;

/**
 * Created by Stanislav Volnjanskij on 2/14/17.
 */

public class GiphyListAdapter extends BaseListAdapter<Giphy> {

    public static class GiphyHolder extends DataHolder<Giphy> implements RequestListener<String, GifDrawable> {

        GiphyItemBinding binding;

        public GiphyHolder(View v, BaseListAdapter<Giphy> adapter) {
            super(v, adapter);
            binding = DataBindingUtil.bind(v);
        }

        @Override
        public void showData(DataItem<Giphy> data, int position) {
            binding.progressBar.setVisibility(View.VISIBLE);
            Glide.clear(binding.imageView);
            ViewGroup.LayoutParams params = binding.imageView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            binding.imageView.setLayoutParams(params);

            Glide.with(binding.imageView.getContext())
                    .load(data.model.get()
                    .getThumb().getUrl()).asGif()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(this)
                    //.centerCrop()
                    .into(binding.imageView);

        }

        @Override
        public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            binding.progressBar.setVisibility(View.GONE);
            return false;
        }
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {

    }

    protected int getEmptyStateResId() {
        return R.layout.giphy_selector_empty_state;
    }

    @Override
    public DataHolder<Giphy> getItemHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.giphy_item, parent, false);
        return new GiphyHolder(v,this);
    }
}
