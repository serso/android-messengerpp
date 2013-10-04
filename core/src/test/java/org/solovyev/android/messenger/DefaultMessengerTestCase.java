package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Inject;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public abstract class DefaultMessengerTestCase extends AbstractMessengerTestCase {

	public static final int ACCOUNT_1_USER_COUNT = 3;
	public static final int ACCOUNT_2_USER_COUNT = 20;
	public static final int ACCOUNT_3_USER_COUNT = 200;

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
	private final List<User> users1 = new ArrayList<User>();

	@Nonnull
	private final List<User> users2 = new ArrayList<User>();

	@Nonnull
	private final List<User> users3 = new ArrayList<User>();

	@Nonnull
	protected AbstractTestMessengerModule newModule(@Nonnull Application application) {
		return new DefaultTestMessengerModule(application);
	}

	protected void populateDatabase() throws Exception {
		account1 = accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_0", 0), null));
		account2 = accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_1", 1), null));
		account3 = accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_2", 2), null));

		populateAccount(account1, ACCOUNT_1_USER_COUNT, users1);
		populateAccount(account2, ACCOUNT_2_USER_COUNT, users2);
		populateAccount(account3, ACCOUNT_3_USER_COUNT, users3);
	}

	private void populateAccount(@Nonnull Account account, int count, @Nonnull List<User> users) throws AccountException {
		users.addAll(addUsers(account, count));
		users.add(0, account.getUser());
	}

	@Nonnull
	private List<User> addUsers(@Nonnull Account account, int count) {
		final List<User> contacts = new ArrayList<User>();
		for(int i = 0; i < count; i++) {
			contacts.add(getContactForAccount(account, i));
		}
		userService.mergeUserContacts(account.getUser().getEntity(), contacts, false, false);
		return contacts;
	}

	@Nonnull
	protected User getContactForAccount(@Nonnull Account account, int i) {
		return newEmptyUser(getEntityForUser(account, i));
	}

	@Nonnull
	protected Entity getEntityForUser(Account account, int i) {
		return newEntity(account.getId(), String.valueOf(i));
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

	@Nonnull
	public List<User> getUsers1() {
		return users1;
	}

	@Nonnull
	public List<User> getUsers2() {
		return users2;
	}

	@Nonnull
	public List<User> getUsers3() {
		return users3;
	}

	@Nonnull
	protected AccountService getAccountService() {
		return accountService;
	}
}
