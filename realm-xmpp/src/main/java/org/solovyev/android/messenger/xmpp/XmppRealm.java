package org.solovyev.android.messenger.xmpp;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.User;

public class XmppRealm extends AbstractRealm<XmppRealmConfiguration> {

    public XmppRealm(@NotNull String id,
                     @NotNull RealmDef realmDef,
                     @NotNull User user,
                     @NotNull XmppRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }
}
