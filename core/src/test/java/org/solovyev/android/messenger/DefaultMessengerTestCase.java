package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.entities.EntityImpl.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public abstract class DefaultMessengerTestCase extends AbstractMessengerTestCase {

	@Nonnull
	@Inject
	private AccountService accountService;

	@Nonnull
	@Inject
	private UserService userService;

	@Nonnull
	@Inject
	private ChatService chatService;

	@Nonnull
	@Inject
	private ChatMessageService messageService;

	@Nonnull
	@Inject
	private TestRealm realm;

	@Nonnull
	private TestAccount account1;

	@Nonnull
	private TestAccount account2;

	@Nonnull
	private TestAccount account3;

	@Nonnull
	protected AbstractTestMessengerModule newModule(@Nonnull Application application) {
		return new DefaultTestMessengerModule(application);
	}

	protected void populateDatabase() throws Exception {
		account1 = accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_0", 0), null));
		account2 = accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_1", 1), null));
		account3 = accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_2", 2), null));

		populateAccount(account1, 3);
		populateAccount(account2, 20);
		populateAccount(account3, 200);
	}

	private void populateAccount(@Nonnull Account account, int count) throws AccountException {
		addUsers(account, count);
	}

	private void addUsers(@Nonnull Account account, int count) {
		final List<User> contacts = new ArrayList<User>();
		for(int i = 0; i < count; i++) {
			contacts.add(getContactForAccount(account, i));
		}
		userService.mergeUserContacts(account.getUser().getEntity(), contacts, false, false);
	}

	@Nonnull
	protected User getContactForAccount(@Nonnull Account account, int i) {
		return newEmptyUser(newEntity(account.getId(), String.valueOf(i)));
	}

	@Nonnull
	public TestAccount getAccount1() {
		return account1;
	}

	@Nonnull
	public TestAccount getAccount2() {
		return account2;
	}

	@Nonnull
	public TestAccount getAccount3() {
		return account3;
	}
}
