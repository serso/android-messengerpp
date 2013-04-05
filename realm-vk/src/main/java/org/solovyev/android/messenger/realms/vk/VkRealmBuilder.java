package org.solovyev.android.messenger.realms.vk;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.HttpTransactions;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.AbstractRealmBuilder;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.realms.RealmState;
import org.solovyev.android.messenger.realms.vk.users.VkUsersGetHttpTransaction;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class VkRealmBuilder extends AbstractRealmBuilder<VkRealmConfiguration> {

    protected VkRealmBuilder(@Nonnull RealmDef realmDef, @Nullable Realm editedRealm, @Nonnull VkRealmConfiguration configuration) {
        super(realmDef, editedRealm, configuration);
    }

    @Nonnull
    @Override
    protected User getRealmUser(@Nonnull String realmId) {
        final String userId = getConfiguration().getUserId();
        final User defaultUser = Users.newEmptyUser(EntityImpl.newInstance(realmId, userId));

        User result;
        try {
            final List<User> users = HttpTransactions.execute(VkUsersGetHttpTransaction.newInstance(new VkRealm(realmId, getRealmDef(), defaultUser, getConfiguration(), RealmState.removed), userId, null));
            if ( users.isEmpty() ) {
                result = defaultUser;
            } else {
                result = users.get(0);
            }
        } catch (IOException e) {
            Log.e("VkRealmBuilder", e.getMessage(), e);
            result = defaultUser;
        }

        return result;
    }

    @Nonnull
    @Override
    protected Realm newRealm(@Nonnull String id, @Nonnull User user, @Nonnull RealmState state) {
        return new VkRealm(id, getRealmDef(), user, getConfiguration(), state);
    }

    @Override
    public void connect() throws ConnectionException {
    }

    @Override
    public void disconnect() throws ConnectionException {
    }

    @Override
    public void loginUser(@Nullable ResolvedCaptcha resolvedCaptcha) throws InvalidCredentialsException {
        final VkRealmConfiguration configuration = getConfiguration();

        final AccountManager am = AccountManager.get(MessengerApplication.getApp());
        final Bundle options = new Bundle();

        //am.addAccount("messengerpp.vk", "auth", new String[]{}, )

        am.getAuthToken(new Account(configuration.getLogin(), "messengerpp.vk"), "SETTINGS", options, null, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {

            }
        }, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        }));
    }

    private OAuthAccessor createOAuthAccessor(){
        String requestTokenUrl = "requestTokenUrl";
        String authorizationUrl = "https://oauth.vk.com/authorize?client_id=APP_ID&scope=SETTINGS&redirect_uri=REDIRECT_URI&display=DISPLAY&response_type=token";
        String accessUrl = "accessUrl";

        final OAuthServiceProvider provider = new OAuthServiceProvider(requestTokenUrl, authorizationUrl, accessUrl);
        final OAuthConsumer consumer = new OAuthConsumer("callbackUrk", "consumerKey", "consumerSecret", provider);
        return new OAuthAccessor(consumer);
    }
}
