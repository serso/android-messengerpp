package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.menu.AMenuItem;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import static android.view.View.GONE;
import static org.solovyev.android.messenger.App.getUserService;

public class ContactFragment extends BaseUserFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "contact-info";

	private ActivityMenu<Menu, MenuItem> menu;

	public ContactFragment() {
		super(R.layout.mpp_fragment_contact);
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactFragmentDef(@Nonnull Context context, @Nonnull Account account, @Nonnull Entity contact, boolean addToBackStack) {
		final Bundle arguments = newUserArguments(account, contact);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, ContactFragment.class, context, arguments, new ContactFragmentReuseCondition(contact));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);

		final User contact = getUser();
		final FragmentActivity activity = getActivity();

		final TextView contactName = (TextView) root.findViewById(R.id.mpp_fragment_title);
		contactName.setText(contact.getDisplayName());

		final ImageView contactIcon = (ImageView) root.findViewById(R.id.mpp_contact_icon_imageview);
		getUserService().getIconsService().setUserPhoto(contact, contactIcon);

		final ViewGroup propertiesViewGroup = (ViewGroup) root.findViewById(R.id.mpp_contact_properties_viewgroup);
		final List<AProperty> contactProperties = getAccountService().getUserProperties(contact, activity);
		for (AProperty contactProperty : contactProperties) {
			final View propertyView = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property).build(activity);

			final TextView propertyLabel = (TextView) propertyView.findViewById(R.id.mpp_property_label);
			propertyLabel.setText(contactProperty.getName());

			final TextView propertyValue = (TextView) propertyView.findViewById(R.id.mpp_property_value);
			propertyValue.setText(contactProperty.getValue());

			propertiesViewGroup.addView(propertyView);
		}

		root.findViewById(R.id.mpp_save_button).setVisibility(GONE);
		root.findViewById(R.id.mpp_remove_button).setVisibility(GONE);

		final View backButton = root.findViewById(R.id.mpp_back_button);
		if(getMultiPaneManager().isTriplePane(activity)) {
			backButton.setVisibility(GONE);
		} else {
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					getFragmentManager().popBackStack();
				}
			});
		}

		getMultiPaneManager().onPaneCreated(activity, root, true);
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getUser().getDisplayName();
	}

		/*
	**********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return this.menu.onOptionsItemSelected(this.getActivity(), item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		this.menu.onPrepareOptionsMenu(this.getActivity(), menu);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>();

		final EditContactMenuItem editContactMenuItem = new EditContactMenuItem();
		menuItems.add(editContactMenuItem);

		this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_contact, menuItems, SherlockMenuHelper.getInstance(), new JPredicate<AMenuItem<MenuItem>>() {
			@Override
			public boolean apply(@Nullable AMenuItem<MenuItem> menuItem) {
				if (menuItem == editContactMenuItem) {
					return !getAccount().getRealm().canEditUsers();
				}
				return false;
			}
		});
		this.menu.onCreateOptionsMenu(this.getActivity(), menu);
	}

	private class EditContactMenuItem implements IdentifiableMenuItem<MenuItem> {

		@Nonnull
		@Override
		public Integer getItemId() {
			return R.id.mpp_menu_edit_contact;
		}

		@Override
		public void onClick(@Nonnull MenuItem menuItem, @Nonnull Context context) {
			if(getAccount().getRealm().canEditUsers()) {
				getEventManager().fire(ContactUiEventType.edit_contact.newEvent(getUser()));
			}
		}
	}
}
