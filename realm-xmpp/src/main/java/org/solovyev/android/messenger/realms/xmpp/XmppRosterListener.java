package org.solovyev.android.messenger.realms.xmpp;

import android.util.Log;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.AccountException;
import org.solovyev.android.messenger.accounts.AccountRuntimeException;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * User: serso
 * Date: 3/4/13
 * Time: 11:49 PM
 */
class XmppRosterListener implements RosterListener {

	@Nonnull
	private static final String TAG = "M++/XmppRosterListener";

	@Nonnull
	private final XmppAccount account;

	XmppRosterListener(@Nonnull XmppAccount account) {
		this.account = account;
	}

	@Override
	public void entriesAdded(@Nonnull Collection<String> contactIds) {
		Log.d(TAG, "entriesAdded() called");
		final AccountUserService accountUserService = account.getAccountUserService();
		final List<User> contacts;
		try {
			contacts = Lists.newArrayList(Iterables.transform(contactIds, new Function<String, User>() {
				@Override
				public User apply(@Nullable String contactId) {
					assert contactId != null;
					// we need to request new user entity because user id should be prepared properly
					final Entity entity = account.newUserEntity(contactId);
					try {
						return accountUserService.getUserById(entity.getAccountEntityId());
					} catch (AccountException e) {
						throw new AccountRuntimeException(e);
					}
				}
			}));

			// we cannot allow delete because we don't know if user is really deleted on remote server - we only know that his presence was changed
			getUserService().mergeUserContacts(account.getUser().getEntity(), contacts, false, true);

		} catch (AccountRuntimeException e) {
			App.getExceptionHandler().handleException(new AccountException(e));
		}
	}

	@Override
	public void entriesUpdated(@Nonnull Collection<String> contactIds) {
		Log.d(TAG, "entriesUpdated() called");
	}

	@Override
	public void entriesDeleted(@Nonnull Collection<String> contactIds) {
		Log.d(TAG, "entriesDeleted() called");
	}

	@Override
	public void presenceChanged(@Nonnull final Presence presence) {
		Log.d(TAG, "presenceChanged() called");
		final String accountUserId = presence.getFrom();

		final User contact = getUserService().getUserById(account.newUserEntity(accountUserId));
		getUserService().onContactPresenceChanged(account.getUser(), contact, presence.isAvailable());
	}

	@Nonnull
	private UserService getUserService() {
		return App.getUserService();
	}
}
