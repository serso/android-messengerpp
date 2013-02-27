package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.vk.VkRealmDef;
import org.solovyev.android.messenger.xmpp.XmppRealmDef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:36 PM
 */
@Singleton
public class MessengerConfigurationImpl implements MessengerConfiguration {

    @NotNull
    private final List<RealmDef> realmDefs = new ArrayList<RealmDef>();

    @Inject
    @NotNull
    private XmppRealmDef xmppRealmDef;

    @Inject
    @NotNull
    private VkRealmDef vkRealmDef;

    public MessengerConfigurationImpl() {
    }

    @NotNull
    @Override
    public Collection<RealmDef> getRealmDefs() {
        synchronized (realmDefs) {
            if (realmDefs.isEmpty()) {
                realmDefs.add(xmppRealmDef);
                realmDefs.add(vkRealmDef);
            }
        }

        return this.realmDefs;
    }
}
