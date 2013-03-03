package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;
import org.solovyev.common.listeners.JEvent;

public interface RealmEvent extends JEvent {

    @Nonnull
    Realm getRealm();
}
