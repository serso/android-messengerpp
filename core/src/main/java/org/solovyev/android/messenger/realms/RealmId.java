package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;
import org.solovyev.common.JCloneable;

/**
 * User: serso
 * Date: 7/19/12
 * Time: 5:28 PM
 */
public interface RealmId<I> extends JCloneable<RealmId<I>> {

    @NotNull
    String getRealm();

    @NotNull
    I getId();
}
