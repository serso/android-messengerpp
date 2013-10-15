package org.solovyev.android.messenger;

import android.app.Application;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.*;
import org.solovyev.android.messenger.chats.AccountChat;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.entities.Entities;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MessageService;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.MutableUser;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.properties.MutableAProperties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.solovyev.android.messenger.chats.Chats.newPrivateAccountChat;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.messages.Messages.newMessage;
import static org.solovyev.android.messenger.users.User.*;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public abstract class DefaultMessengerTest extends AbstractMessengerTest {

	private static final int ACCOUNT_1_USER_COUNT = 3;
	private static final int ACCOUNT_2_USER_COUNT = 20;
	private static final int ACCOUNT_3_USER_COUNT = 200;

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
	private MessageService messageService;

	@Nonnull
	@Inject
	private TestRealm realm;

	@Nonnull
	private final List<AccountData> accountDataList = new ArrayList<AccountData>();

	@Nonnull
	protected AbstractTestMessengerModule newModule(@Nonnull Application application) {
		return new DefaultTestMessengerModule(application);
	}

	protected void populateDatabase() throws Exception {
		accountDataList.add(createAccountData(0, ACCOUNT_1_USER_COUNT));
		accountDataList.add(createAccountData(1, ACCOUNT_2_USER_COUNT));
		accountDataList.add(createAccountData(2, ACCOUNT_3_USER_COUNT));
	}

	@Nonnull
	private AccountData createAccountData(int index, int count) throws AccountException, InvalidCredentialsException, AccountAlreadyExistsException {
		final AccountData result = new AccountData(accountService.saveAccount(new TestAccountBuilder(realm, new TestAccountConfiguration("test_" + index, index), null)));
		result.users.addAll(addUsers(result.account, count));
		final User user = result.account.getUser();
		result.users.add(0, user);
		for (User contact : result.getContacts()) {
			final List<Message> messages = new ArrayList<Message>();
			for(int i = 0; i < 10; i++) {
				messages.add(generateMessage(i, user, contact, result.account));
			}
			result.chats.add(newPrivateAccountChat(chatService.getPrivateChatId(user.getEntity(), contact.getEntity()), Arrays.asList(user, contact), messages));
		}
		chatService.mergeUserChats(user.getEntity(), result.chats);
		return result;
	}

	private MutableMessage generateMessage(int i, @Nonnull User user, @Nonnull User contact, @Nonnull TestAccount account) {
		final MutableMessage message = newMessage(Entities.generateEntity(account));

		if (i % 2 == 0) {
			message.setAuthor(user.getEntity());
			message.setRecipient(contact.getEntity());
		} else {
			message.setAuthor(contact.getEntity());
			message.setRecipient(user.getEntity());
		}

		message.setBody(Strings.generateRandomString(10));
		message.setSendDate(new DateTime(0).plusMinutes(i));
		message.setRead(false);
		return message;
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
		final MutableUser user = newEmptyUser(getEntityForContact(account, i));
		final MutableAProperties properties = user.getProperties();
		user.setLastName("last_name_" + i);
		user.setFirstName("first_name_" + i);
		user.setOnline(i % 2 == 0);
		properties.setProperty(PROPERTY_PHONE, "phone_" + i);
		return user;
	}

	@Nonnull
	protected Entity getEntityForContact(@Nonnull Account account, int i) {
		return newEntity(account.getId(), String.valueOf(i));
	}

	@Nonnull
	public List<AccountData> getAccountDataList() {
		return accountDataList;
	}

	@Nonnull
	public TestAccount getAccount1() {
		return accountDataList.get(0).account;
	}

	@Nonnull
	public TestAccount getAccount2() {
		return accountDataList.get(1).account;
	}

	@Nonnull
	public TestAccount getAccount3() {
		return accountDataList.get(2).account;
	}

	@Nonnull
	public List<User> getUsers1() {
		return accountDataList.get(0).users;
	}

	@Nonnull
	public List<User> getUsers2() {
		return accountDataList.get(1).users;
	}

	@Nonnull
	public List<User> getUsers3() {
		return accountDataList.get(2).users;
	}

	@Nonnull
	protected AccountService getAccountService() {
		return accountService;
	}

	@Nonnull
	public AccountData getAccountData1() {
		return accountDataList.get(0);
	}

	@Nonnull
	public AccountData getAccountData2() {
		return accountDataList.get(1);
	}

	@Nonnull
	public AccountData getAccountData3() {
		return accountDataList.get(2);
	}

	public static final class AccountData {

		@Nonnull
		private final TestAccount account;

		@Nonnull
		private final List<User> users = new ArrayList<User>();

		@Nonnull
		private final List<AccountChat> chats = new ArrayList<AccountChat>();

		private AccountData(@Nonnull TestAccount account) {
			this.account = account;
		}

		@Nonnull
		public TestAccount getAccount() {
			return account;
		}

		@Nonnull
		public List<User> getUsers() {
			return users;
		}

		@Nonnull
		public List<User> getContacts() {
			return users.subList(1, users.size());
		}

		@Nonnull
		public List<AccountChat> getChats() {
			return chats;
		}
	}
}
