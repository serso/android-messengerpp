package org.solovyev.android.messenger.realms.sms;

import android.content.Context;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.solovyev.android.messenger.accounts.AbstractAccount;
import org.solovyev.android.messenger.accounts.AccountState;
import org.solovyev.android.messenger.accounts.connection.AccountConnection;
import org.solovyev.android.messenger.chats.AccountChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.users.AccountUserService;
import org.solovyev.android.messenger.users.CompositeUserChoice;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperties;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static org.solovyev.android.messenger.users.CompositeUserChoice.newCompositeUserChoice;
import static org.solovyev.android.messenger.users.User.PROPERTY_PHONE;
import static org.solovyev.android.messenger.users.Users.isValidPhoneNumber;
import static org.solovyev.android.properties.Properties.newProperty;

/**
 * User: serso
 * Date: 5/27/13
 * Time: 8:43 PM
 */
final class SmsAccount extends AbstractAccount<SmsAccountConfiguration> {

	public SmsAccount(@Nonnull String id, @Nonnull Realm realm, @Nonnull User user, @Nonnull SmsAccountConfiguration configuration, @Nonnull AccountState state) {
		super(id, realm, user, configuration, state);
	}

	@Nonnull
	@Override
	protected AccountConnection createConnection(@Nonnull Context context) {
		return new SmsAccountConnection(this, context);
	}

	@Nonnull
	@Override
	public String getDisplayName(@Nonnull Context context) {
		return context.getString(getRealm().getNameResId());
	}

	@Nonnull
	@Override
	public AccountUserService getAccountUserService() {
		return new SmsAccountUserService(this);
	}

	@Nonnull
	@Override
	public AccountChatService getAccountChatService() {
		return new SmsAccountChatService();
	}

	@Override
	public boolean isCompositeUser(@Nonnull User user) {
		return true;
	}

	@Override
	public boolean isCompositeUserDefined(@Nonnull User user) {
		final String phoneNumber = user.getPropertyValueByName(PROPERTY_PHONE);
		return !Strings.isEmpty(phoneNumber);
	}

	@Nonnull
	@Override
	public List<CompositeUserChoice> getCompositeUserChoices(@Nonnull User user) {
		final AtomicInteger index = new AtomicInteger(0);
		return newArrayList(transform(user.getPhoneNumbers(), new Function<String, CompositeUserChoice>() {
			@Override
			public CompositeUserChoice apply(String phone) {
				return newCompositeUserChoice(phone, index.getAndIncrement());
			}
		}));
	}

	@Nonnull
	@Override
	public User applyCompositeChoice(@Nonnull CompositeUserChoice compositeUserChoice, @Nonnull User user) {
		return user.cloneWithNewProperty(newProperty(PROPERTY_PHONE, compositeUserChoice.getName().toString()));
	}

	@Override
	public boolean isCompositeUserChoicePersisted() {
		return true;
	}

	@Override
	public int getCompositeDialogTitleResId() {
		return R.string.mpp_sms_realm_composite_dialog_title;
	}

	@Override
	public boolean canCall(@Nonnull User contact) {
		return contact.getPhoneNumber() != null || !contact.getPhoneNumbers().isEmpty();
	}
}
