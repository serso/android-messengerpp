package org.solovyev.android.messenger;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Checkable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.common.compare.CompareTools;

import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:58 PM
 */
public class AbstractMessengerListItemAdapter<LI extends ListItem> extends ListItemArrayAdapter<LI> implements UserEventListener {

    @NotNull
    private User user;

    private boolean initialized = false;

    @Nullable
    private CharSequence filterText;

    @Nullable
    private ListItem selectedItem = null;
    private int selectedItemPosition = -1;

    @NotNull
    private final AdapterView.OnItemClickListener selectedItemListener = new SelectedItemListener();

    public AbstractMessengerListItemAdapter(@NotNull Context context, @NotNull List<? extends LI> listItems, @NotNull User user) {
        super(context, listItems);
        this.user = user;
    }

    @NotNull
    protected User getUser() {
        return user;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        if (userEventType == UserEventType.changed) {
            if (eventUser.equals(user)) {
                user = eventUser;
            }
        }
    }

    protected void addListItem(@NotNull LI listItem) {
        this.add(listItem);

        final Comparator<? super LI> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    protected void removeListItem(@NotNull LI listItem) {
        this.remove(listItem);

        final Comparator<? super LI> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    protected void addListItems(@NotNull List<LI> listItems) {
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

        @NotNull
        private static final ListItemComparator instance = new ListItemComparator();

        @NotNull
        public static ListItemComparator getInstance() {
            return instance;
        }

        @Override
        public int compare(ListItem lhs, ListItem rhs) {
            return CompareTools.comparePreparedObjects(lhs.toString(), rhs.toString());
        }
    }

    @NotNull
    public AdapterView.OnItemClickListener getSelectedItemListener() {
        return selectedItemListener;
    }

    private final class SelectedItemListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final ListItem selectedItem = getItem(position);
            if (AbstractMessengerListItemAdapter.this.selectedItem != selectedItem) {
                selectItem(selectedItem, true);
                selectItem(AbstractMessengerListItemAdapter.this.selectedItem, false);

                AbstractMessengerListItemAdapter.this.selectedItem = selectedItem;
                AbstractMessengerListItemAdapter.this.selectedItemPosition = position;
            }

            notifyDataSetChanged();
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
