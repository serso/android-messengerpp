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
public final class ContactListItem extends AbstractMessengerListItem<UiContact> /*implements UserEventListener*/ {

    @Nonnull
    private static final String TAG_PREFIX = "contact_list_item_";

    public ContactListItem(@Nonnull User contact) {
        this(UiContact.newInstance(contact, getUnreadMessagesCount(contact)));
    }

    private static int getUnreadMessagesCount(@Nonnull User contact) {
        return MessengerApplication.getServiceLocator().getUserService().getUnreadMessagesCount(contact.getEntity());
    }

    public ContactListItem(@Nonnull UiContact contact) {
        super(TAG_PREFIX, contact, R.layout.mpp_list_item_contact);
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

        switch (type) {
            case changed:
                if (contact.equals(eventUser)) {
                    setData(getData().copyForNewUser(eventUser));
                }
                break;
            case contact_offline:
            case contact_online:
                final User eventContact = event.getDataAsUser();
                if (contact.equals(eventContact)) {
                    setData(getData().copyForNewUser(eventContact));
                }
                break;
            case unread_messages_count_changed:
                if (contact.equals(eventUser)) {
                    setData(getData().copyForNewUnreadMessagesCount(event.getDataAsInteger()));
                }
                break;
        }
    }

    @Nonnull
    public User getContact() {
        return getData().getContact();
    }

    @Nonnull
    @Override
    protected CharSequence getDisplayName(@Nonnull UiContact contact, @Nonnull Context context) {
        String displayName = contact.getContact().getDisplayName();
        if ( contact.getUnreadMessagesCount() > 0 ) {
            displayName += " (" + contact.getUnreadMessagesCount() + ")";
        }
        return displayName;
    }

    @Override
    protected void fillView(@Nonnull UiContact contact, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final ImageView contactIcon = viewTag.getViewById(R.id.mpp_li_contact_icon_imageview);
        MessengerApplication.getServiceLocator().getUserService().setUserIcon(contact.getContact(), contactIcon);

        final TextView contactName = viewTag.getViewById(R.id.mpp_li_contact_name_textview);
        contactName.setText(getDisplayName());

        final RealmService realmService = MessengerApplication.getServiceLocator().getRealmService();

        final TextView accountName = viewTag.getViewById(R.id.mpp_li_contact_account_textview);
        if (realmService.isOneRealm()) {
            accountName.setVisibility(View.GONE);
        } else {
            accountName.setVisibility(View.VISIBLE);
            final Realm realm = realmService.getRealmById(getContact().getEntity().getRealmId());
            accountName.setText("[" + realm.getUser().getDisplayName() + "]");
        }

        final View contactOnline = viewTag.getViewById(R.id.mpp_li_contact_online_view);
        if (contact.getContact().isOnline()) {
            contactOnline.setVisibility(View.VISIBLE);
        } else {
            contactOnline.setVisibility(View.INVISIBLE);
        }
    }
}
