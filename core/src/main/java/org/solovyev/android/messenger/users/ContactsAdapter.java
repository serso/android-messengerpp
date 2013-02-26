package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:03 PM
 */
public class ContactsAdapter extends AbstractContactsAdapter {

    public ContactsAdapter(@NotNull Context context) {
        super(context);
    }

    @Override
    protected void onListItemChanged(@NotNull User user, @NotNull User contact) {
    }

    @Override
    protected boolean canAddContact(@NotNull User contact) {
        return true;
    }
}
