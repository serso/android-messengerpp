package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.common.listeners.JEvent;

public interface RealmEvent extends JEvent {

    @NotNull
    Realm getRealm();
}
