package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.AbstractMessengerListItemAdapter;

import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:24 PM
 */
public class OnlineContactsAsyncLoader extends AbstractAsyncLoader<User> {

    OnlineContactsAsyncLoader(@NotNull User user, @NotNull Context context, @NotNull ListItemArrayAdapter adapter, @Nullable Runnable onPostExecute) {
        super(user, context, adapter, onPostExecute);
    }

    @NotNull
    protected List<User> getElements(@NotNull Context context) {
        return getServiceLocator().getUserService().getOnlineUserContacts(getUser().getId(), context);
    }

    @Override
    protected Comparator<? super ListItem<? extends View>> getComparator() {
        return AbstractMessengerListItemAdapter.ListItemComparator.getInstance();
    }

    @NotNull
    @Override
    protected ListItem<?> createListItem(@NotNull User contact) {
        return new ContactListItem(getUser(), contact);
    }
}
