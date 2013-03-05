package org.solovyev.android.messenger.realms.xmpp;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.realms.RealmIsNotConnectedException;
import org.solovyev.android.messenger.security.AuthData;
import org.solovyev.android.messenger.security.AuthDataImpl;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.security.RealmAuthService;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 10:25 PM
 */
public class XmppRealmAuthService extends AbstractXmppRealmService implements RealmAuthService {

    public XmppRealmAuthService(@Nonnull XmppRealm realm) {
        super(realm);
    }

    @Nonnull
    @Override
    public AuthData loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        return doConnected(new XmppConnectedCallable<AuthData>() {
            @Override
            public AuthData call(@Nonnull XMPPConnection connection) throws RealmIsNotConnectedException, XMPPException {
                final XmppRealmConfiguration configuration = getRealm().getConfiguration();

                if (!connection.isAuthenticated()) {
                    connection.login(configuration.getLogin(), configuration.getPassword());
                }

                final AuthDataImpl result = new AuthDataImpl();

                result.setRealmUserId(configuration.getLogin());
                result.setRealmUserLogin(configuration.getLogin());
                result.setAccessToken("");

                return result;
            }
        });
    }

    @Override
    public void logoutUser(@Nonnull User user) {
    }
}
