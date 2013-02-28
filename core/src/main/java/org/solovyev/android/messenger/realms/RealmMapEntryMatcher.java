package org.solovyev.android.messenger.realms;

import com.google.common.base.Predicate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
* User: serso
* Date: 2/28/13
* Time: 9:00 PM
*/
public class RealmMapEntryMatcher implements Predicate<Map.Entry<RealmEntity, ?>> {

    @NotNull
    private final String realmId;

    private RealmMapEntryMatcher(@NotNull String realmId) {
        this.realmId = realmId;
    }

    @NotNull
    public static RealmMapEntryMatcher forRealm(@NotNull String realmId) {
        return new RealmMapEntryMatcher(realmId);
    }

    @Override
    public boolean apply(@javax.annotation.Nullable Map.Entry<RealmEntity, ?> entry) {
        return entry != null && entry.getKey().getRealmId().equals(realmId);
    }
}
