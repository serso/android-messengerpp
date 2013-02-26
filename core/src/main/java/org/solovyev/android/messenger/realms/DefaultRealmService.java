package org.solovyev.android.messenger.realms;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerConfiguration;

import java.util.*;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 1:02 AM
 */
@Singleton
public class DefaultRealmService implements RealmService {

    @NotNull
    private final Map<String, RealmDef> realmDefs = new HashMap<String, RealmDef>();

    @NotNull
    private final Map<String, Realm> realms = new HashMap<String, Realm>();

    @Inject
    public DefaultRealmService(@NotNull MessengerConfiguration configuration) {
        this(Arrays.asList(configuration.getRealm()));
    }

    public DefaultRealmService(@NotNull List<? extends RealmDef> realmDefs) {
        for (RealmDef realmDef : realmDefs) {
            this.realmDefs.put(realmDef.getId(), realmDef);
        }
    }

    @NotNull
    @Override
    public Collection<RealmDef> getRealmDefs() {
        return Collections.unmodifiableCollection(this.realmDefs.values());
    }

    @NotNull
    @Override
    public Collection<Realm> getRealms() {
        return Collections.unmodifiableCollection(this.realms.values());
    }

    @NotNull
    @Override
    public RealmDef getRealmDefById(@NotNull String realmDefId) throws UnsupportedRealmException {
        final RealmDef realm = this.realmDefs.get(realmDefId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmDefId);
        }
        return realm;
    }

    @NotNull
    @Override
    public Realm getRealmById(@NotNull String realmId) throws UnsupportedRealmException {
        final Realm realm = this.realms.get(realmId);
        if ( realm == null ) {
            throw new UnsupportedRealmException(realmId);
        }
        return realm;
    }
}
