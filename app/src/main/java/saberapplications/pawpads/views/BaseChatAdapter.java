package saberapplications.pawpads.views;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Stanislav Volnjanskij on 26.10.16.
 */

public abstract class BaseChatAdapter<T> extends BaseListAdapter<T> {

    protected static final int VIEW_TYPE_MY_MESSAGE = 100;
    private static final int VIEW_TYPE_SOMEONE_MESSAGE = 101;
    private static final int ZERO_POSITION = 0;

    /**
     * <p>Return item type.</p>
     * <p>If the parent type of View {@link BaseListAdapter#DATA_ITEM} and {@link BaseChatAdapter#getMessageSelf(int)} is true
     * return {@link BaseChatAdapter#VIEW_TYPE_MY_MESSAGE} else return {@link BaseChatAdapter#VIEW_TYPE_SOMEONE_MESSAGE}</p>
     *
     * @param position item position
     *
     * @return View type
     * @see BaseListAdapter#getItemCount()
     * @see BaseListAdapter#getItemViewType(int)
     * @see BaseChatAdapter#VIEW_TYPE_MY_MESSAGE
     * @see BaseChatAdapter#VIEW_TYPE_SOMEONE_MESSAGE
     */
    @Override
    public int getItemViewType(int position) {
        int itemType = super.getItemViewType(position);
        if (super.getItemViewType(position) == DATA_ITEM) {
            itemType = getMessageSelf(position) ?
                    VIEW_TYPE_MY_MESSAGE : VIEW_TYPE_SOMEONE_MESSAGE;
        }
        return itemType;
    }

    /**
     * Returns boolean isSefl value the to set a type of View
     *
     * @param position item position
     *
     * @return message sender value. If useDefault is false means,
     *          that the message is from someone. If true - user message.
     */
    abstract public boolean getMessageSelf(int position);

    @Override
    public DataHolder<T> getItemHolder(ViewGroup parent) {
        // not needed
        return null;
    }

    /**
     * Returns the right item view holder.
     *
     * @param parent The parent view group
     *
     * @return Item view holder
     */
    abstract public DataHolder<T> getChatItemHolderLeft(ViewGroup parent);

    /**
     * Returns the left item view holder.
     *
     * @param parent The parent view group
     *
     * @return Item view holder
     */
    abstract public DataHolder<T> getChatItemHolderRight(ViewGroup parent);

    /**
     * <p>Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.</p>
     * <p>Returns view holder received by
     * {@link BaseChatAdapter#getChatItemHolderRight(ViewGroup)}
     * if the view type is {@link BaseChatAdapter#VIEW_TYPE_MY_MESSAGE} - for right-oriented layout
     * or {@link BaseChatAdapter#getChatItemHolderLeft(ViewGroup)}
     * if the view type is {@link BaseChatAdapter#VIEW_TYPE_SOMEONE_MESSAGE} - for left-oriented layout</p>
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return {@link RecyclerView.ViewHolder} which corresponds to viewType
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
        if (holder == null) {
            if (viewType == VIEW_TYPE_MY_MESSAGE) {
                return getChatItemHolderRight(parent);
            } else {
                return getChatItemHolderLeft(parent);
            }
        }
        return holder;
    }

    /**
     * Add a new message down of the chat
     *
     * @param data The new item
     */
    @Override
    public void addItem(T data) {
        DataItem<T> item = new DataItem<>();
        item.model.set(data);
        this.items.add(ZERO_POSITION, item);
        isBusy = false;
        notifyDataSetChanged();
    }

    @Override
    public void addItems(List<T> items) {
        for (T item : items) {
            DataItem<T> dataItem = new DataItem<>();
            dataItem.model.set(item);
            this.items.add(dataItem);
        }
        isBusy = false;
        notifyDataSetChanged();
    }
}