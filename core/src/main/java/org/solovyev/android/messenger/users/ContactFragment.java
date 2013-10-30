package org.solovyev.android.messenger.users;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.properties.AProperty;
import org.solovyev.android.view.ViewFromLayoutBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static org.solovyev.android.messenger.App.getUserService;

public class ContactFragment extends BaseUserFragment {

	@Nonnull
	public static final String FRAGMENT_TAG = "contact-info";

	public ContactFragment() {
		super(R.layout.mpp_fragment_contact);
	}

	@Nonnull
	public static MultiPaneFragmentDef newViewContactFragmentDef(@Nonnull Context context, @Nonnull Account account, @Nonnull Entity contact, boolean addToBackStack) {
		final Bundle arguments = newUserArguments(account, contact);
		return MultiPaneFragmentDef.forClass(FRAGMENT_TAG, addToBackStack, ContactFragment.class, context, arguments, new ContactFragmentReuseCondition(contact));
	}

	@Override
	public void onViewCreated(@Nonnull View root, Bundle savedInstanceState) {
		super.onViewCreated(root, savedInstanceState);
		final User contact = getUser();

		final TextView contactName = (TextView) root.findViewById(R.id.mpp_fragment_title);
		contactName.setText(contact.getDisplayName());

		final ImageView contactIcon = (ImageView) root.findViewById(R.id.mpp_contact_icon_imageview);
		getUserService().getIconsService().setUserPhoto(contact, contactIcon);

		final ViewGroup propertiesViewGroup = (ViewGroup) root.findViewById(R.id.mpp_contact_properties_viewgroup);
		final List<AProperty> contactProperties = getAccountService().getUserProperties(contact, this.getActivity());
		for (AProperty contactProperty : contactProperties) {
			final View propertyView = ViewFromLayoutBuilder.newInstance(R.layout.mpp_property).build(this.getActivity());

			final TextView propertyLabel = (TextView) propertyView.findViewById(R.id.mpp_property_label);
			propertyLabel.setText(contactProperty.getName());

			final TextView propertyValue = (TextView) propertyView.findViewById(R.id.mpp_property_value);
			propertyValue.setText(contactProperty.getValue());

			propertiesViewGroup.addView(propertyView);
		}

		getMultiPaneManager().onPaneCreated(getActivity(), root, true);
	}

	@Nullable
	@Override
	protected CharSequence getFragmentTitle() {
		return getUser().getDisplayName();
	}
}
