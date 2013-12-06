/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.realms.test;

import com.google.common.base.Splitter;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.joda.time.DateTime.now;
import static org.solovyev.android.messenger.users.Users.*;

public class TestAccountService implements AccountUserService, AccountChatService {

	private static final String contactNames = "Erica Carter\n" +
			"Helen Blum\n" +
			"Jasmine Wheatley\n" +
			"Kendall Richey\n" +
			"Carly Noel\n" +
			"Genevieve Cardenas\n" +
			"Alayna Ring\n" +
			"Kayla Griffin\n" +
			"Mackenzie Alonzo\n" +
			"Annabelle Drummond\n" +
			"Alicia Stevenson\n" +
			"Rihanna Gee\n" +
			"Isabel Mckee\n" +
			"Claire German\n" +
			"Alaina Cyr\n" +
			"Addyson Martinez\n" +
			"Summer Bernier\n" +
			"Courtney Winslow\n" +
			"Cora Ibarra\n" +
			"Lola Ledbetter\n" +
			"Rihanna Swanson\n" +
			"Zoe Starkey\n" +
			"Kaitlyn Mendoza\n" +
			"Valeria Coats\n" +
			"Megan Gipson\n" +
			"Malia Aldrich\n" +
			"Katelynn Madden\n" +
			"Peyton Marx\n" +
			"Isabelle Thurston\n" +
			"Isabelle Denton\n" +
			"Paige Hacker\n" +
			"Alexandra Delvalle\n" +
			"Tatum Fontenot\n" +
			"Skylar Moore\n" +
			"Genevieve Adam\n" +
			"Harmony Rowe\n" +
			"Paola Lay\n" +
			"Ruth Mcguire\n" +
			"Leslie Pate\n" +
			"Eleanor Andrade\n" +
			"Samantha Campbell\n" +
			"Caitlin Paxton\n" +
			"Alexandria Grimes\n" +
			"Ariel McClain\n" +
			"Amaya Marin\n" +
			"Alexa Snowden\n" +
			"Alyssa Ferrara\n" +
			"Lily Lockhart\n" +
			"Alexandra Franco\n" +
			"Jordyn Butts\n" +
			"Tony Brunner\n" +
			"Michael Brennan\n" +
			"Lukas Osorio\n" +
			"Andrew Reese\n" +
			"Myles Monahan\n" +
			"Judah Carbone\n" +
			"Diego Bush\n" +
			"Calvin Copeland\n" +
			"Sawyer Kang\n" +
			"Marcos Durham\n" +
			"Christian Joyner\n" +
			"Brenden Sellers\n" +
			"Gregory Massey\n" +
			"Armando Solis\n" +
			"William Alvarado\n" +
			"Ricardo Thorne\n" +
			"Hunter Whatley\n" +
			"Hayden Winslow\n" +
			"Alexander Waite\n" +
			"Dennis Walter\n" +
			"Luke Paulsen\n" +
			"Seth Novak\n" +
			"Armando Burkett\n" +
			"Carter Chavarria\n" +
			"Edward Bennett\n" +
			"Hector Burns\n" +
			"Kaiden Schrader\n" +
			"Hayden Solis\n" +
			"Landon Macdonald\n" +
			"Oliver Wade\n" +
			"Kaiden Purdy\n" +
			"Chase Byrd\n" +
			"Grady Krebs\n" +
			"Ezekiel Priest\n" +
			"Maddox Cartwright\n" +
			"Jalen Himes\n" +
			"Louis Williams\n" +
			"Camden Maynard\n" +
			"Trey Conner\n" +
			"Landen Dooley\n" +
			"John Gilliam\n" +
			"Kaiden Anaya\n" +
			"Manuel Funk\n" +
			"Troy Carlisle\n" +
			"Alan Pedersen\n" +
			"Jackson Brower\n" +
			"Aidan Bryson\n" +
			"Jeffrey Prince\n" +
			"Tanner Abernathy\n" +
			"Adam Ornelas\n";

	@Nonnull
	private static AtomicLong nextSendDate = new AtomicLong(now().getMillis());

	@Nonnull
	private final TestAccount account;

	public TestAccountService(@Nonnull TestAccount account) {
		this.account = account;
	}

	@Nullable
	@Override
	public User getUserById(@Nonnull String accountUserId) {
		return null;
	}

	@Nonnull
	@Override
	public List<User> getContacts() {
		final List<User> contacts = new ArrayList<User>();
		int index = 0;
		for (String contactName : Splitter.on('\n').split(contactNames)) {
			final List<AProperty> properties = new ArrayList<AProperty>();
			tryParseNameProperties(properties, contactName);
			if (index % 2 == 0) {
				properties.add(newOnlineProperty(true));
			}
			contacts.add(newUser(account.newUserEntity(String.valueOf(index)), properties));
			index++;
		}
		return contacts;
	}

	@Nonnull
	@Override
	public List<User> getOnlineUsers() {
		final List<User> contacts = new ArrayList<User>();
		for (User contact : getContacts()) {
			if (contact.isOnline()) {
				contacts.add(contact);
			}
		}
		return contacts;
	}

	@Nonnull
	@Override
	public User saveUser(@Nonnull User user) {
		return user;
	}

	@Nonnull
	@Override
	public List<Message> getMessages() {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getNewerMessagesForChat(@Nonnull String accountChatId) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<Message> getOlderMessagesForChat(@Nonnull String accountChatId, @Nonnull Integer offset) {
		return Collections.emptyList();
	}

	@Nonnull
	@Override
	public List<AccountChat> getChats() {
		final List<User> contacts = getContacts();
		final User user = account.getUser();

		final List<AccountChat> chats = new ArrayList<AccountChat>();
		for (int i = 0; i < contacts.size(); i++) {
			User contact = contacts.get(i);
			chats.add(createChat(user, contact, i, contacts.size()));
		}
		return chats;
	}

	private MutableAccountChat createChat(@Nonnull User user, @Nonnull User contact, int position, int size) {
		final MutableAccountChat chat = Chats.newAccountChat(account.newChatEntity(contact.getId()), true);
		chat.addParticipant(user);
		chat.addParticipant(contact);

		switch (size - position - 1) {
			case 0:
				chat.addMessage(newIncomingMessage(contact, chat, "What did you have for breakfast?"));
				chat.addMessage(newOutgoingMessage(chat, "Chicken liver and mashed potatoes with chocolate milk and a pineapple."));
				break;
			case 1:
				chat.addMessage(newIncomingMessage(contact, chat, "What are you talking about Tema?"));
				chat.addMessage(newOutgoingMessage(chat, "My two cats."));
				break;
			case 2:
				chat.addMessage(newIncomingMessage(contact, chat, "Why do you boast of mischief, mighty man?").cloneRead());
				chat.addMessage(newOutgoingMessage(chat, "I say it because I don't like you."));
				break;
			case 3:
				chat.addMessage(newIncomingMessage(contact, chat, "Will you make me a cup of coffee?"));
				chat.addMessage(newOutgoingMessage(chat, "No."));
				chat.addMessage(newIncomingMessage(contact, chat, "Why?"));
				break;
			case 4:
				chat.addMessage(newIncomingMessage(contact, chat, "Hi, how are you?"));
				break;
			case 5:
				chat.addMessage(newOutgoingMessage(chat, "Haven't heard from you for ages!"));
				break;
			default:
				chat.addMessage(newIncomingMessage(contact, chat, "Hi! My name is " + contact.getDisplayName()).cloneRead());
				break;
		}

		return chat;
	}

	@Nonnull
	private MutableMessage newOutgoingMessage(@Nonnull MutableAccountChat chat, @Nonnull String textBody) {
		final MutableMessage message = Messages.newOutgoingMessage(account, chat.getChat(), textBody, null);
		message.setSendDate(getNextSendDate());
		return message;
	}

	@Nonnull
	private DateTime getNextSendDate() {
		return new DateTime(nextSendDate.getAndAdd(10000L));
	}

	@Nonnull
	private MutableMessage newIncomingMessage(@Nonnull User contact, @Nonnull MutableAccountChat chat, @Nonnull String textBody) {
		final MutableMessage message = Messages.newIncomingMessage(account, chat.getChat(), textBody, null, contact.getEntity());
		message.setSendDate(getNextSendDate());
		return message;
	}

	@Nonnull
	@Override
	public String sendMessage(@Nonnull Chat chat, @Nonnull Message message) {
		return "test_message_id";
	}

	@Override
	public void beforeSendMessage(@Nonnull Chat chat, @Nullable User recipient, @Nonnull MutableMessage message) throws AccountConnectionException {
	}

	@Nonnull
	@Override
	public MutableChat newPrivateChat(@Nonnull Entity accountChat, @Nonnull String accountUserId1, @Nonnull String accountUserId2) {
		return Chats.newPrivateChat(accountChat);
	}
}
