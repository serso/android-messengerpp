package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

import android.view.View;
import org.joda.time.DateTime;
import org.solovyev.android.Fragments2;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.App;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.view.ViewAwareTag;
import org.solovyev.android.properties.AProperties;
import org.solovyev.android.properties.AProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.solovyev.android.messenger.App.TAG;
import static org.solovyev.android.messenger.App.getEventManager;
import static org.solovyev.android.messenger.App.getUiHandler;
import static org.solovyev.android.messenger.users.BaseEditUserFragment.newCreateUserArguments;
import static org.solovyev.android.messenger.users.BaseEditUserFragment.newEditUserArguments;
import static org.solovyev.android.messenger.entities.Entities.newEntityFromEntityId;
import static org.solovyev.android.messenger.entities.Entities.newEntity;
import static org.solovyev.android.messenger.users.ContactUiEventType.call_contact;
import static org.solovyev.android.properties.Properties.newProperty;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 3:17 PM
 */
public final class Users {

	@Nonnull
	public static final String CONTACTS_FRAGMENT_TAG = "contacts";

	@Nonnull
	public static final String CREATE_USER_FRAGMENT_TAG = "create_user";

	static final int MAX_SEARCH_CONTACTS = 20;

	@Nonnull
	static final ContactsDisplayMode DEFAULT_CONTACTS_MODE = ContactsDisplayMode.all_contacts;

	private Users() {
	}

	@Nonnull
	public static String getDisplayNameFor(@Nonnull Entity user) {
		return App.getUserService().getUserById(user).getDisplayName();
	}

	@Nonnull
	public static MutableUser newUser(@Nonnull String accountId,
							   @Nonnull String accountUserId,
							   @Nonnull UserSyncData userSyncData,
							   @Nonnull List<AProperty> properties) {
		final Entity entity = newEntity(accountId, accountUserId);
		return newUser(entity, userSyncData, properties);
	}

	@Nonnull
	public static MutableUser newEmptyUser(@Nonnull Entity accountUser) {
		return newUser(accountUser, Users.newNeverSyncedUserSyncData(), Collections.<AProperty>emptyList());
	}

	@Nonnull
	public static User newEmptyUser(@Nonnull String userId) {
		return newEmptyUser(newEntityFromEntityId(userId));
	}

	@Nonnull
	public static MutableUser newUser(@Nonnull Entity entity,
							   @Nonnull UserSyncData userSyncData,
							   @Nonnull Collection<AProperty> properties) {
		return UserImpl.newInstance(entity, userSyncData, properties);
	}

	@Nonnull
	public static MutableUser newUser(@Nonnull Entity entity,
							   @Nonnull UserSyncData userSyncData,
							   @Nonnull AProperties properties) {
		return UserImpl.newInstance(entity, userSyncData, properties.getPropertiesCollection());
	}

	@Nonnull
	public static UserSyncData newNeverSyncedUserSyncData() {
		return UserSyncDataImpl.newNeverSyncedInstance();
	}

	@Nonnull
	public static UserSyncData newUserSyncData(@Nullable DateTime lastPropertiesSyncDate,
											   @Nullable DateTime lastContactsSyncDate,
											   @Nullable DateTime lastChatsSyncDate,
											   @Nullable DateTime lastUserIconsSyncDate) {
		return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	@Nonnull
	public static UserSyncData newUserSyncData(@Nullable String lastPropertiesSyncDate,
											   @Nullable String lastContactsSyncDate,
											   @Nullable String lastChatsSyncDate,
											   @Nullable String lastUserIconsSyncDate) {
		return UserSyncDataImpl.newInstance(lastPropertiesSyncDate, lastContactsSyncDate, lastChatsSyncDate, lastUserIconsSyncDate);
	}

	public static void tryParseNameProperties(@Nonnull List<AProperty> properties, @Nullable String fullName) {
		if (fullName != null) {
			int firstSpaceSymbolIndex = fullName.indexOf(' ');
			int lastSpaceSymbolIndex = fullName.lastIndexOf(' ');
			if (firstSpaceSymbolIndex != -1 && firstSpaceSymbolIndex == lastSpaceSymbolIndex) {
				// only one space in the string
				// Proof:
				// 1. if no spaces => both return -1
				// 2. if more than one spaces => both return different
				final String firstName = fullName.substring(0, firstSpaceSymbolIndex);
				final String lastName = fullName.substring(firstSpaceSymbolIndex + 1);
				properties.add(newProperty(User.PROPERTY_FIRST_NAME, firstName));
				properties.add(newProperty(User.PROPERTY_LAST_NAME, lastName));
			} else {
				// just store full name in first name field
				properties.add(newProperty(User.PROPERTY_FIRST_NAME, fullName));
			}
		}
	}

	public static boolean tryShowCreateUserFragment(@Nonnull Account account, @Nonnull BaseFragmentActivity activity) {
		final Realm realm = account.getRealm();
		if (realm.canCreateUsers()) {
			final MessengerMultiPaneFragmentManager mpfm = activity.getMultiPaneFragmentManager();
			mpfm.setSecondOrMainFragment(realm.getCreateUserFragmentClass(), newCreateUserArguments(account), CREATE_USER_FRAGMENT_TAG);
			return true;
		}

		return false;
	}

	public static boolean tryShowEditUserFragment(@Nonnull User user, @Nonnull final BaseFragmentActivity activity) {
		try {
			final Account account = App.getAccountService().getAccountByEntity(user.getEntity());
			final Realm realm = account.getRealm();
			if (realm.canCreateUsers()) {
				final Bundle fragmentArgs = newEditUserArguments(account, user);
				// fix for EventManager. Event manager doesn't support removal/creation of listeners in onEvent() method => let's do it on the next main loop cycle
				getUiHandler().post(new Runnable() {
					@Override
					public void run() {
						final DialogFragment fragment = (DialogFragment) Fragment.instantiate(activity, realm.getCreateUserFragmentClass().getName(), fragmentArgs);
						Fragments2.showDialog(fragment, CREATE_USER_FRAGMENT_TAG, activity.getSupportFragmentManager(), true, false);
					}
				});
				return true;
			}
		} catch (UnsupportedAccountException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return false;
	}

	@Nonnull
	public static AProperty newOnlineProperty(boolean online) {
		return newProperty(User.PROPERTY_ONLINE, String.valueOf(online));
	}

	public static void fillContactPresenceViews(@Nonnull final Context context,
												@Nonnull ViewAwareTag viewTag,
												@Nonnull final User contact,
												@Nullable Account account) {
		final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
		final View contactCall = viewTag.getViewById(R.id.mpp_li_contact_call_view);
		final View contactDivider = viewTag.getViewById(R.id.mpp_li_contact_divider_view);
		if (account == null || !account.canCall(contact)) {
			contactCall.setOnClickListener(null);
			contactCall.setVisibility(GONE);
			contactDivider.setVisibility(GONE);

			if (contact.isOnline()) {
				contactOnline.setVisibility(VISIBLE);
			} else {
				contactOnline.setVisibility(INVISIBLE);
			}
		} else {
			contactOnline.setVisibility(GONE);

			// for some reason following properties set from styles xml are not applied => apply them manually
			contactCall.setFocusable(false);
			contactCall.setFocusableInTouchMode(false);

			contactCall.setVisibility(VISIBLE);
			contactCall.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getEventManager(context).fire(call_contact.newEvent(contact));
				}
			});

			contactDivider.setVisibility(VISIBLE);
		}
	}
}
