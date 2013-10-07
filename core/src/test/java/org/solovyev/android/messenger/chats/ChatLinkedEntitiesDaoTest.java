package org.solovyev.android.messenger.chats;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.DefaultLinkedEntitiesDaoTest;
import org.solovyev.android.messenger.DefaultMessengerTest;
import org.solovyev.android.messenger.LinkedEntitiesDao;
import org.solovyev.common.equals.Equalizer;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

public class ChatLinkedEntitiesDaoTest extends DefaultLinkedEntitiesDaoTest<Chat> {

	@Inject
	@Nonnull
	private ChatDao dao;

	@Nonnull
	@Override
	protected LinkedEntitiesDao<Chat> getDao() {
		return dao;
	}

	@Nonnull
	@Override
	protected Chat newLinkedEntity(@Nonnull AccountData ad, int i) {
		return Chats.newPrivateChat(ad.getAccount().newChatEntity("linked_chat_" + i));
	}

	@Nonnull
	@Override
	protected List<Chat> getLinkedEntities(@Nonnull AccountData ad) {
		return newArrayList(transform(ad.getChats(), new Function<ApiChat, Chat>() {
			@Override
			public Chat apply(@Nullable ApiChat apiChat) {
				return apiChat.getChat();
			}
		}));
	}

	@Nonnull
	@Override
	protected String getId() {
		return getAccountData1().getAccount().getUser().getId();
	}

	@Nonnull
	@Override
	protected Collection<String> getLinkedIds() {
		return newArrayList(transform(getAccountData1().getChats(), new Function<ApiChat, String>() {
			@Override
			public String apply(@Nullable ApiChat apiChat) {
				return apiChat.getChat().getId();
			}
		}));
	}
}
