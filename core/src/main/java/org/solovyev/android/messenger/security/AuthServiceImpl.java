package org.solovyev.android.messenger.security;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * User: serso
 * Date: 5/24/12
 * Time: 9:22 PM
 */
@Singleton
@Root
public class AuthServiceImpl implements AuthService {

    @Nonnull
    private static final String AUTH_XML = "auth_xml";

    @ElementMap(keyType = String.class, valueType = AuthDataImpl.class)
    @Nonnull
    private Map<String, AuthData> authDataMap = new HashMap<String, AuthData>();

    @Nonnull
    private final Object lock = new Object();

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private Application context;

    @Nonnull
    @Override
    public AuthData loginUser(@Nonnull String realm,
                              @Nonnull String login,
                              @Nonnull String password,
                              @Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {

        final RealmAuthService realmAuthService = getRealmById(realm).getRealmAuthService();

        final AuthData result;
        synchronized (lock) {
            if (!isUserLoggedIn(realm)) {
                result = realmAuthService.loginUser(resolvedCaptcha);
                authDataMap.put(realm, result);
            } else {
                try {
                    result = getAuthData(realm);
                } catch (UserIsNotLoggedInException e) {
                    // unavailable
                    throw new AssertionError(e);
                }
            }
        }

        save();

        return result;
    }

    @Nonnull
    private Realm getRealmById(@Nonnull String realm) {
        return realmService.getRealmById(realm);
    }


    @Nonnull
    @Override
    public User getUser(@Nonnull String realm) throws UserIsNotLoggedInException {
        return getUserById(getAuthData(realm));
    }

    @Nonnull
    @Override
    public AuthData getAuthData(@Nonnull String realm) throws UserIsNotLoggedInException {
        final AuthData authData;

        synchronized (lock) {
            if (isUserLoggedIn(realm)) {
                authData = authDataMap.get(realm);
            } else {
                throw new UserIsNotLoggedInException("User must be logged in before calling getUser() method!");
            }
        }

        return authData;
    }

    @Nonnull
    private User getUserById(@Nonnull AuthData authData) {
        // todo serso: continue
        throw new UnsupportedOperationException();
        //return this.userService.getUserById(RealmEntityImpl.fromEntityId(authData.getRealmUserId()));
    }

    @Override
    public boolean isUserLoggedIn(@Nonnull String realm) {
        synchronized (lock) {
            return authDataMap.get(realm) != null;
        }
    }

    @Override
    public void logoutUser(@Nonnull String realm) {
        synchronized (lock) {
            if (isUserLoggedIn(realm)) {
                final RealmAuthService realmAuthService = getRealmById(realm).getRealmAuthService();

                try {
                    realmAuthService.logoutUser(getUser(realm));
                } catch (UserIsNotLoggedInException e) {
                    // unavailable
                    throw new AssertionError(e);
                }

                this.authDataMap.remove(realm);
            }
        }

        save();
    }

    /*
    **********************************************************************
    *
    *                           SAVING/RESTORING STATE
    *
    **********************************************************************
    */

    @Override
    public void save() {

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        final StringWriter sw = new StringWriter();
        final Serializer serializer = new Persister();
        try {
            serializer.write(this, sw);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        editor.putString(AUTH_XML, sw.toString());

        editor.commit();
    }

    @Override
    public void load() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        final String value = preferences.getString(AUTH_XML, null);
        if (value != null) {
            final Serializer serializer = new Persister();
            try {
                final AuthServiceImpl authService = serializer.read(AuthServiceImpl.class, value);
                authDataMap.clear();
                authDataMap.putAll(authService.authDataMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}
