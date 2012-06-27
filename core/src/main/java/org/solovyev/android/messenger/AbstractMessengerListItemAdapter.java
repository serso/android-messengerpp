package org.solovyev.android.messenger;

import android.content.Context;
import android.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventListener;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.common.utils.CompareTools;

import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:58 PM
 */
public class AbstractMessengerListItemAdapter extends ListItemArrayAdapter implements UserEventListener {

    @NotNull
    private User user;

    private boolean initialized = false;

    @Nullable
    private CharSequence filterText;

    public AbstractMessengerListItemAdapter(@NotNull Context context, @NotNull List<ListItem<? extends View>> listItems, @NotNull User user) {
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

    protected void addListItem(@NotNull ListItem<?> listItem) {
        this.add(listItem);

        final Comparator<? super ListItem<? extends View>> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    protected void removeListItem(@NotNull ListItem<?> listItem) {
        this.remove(listItem);

        final Comparator<? super ListItem<? extends View>> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    protected void addListItems(@NotNull List<ListItem<?>> listItems) {
        this.addAll(listItems);

        final Comparator<? super ListItem<? extends View>> comparator = getComparator();
        if (comparator != null) {
            sort(comparator);
        }

        filter(filterText);
    }

    @Nullable
    protected Comparator<? super ListItem<? extends View>> getComparator() {
        return ListItemComparator.getInstance();
    }

    // todo serso: move to ListAdapter
    public void filter(@Nullable CharSequence filterText) {
        this.filterText = filterText;
        this.getFilter().filter(filterText);
    }

    public static final class ListItemComparator implements Comparator<ListItem<?>> {

        @NotNull
        private static final ListItemComparator instance = new ListItemComparator();

        @NotNull
        public static ListItemComparator getInstance() {
            return instance;
        }

        @Override
        public int compare(ListItem<?> lhs, ListItem<?> rhs) {
            return CompareTools.comparePreparedObjects(lhs.toString(), rhs.toString());
        }
    }
}
