package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.users.ContactListItem;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:24 PM
 */
public class ChatListItem implements ListItem, Comparable<ChatListItem>, ChatEventListener, Checkable {

    @Nonnull
    private static final String TAG_PREFIX = "chat_list_item_view_";

    @Nonnull
    private User user;

    @Nonnull
    private Chat chat;

    @Nullable
    private ChatMessage lastChatMessage;

    private boolean checked = false;

    public ChatListItem(@Nonnull User user, @Nonnull Chat chat, @Nullable Context context) {
        this.user = user;
        this.chat = chat;
        if (context != null) {
            // todo serso: calling on the main thread
            this.lastChatMessage = getChatService().getLastMessage(chat.getRealmChat());
        }
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(ChatGuiEventType.newChatClicked(chat));
            }
        };
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @Nonnull
    public User getUser() {
        return user;
    }

    @Nonnull
    public Chat getChat() {
        return chat;
    }

    @Nullable
    public ChatMessage getLastMessage() {
        return this.lastChatMessage;
    }

    @Nonnull
    @Override
    public View updateView(@Nonnull Context context, @Nonnull View view) {
        if (String.valueOf(view.getTag()).startsWith(TAG_PREFIX)) {
            fillView((ViewGroup) view, context);
            return view;
        } else {
            return build(context);
        }
    }

    @Nonnull
    @Override
    public View build(@Nonnull Context context) {
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_chat).build(context);
        fillView(view, context);
        return view;
    }

    @Nonnull
    private String createTag() {
        return TAG_PREFIX + chat.getRealmChat().getEntityId();
    }

    private void fillView(@Nonnull final ViewGroup view, @Nonnull Context context) {
        final String tag = createTag();

        ContactListItem.toggleSelected(view, checked);

        if (!tag.equals(view.getTag())) {
            view.setTag(tag);

            final ImageView chatIcon = (ImageView) view.findViewById(R.id.chat_icon);
            getChatService().setChatIcon(chatIcon, chat, user);


            final ChatMessage lastMessage = getLastMessage();

            final TextView chatTitle = (TextView) view.findViewById(R.id.chat_title);
            chatTitle.setText(getDisplayName(chat, lastMessage, user));

            final TextView lastMessageTextTime = (TextView) view.findViewById(R.id.last_message_text_time);
            final TextView lastMessageText = (TextView) view.findViewById(R.id.last_message_text);
            if (lastMessage != null) {
                lastMessageText.setText(getMessageTitle(chat, lastMessage, user));
                lastMessageTextTime.setText(getMessageTime(lastMessage));
            } else {
                lastMessageText.setText("");
                lastMessageTextTime.setText("");
            }
        }
    }

    @Nonnull
    public static CharSequence getMessageTime(@Nonnull ChatMessage message) {
        final LocalDate sendDate = message.getSendDate().toLocalDate();
        final LocalDate today = DateTime.now().toLocalDate();
        final LocalDate yesterday = today.minusDays(1);

        if (sendDate.toDateTimeAtStartOfDay().compareTo(today.toDateTimeAtStartOfDay()) == 0) {
            // today
            // print time
            return DateTimeFormat.shortTime().print(message.getSendDate());
        } else if (sendDate.toDateTimeAtStartOfDay().compareTo(yesterday.toDateTimeAtStartOfDay()) == 0) {
            // yesterday
            // todo serso: translate
            return "Yesterday";// + ", " + DateTimeFormat.shortTime().print(sendDate);
        } else {
            // the days before yesterday
            return DateTimeFormat.shortDate().print(sendDate);
        }
    }

    @Nonnull
    private ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @Nonnull
    private CharSequence getMessageTitle(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull User user) {
        final String authorName = getMessageAuthorDisplayName(chat, message, user);
        if (Strings.isEmpty(authorName)) {
            return Html.fromHtml(message.getBody());
        } else {
            return authorName + ": " + Html.fromHtml(message.getBody());
        }
    }

    @Nonnull
    private String getMessageAuthorDisplayName(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull User user) {
        final User author = message.getAuthor();
        if (user.equals(author)) {
            return "Me";
        } else {
            if (!chat.isPrivate()) {
                return author.getDisplayName();
            } else {
                return "";
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatListItem)) return false;

        ChatListItem that = (ChatListItem) o;

        if (!chat.equals(that.chat)) return false;
        if (!user.equals(that.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + chat.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // NOTE: this code is used inside the ArrayAdapter for filtering
        return getDisplayName(chat, getLastMessage(), user);
    }

    @Nonnull
    public static String getDisplayName(@Nonnull Chat chat, @Nullable ChatMessage lastMessage, @Nonnull User user) {
        if (lastMessage == null) {
            return "";
        } else {
            return getChatTitle(chat, lastMessage, user);
        }
    }

    @Nonnull
    private static String getChatTitle(@Nonnull Chat chat, @Nonnull ChatMessage message, @Nonnull User user) {
        final String title = message.getTitle();
        if (Strings.isEmpty(title) || title.equals(" ... ")) {

            if (chat.isPrivate()) {
                final User secondUser = message.getSecondUser(user);
                if (secondUser != null) {
                    return secondUser.getDisplayName();
                } else {
                    return "";
                }
            } else {
                return "";
            }
        } else {
            return title;
        }
    }

    @Override
    public int compareTo(@Nonnull ChatListItem another) {
        return this.toString().compareTo(another.toString());
    }

    @Override
    public void onChatEvent(@Nonnull Chat eventChat, @Nonnull ChatEventType chatEventType, @Nullable Object data) {
        if (chatEventType == ChatEventType.changed) {
            if (eventChat.equals(chat)) {
                chat = eventChat;
            }
        }

        if (chatEventType == ChatEventType.last_message_changed) {
            if (eventChat.equals(chat)) {
                lastChatMessage = (ChatMessage) data;
            }
        }
    }

    @Override
    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        this.checked = !checked;
    }

    public static final class Comparator implements java.util.Comparator<ChatListItem> {

        @Nonnull
        private static final Comparator instance = new Comparator();

        private Comparator() {
        }

        @Nonnull
        public static Comparator getInstance() {
            return instance;
        }

        @Override
        public int compare(@Nonnull ChatListItem lhs, @Nonnull ChatListItem rhs) {
            final ChatMessage llm = lhs.getLastMessage();
            final ChatMessage rlm = rhs.getLastMessage();
            if (llm != null && rlm != null) {
                return -llm.getSendDate().compareTo(rlm.getSendDate());
            } else if (llm != null) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
