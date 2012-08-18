package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.users.ContactListItem;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.StringUtils;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:24 PM
 */
public class ChatListItem implements ListItem, Comparable<ChatListItem>, ChatEventListener, Checkable {

    @NotNull
    private static final String TAG_PREFIX = "chat_list_item_view_";

    @NotNull
    private User user;

    @NotNull
    private Chat chat;

    @Nullable
    private ChatMessage lastChatMessage;

    private boolean checked = false;

    public ChatListItem(@NotNull User user, @NotNull Chat chat, @Nullable Context context) {
        this.user = user;
        this.chat = chat;
        if (context != null) {
            // todo serso: calling on the main thread
            this.lastChatMessage = getChatService().getLastMessage(chat.getId(), context);
        }
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@NotNull Context context, @NotNull ListAdapter<? extends ListItem> adapter, @NotNull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(ChatGuiEventType.newChatClicked(chat));
            }
        };
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public Chat getChat() {
        return chat;
    }

    @Nullable
    public ChatMessage getLastMessage() {
        return this.lastChatMessage;
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
        final ViewGroup view = (ViewGroup) ViewFromLayoutBuilder.newInstance(R.layout.msg_list_item_chat).build(context);
        fillView(view, context);
        return view;
    }

    @NotNull
    private String createTag() {
        return TAG_PREFIX + chat.getId();
    }

    private void fillView(@NotNull final ViewGroup view, @NotNull Context context) {
        final String tag = createTag();

        ContactListItem.toggleSelected(view, checked);

        if (!tag.equals(view.getTag())) {
            view.setTag(tag);

            final ImageView chatIcon = (ImageView) view.findViewById(R.id.chat_icon);
            getChatService().setChatIcon(chatIcon, chat, user, context);


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

    @NotNull
    public static CharSequence getMessageTime(@NotNull ChatMessage message) {
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

    @NotNull
    private ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @NotNull
    private CharSequence getMessageTitle(@NotNull Chat chat, @NotNull ChatMessage message, @NotNull User user) {
        final String authorName = getMessageAuthorDisplayName(chat, message, user);
        if (StringUtils.isEmpty(authorName)) {
            return Html.fromHtml(message.getBody());
        } else {
            return authorName + ": " + Html.fromHtml(message.getBody());
        }
    }

    @NotNull
    private String getMessageAuthorDisplayName(@NotNull Chat chat, @NotNull ChatMessage message, @NotNull User user) {
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

    @NotNull
    public static String getDisplayName(@NotNull Chat chat, @Nullable ChatMessage lastMessage, @NotNull User user) {
        if (lastMessage == null) {
            return "";
        } else {
            return getChatTitle(chat, lastMessage, user);
        }
    }

    @NotNull
    private static String getChatTitle(@NotNull Chat chat, @NotNull ChatMessage message, @NotNull User user) {
        final String title = message.getTitle();
        if (StringUtils.isEmpty(title) || title.equals(" ... ")) {

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
    public int compareTo(@NotNull ChatListItem another) {
        return this.toString().compareTo(another.toString());
    }

    @Override
    public void onChatEvent(@NotNull Chat eventChat, @NotNull ChatEventType chatEventType, @Nullable Object data) {
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

        @NotNull
        private static final Comparator instance = new Comparator();

        private Comparator() {
        }

        @NotNull
        public static Comparator getInstance() {
            return instance;
        }

        @Override
        public int compare(@NotNull ChatListItem lhs, @NotNull ChatListItem rhs) {
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
