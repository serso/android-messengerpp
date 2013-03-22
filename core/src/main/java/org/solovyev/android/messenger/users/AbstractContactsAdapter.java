package org.solovyev.android.messenger.users;

import android.content.Context;
import android.util.Log;
import android.widget.Filter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.android.list.PrefixFilter;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.common.JPredicate;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:55 PM
 */
public abstract class AbstractContactsAdapter extends MessengerListItemAdapter<ContactListItem> {

    @Nonnull
    private MessengerContactsMode mode = MessengerContactsFragment.DEFAULT_CONTACTS_MODE;

    @Nonnull
    private final RealmService realmService;

    public AbstractContactsAdapter(@Nonnull Context context, @Nonnull RealmService realmService) {
        super(context, new ArrayList<ContactListItem>());
        this.realmService = realmService;
    }

    @Override
    public void onEvent(@Nonnull UserEvent event) {
        super.onEvent(event);

        final UserEventType type = event.getType();
        final User eventUser = event.getUser();

        if (type == UserEventType.contact_removed) {
            final String contactId = event.getDataAsUserId();
            removeListItem(contactId);
        }

        if (type == UserEventType.contact_added) {
            final User contact = event.getDataAsUser();
            if (canAddContact(contact)) {
                addListItem(contact);
            }
        }

        if (type == UserEventType.contact_added_batch) {
            // first - filter contacts which can be added
            // then - transform user objects to list items objects
            final List<User> contacts = event.getDataAsUsers();
            addListItems(Lists.newArrayList(Iterables.transform(Iterables.filter(contacts, new Predicate<User>() {
                @Override
                public boolean apply(@Nullable User contact) {
                    assert contact != null;
                    return canAddContact(contact);
                }
            }), new Function<User, ContactListItem>() {
                @Override
                public ContactListItem apply(@Nullable User contact) {
                    assert contact != null;
                    return createListItem(contact);
                }
            })));
        }

        if (event.isOfType(UserEventType.changed)) {
            final ContactListItem listItem = findInAllElements(eventUser);
            if (listItem != null) {
                listItem.onEvent(event);
                onListItemChanged(eventUser);
            }
        }

        if (event.isOfType(UserEventType.contact_online, UserEventType.contact_offline)) {
            final ContactListItem listItem = findInAllElements(event.getDataAsUser());
            if (listItem != null) {
                listItem.onEvent(event);
                onListItemChanged(eventUser);
            }
            refilter();
        }
    }

    @Nullable
    protected ContactListItem findInAllElements(@Nonnull User contact) {
        return Iterables.find(getAllElements(), Predicates.<ContactListItem>equalTo(createListItem(contact)), null);
    }


    protected void removeListItem(@Nonnull String contactId) {
        final User contact = Users.newEmptyUser(contactId);
        removeListItem(contact);
    }

    protected void removeListItem(@Nonnull User contact) {
        remove(createListItem(contact));
    }

    protected void addListItem(@Nonnull User contact) {
        addListItem(createListItem(contact));
    }

    @Nonnull
    private ContactListItem createListItem(@Nonnull User contact) {
        return new ContactListItem(contact, realmService);
    }

    protected abstract void onListItemChanged(@Nonnull User contact);

    protected abstract boolean canAddContact(@Nonnull User contact);

    public void setMode(@Nonnull MessengerContactsMode newMode) {
        boolean refilter = this.mode != newMode;
        this.mode = newMode;
        if (refilter) {
            refilter();
        }
    }

    @Nonnull
    @Override
    protected Filter createFilter() {
        return new ContactsFilter(new AdapterHelper());
    }

    private class ContactsFilter extends AdapterFilter<ContactListItem> {

        @Nonnull
        private ContactFilter emptyPrefixFilter = new ContactFilter(null);

        private ContactsFilter(@Nonnull Helper<ContactListItem> helper) {
            super(helper);
        }

        @Override
        protected boolean doFilterOnEmptyString() {
            return true;
        }

        @Override
        protected JPredicate<ContactListItem> getFilter(@Nullable final CharSequence prefix) {
            return Strings.isEmpty(prefix) ? emptyPrefixFilter : new ContactFilter(prefix);
        }

        private class ContactFilter implements JPredicate<ContactListItem> {

            @Nullable
            private final CharSequence prefix;

            public ContactFilter(@Nullable CharSequence prefix) {
                this.prefix = prefix;
            }

            @Override
            public boolean apply(@Nullable ContactListItem listItem) {
                if (listItem != null) {
                    final User contact = listItem.getContact();

                    boolean shown = true;
                    if (mode == MessengerContactsMode.all_contacts) {
                        shown = true;
                    } else if (mode == MessengerContactsMode.only_online_contacts) {
                        shown = contact.isOnline();
                    }

                    if (shown) {
                        if (!Strings.isEmpty(prefix)) {
                            assert prefix != null;
                            shown = new PrefixFilter<String>(prefix.toString().toLowerCase()).apply(contact.getDisplayName());
                            if ( !shown ) {
                                Log.d("Filtering", contact.getDisplayName() + " is filtered due to filter " + prefix);
                            }
                        }
                    } else {
                        Log.d("Filtering", contact.getDisplayName() + " is filtered due to mode " + mode);
                    }

                    return !shown;
                }

                return true;
            }
        }
    }
}
