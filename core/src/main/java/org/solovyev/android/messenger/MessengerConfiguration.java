package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.RealmDef;

import java.util.Collection;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:24 PM
 */
public interface MessengerConfiguration {

    @NotNull
    Collection<RealmDef> getRealmDefs();

}
