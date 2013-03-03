package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
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

    @Nonnull
    private final List<RealmDef> realmDefs = new ArrayList<RealmDef>();

    @Inject
    @Nonnull
    private XmppRealmDef xmppRealmDef;

    @Inject
    @Nonnull
    private VkRealmDef vkRealmDef;

    public MessengerConfigurationImpl() {
    }

    @Nonnull
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
