package org.solovyev.android.messenger.chats;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.view.AbstractMessengerListItem;
import org.solovyev.android.messenger.view.ViewAwareTag;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/7/12
 * Time: 6:24 PM
 */
public class ChatListItem extends AbstractMessengerListItem<UserChat> /*implements ChatEventListener*/ {

    @Nonnull
    private static final String TAG_PREFIX = "chat_list_item_";

    public ChatListItem(@Nonnull User user, @Nonnull Chat chat, @Nullable Context context) {
        super(TAG_PREFIX, R.layout.mpp_list_item_chat, UserChat.newInstance(user, chat, loadLastChatMessage(chat, context)));
    }

    @Nullable
    private static ChatMessage loadLastChatMessage(Chat chat, Context context) {
        if (context != null) {
            // todo serso: calling on the main thread
            return getChatService().getLastMessage(chat.getEntity());
        } else {
            return null;
        }
    }

    @Override
    public OnClickAction getOnClickAction() {
        return new OnClickAction() {
            @Override
            public void onClick(@Nonnull Context context, @Nonnull ListAdapter<? extends ListItem> adapter, @Nonnull ListView listView) {
                final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                eventManager.fire(ChatGuiEventType.chat_clicked.newEvent(getChat()));
            }
        };
    }

    @Override
    public OnClickAction getOnLongClickAction() {
        return null;
    }

    @Nonnull
    public User getUser() {
        return getData().getUser();
    }

    @Nonnull
    public Chat getChat() {
        return getData().getChat();
    }

    @Nullable
    public ChatMessage getLastMessage() {
        return this.getData().getLastMessage();
    }

    @Nonnull
    @Override
    protected String getDisplayName(@Nonnull UserChat userChat, @Nonnull Context context) {
        return Chats.getDisplayName(userChat.getChat(), userChat.getLastMessage(), userChat.getUser());
    }

    @Override
    protected void fillView(@Nonnull UserChat userChat, @Nonnull Context context, @Nonnull ViewAwareTag viewTag) {
        final Chat chat = userChat.getChat();
        final User user = userChat.getUser();

        final ImageView chatIcon = viewTag.getViewById(R.id.mpp_li_chat_icon_imageview);
        getChatService().setChatIcon(chat, chatIcon);

        final ChatMessage lastMessage = getLastMessage();

        final TextView chatTitle = viewTag.getViewById(R.id.mpp_li_chat_title_textview);
        chatTitle.setText(getDisplayName());

        final TextView lastMessageTextTime = viewTag.getViewById(R.id.mpp_li_last_message_text_time_textview);
        final TextView lastMessageText = viewTag.getViewById(R.id.mpp_li_last_message_text_textview);
        if (lastMessage != null) {
            lastMessageText.setText(Messages.getMessageTitle(chat, lastMessage, user));
            lastMessageTextTime.setText(Messages.getMessageTime(lastMessage));
        } else {
            lastMessageText.setText("");
            lastMessageTextTime.setText("");
        }
    }

    @Nonnull
    private static ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    /*@Override*/
    public void onEvent(@Nonnull ChatEvent event) {
        final Chat chat = getChat();
        final Chat eventChat = event.getChat();

        if (event.getType() == ChatEventType.changed) {
            if (eventChat.equals(chat)) {
                setData(getData().copyForNewChat(eventChat));
            }
        }

        if (event.getType() == ChatEventType.last_message_changed) {
            if (eventChat.equals(chat)) {
                setData(getData().copyForNewLastMessage(event.getDataAsChatMessage()));
            }
        }
    }

}
