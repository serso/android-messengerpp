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
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;
import org.solovyev.android.messenger.messages.MutableMessage;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.solovyev.android.messenger.messages.Messages.newIncomingMessage;
import static org.solovyev.android.messenger.messages.Messages.newOutgoingMessage;
import static org.solovyev.android.messenger.users.Users.newUser;

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
			Users.tryParseNameProperties(properties, contactName);
			contacts.add(newUser(account.newUserEntity(String.valueOf(index)), properties));
			index++;
		}
		return contacts;
	}

	@Nonnull
	@Override
	public List<User> getOnlineUsers() {
		return getContacts().subList(0, 10);
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
		final AtomicInteger index = new AtomicInteger();

		final List<User> contacts = getContacts();
		final User user = account.getUser();

		final List<AccountChat> chats = new ArrayList<AccountChat>();
		chats.add(createChat(index, contacts, user));
		chats.add(createChat(index, contacts, user));
		return chats;
	}

	private MutableAccountChat createChat(@Nonnull AtomicInteger index, @Nonnull List<User> contacts, @Nonnull User user) {
		final int id = index.getAndIncrement();
		final User contact = contacts.get(id);

		final MutableAccountChat chat = Chats.newAccountChat(account.newChatEntity(String.valueOf(id)), true);
		chat.addParticipant(user);
		chat.addParticipant(contact);

		switch (id) {
			case 0:
				chat.addMessage(newIncomingMessage(account, chat.getChat(), "What did you have for breakfast?", null, contact.getEntity()));
				chat.addMessage(newOutgoingMessage(account, chat.getChat(), "Chicken liver and mashed potatoes with chocolate milk and a pineapple.", null));
				break;
			case 1:
				chat.addMessage(newIncomingMessage(account, chat.getChat(), "What are you talking about Tema?", null, contact.getEntity()));
				chat.addMessage(newOutgoingMessage(account, chat.getChat(), "My two cats.", null));
				break;
			case 2:
				chat.addMessage(newIncomingMessage(account, chat.getChat(), "Why do you boast of mischief, mighty man?", null, contact.getEntity()).cloneRead());
				chat.addMessage(newOutgoingMessage(account, chat.getChat(), "I say it because I don't like you.", null));
				break;
			case 3:
				chat.addMessage(newIncomingMessage(account, chat.getChat(), "Will you make me a cup of coffee?", null, contact.getEntity()));
				chat.addMessage(newOutgoingMessage(account, chat.getChat(), "No.", null));
				chat.addMessage(newIncomingMessage(account, chat.getChat(), "Why?", null, contact.getEntity()));
				break;
		}

		return chat;
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
