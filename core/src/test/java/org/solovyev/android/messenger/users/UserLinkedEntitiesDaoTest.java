package org.solovyev.android.messenger.users;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.DefaultLinkedEntitiesDaoTest;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.android.messenger.chats.ChatDao;

import com.google.common.base.Function;
import com.google.inject.Inject;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.users.Users.newEmptyUser;

public class UserLinkedEntitiesDaoTest extends DefaultLinkedEntitiesDaoTest<User> {

	@Inject
	@Nonnull
	private UserDao dao;

	@Inject
	@Nonnull
	private ChatDao chatDao;

	public UserLinkedEntitiesDaoTest() {
		super(new UserSameEqualizer());
	}

	@Nonnull
	@Override
	protected LinkedEntitiesDao<User> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected List<User> getLinkedEntities(@Nonnull AccountData ad) {
		return ad.getContacts();
	}

	@Nonnull
	@Override
	protected User newLinkedEntity(@Nonnull AccountData ad, int i) {
		return newEmptyUser(ad.getAccount().newUserEntity("linked_contact_" + i));
	}

	@Nonnull
	@Override
	protected String getId() {
		return getAccount1().getUser().getId();
	}

	@Nonnull
	@Override
	protected Collection<String> getLinkedIds() {
		return newArrayList(transform(getAccountData1().getContacts(), new Function<User, String>() {
			@Override
			public String apply(@Nullable User contact) {
				return contact.getId();
			}
		}));
	}
}
