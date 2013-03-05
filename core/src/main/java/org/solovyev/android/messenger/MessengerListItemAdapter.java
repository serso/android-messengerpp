package org.solovyev.android.messenger;

import android.content.Context;
import android.widget.Checkable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
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
public class MessengerListItemAdapter<LI extends ListItem> extends ListItemArrayAdapter<LI> implements UserEventListener {

    private boolean initialized = false;

    @Nullable
    private CharSequence filterText;

    @Nullable
    private ListItem selectedItem = null;
    private int selectedItemPosition = -1;

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

    @Override
    public void onUserEvent(@Nonnull User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data) {
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

    public void refilter() {
        this.getFilter().filter(filterText);
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
            if (MessengerListItemAdapter.this.selectedItem != selectedItem) {
                selectItem(selectedItem, true);
                selectItem(MessengerListItemAdapter.this.selectedItem, false);

                MessengerListItemAdapter.this.selectedItem = selectedItem;
                MessengerListItemAdapter.this.selectedItemPosition = position;

                notifyDataSetChanged();
            }
        }

        public void onItemClick(@Nonnull ListItem selectedItem) {

            final int position = getPosition((LI) selectedItem);
            if (MessengerListItemAdapter.this.selectedItem != selectedItem) {
                selectItem(selectedItem, true);
                selectItem(MessengerListItemAdapter.this.selectedItem, false);

                MessengerListItemAdapter.this.selectedItem = selectedItem;
                MessengerListItemAdapter.this.selectedItemPosition = position;

                notifyDataSetChanged();
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
