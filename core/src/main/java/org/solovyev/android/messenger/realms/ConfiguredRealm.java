package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

public interface ConfiguredRealm {

    @NotNull
    Realm getRealm();
}
