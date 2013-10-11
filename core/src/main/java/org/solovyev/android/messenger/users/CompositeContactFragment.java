package org.solovyev.android.messenger.users;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

import org.solovyev.android.Activities;
import org.solovyev.android.messenger.MultiPaneManager;
import org.solovyev.android.messenger.accounts.AccountService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;

import static org.solovyev.android.Activities.restartActivity;
import static org.solovyev.android.messenger.users.ContactUiEventType.show_composite_user_dialog;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 1:22 AM
 */
public class CompositeContactFragment extends RoboSherlockFragment {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final String ARG_CONTACT = "contact";
	private static final String ARG_NEXT_EVENT_TYPE = "next_event_type";

	@Nonnull
	public static final String FRAGMENT_TAG = "composite-contact-choice";

	@Inject
	@Nonnull
	private UserService userService;

	@Inject
	@Nonnull
	private AccountService accountService;

	@Inject
	@Nonnull
	private EventManager eventManager;

	@Inject
	@Nonnull
	private MultiPaneManager multiPaneManager;

	private User contact;

	private Entity accountContact;

	private ContactUiEventType nextEventType;


    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	public CompositeContactFragment() {
		// will be loaded later by id from fragment arguments
		this.contact = null;
		this.accountContact = null;
	}

	public CompositeContactFragment(@Nonnull User contact) {
		this.contact = contact;
		this.accountContact = contact.getEntity();
	}

	public CompositeContactFragment(@Nonnull Entity accountContact) {
		// will be loaded later by accountContact
		this.contact = null;
		this.accountContact = accountContact;
	}


	@Nonnull
	public static CompositeContactFragment newCompositeContactFragment(@Nonnull User contact, @Nonnull ContactUiEventType nextEventType) {
		final CompositeContactFragment result = new CompositeContactFragment(contact);
		fillArguments(contact.getEntity(), nextEventType, result);
		return result;
	}

	@Nonnull
	public static CompositeContactFragment newCompositeContactFragment(@Nonnull Entity contact, @Nonnull ContactUiEventType nextEventType) {
		final CompositeContactFragment result = new CompositeContactFragment(contact);
		fillArguments(contact, nextEventType, result);
		return result;
	}

	private static void fillArguments(@Nonnull Entity user, @Nonnull ContactUiEventType nextEventType, @Nonnull CompositeContactFragment result) {
		final Bundle args = new Bundle();
		args.putParcelable(ARG_CONTACT, user);
		args.putSerializable(ARG_NEXT_EVENT_TYPE, nextEventType);
		result.setArguments(args);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (contact == null) {
			// restore state
			final Entity accountContact;

			if (this.accountContact != null) {
				accountContact = this.accountContact;
			} else {
				accountContact = getArguments().getParcelable(ARG_CONTACT);
			}

			if (accountContact != null) {
				this.contact = this.userService.getUserById(accountContact);
				this.accountContact = accountContact;
			}
		}

		if (nextEventType == null) {
			nextEventType = (ContactUiEventType) getArguments().getSerializable(ARG_NEXT_EVENT_TYPE);
		}

		if (contact == null || nextEventType == null) {
			restartActivity(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View result = ViewFromLayoutBuilder.newInstance(R.layout.mpp_fragment_composite_contact).build(this.getActivity());

		multiPaneManager.onCreatePane(this.getActivity(), container, result);

		result.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

		return result;
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final View openCompositeChoiceDialogButton = root.findViewById(R.id.mpp_open_composite_choice_dialog_button);
		openCompositeChoiceDialogButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				eventManager.fire(show_composite_user_dialog.newEvent(contact, nextEventType));
			}
		});

		multiPaneManager.onPaneCreated(getActivity(), root);
	}

	@Nonnull
	public Entity getContact() {
		return accountContact;
	}
}
