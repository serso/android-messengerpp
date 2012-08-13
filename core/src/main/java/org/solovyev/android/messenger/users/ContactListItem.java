package org.solovyev.android.messenger.users;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.AbstractMessengerListFragment;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.messages.MessengerMessagesActivity;
import org.solovyev.android.view.ViewFromLayoutBuilder;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:04 PM
 */
public class ContactListItem implements ListItem<View>, UserEventListener, Comparable<ContactListItem> {

    @NotNull
    private static final String TAG_PREFIX = "contact_list_item_view_";

    @NotNull
    private User user;

    @NotNull
    private User contact;

    public ContactListItem(@NotNull User user, @NotNull User contact) {
        this.user = user;
        this.contact = contact;
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@NotNull final Context context, @NotNull final ListAdapter<ListItem<? extends View>> adapter, @NotNull ListView listView) {

                final AbstractMessengerListFragment<?> rightPaneFragment = ContactDualPaneController.getInstance().getDualPaneFragment(context);
                if (rightPaneFragment != null) {
                    rightPaneFragment.setSelection(adapter.getPosition(ContactListItem.this));
                } else {

                    new AsyncTask<Void, Void, Chat>() {

                        @Override
                        protected Chat doInBackground(Void... params) {
                            return MessengerApplication.getServiceLocator().getUserService().getPrivateChat(user.getId(), contact.getId(), context);
                        }

                        @Override
                        protected void onPostExecute(@NotNull Chat chat) {
                            super.onPostExecute(chat);

                            MessengerMessagesActivity.startActivity((Activity) context, chat);
                        }

                    }.execute(null, null);
                }

            }
        };
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }


    @NotNull
    @Override
    public View updateView(@NotNull Context context, @NotNull View view) {
        if (String.valueOf(view.getTag()).startsWith(TAG_PREFIX)) {
            fillView((ViewGroup) view, context);
            return view;
        } else {
            return build(context);
        }
    }

    @NotNull
    @Override
    public View build(@NotNull Context context) {
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_contact).build(context);
        fillView(view, context);
        return view;
    }

    @NotNull
    private String createTag() {
        return TAG_PREFIX + contact.getId();
    }

    private void fillView(@NotNull final ViewGroup view, @NotNull Context context) {
        final String tag = createTag();

        if (!tag.equals(view.getTag())) {
            view.setTag(tag);

            final ImageView contactIcon = (ImageView) view.findViewById(R.id.contact_icon);

            MessengerApplication.getServiceLocator().getUserService().setUserIcon(contactIcon, contact, context);

            final TextView contactName = (TextView) view.findViewById(R.id.contact_name);
            contactName.setText(contact.getDisplayName());

            final TextView contactOnline = (TextView) view.findViewById(R.id.contact_online);
            if (contact.isOnline()) {
                contactOnline.setText("·");
            } else {
                contactOnline.setText("");
            }
        }
    }

    @Override
    public void onUserEvent(@NotNull User eventUser, @NotNull UserEventType userEventType, @Nullable Object data) {
        if (userEventType == UserEventType.changed) {
            if (eventUser.equals(user)) {
                user = eventUser;
            }

            if (eventUser.equals(contact)) {
                contact = eventUser;
            }
        }

        if ( userEventType == UserEventType.contact_offline || userEventType == UserEventType.contact_online ) {
            if ( eventUser.equals(user) && contact.equals(data) ) {
                contact = (User) data;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactListItem)) return false;

        ContactListItem that = (ContactListItem) o;

        if (!contact.equals(that.contact)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + contact.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // NOTE: this code is used inside the ArrayAdapter for filtering
        return contact.getDisplayName();
    }

    @Override
    public int compareTo(@NotNull ContactListItem another) {
        return this.toString().compareTo(another.toString());
    }

    @NotNull
    public User getContact() {
        return contact;
    }

    @NotNull
    public User getUser() {
        return user;
    }
}
