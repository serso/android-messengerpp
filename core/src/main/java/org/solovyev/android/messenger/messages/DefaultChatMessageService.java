package org.solovyev.android.messenger.messages;

import android.app.Application;
import android.widget.ImageView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.joda.time.DateTime;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityImpl;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.UserService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:50 PM
 */
@Singleton
public class DefaultChatMessageService implements ChatMessageService {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private ImageLoader imageLoader;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private ChatMessageDao chatMessageDao;

    @Inject
    @Nonnull
    private Application context;

    @Override
    public void init() {
    }

    @Nonnull
    @Override
    public synchronized Entity generateEntity(@Nonnull Realm realm) {
        // todo serso: create normal way of generating ids
        final Entity tmp = EntityImpl.newInstance(realm.getId(), String.valueOf(System.currentTimeMillis()));

        // NOTE: empty realm entity id in order to get real from realm service
        return EntityImpl.newInstance(realm.getId(), ChatMessageService.NO_REALM_MESSAGE_ID, tmp.getEntityId());
    }

    @Nonnull
    @Override
    public List<ChatMessage> getChatMessages(@Nonnull Entity realmChat) {
        return getChatMessageDao().loadChatMessages(realmChat.getEntityId());
    }

    @Override
    public void setMessageIcon(@Nonnull ChatMessage message, @Nonnull ImageView imageView) {
        final Entity author = message.getAuthor();
        userService.setUserIcon(userService.getUserById(author), imageView);
    }

    @Nonnull
    private ChatMessageDao getChatMessageDao() {
        return chatMessageDao;
    }

    @Nullable
    @Override
    public ChatMessage sendChatMessage(@Nonnull Entity user, @Nonnull Chat chat, @Nonnull ChatMessage chatMessage) {
        final Realm realm = getRealmByUser(user);
        final RealmChatService realmChatService = realm.getRealmChatService();

        final String realmMessageId = realmChatService.sendChatMessage(chat, chatMessage);

        final LiteChatMessageImpl message = LiteChatMessageImpl.newInstance(realm.newMessageEntity(realmMessageId == null ? NO_REALM_MESSAGE_ID : realmMessageId, chatMessage.getEntity().getEntityId()));

        message.setAuthor(user);
        if (chat.isPrivate()) {
            final Entity secondUser = chat.getSecondUser();
            message.setRecipient(secondUser);
        }
        message.setBody(chatMessage.getBody());
        message.setTitle(chatMessage.getTitle());
        message.setSendDate(DateTime.now());

        final ChatMessageImpl result = new ChatMessageImpl(message);
        for (LiteChatMessage fwtMessage : chatMessage.getFwdMessages()) {
            result.addFwdMessage(fwtMessage);
        }

        result.setDirection(MessageDirection.out);
        result.setRead(true);

        if ( realm.getRealmDef().notifySentMessagesImmediately() ) {
            chatService.saveChatMessages(chat.getEntity(), Arrays.asList(result), false);
        }

        return result;
    }

    @Nonnull
    private Realm getRealmByUser(@Nonnull Entity userEntity) {
        return realmService.getRealmById(userEntity.getRealmId());
    }
}
