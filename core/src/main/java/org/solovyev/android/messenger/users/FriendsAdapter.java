package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:03 PM
 */
public class FriendsAdapter extends AbstractFriendsAdapter {

    public FriendsAdapter(@NotNull Context context, @NotNull User user) {
        super(context, user);
    }

    @Override
    protected void onListItemChanged(@NotNull User user, @NotNull User friend) {
    }

    @Override
    protected boolean canAddFriend(@NotNull User friend) {
        return true;
    }
}
