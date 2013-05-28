package org.solovyev.android.messenger.users;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.realms.UnsupportedRealmException;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;
import com.google.inject.Inject;

public final class CompositeUserDialogFragment extends RoboSherlockDialogFragment {

	/*
	**********************************************************************
	*
	*                           CONSTANTS
	*
	**********************************************************************
	*/
	public static final String FRAGMENT_TAG = "composite-user-dialog";

	private static final String TAG = CompositeUserDialogFragment.class.getSimpleName();

	private static final String USER_ENTITY = "user_entity";

	/*
	**********************************************************************
	*
	*                           AUTO INJECTED FIELDS
	*
	**********************************************************************
	*/

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private RealmService realmService;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	private User user;

	private Realm<?> realm;


	/*
	**********************************************************************
	*
	*                           CONSTRUCTORS
	*
	**********************************************************************
	*/

	public CompositeUserDialogFragment() {
	}

	public CompositeUserDialogFragment(@Nonnull User user) {
		this.user = user;
	}

	/*
	**********************************************************************
	*
	*                           METHODS
	*
	**********************************************************************
	*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (user == null) {
			if (savedInstanceState != null) {
				final Parcelable userEntity = savedInstanceState.getParcelable(USER_ENTITY);
				if (userEntity instanceof Entity) {
					user = userService.getUserById((Entity) userEntity);
				}
			}
		}

		if (user == null) {
			throw new IllegalStateException("User is null");
		} else {
			try {
				realm = realmService.getRealmByEntityAware(user);
			} catch (UnsupportedRealmException e) {
				MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		List<CompositeUserChoice> choices = Collections.emptyList();
		if (user != null) {
			if (realm.isCompositeUser(user)) {
				choices = realm.getCompositeUserChoices(user);
			} else {
				Log.w(TAG, "Expecting composite user, got " + user.getClass() + ". User id: " + user.getId());
			}
		}

		final CharSequence[] choicesStrings = new CharSequence[choices.size()];
		for (int i = 0; i < choices.size(); i++) {
			choicesStrings[i] = choices.get(i).getName();
		}
		builder.setItems(choicesStrings, new ChoiceOnClickListener(choices));

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}


	private class ChoiceOnClickListener implements DialogInterface.OnClickListener {
		@Nonnull
		private final List<CompositeUserChoice> choices;

		public ChoiceOnClickListener(@Nonnull List<CompositeUserChoice> choices) {
			this.choices = choices;
		}

		@Override
		public void onClick(DialogInterface dialog, int position) {
			onChoiceSelected(choices.get(position));
		}
	}

	private void onChoiceSelected(@Nonnull CompositeUserChoice compositeUserChoice) {
		if (user != null && realm != null) {
			final User newUser = realm.applyCompositeChoice(compositeUserChoice, user);
			if (realm.isCompositeUserChoicePersisted()) {
				userService.updateUser(newUser);
			}

			final EventManager eventManager = RoboGuice.getInjector(getActivity()).getInstance(EventManager.class);
			eventManager.fire(ContactGuiEventType.newContactClicked(newUser));
		}
	}
}
