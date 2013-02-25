package org.solovyev.android.messenger.messages;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.db.AbstractDbQuery;
import org.solovyev.android.db.AbstractObjectDbExec;
import org.solovyev.android.db.AbstractSQLiteHelper;
import org.solovyev.android.db.AndroidDbUtils;
import org.solovyev.android.db.DbExec;
import org.solovyev.android.db.ListMapper;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.chats.ChatMessageMapper;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.db.StringIdMapper;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.collections.Collections;
import roboguice.inject.ContextSingleton;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: serso
 * Date: 6/11/12
 * Time: 7:41 PM
 */
@ContextSingleton
public class SqliteChatMessageDao extends AbstractSQLiteHelper implements ChatMessageDao {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    public SqliteChatMessageDao(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    @NotNull
    @Override
    public List<String> loadChatMessageIds(@NotNull String chatId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatMessageIdsByChatId(getContext(), chatId, getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<ChatMessage> loadChatMessages(@NotNull String chatId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatMessages(getContext(), chatId, this.userService, getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public String getOldestMessageForChat(@NotNull String chatId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new OldestChatMessageLoader(getContext(), getSqliteOpenHelper(), chatId));
    }

    @Nullable
    @Override
    public ChatMessage loadLastChatMessage(@NotNull String chatId) {
        final Integer lastChatMessageId = AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LastChatMessageLoader(getContext(), getSqliteOpenHelper(), chatId));
        final List<ChatMessage> messages = AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatMessage(getContext(), lastChatMessageId, this.userService, getSqliteOpenHelper()));
        return Collections.getFirstListElement(messages);
    }

    @NotNull
    @Override
    public MergeDaoResult<ChatMessage, String> mergeChatMessages(@NotNull String chatId, @NotNull List<ChatMessage> messages, boolean allowDelete, @NotNull Context context) {
        final MergeDaoResultImpl<ChatMessage, String> result = new MergeDaoResultImpl<ChatMessage, String>(messages);

        final Chat chat = getChatService().getChatById(chatId, context);

        final List<String> messageIdsFromDb = loadChatMessageIds(chatId);
        for (final String chatMessageIdFromDb : messageIdsFromDb) {
            try {
                // message exists both in db and on remote server => just update message properties
                result.addUpdatedObject(Iterables.find(messages, new ChatMessageByIdFinder(chatMessageIdFromDb)));
            } catch (NoSuchElementException e) {
                // message was removed on remote server => need to remove from local db
                result.addRemovedObjectId(chatMessageIdFromDb);
            }
        }

        for (ChatMessage message : messages) {
            try {
                // message exists both in db and on remote server => case already covered above
                Iterables.find(messageIdsFromDb, Predicates.equalTo(message.getId()));
            } catch (NoSuchElementException e) {
                // message was added on remote server => need to add to local db
                if (!messageIdsFromDb.contains(message.getId())) {
                    // no message information in local db is available - full message insertion
                    result.addAddedObject(message);
                }
            }
        }

        final List<DbExec> execs = new ArrayList<DbExec>();

        if (allowDelete) {
            if (!result.getRemovedObjectIds().isEmpty()) {
                execs.addAll(RemoveMessages.newInstances(result.getRemovedObjectIds()));
            }
        }

        for (ChatMessage updatedMessage : result.getUpdatedObjects()) {
            execs.add(new UpdateMessage(updatedMessage, chat));
        }

        for (ChatMessage addedMessage : result.getAddedObjects()) {
            execs.add(new InsertMessage(chat, addedMessage));
        }

        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), execs);

        return result;
    }

    @NotNull
    private ChatService getChatService() {
        return this.chatService;
    }

    private static class LoadChatMessageIdsByChatId extends AbstractDbQuery<List<String>> {

        @NotNull
        private final String chatId;

        private LoadChatMessageIdsByChatId(@NotNull Context context, @NotNull String chatId, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.chatId = chatId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("messages", null, "chat_id = ? ", new String[]{String.valueOf(chatId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<String> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
        }
    }

    private static class ChatMessageByIdFinder implements Predicate<ChatMessage> {

        @NotNull
        private final String messageId;

        public ChatMessageByIdFinder(@NotNull String messageId) {
            this.messageId = messageId;
        }

        @Override
        public boolean apply(@javax.annotation.Nullable ChatMessage message) {
            return message != null && message.getId().equals(messageId);
        }
    }

    public static final class InsertMessage extends AbstractObjectDbExec<ChatMessage> {

        @NotNull
        private final Chat chat;

        public InsertMessage(@NotNull Chat chat, @Nullable ChatMessage chatMessage) {
            super(chatMessage);
            this.chat = chat;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final ChatMessage chatMessage = getNotNullObject();

            final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

            final ContentValues values = new ContentValues();

            values.put("id", chatMessage.getId());
            values.put("chat_id", chat.getId());
            values.put("author_id", chatMessage.getAuthor().getId());
            final User recipient = chatMessage.getRecipient();
            values.put("recipient_id", recipient == null ? null : recipient.getId());
            values.put("send_date", dateTimeFormatter.print(chatMessage.getSendDate()));
            values.put("title", chatMessage.getTitle());
            values.put("body", chatMessage.getBody());

            db.insert("messages", null, values);
        }
    }

    private static final class UpdateMessage extends AbstractObjectDbExec<ChatMessage> {

        @NotNull
        private final Chat chat;

        private UpdateMessage(@NotNull ChatMessage chatMessage, @NotNull Chat chat) {
            super(chatMessage);
            this.chat = chat;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final ChatMessage chatMessage = getNotNullObject();

            final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

            final ContentValues values = new ContentValues();

            values.put("id", chatMessage.getId());
            values.put("chat_id", chat.getId());
            values.put("author_id", chatMessage.getAuthor().getId());
            final User recipient = chatMessage.getRecipient();
            values.put("recipient_id", recipient == null ? null : recipient.getId());
            values.put("send_date", dateTimeFormatter.print(chatMessage.getSendDate()));
            values.put("title", chatMessage.getTitle());
            values.put("body", chatMessage.getBody());

            db.update("messages", values, "id = ?", new String[]{String.valueOf(chatMessage.getId())});
        }
    }

    private static final class RemoveMessages implements DbExec {

        @NotNull
        private List<String> messagesIds;

        private RemoveMessages(@NotNull List<String> messagesIds) {
            this.messagesIds = messagesIds;
        }

        @NotNull
        private static List<RemoveMessages> newInstances(@NotNull List<String> messagesIds) {
            final List<RemoveMessages> result = new ArrayList<RemoveMessages>();

            for (List<String> messagesIdsChunk : Collections.split(messagesIds, MAX_IN_COUNT)) {
                result.add(new RemoveMessages(messagesIdsChunk));
            }

            return result;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            db.delete("messages", "chat_id in " + AndroidDbUtils.inClause(messagesIds), AndroidDbUtils.inClauseValues(messagesIds));
        }
    }


    private static final class LoadChatMessages extends AbstractDbQuery<List<ChatMessage>> {

        @NotNull
        private final String chatId;

        @NotNull
        private final UserService userService;

        private LoadChatMessages(@NotNull Context context,
                                 @NotNull String chatId,
                                 @NotNull UserService userService,
                                 @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.chatId = chatId;
            this.userService = userService;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("messages", null, "chat_id = ? ", new String[]{chatId}, null, null, null);
        }

        @NotNull
        @Override
        public List<ChatMessage> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<ChatMessage>(new ChatMessageMapper(this.userService, getContext())).convert(cursor);
        }
    }

    private static final class LoadChatMessage extends AbstractDbQuery<List<ChatMessage>> {

        @NotNull
        private final Integer messageId;

        @NotNull
        private final UserService userService;

        private LoadChatMessage(@NotNull Context context,
                                @NotNull Integer messageId,
                                @NotNull UserService userService,
                                @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.messageId = messageId;
            this.userService = userService;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("messages", null, "id = ? ", new String[]{String.valueOf(messageId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<ChatMessage> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<ChatMessage>(new ChatMessageMapper(this.userService, getContext())).convert(cursor);
        }
    }

    private static class OldestChatMessageLoader extends AbstractDbQuery<String> {

        @NotNull
        private String chatId;

        protected OldestChatMessageLoader(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull String chatId) {
            super(context, sqliteOpenHelper);
            this.chatId = chatId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.rawQuery("select min(send_date) from messages where chat_id = ?", new String[]{chatId});
        }

        @NotNull
        @Override
        public String retrieveData(@NotNull Cursor cursor) {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            } else {
                return "";
            }
        }
    }

    private static class LastChatMessageLoader extends AbstractDbQuery<Integer> {

        @NotNull
        private String chatId;

        protected LastChatMessageLoader(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull String chatId) {
            super(context, sqliteOpenHelper);
            this.chatId = chatId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.rawQuery("select max(id) from messages where chat_id = ?", new String[]{chatId});
        }

        @NotNull
        @Override
        public Integer retrieveData(@NotNull Cursor cursor) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                return 0;
            }
        }
    }
}
