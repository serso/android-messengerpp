package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import com.google.inject.Singleton;
import org.jivesfotware.smackx.enitycaps.provider.MessengerCapsExtensionProvider;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.solovyev.android.messenger.realms.AbstractRealmDef;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmBuilder;
import org.solovyev.android.messenger.realms.RealmConfiguration;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.properties.APropertyImpl;
import org.solovyev.android.security.base64.ABase64StringDecoder;

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
                result.add(APropertyImpl.newInstance(context.getString(R.string.mpp_nickname), property.getValue()));
            }
        }

        return result;
    }

    @Nullable
    @Override
    public BitmapDrawable getUserIcon(@Nonnull User user) {
        BitmapDrawable result = null;

        final String userIconBase64 = user.getPropertyValueByName(USER_PROPERTY_AVATAR_BASE64);
        if ( userIconBase64 != null ) {
            try {
                final byte[] userIconBytes = ABase64StringDecoder.getInstance().convert(userIconBase64);
                result = new BitmapDrawable(BitmapFactory.decodeByteArray(userIconBytes, 0, userIconBytes.length));
            } catch (IllegalArgumentException e) {
                Log.e("XmppRealmDef", e.getMessage(), e);
            }
        }

        return result;
    }
}
