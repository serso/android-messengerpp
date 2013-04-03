package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import android.widget.Checkable;
import android.widget.Filter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.common.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:58 PM
 */
public class MessengerListItemAdapter<LI extends ListItem> extends ListItemAdapter<LI> /*implements UserEventListener*/ {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    @Nonnull
    private static final String POSITION = "position";

    private static final int NOT_SELECTED = -1;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    private boolean initialized = false;

    @Nullable
    private CharSequence filterText;

    @Nullable
    private ListItem selectedItem = null;

    private int selectedItemPosition = NOT_SELECTED;

    @Nonnull
    private final SelectedItemListener selectedItemListener = new SelectedItemListener();

    public MessengerListItemAdapter(@Nonnull Context context, @Nonnull List<? extends LI> listItems) {
        super(context, listItems);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /*@Override*/
    public void onEvent(@Nonnull UserEvent event) {
    }

    protected void addListItem(@Nonnull LI listItem) {
        this.add(listItem);

        final Comparator<? super LI> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    protected void removeListItem(@Nonnull LI listItem) {
        this.remove(listItem);

        final Comparator<? super LI> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    protected void addListItems(@Nonnull List<LI> listItems) {
        this.addAll(listItems);

        final Comparator<? super LI> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    @Nullable
    protected Comparator<? super LI> getComparator() {
        return ListItemComparator.getInstance();
    }

    // todo serso: move to ListAdapter
    public void filter(@Nullable CharSequence filterText) {
        this.filterText = filterText;
        this.getFilter().filter(filterText);
    }

    public void filter(@Nullable CharSequence filterText, @Nullable Filter.FilterListener listener) {
        this.filterText = filterText;
        this.getFilter().filter(filterText, listener);
    }


    public void refilter() {
        this.getFilter().filter(filterText);
    }

    public void saveState(@Nonnull Bundle outState) {
        final int selectedItemPosition = this.getSelectedItemPosition();
        if (selectedItemPosition != NOT_SELECTED) {
            outState.putInt(POSITION, selectedItemPosition);
        }
    }

    public int loadState(@Nonnull Bundle savedInstanceState, int defaultPosition) {
        return savedInstanceState.getInt(POSITION, defaultPosition);
    }

    public static final class ListItemComparator implements Comparator<ListItem> {

        @Nonnull
        private static final ListItemComparator instance = new ListItemComparator();

        @Nonnull
        public static ListItemComparator getInstance() {
            return instance;
        }

        @Override
        public int compare(ListItem lhs, ListItem rhs) {
            return Objects.compare(lhs.toString(), rhs.toString());
        }
    }

    @Nonnull
    public SelectedItemListener getSelectedItemListener() {
        return selectedItemListener;
    }

    public final class SelectedItemListener {

        public void onItemClick(int position) {
            final LI selectedItem = getItem(position);
            onItemClick(position, selectedItem);
        }

        private void onItemClick(int position, @Nonnull LI selectedItem) {
            if (MessengerListItemAdapter.this.selectedItem != selectedItem) {
                selectItem(selectedItem, true);
                selectItem(MessengerListItemAdapter.this.selectedItem, false);

                MessengerListItemAdapter.this.selectedItem = selectedItem;
                MessengerListItemAdapter.this.selectedItemPosition = position;

                notifyDataSetChanged();
            }
        }

        public int onItemClick(@Nonnull ListItem selectedItem) {
            final LI selectedListItem = (LI) selectedItem;
            final int position = getPosition(selectedListItem);
            if (position >= 0) {
                onItemClick(position);
                return position;
            } else {
                return NOT_SELECTED;
            }
        }

        private void selectItem(@Nullable ListItem item, boolean selected) {
            if ( item instanceof Checkable) {
                ((Checkable) item).setChecked(selected);
            }
        }
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }
}
