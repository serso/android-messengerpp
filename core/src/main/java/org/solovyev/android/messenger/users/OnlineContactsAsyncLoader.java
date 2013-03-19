package org.solovyev.android.messenger.users;

import android.content.Context;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:24 PM
 */
public class OnlineContactsAsyncLoader extends AbstractAsyncLoader<UserContact, ContactListItem> {

    @Nonnull
    private final RealmService realmService;

    OnlineContactsAsyncLoader(@Nonnull Context context,
                              @Nonnull ListItemArrayAdapter<ContactListItem> adapter,
                              @Nullable Runnable onPostExecute,
                              @Nonnull RealmService realmService) {
        super(context, adapter, onPostExecute);
        this.realmService = realmService;
    }

    @Nonnull
    protected List<UserContact> getElements(@Nonnull Context context) {
        final List<UserContact> result = new ArrayList<UserContact>();

        for (User user : realmService.getRealmUsers()) {
            for (User contact : MessengerApplication.getServiceLocator().getUserService().getOnlineUserContacts(user.getEntity())) {
                result.add(new UserContact(user, contact));
            }
        }

        return result;
    }

    @Override
    protected Comparator<? super ContactListItem> getComparator() {
        return MessengerListItemAdapter.ListItemComparator.getInstance();
    }

    @Nonnull
    @Override
    protected ContactListItem createListItem(@Nonnull UserContact userContact) {
        return new ContactListItem(userContact.getContact(), realmService);
    }
}
