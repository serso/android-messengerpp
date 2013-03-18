package org.solovyev.android.messenger.users;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:04 PM
 */
public final class ContactListItem extends AbstractMessengerListItem<User> /*implements UserEventListener*/ {

    @Nonnull
    private static final String TAG_PREFIX = "contact_list_item_";

    @Nonnull
    private final RealmService realmService;

    public ContactListItem(@Nonnull User contact, @Nonnull RealmService realmService) {
        super(TAG_PREFIX, R.layout.mpp_list_item_contact, contact);
        this.realmService = realmService;
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

    /*@Override*/
    public void onEvent(@Nonnull UserEvent event) {
        final User contact = getContact();

        final UserEventType type = event.getType();
        final User eventUser = event.getUser();

        if (type == UserEventType.changed) {
            if (contact.equals(eventUser)) {
                setData(eventUser);
            }
        }

        if (type == UserEventType.contact_offline || type == UserEventType.contact_online) {
            final User eventContact = event.getDataAsUser();
            if (contact.equals(eventContact)) {
                setData(eventContact);
            }
        }
    }

    @Nonnull
    public User getContact() {
        return getData();
    }

    @Nonnull
    @Override
    protected CharSequence getDisplayName(@Nonnull User contact, @Nonnull Context context) {
        return contact.getDisplayName();
    }

    @Override
    protected void fillView(@Nonnull User contact, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView contactIcon = viewTag.getViewById(R.id.mpp_li_contact_icon_imageview);
        MessengerApplication.getServiceLocator().getUserService().setUserIcon(contact, contactIcon);

        final TextView contactName = viewTag.getViewById(R.id.mpp_li_contact_name_textview);
        contactName.setText(getDisplayName());

        final TextView accountName = viewTag.getViewById(R.id.mpp_li_contact_account_textview);
        if (realmService.isOneRealm()) {
            accountName.setVisibility(View.GONE);
        } else {
            accountName.setVisibility(View.VISIBLE);
            final Realm realm = realmService.getRealmById(getContact().getEntity().getRealmId());
            accountName.setText("[" + realm.getUser().getDisplayName() + "]");
        }

        final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
        if (contact.isOnline()) {
            contactOnline.setVisibility(View.VISIBLE);
        } else {
            contactOnline.setVisibility(View.INVISIBLE);
        }
    }
}
