package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:04 PM
 */
public class ContactListItem extends AbstractMessengerListItem<User> implements UserEventListener, Comparable<ContactListItem> {

    @Nonnull
    private static final String TAG_PREFIX = "contact_list_item_view_";

    public ContactListItem(@Nonnull User contact) {
        super(TAG_PREFIX, R.layout.mpp_list_item_contact, contact);
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull final Context context, @Nonnull final ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(ContactGuiEventType.newContactClicked(getContact()));

            }
        };
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @Override
    public void onUserEvent(@Nonnull User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data) {
        final User contact = getContact();

        if (userEventType == UserEventType.changed) {
            if (eventUser.equals(contact)) {
                setData(eventUser);
            }
        }

        if (userEventType == UserEventType.contact_offline || userEventType == UserEventType.contact_online) {
            if (contact.equals(data)) {
                setData((User) data);
            }
        }
    }

    @Override
    public String toString() {
        // NOTE: this code is used inside the ArrayAdapter for filtering
        return getContact().getDisplayName();
    }

    @Override
    public int compareTo(@Nonnull ContactListItem another) {
        return this.toString().compareTo(another.toString());
    }

    @Nonnull
    public User getContact() {
        return getData();
    }

    @Nonnull
    @Override
    protected String getDataId(@Nonnull User contact) {
        return contact.getRealmUser().getEntityId();
    }

    @Override
    protected void fillView(@Nonnull User contact, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView contactIcon = viewTag.getViewById(R.id.mpp_contact_icon);
        MessengerApplication.getServiceLocator().getUserService().setUserIcon(contact, contactIcon);

        final TextView contactName = viewTag.getViewById(R.id.mpp_contact_name);
        contactName.setText(contact.getDisplayName());

        final TextView contactOnline = viewTag.getViewById(R.id.mpp_contact_online);
        if (contact.isOnline()) {
            contactOnline.setText("·");
        } else {
            contactOnline.setText("");
        }
    }
}
