package org.solovyev.android.messenger.realms.xmpp;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import org.jivesfotware.smackx.enitycaps.provider.MessengerCapsExtensionProvider;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountBuilder;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.icons.RealmIconService;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.security.Cipherer;
import org.solovyev.common.security.CiphererException;

import com.google.inject.Inject;

import static org.solovyev.android.messenger.App.newTag;

public class XmppRealm extends AbstractRealm<XmppAccountConfiguration> {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	public static final String USER_PROPERTY_AVATAR_HASH = "avatar_hash";
	public static final String USER_PROPERTY_AVATAR_BASE64 = "avatar_base64";

	public static final String TAG = newTag("XMPP");

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

	protected XmppRealm(@Nonnull String realmId, int nameResId, int iconResId, @Nonnull Class<? extends XmppAccountConfigurationFragment> configurationFragmentClass) {
		super(realmId, nameResId, iconResId, configurationFragmentClass, XmppAccountConfiguration.class, false, null);
	}

	@Nonnull
	@Override
	public Account<XmppAccountConfiguration> newAccount(@Nonnull String accountId, @Nonnull User user, @Nonnull XmppAccountConfiguration configuration, @Nonnull AccountState state) {
		return new XmppAccount(accountId, this, user, configuration, state);
	}

	@Override
	@Nonnull
	public AccountBuilder newAccountBuilder(@Nonnull XmppAccountConfiguration configuration, @Nullable Account editedAccount) {
		return new XmppAccountBuilder(this, (XmppAccount) editedAccount, configuration);
	}

	@Override
	public void init(@Nonnull Context context) {
		super.init(context);

		SmackAndroid.init(context);

		ProviderManager.getInstance().addExtensionProvider("c", "http://jabber.org/protocol/caps", new MessengerCapsExtensionProvider());

		// we need to call static initializer block
		ServiceDiscoveryManager.class.getName();
	}

	@Nonnull
	@Override
	public List<AProperty> getUserDisplayProperties(@Nonnull User user, @Nonnull Context context) {
		final List<AProperty> result = new ArrayList<AProperty>(user.getPropertiesCollection().size());

		for (AProperty property : user.getPropertiesCollection()) {
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
		return new XmppRealmIconService(context, R.drawable.mpp_icon_user, R.drawable.mpp_icon_users);
	}

	@Nullable
	@Override
	public Cipherer<XmppAccountConfiguration, XmppAccountConfiguration> getCipherer() {
		return new XmppRealmConfigurationCipherer(App.getSecurityService().getStringSecurityService().getCipherer());
	}

    /*
    **********************************************************************
    *
    *                           STATIC/INNER CLASSES
    *
    **********************************************************************
    */

	private static class XmppRealmConfigurationCipherer implements Cipherer<XmppAccountConfiguration, XmppAccountConfiguration> {

		@Nonnull
		private final Cipherer<String, String> stringCipherer;

		private XmppRealmConfigurationCipherer(@Nonnull Cipherer<String, String> stringCipherer) {
			this.stringCipherer = stringCipherer;
		}

		@Nonnull
		public XmppAccountConfiguration encrypt(@Nonnull SecretKey secret, @Nonnull XmppAccountConfiguration decrypted) throws CiphererException {
			final XmppAccountConfiguration encrypted = decrypted.clone();
			encrypted.setPassword(stringCipherer.encrypt(secret, decrypted.getPassword()));
			return encrypted;
		}

		@Nonnull
		public XmppAccountConfiguration decrypt(@Nonnull SecretKey secret, @Nonnull XmppAccountConfiguration encrypted) throws CiphererException {
			final XmppAccountConfiguration decrypted = encrypted.clone();
			decrypted.setPassword(stringCipherer.decrypt(secret, encrypted.getPassword()));
			return decrypted;
		}
	}
}
