package saberapplications.pawpads.views;

import android.databinding.ObservableField;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import saberapplications.pawpads.R;

/**
 * Created by Stanislav Volnjanskij on 29.08.16.
 */

public abstract class BaseListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int DATA_ITEM = 2;
    protected static final int LOADMORE_ITEM = 1;
    protected static final int INITIAL_LOAD_ITEM = 3;
    protected static final int EMPTY_STATE_ITEM = 4;
    protected boolean showInitialLoad = true;
    private boolean loadMoreEnabled = true;
    boolean isBusy = true;

    public static class DataItem<T> {
        public final ObservableField<T> model = new ObservableField<>();
        public final ObservableField<Boolean> selected = new ObservableField<Boolean>(false);

        public DataItem(T item) {
            model.set(item);
        }

        public DataItem() {

        }
    }

    public interface Callback<T> {
        void onLoadMore();

        void onItemClick(T item);
    }


    protected ArrayList<DataItem<T>> items = new ArrayList<>();

    public final ObservableField<Boolean> selectMode = new ObservableField<>(false);

    public void removeItem(T removedItem) {
        Iterator<DataItem<T>> iterator = items.iterator();
        while (iterator.hasNext()) {
            DataItem<T> item = iterator.next();
            if (item.model.get().equals(removedItem)) {
                iterator.remove();
            }
        }
        notifyDataSetChanged();
    }

    public void updateItem(T updatedItem) {
        Iterator<DataItem<T>> iterator = items.iterator();
        while (iterator.hasNext()) {
            DataItem<T> item = iterator.next();
            if (item.model.get().equals(updatedItem)) {
                item.model.set(updatedItem);
            }
        }
        notifyDataSetChanged();
    }


    private Callback callback;


    public abstract DataHolder<T> getItemHolder(ViewGroup parent);


    public abstract static class DataHolder<T> extends RecyclerView.ViewHolder {

        protected View view;
        protected DataItem<T> data;
        protected BaseListAdapter<T> adapter;

        public DataHolder(View v, final BaseListAdapter<T> adapter) {
            super(v);
            this.view = v;
            this.adapter = adapter;
            bindEvents();
        }

        protected void bindEvents() {
            final Callback<T> callback = adapter.getCallback();
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null && data != null) {
                        callback.onItemClick(data.model.get());
                    }
                }
            });
        }

        public void setData(DataItem<T> data) {
            this.data = data;
        }

        public abstract void showData(DataItem<T> model,int position);
    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        public LoadMoreHolder(View itemView) {
            super(itemView);
        }
    }

    public static class InitialLoadHolder extends RecyclerView.ViewHolder {
        public InitialLoadHolder(View itemView) {
            super(itemView);
        }
    }

    public static class EmptyStateHolder extends RecyclerView.ViewHolder {
        public EmptyStateHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.size() == 0) {
            if (loadMoreEnabled && showInitialLoad) return INITIAL_LOAD_ITEM;
            else if (!isBusy) return EMPTY_STATE_ITEM;
        } else {
            if (position < items.size()) {
                return DATA_ITEM;
            } else {
                return LOADMORE_ITEM;
            }
        }
        return EMPTY_STATE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case DATA_ITEM:
                return getItemHolder(parent);
            case LOADMORE_ITEM:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.load_more, parent, false);
                return new LoadMoreHolder(v);
            case INITIAL_LOAD_ITEM:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.initial_load, parent, false);
                return new InitialLoadHolder(v);
            case EMPTY_STATE_ITEM:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(getEmptyStateResId(), parent, false);
                return new EmptyStateHolder(v);
        }
        return null;
    }

    protected int getEmptyStateResId() {
        return R.layout.empty_state;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadMoreHolder) {
            if (callback != null && !isBusy && loadMoreEnabled) {
                isBusy = true;
                callback.onLoadMore();
            }
        } else if (holder instanceof DataHolder) {
            DataHolder<T> dataholder = (DataHolder<T>) holder;
            DataItem<T> item = items.get(position);
            dataholder.setData(item);
            dataholder.showData(item,position);

        }
    }


    @Override
    public int getItemCount() {
        if(items.size()==0) return 1;
        if (isBusy && !showInitialLoad){
            return 0;
        }
        if (loadMoreEnabled) {
            return items.size() + 1;
        }else{
            return items.size();
        }
    }

    public void setCallback(Callback<T> callback) {
        this.callback = callback;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void addItems(List<T> items) {

        for (T item : items) {
            DataItem<T> dataItem = new DataItem<>();
            dataItem.model.set(item);
            this.items.add(dataItem);
        }
        isBusy = false;
        notifyItemRangeInserted(this.items.size(),items.size());
    }

    public void addItem(T data) {
        DataItem<T> item = new DataItem<>();
        item.model.set(data);
        this.items.add(item);
        isBusy = false;
        //Collections.sort(items, Collections.<Jogging>reverseOrder());
        notifyItemChanged(this.items.size()-1);

    }

    public void disableLoadMore() {
        loadMoreEnabled = false;
        isBusy = false;
        notifyDataSetChanged();
    }

    public ArrayList<T> getSelected() {
        ArrayList<T> out = new ArrayList<>();
        for (DataItem<T> item : items) {
            if (item.selected.get()) {
                out.add(item.model.get());
            }
        }
        return out;
    }

    public void clear() {
        items.clear();
        loadMoreEnabled = true;
        notifyDataSetChanged();
    }

    public void removeSelected() {
        Iterator<DataItem<T>> iterator = items.iterator();
        while (iterator.hasNext()) {
            DataItem<T> item = iterator.next();
            if (item.selected.get()) {
                iterator.remove();
            }

        }
        notifyDataSetChanged();
    }

    public Callback<T> getCallback() {
        return callback;
    }

    public boolean isShowInitialLoad() {
        return showInitialLoad;
    }

    public void setShowInitialLoad(boolean showInitialLoad) {
        this.showInitialLoad = showInitialLoad;
    }
    public List<T> getItems(){
        ArrayList<T> out=new ArrayList<>();
        for (DataItem<T> item:items
        ){
            out.add(item.model.get());
        }
        return out;
    }
}
