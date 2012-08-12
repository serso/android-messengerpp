package org.solovyev.android.messenger.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.solovyev.android.ResolvedCaptcha;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;
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

    @NotNull
    private static final String AUTH_XML = "auth_xml";

    @ElementMap(keyType = String.class, valueType = AuthDataImpl.class)
    @NotNull
    private Map<String, AuthData> authDataMap = new HashMap<String, AuthData>();

    @NotNull
    private final Object lock = new Object();

    @Inject
    @NotNull
    private RealmService realmService;

    @Inject
    @NotNull
    private UserService userService;

    @NotNull
    @Override
    public AuthData loginUser(@NotNull String realm,
                              @NotNull String login,
                              @NotNull String password,
                              @Nullable ResolvedCaptcha resolvedCaptcha,
                              @NotNull Context context) throws InvalidCredentialsException {

        final RealmAuthService realmAuthService = getRealmService(realm).getRealmAuthService();

        final AuthData result;
        synchronized (lock) {
            if (!isUserLoggedIn(realm)) {
                result = realmAuthService.loginUser(login, password, resolvedCaptcha);
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

        save(context);

        return result;
    }

    @NotNull
    private Realm getRealmService(@NotNull String realm) {
        try {
            return realmService.getRealmById(realm);
        } catch (UnsupportedRealmException e) {
            throw new RuntimeException(e);
        }
    }


    @NotNull
    @Override
    public User getUser(@NotNull String realm, @NotNull Context context) throws UserIsNotLoggedInException {
        return getUserById(context, getAuthData(realm));
    }

    @NotNull
    @Override
    public AuthData getAuthData(@NotNull String realm) throws UserIsNotLoggedInException {
        final AuthData authData;

        synchronized (lock) {
            if (isUserLoggedIn(realm)) {
                authData = authDataMap.get(realm);
            } else {
                throw new UserIsNotLoggedInException("User must be logged in before calling org.solovyev.android.messenger.security.AbstractAuthService.getUser!");
            }
        }

        return authData;
    }

    @NotNull
    private User getUserById(@NotNull Context context, @NotNull AuthData authData) {
        return this.userService.getUserById(authData.getUserId(), context);
    }

    @Override
    public boolean isUserLoggedIn(@NotNull String realm) {
        synchronized (lock) {
            return authDataMap.get(realm) != null;
        }
    }

    @Override
    public void logoutUser(@NotNull String realm, @NotNull Context context) {
        synchronized (lock) {
            if (isUserLoggedIn(realm)) {
                final RealmAuthService realmAuthService = getRealmService(realm).getRealmAuthService();

                try {
                    realmAuthService.logoutUser(getUser(realm, context));
                } catch (UserIsNotLoggedInException e) {
                    // unavailable
                    throw new AssertionError(e);
                }

                this.authDataMap.remove(realm);
            }
        }

        save(context);
    }

    /*
    **********************************************************************
    *
    *                           SAVING/RESTORING STATE
    *
    **********************************************************************
    */

    @Override
    public void save(@NotNull Context context) {

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
    public void load(@NotNull Context context) {
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
