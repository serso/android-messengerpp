package org.solovyev.android.messenger.users;

import android.content.Context;
import android.widget.Filter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.list.AdapterFilter;
import org.solovyev.android.list.PrefixFilter;
import org.solovyev.android.messenger.MessengerListItemAdapter;
import org.solovyev.common.JPredicate;
import org.solovyev.common.text.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:55 PM
 */
public abstract class AbstractContactsAdapter extends MessengerListItemAdapter<ContactListItem> implements UserEventListener {

    @Nonnull
    private MessengerContactsMode mode = MessengerContactsMode.all_contacts;


    public AbstractContactsAdapter(@Nonnull Context context) {
        super(context, new ArrayList<ContactListItem>());
    }

    @Override
    public void onUserEvent(@Nonnull final User eventUser, @Nonnull UserEventType userEventType, @Nullable Object data) {
        super.onUserEvent(eventUser, userEventType, data);

        if (userEventType == UserEventType.contact_removed) {
            if (eventUser.equals(getUser())) {
                final String contactId = (String) data;
                removeListItem(eventUser, contactId);
            }
        }

        if (userEventType == UserEventType.contact_added) {
            if (eventUser.equals(getUser())) {
                final User contact = (User) data;
                if (canAddContact(contact)) {
                    addListItem(eventUser, contact);
                }
            }
        }

        if (userEventType == UserEventType.contact_added_batch) {
            if (eventUser.equals(getUser())) {
                // first - filter contacts which can be added
                // then - transform user objects to list items objects
                addListItems(Lists.newArrayList(Iterables.transform(Iterables.filter((List<User>) data, new Predicate<User>() {
                    @Override
                    public boolean apply(@javax.annotation.Nullable User contact) {
                        assert contact != null;
                        return canAddContact(contact);
                    }
                }), new Function<User, ContactListItem>() {
                    @Override
                    public ContactListItem apply(@javax.annotation.Nullable User contact) {
                        return createListItem(eventUser, contact);
                    }
                })));
            }
        }

        if (userEventType == UserEventType.changed) {

            final ContactListItem listItem = findInAllElements(getUser(), eventUser);
            if (listItem != null) {
                listItem.onUserEvent(eventUser, userEventType, data);
                onListItemChanged(getUser(), eventUser);
            }
            //notifyDataSetChanged();
        }

        if ( userEventType == UserEventType.contact_online || userEventType == UserEventType.contact_offline ) {
            if (eventUser.equals(getUser())) {
                refilter();
            }
        }
    }

    protected User getUser() {
        // todo serso: continue
        throw new UnsupportedOperationException();
    }


    @Nullable
    protected ContactListItem findInAllElements(@Nonnull User user, @Nonnull User contact) {
        return Iterables.find(getAllElements(), Predicates.<ContactListItem>equalTo(createListItem(user, contact)), null);
    }


    protected void removeListItem(@Nonnull User user, @Nonnull String contactId) {
        // todo serso: not good solution => better way is to load full user object for contact (but it can take long time)
        final User contact = UserImpl.newFakeInstance(contactId);
        removeListItem(user, contact);
    }

    protected void removeListItem(@Nonnull User user, @Nonnull User contact) {
        remove(createListItem(user, contact));
    }

    protected void addListItem(@Nonnull User user, @Nonnull User contact) {
        addListItem(createListItem(user, contact));
    }

    @Nonnull
    private ContactListItem createListItem(@Nonnull User user, @Nonnull User contact) {
        return new ContactListItem(user, contact);
    }

    protected abstract void onListItemChanged(@Nonnull User user, @Nonnull User contact);

    protected abstract boolean canAddContact(@Nonnull User contact);

    public void setMode(@Nonnull MessengerContactsMode newMode) {
        boolean refilter = this.mode != newMode;
        this.mode = newMode;
        if ( refilter ) {
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
                    if ( mode == MessengerContactsMode.all_contacts ) {
                        shown = true;
                    } else if ( mode == MessengerContactsMode.only_online_contacts ) {
                         if ( contact.isOnline() ) {
                             shown = true;
                         } else {
                             shown = false;
                         }
                    }

                    if (shown) {
                        if (!Strings.isEmpty(prefix)) {
                            shown = new PrefixFilter<ContactListItem>(prefix.toString().toLowerCase()).apply(listItem);
                        }
                    }

                    return !shown;
                }

                return true;
            }
        }
    }
}
