package org.solovyev.android.messenger.realms.xmpp;

import android.content.Context;
import android.util.Log;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.RealmConnection;
import org.solovyev.android.messenger.chats.ApiChat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.chats.Chats;
import org.solovyev.android.messenger.chats.RealmChatService;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.ChatMessageService;
import org.solovyev.android.messenger.messages.LiteChatMessageImpl;
import org.solovyev.android.messenger.messages.Messages;
import org.solovyev.android.messenger.realms.AbstractRealm;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmDef;
import org.solovyev.android.messenger.users.RealmUserService;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.Users;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class XmppRealm extends AbstractRealm<XmppRealmConfiguration> {

    private static final String TAG = XmppRealm.class.getSimpleName();

    public XmppRealm(@Nonnull String id,
                     @Nonnull RealmDef realmDef,
                     @Nonnull User user,
                     @Nonnull XmppRealmConfiguration configuration) {
        super(id, realmDef, user, configuration);
    }

    @Nonnull
    @Override
    protected RealmConnection newRealmConnection0(@Nonnull Context context) {
        return new XmppRealmConnection(this, context);
    }

    @Nonnull
    @Override
    public String getDisplayName(@Nonnull Context context) {
        final StringBuilder sb = new StringBuilder();

        sb.append(context.getText(getRealmDef().getNameResId()));
        sb.append("@");
        sb.append(getConfiguration().getServer());

        return sb.toString();
    }

    @Nonnull
    @Override
    public RealmUserService getRealmUserService() {
        return new XmppRealmUserService(this, getXmppConnectionAware());
    }

    @Nonnull
    private XmppConnectionAware getXmppConnectionAware() {
        XmppConnectionAware realmAware = getRealmConnection();
        if ( realmAware == null ) {
            realmAware = TemporaryXmppConnectionAware.newInstance(this);
            Log.w(TAG, "Creation of temporary xmpp connection!");
        }
        return realmAware;
    }

    @Nullable
    protected XmppRealmConnection getRealmConnection() {
        return (XmppRealmConnection) super.getRealmConnection();
    }

    @Nonnull
    @Override
    public RealmChatService getRealmChatService() {
        return new XmppRealmChatService(this, getXmppConnectionAware());
    }

    @Nonnull
    public Entity newUserEntity(@Nonnull String realmUserId) {
        return newEntity(realmUserId);
    }

    @Nonnull
    private Entity newEntity(@Nonnull String realmUserId) {
        final int index = realmUserId.indexOf('/');
        if ( index >= 0 ) {
            return newRealmEntity(realmUserId.substring(0, index));
        } else {
            return newRealmEntity(realmUserId);
        }
    }

    @Nonnull
    public Entity newChatEntity(@Nonnull String realmUserId) {
        return newEntity(realmUserId);
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */


    @Nonnull
    private static ChatService getChatService() {
        return MessengerApplication.getServiceLocator().getChatService();
    }

    @Nonnull
    private static ChatMessageService getChatMessageService() {
        return MessengerApplication.getServiceLocator().getChatMessageService();
    }

    @Nonnull
    static ApiChat toApiChat(@Nonnull Chat smackChat, @Nonnull List<Message> messages, @Nonnull Realm realm) {
        final User participant = toUser(smackChat.getParticipant(), realm);

        final Entity chat;

        final String realmChatId = smackChat.getThreadID();
        if (Strings.isEmpty(realmChatId) ) {
            chat = getChatService().getPrivateChatId(realm.getUser().getEntity(), participant.getEntity());
        } else {
            chat = realm.newChatEntity(realmChatId);
        }

        final List<ChatMessage> chatMessages = toMessages(realm, messages);
        final List<User> participants = Arrays.asList(realm.getUser(), participant);
        return Chats.newPrivateApiChat(chat, participants, chatMessages);
    }

    @Nonnull
    static List<ChatMessage> toMessages(@Nonnull Realm realm, @Nonnull Iterable<Message> messages) {
        return toMessages(realm, messages.iterator());
    }

    static List<ChatMessage> toMessages(@Nonnull Realm realm, @Nonnull Iterator<Message> messages) {
        final List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

        while ( messages.hasNext() ) {
            final Message message = messages.next();
            final ChatMessage chatMessage = toChatMessage(message, realm);
            if (chatMessage != null) {
                chatMessages.add(chatMessage);
            }
        }
        return chatMessages;
    }

    @Nullable
    private static ChatMessage toChatMessage(@Nonnull Message message, @Nonnull Realm realm) {
        final String body = message.getBody();
        if (!Strings.isEmpty(body)) {
            final LiteChatMessageImpl liteChatMessage = Messages.newMessage(getChatMessageService().generateEntity(realm));
            liteChatMessage.setBody(body);
            liteChatMessage.setAuthor(realm.newUserEntity(message.getFrom()));
            liteChatMessage.setRecipient(realm.newUserEntity(message.getTo()));
            liteChatMessage.setSendDate(DateTime.now());
            // new message by default unread
            return Messages.newInstance(liteChatMessage, false);
        } else {
            return null;
        }
    }

    @Nonnull
    private static User toUser(@Nonnull String realmUserId, @Nonnull Realm realm) {
        return Users.newEmptyUser(realm.newUserEntity(realmUserId));
    }
}
