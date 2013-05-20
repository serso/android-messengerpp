package org.solovyev.android.messenger.realms.xmpp;

import android.app.Application;
import android.content.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jivesfotware.smackx.enitycaps.provider.MessengerCapsExtensionProvider;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.*;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 2/24/13
 * Time: 8:09 PM
 */
@Singleton
public final class XmppRealmDef extends AbstractRealmDef {

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
	public Realm newRealm(@Nonnull String realmId, @Nonnull User user, @Nonnull RealmConfiguration configuration, @Nonnull RealmState state) {
		return new XmppRealm(realmId, this, user, (XmppRealmConfiguration) configuration, state);
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
			if (name.equals(User.PROPERTY_NICKNAME)) {
				addUserProperty(context, result, R.string.mpp_nickname, property.getValue());
			}
		}

		return result;
	}

	@Nonnull
	@Override
	public RealmIconService getRealmIconService() {
		return new XmppRealmIconService(context, R.drawable.mpp_icon_user_empty, R.drawable.mpp_icon_users);
	}

	@Nullable
	@Override
	public Cipherer<RealmConfiguration, RealmConfiguration> getCipherer() {
		return new XmppRealmConfigurationCipherer(MessengerApplication.getServiceLocator().getSecurityService().getStringSecurityService().getCipherer());
	}

    /*
    **********************************************************************
    *
    *                           STATIC/INNER CLASSES
    *
    **********************************************************************
    */

	private static class XmppRealmConfigurationCipherer implements Cipherer<RealmConfiguration, RealmConfiguration> {

		@Nonnull
		private final Cipherer<String, String> stringCipherer;

		private XmppRealmConfigurationCipherer(@Nonnull Cipherer<String, String> stringCipherer) {
			this.stringCipherer = stringCipherer;
		}

		@Nonnull
		@Override
		public RealmConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull RealmConfiguration decrypted) throws CiphererException {
			return encrypt(secret, (XmppRealmConfiguration) decrypted);
		}

		@Nonnull
		public RealmConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull XmppRealmConfiguration decrypted) throws CiphererException {
			final XmppRealmConfiguration encrypted = decrypted.clone();
			encrypted.setPassword(stringCipherer.encrypt(secret, decrypted.getPassword()));
			return encrypted;
		}

		@Nonnull
		@Override
		public RealmConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull RealmConfiguration encrypted) throws CiphererException {
			return decrypt(secret, (XmppRealmConfiguration) encrypted);
		}

		@Nonnull
		public RealmConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull XmppRealmConfiguration encrypted) throws CiphererException {
			final XmppRealmConfiguration decrypted = encrypted.clone();
			decrypted.setPassword(stringCipherer.decrypt(secret, encrypted.getPassword()));
			return decrypted;
		}
	}
}
