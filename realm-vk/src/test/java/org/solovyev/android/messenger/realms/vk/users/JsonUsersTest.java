package org.solovyev.android.messenger.realms.vk.users;

import org.junit.Test;
import org.solovyev.android.messenger.accounts.TestAccount;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.users.User;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.solovyev.android.messenger.realms.vk.users.JsonUsers.newFromJson;

public class JsonUsersTest {

	@Test
	public void testShouldConvertJson() throws Exception {
		final TestAccount account = new TestAccount(new TestRealm());
		final JsonUsers jsonUsers = newFromJson("{\"response\":[{\"uid\":\"1\",\"first_name\":\"Павел\",\"last_name\":\"Дуров\",\n" +
				"\"photo\":\"http:\\/\\/cs109.vkontakte.ru\\/u00001\\/c_df2abf56.jpg\"},\n" +
				"{\"uid\":\"6492\",\"first_name\":\"Andrew\",\"last_name\":\"Rogozov\",\n" +
				"\"photo\":\"http:\\/\\/cs537.vkontakte.ru\\/u06492\\/c_28629f1d.jpg\"}]}");

		final List<JsonUser> users = jsonUsers.getUsers();
		assertEquals(2, users.size());
		final User user1 = users.get(0).toUser(account);
		assertEquals("1", user1.getEntity().getAccountEntityId());
		assertEquals("Павел", user1.getFirstName());
		assertEquals("Дуров", user1.getLastName());
	}
}
