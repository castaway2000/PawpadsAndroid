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

    public static class DataItem<T>{
        public final ObservableField<T> model=new ObservableField<>();
        public final ObservableField<Boolean> selected= new ObservableField<Boolean>(false);

        public DataItem(T item) {
            model.set(item);
        }

        public DataItem() {

        }
    }
    public interface Callback<T> {
        void onLoadMore();
        void onItemClick(T item);
        void onSelectMode(boolean selectMode);
    }


    ArrayList<DataItem<T>> items = new ArrayList<>();
    private boolean loadMoreEnabled = true;
    public final ObservableField<Boolean> selectMode = new ObservableField<>(false);

    public void removeItem(T removedItem) {
        Iterator<DataItem<T>> iterator = items.iterator();
        while (iterator.hasNext()){
            DataItem<T> item=iterator.next();
            if (item.model.get().equals(removedItem)){
                iterator.remove();
            }
        }
        notifyDataSetChanged();
    }

    public void updateItem(T updatedItem) {
        Iterator<DataItem<T>> iterator = items.iterator();
        while (iterator.hasNext()){
            DataItem<T> item=iterator.next();
            if (item.model.get().equals(updatedItem)){
                item.model.set(updatedItem);
            }
        }
        notifyDataSetChanged();
    }






    private Callback callback;
    boolean isBusy = false;

    public abstract DataHolder<T> getItemHolder(ViewGroup parent);


    public abstract static class DataHolder<T> extends RecyclerView.ViewHolder{

        protected View view;
        protected DataItem<T> data;

        public DataHolder(View v, final BaseListAdapter<T> adapter){
            super(v);
            this.view=v;
            final Callback<T> callback=adapter.getCallback();
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(callback!=null && data!=null ){
                        callback.onItemClick(data.model.get());
                    }
                }
            });
        }
        public void setData(DataItem<T> data){
            this.data=data;
        }

        public abstract void showData(DataItem<T> model);
    }

    public static class LoadMoreHolder extends RecyclerView.ViewHolder {
        public LoadMoreHolder(View itemView) {
            super(itemView);
        }
    }




    @Override
    public int getItemViewType(int position) {
        if (position < items.size()) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return getItemHolder(parent);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.load_more, parent, false);
            return new LoadMoreHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadMoreHolder){
            if (callback != null && !isBusy && loadMoreEnabled) {
                isBusy = true;
                callback.onLoadMore();
            }
        }else{
            DataHolder<T> dataholder=(DataHolder<T>)holder;
            DataItem<T> item=items.get(position);
            dataholder.setData(item);
            dataholder.showData(item);

        }
    }


    @Override
    public int getItemCount() {
        if (!loadMoreEnabled || callback==null || items.size()==0) {
            return items.size();
        } else {
            return items.size() + 1;
        }
    }

    public void setCallback(Callback<T> callback) {
        this.callback = callback;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public void addItems(List<T> items) {
        for(T item:items){
            DataItem<T> dataItem=new DataItem<>();
            dataItem.model.set(item);
            this.items.add(dataItem);
        }
        isBusy=false;
        notifyDataSetChanged();
    }

    public void addItem(T data) {
        DataItem<T> item=new DataItem<>();
        item.model.set(data);
        this.items.add(item);
        isBusy=false;
        //Collections.sort(items, Collections.<Jogging>reverseOrder());
        notifyDataSetChanged();

    }

    public void disableLoadMore(){
        loadMoreEnabled =false;
        isBusy=false;
        notifyDataSetChanged();
    }

    public ArrayList<T> getSelected(){
        ArrayList<T> out=new ArrayList<>();
        for(DataItem<T> item:items){
            if (item.selected.get()){
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
        while (iterator.hasNext()){
            DataItem<T> item=iterator.next();
            if(item.selected.get()){
                iterator.remove();
            }

        }
        notifyDataSetChanged();
    }

    public Callback<T> getCallback() {
        return callback;
    }
}
