package org.solovyev.android.messenger.realms.xmpp;

import android.app.Application;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jivesfotware.smackx.enitycaps.provider.MessengerCapsExtensionProvider;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.AbstractRealmDef;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:09 PM
 */
@Singleton
public class XmppRealmDef extends AbstractRealmDef {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    public static final String USER_PROPERTY_AVATAR_HASH = "avatar_hash";
    public static final String USER_PROPERTY_AVATAR_BASE64 = "avatar_base64";

    @Nonnull
    static final String REALM_ID = "xmpp";

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private Application context;

    public XmppRealmDef() {
        super(REALM_ID, R.string.mpp_xmpp_name, R.drawable.mpp_xmpp_icon, XmppRealmConfigurationFragment.class, XmppRealmConfiguration.class, true);
    }

    @Nonnull
    @Override
    public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration) {
        return new XmppRealm(realmId, this, user, (XmppRealmConfiguration) configuration);
    }

    @Override
    @Nonnull
    public RealmBuilder newRealmBuilder(@Nonnull RealmConfiguration configuration, @Nullable Realm editedRealm) {
        return new XmppRealmBuilder(this, editedRealm, (XmppRealmConfiguration) configuration);
    }

    @Override
    public void init(@Nonnull Context context) {
        super.init(context);

        SmackAndroid.init(context);
        SmackConfiguration.setPacketReplyTimeout(300000);
        ProviderManager.getInstance().addExtensionProvider("c", "http://jabber.org/protocol/caps", new MessengerCapsExtensionProvider());
    }


    @Nonnull
    @Override
    public List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context) {
        final List<AProperty> result = new ArrayList<AProperty>(user.getProperties().size());

        for (AProperty property : user.getProperties()) {
            final String name = property.getName();
            if ( name.equals(User.PROPERTY_NICKNAME) ) {
                result.add(Properties.newProperty(context.getString(R.string.mpp_nickname), property.getValue()));
            }
        }

        return result;
    }

    @Nonnull
    @Override
    public RealmIconService getRealmIconService() {
        return new XmppRealmIconService(context, R.drawable.mpp_icon_user_empty);
    }
}
