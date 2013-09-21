package org.solovyev.android.messenger.users;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.solovyev.android.messenger.accounts.UnsupportedAccountException;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;

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
	private static final String DO_NOT_ASK_AGAIN = "do_not_ask_again";

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
	private AccountService accountService;

	/*
	**********************************************************************
	*
	*                           FIELDS
	*
	**********************************************************************
	*/

	private User user;

	private Account<?> account;

	private boolean doNotAskAgain = false;


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

		if (savedInstanceState != null) {
			doNotAskAgain = savedInstanceState.getBoolean(DO_NOT_ASK_AGAIN, false);
		}

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
				account = accountService.getAccountByEntityAware(user);
			} catch (UnsupportedAccountException e) {
				MessengerApplication.getServiceLocator().getExceptionHandler().handleException(e);
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		List<CompositeUserChoice> choices = Collections.emptyList();
		if (user != null) {
			builder.setTitle(account.getCompositeDialogTitleResId());

			if (account.isCompositeUser(user)) {
				choices = account.getCompositeUserChoices(user);
			} else {
				Log.w(TAG, "Expecting composite user, got " + user.getClass() + ". User id: " + user.getId());
			}
		}

		final CharSequence[] choicesStrings = new CharSequence[choices.size()];
		for (int i = 0; i < choices.size(); i++) {
			choicesStrings[i] = choices.get(i).getName();
		}
		builder.setItems(choicesStrings, new ChoiceOnClickListener(choices));

		if (account.isCompositeUserChoicePersisted()) {
			// NOTE: context from builder is used as custom style may be applied here
			final LayoutInflater inflater = LayoutInflater.from(builder.getContext());
			final CheckBox checkBox = (CheckBox) inflater.inflate(R.layout.mpp_dialog_checkbox, null);
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
					doNotAskAgain = checked;
				}
			});
			checkBox.setChecked(doNotAskAgain);

			builder.setView(checkBox);
		}

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (user != null) {
			outState.putParcelable(USER_ENTITY, user.getEntity());
		}

		outState.putBoolean(DO_NOT_ASK_AGAIN, doNotAskAgain);
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
		if (user != null && account != null) {
			final User newUser = account.applyCompositeChoice(compositeUserChoice, user);
			if (account.isCompositeUserChoicePersisted() && doNotAskAgain) {
				userService.updateUser(newUser);
			}

			final EventManager eventManager = RoboGuice.getInjector(getActivity()).getInstance(EventManager.class);
			eventManager.fire(ContactUiEventType.newContactClicked(newUser));
		}
	}
}
