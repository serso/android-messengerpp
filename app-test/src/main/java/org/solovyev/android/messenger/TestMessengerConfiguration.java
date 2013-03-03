package org.solovyev.android.messenger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.TestRealmDef;
import org.solovyev.android.messenger.vk.VkRealmDef;
import org.solovyev.android.messenger.xmpp.XmppRealmDef;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: serso
 * Date: 2/27/13
 * Time: 9:42 PM
 */
@Singleton
public class TestMessengerConfiguration implements MessengerConfiguration {

    @Inject
    @Nonnull
    private TestRealmDef testRealmDef;

    @Inject
    @Nonnull
    private VkRealmDef vkRealmDef;

    @Inject
    @Nonnull
    private XmppRealmDef xmppRealmDef;

    @Nonnull
    @Override
    public Collection<RealmDef> getRealmDefs() {
        return Arrays.<RealmDef>asList(testRealmDef, vkRealmDef, xmppRealmDef);
    }
}
