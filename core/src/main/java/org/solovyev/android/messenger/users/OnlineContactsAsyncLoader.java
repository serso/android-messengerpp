package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.AbstractAsyncLoader;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:24 PM
 */
public class OnlineContactsAsyncLoader extends AbstractAsyncLoader<UserContact, ContactListItem> {

    @NotNull
    private final RealmService realmService;

    OnlineContactsAsyncLoader(@NotNull Context context,
                              @NotNull ListItemArrayAdapter<ContactListItem> adapter,
                              @Nullable Runnable onPostExecute,
                              @NotNull RealmService realmService) {
        super(context, adapter, onPostExecute);
        this.realmService = realmService;
    }

    @NotNull
    protected List<UserContact> getElements(@NotNull Context context) {
        final List<UserContact> result = new ArrayList<UserContact>();

        for (Realm realm : realmService.getRealms()) {
            final User user = realm.getUser();
            for (User contact : MessengerApplication.getServiceLocator().getUserService().getOnlineUserContacts(user.getRealmUser())) {
                result.add(new UserContact(user, contact));
            }
        }

        return result;
    }

    @Override
    protected Comparator<? super ContactListItem> getComparator() {
        return MessengerListItemAdapter.ListItemComparator.getInstance();
    }

    @NotNull
    @Override
    protected ContactListItem createListItem(@NotNull UserContact userContact) {
        return new ContactListItem(userContact.getUser(), userContact.getContact());
    }
}
