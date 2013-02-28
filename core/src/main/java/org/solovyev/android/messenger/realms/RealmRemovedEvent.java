package org.solovyev.android.messenger.realms;

import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 9:16 PM
 */
public class RealmRemovedEvent extends AbstractRealmEvent {

    public RealmRemovedEvent(@NotNull Realm realm) {
        super(realm);
    }
}
