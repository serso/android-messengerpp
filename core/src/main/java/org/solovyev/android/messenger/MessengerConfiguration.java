package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import org.solovyev.android.messenger.realms.RealmDef;

import java.util.Collection;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:24 PM
 */
public interface MessengerConfiguration {

    @Nonnull
    Collection<RealmDef> getRealmDefs();

}
