package org.solovyev.android.messenger.realms;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 9:16 PM
 */
public class RealmRemovedEvent extends AbstractRealmEvent {

    public RealmRemovedEvent(@Nonnull Realm realm) {
        super(realm);
    }
}
