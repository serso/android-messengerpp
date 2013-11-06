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

package org.solovyev.android.messenger.users;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.Fragments2;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;
import com.google.inject.Inject;

import static org.solovyev.android.Activities.restartActivity;
import static org.solovyev.android.messenger.App.getEventManager;

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

	private static final String ARG_USER_ENTITY = "user_entity";
	private static final String ARG_NEXT_EVENT_TYPE = "next_event_type";

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

	private ContactUiEventType nextEventType;

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
			final Parcelable userEntity = getArguments().getParcelable(ARG_USER_ENTITY);
			if (userEntity instanceof Entity) {
				user = userService.getUserById((Entity) userEntity);
			}
		}

		if(nextEventType == null) {
			nextEventType = (ContactUiEventType) getArguments().getSerializable(ARG_NEXT_EVENT_TYPE);
		}

		if (user == null || nextEventType == null) {
			restartActivity(getActivity());
		} else {
			account = accountService.getAccountByEntity(user.getEntity());
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
			final LayoutInflater inflater = LayoutInflater.from(getActivity());
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
	public void onDestroy() {
		super.onDestroy();

		if (user != null && account != null && nextEventType != null && account.isCompositeUserDefined(user)) {
			getEventManager(getActivity()).fire(nextEventType.newEvent(user));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(DO_NOT_ASK_AGAIN, doNotAskAgain);
	}

	public static void show(@Nonnull User contact, @Nonnull ContactUiEventType nextEventType, @Nonnull FragmentActivity activity) {
		final CompositeUserDialogFragment fragment = new CompositeUserDialogFragment(contact);

		final Bundle args = new Bundle();
		args.putParcelable(ARG_USER_ENTITY, contact.getEntity());
		args.putSerializable(ARG_NEXT_EVENT_TYPE, nextEventType);
		fragment.setArguments(args);

		Fragments2.showDialog(fragment, CompositeUserDialogFragment.FRAGMENT_TAG, activity.getSupportFragmentManager());
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
			user = account.applyCompositeChoice(compositeUserChoice, this.user);
			if (account.isCompositeUserChoicePersisted() && doNotAskAgain) {
				userService.updateUser(user);
			}
		}
	}
}
