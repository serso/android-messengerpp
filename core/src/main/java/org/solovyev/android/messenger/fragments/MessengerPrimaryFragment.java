package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import org.solovyev.android.messenger.chats.MessengerChatsFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.MessengerRealmsFragment;
import org.solovyev.android.messenger.users.MessengerContactsFragment;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:04 PM
 */
public enum MessengerPrimaryFragment {

    contacts(MessengerContactsFragment.class, R.string.mpp_tab_contacts),
    messages(MessengerChatsFragment.class, R.string.mpp_tab_messages),
    realms(MessengerRealmsFragment.class, R.string.mpp_tab_realms);

    // todo serso: make settings a fragment
    // special logic for settings tab as it is not just a fragment
    /*settings;*/

    @Nonnull
    private final Class<? extends Fragment> fragmentClass;

    private final int titleResId;

    MessengerPrimaryFragment(@Nonnull Class<? extends Fragment> fragmentClass, int titleResId) {
        this.fragmentClass = fragmentClass;
        this.titleResId = titleResId;
    }

    @Nonnull
    public String getTag() {
        return this.name();
    }

    @Nonnull
    public Class<? extends Fragment> getFragmentClass() {
        return fragmentClass;
    }

    public int getTitleResId() {
        return titleResId;
    }
}
