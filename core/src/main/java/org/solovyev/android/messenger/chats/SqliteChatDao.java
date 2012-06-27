package org.solovyev.android.messenger.chats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.solovyev.android.AProperty;
import org.solovyev.android.db.*;
import org.solovyev.android.messenger.MergeDaoResult;
import org.solovyev.android.messenger.MergeDaoResultImpl;
import org.solovyev.android.messenger.MessengerConfigurationImpl;
import org.solovyev.android.messenger.db.StringIdMapper;
import org.solovyev.android.messenger.messages.SqliteChatMessageDao;
import org.solovyev.android.messenger.properties.PropertyByIdDbQuery;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.utils.CollectionsUtils;
import org.solovyev.common.utils.CollectionsUtils2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 3:27 PM
 */
public class SqliteChatDao extends AbstractSQLiteHelper implements ChatDao {

    public SqliteChatDao(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
        super(context, sqliteOpenHelper);
    }

    @NotNull
    @Override
    public List<String> loadUserChatIds(@NotNull Integer userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatIdsByUserId(getContext(), userId, getSqliteOpenHelper()));
    }

    @Override
    public void updateChat(@NotNull Chat chat) {
        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(new UpdateChat(chat), new DeleteChatProperties(chat), new InsertChatProperties(chat)));
    }

    @NotNull
    @Override
    public List<String> loadChatIds() {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatIds(getContext(), getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<AProperty> loadChatPropertiesById(@NotNull String chatId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatPropertiesDbQuery(chatId, getContext(), getSqliteOpenHelper()));
    }

    @NotNull
    @Override
    public List<Chat> loadUserChats(@NotNull Integer userId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatsByUserId(getContext(), userId, getSqliteOpenHelper(), this));
    }

    @NotNull
    @Override
    public List<User> loadChatParticipants(@NotNull String chatId) {
        return AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadChatParticipants(getContext(), chatId, getSqliteOpenHelper()));
    }

    @Override
    public Chat loadChatById(@NotNull String chatId) {
        return CollectionsUtils.getFirstListElement(AndroidDbUtils.doDbQuery(getSqliteOpenHelper(), new LoadByChatId(getContext(), chatId, getSqliteOpenHelper(), this)));
    }

    private static final class LoadChatParticipants extends AbstractDbQuery<List<User>> {

        @NotNull
        private final String chatId;

        private LoadChatParticipants(@NotNull Context context, @NotNull String chatId, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.chatId = chatId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("user_chats", null, "chat_id = ? ", new String[]{chatId}, null, null, null);
        }

        @NotNull
        @Override
        public List<User> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<User>(new ChatParticipantMapper(getUserService(), getContext())).convert(cursor);
        }

        @NotNull
        private UserService getUserService() {
            return MessengerConfigurationImpl.getInstance().getServiceLocator().getUserService();
        }
    }

    private static final class LoadChatsByUserId extends AbstractDbQuery<List<Chat>> {

        @NotNull
        private final Integer userId;

        @NotNull
        private final ChatDao chatDao;

        private LoadChatsByUserId(@NotNull Context context, @NotNull Integer userId, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull ChatDao chatDao) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
            this.chatDao = chatDao;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("chats", null, "id in (select chat_id from user_chats where user_id = ? ) ", new String[]{String.valueOf(userId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<Chat> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<Chat>(new ChatMapper(chatDao)).convert(cursor);
        }
    }

    public static final class LoadChatPropertiesDbQuery extends PropertyByIdDbQuery {

        public LoadChatPropertiesDbQuery(@NotNull String chatId, @NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper, "chat_properties", "chat_id", chatId);
        }
    }

    @NotNull
    @Override
    public MergeDaoResult<ApiChat, String> mergeUserChats(@NotNull Integer userId, @NotNull List<? extends ApiChat> apiChats) {
        final MergeDaoResultImpl<ApiChat, String> result = new MergeDaoResultImpl<ApiChat, String>(apiChats);

        final List<String> chatsFromDb = loadUserChatIds(userId);
        for (final String chatIdFromDb : chatsFromDb) {
            try {
                // chat exists both in db and on remote server => just update chat properties
                result.addUpdatedObject(Iterables.find(apiChats, new ChatByIdFinder(chatIdFromDb)));
            } catch (NoSuchElementException e) {
                // !!! actually not all chats are loaded and we cannot delete the chat just because it is not in the list

                // chat was removed on remote server => need to remove from local db
                //result.addRemovedObjectId(chatIdFromDb);
            }
        }

        final List<String> chatIdsFromDb = loadChatIds();
        for (ApiChat apiChat : apiChats) {
            try {
                // chat exists both in db and on remote server => case already covered above
                Iterables.find(chatsFromDb, Predicates.equalTo(apiChat.getChat().getId()));
            } catch (NoSuchElementException e) {
                // chat was added on remote server => need to add to local db
                if (chatIdsFromDb.contains(apiChat.getChat().getId())) {
                    // only link must be added - chat already in chats table
                    result.addAddedObjectLink(apiChat);
                } else {
                    // no chat information in local db is available - full chat insertion
                    result.addAddedObject(apiChat);
                }
            }
        }

        final List<DbExec> execs = new ArrayList<DbExec>();

        if (!result.getRemovedObjectIds().isEmpty()) {
            execs.addAll(RemoveChats.newInstances(userId, result.getRemovedObjectIds()));
        }

        for (ApiChat updatedChat : result.getUpdatedObjects()) {
            execs.add(new UpdateChat(updatedChat.getChat()));
            execs.add(new DeleteChatProperties(updatedChat.getChat()));
            execs.add(new InsertChatProperties(updatedChat.getChat()));
        }

        for (ApiChat addedChatLink : result.getAddedObjectLinks()) {
            execs.add(new UpdateChat(addedChatLink.getChat()));
            execs.add(new DeleteChatProperties(addedChatLink.getChat()));
            execs.add(new InsertChatProperties(addedChatLink.getChat()));
            execs.add(new InsertChatLink(userId, addedChatLink.getChat().getId()));
        }

        for (ApiChat addedChat : result.getAddedObjects()) {
            execs.add(new InsertChat(addedChat.getChat()));
            execs.add(new InsertChatProperties(addedChat.getChat()));
            execs.add(new InsertChatLink(userId, addedChat.getChat().getId()));
            for (ChatMessage chatMessage : addedChat.getMessages()) {
                execs.add(new SqliteChatMessageDao.InsertMessage(addedChat.getChat(), chatMessage));
            }

            for (User participant : addedChat.getParticipants()) {
                if (!participant.getId().equals(userId)) {
                    execs.add(new InsertChatLink(participant.getId(), addedChat.getChat().getId()));
                }
            }
        }

        AndroidDbUtils.doDbExecs(getSqliteOpenHelper(), execs);

        return result;
    }

    private static final class LoadChatIds extends AbstractDbQuery<List<String>> {

        private LoadChatIds(@NotNull Context context, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("users", null, null, null, null, null, null);
        }

        @NotNull
        @Override
        public List<String> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
        }
    }


    private static final class LoadChatIdsByUserId extends AbstractDbQuery<List<String>> {

        @NotNull
        private final Integer userId;

        private LoadChatIdsByUserId(@NotNull Context context, @NotNull Integer userId, @NotNull SQLiteOpenHelper sqliteOpenHelper) {
            super(context, sqliteOpenHelper);
            this.userId = userId;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("chats", null, "id in (select chat_id from user_chats where user_id = ? ) ", new String[]{String.valueOf(userId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<String> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<String>(StringIdMapper.getInstance()).convert(cursor);
        }
    }

    private static class ChatByIdFinder implements Predicate<ApiChat> {

        @NotNull
        private final String chatId;

        public ChatByIdFinder(@NotNull String chatId) {
            this.chatId = chatId;
        }

        @Override
        public boolean apply(@javax.annotation.Nullable ApiChat apiChat) {
            return apiChat != null && chatId.equals(apiChat.getChat().getId());
        }
    }

    private static final class RemoveChats implements DbExec {

        @NotNull
        private Integer userId;

        @NotNull
        private List<String> chatIds;

        private RemoveChats(@NotNull Integer userId, @NotNull List<String> chatIds) {
            this.userId = userId;
            this.chatIds = chatIds;
        }

        @NotNull
        private static List<RemoveChats> newInstances(@NotNull Integer userId, @NotNull List<String> chatIds) {
            final List<RemoveChats> result = new ArrayList<RemoveChats>();

            for (List<String> friendIdsChunk : CollectionsUtils2.split(chatIds, MAX_IN_COUNT)) {
                result.add(new RemoveChats(userId, friendIdsChunk));
            }

            return result;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            db.delete("user_chats", "user_id = ? and chat_id in " + AndroidDbUtils.inClause(chatIds), AndroidDbUtils.inClauseValues(chatIds, String.valueOf(userId)));
        }
    }


    private static final class UpdateChat extends AbstractObjectDbExec<Chat> {

        private UpdateChat(@NotNull Chat chat) {
            super(chat);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final Chat chat = getNotNullObject();

            final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

            final DateTime lastMessagesSyncDate = chat.getLastMessagesSyncDate();

            final ContentValues values = new ContentValues();
            values.put("id", chat.getId());
            values.put("messages_count", chat.getMessagesCount());
            values.put("last_messages_sync_date", lastMessagesSyncDate == null ? null : dateTimeFormatter.print(lastMessagesSyncDate));

            db.update("chats", values, "id = ?", new String[]{String.valueOf(chat.getId())});
        }
    }

    private static final class InsertChat extends AbstractObjectDbExec<Chat> {

        private InsertChat(@NotNull Chat chat) {
            super(chat);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final Chat chat = getNotNullObject();

            final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.basicDateTime();

            final DateTime lastMessagesSyncDate = chat.getLastMessagesSyncDate();

            final ContentValues values = new ContentValues();

            values.put("id", chat.getId());
            values.put("messages_count", chat.getMessagesCount());
            values.put("last_messages_sync_date", lastMessagesSyncDate == null ? null : dateTimeFormatter.print(lastMessagesSyncDate));

            db.insert("chats", null, values);
        }
    }

    private static final class DeleteChatProperties extends AbstractObjectDbExec<Chat> {

        private DeleteChatProperties(@NotNull Chat chat) {
            super(chat);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final Chat chat = getNotNullObject();

            db.delete("chat_properties", "chat_id = ?", new String[]{String.valueOf(chat.getId())});
        }
    }

    private static final class InsertChatProperties extends AbstractObjectDbExec<Chat> {

        private InsertChatProperties(@NotNull Chat chat) {
            super(chat);
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final Chat chat = getNotNullObject();

            for (AProperty property : chat.getProperties()) {
                final ContentValues values = new ContentValues();
                values.put("chat_id", chat.getId());
                values.put("property_name", property.getName());
                values.put("property_value", property.getValue());
                db.insert("chat_properties", null, values);
            }
        }
    }

    private static final class InsertChatLink implements DbExec {

        @NotNull
        private Integer userId;

        @NotNull
        private String chatId;

        private InsertChatLink(@NotNull Integer userId, @NotNull String chatId) {
            this.userId = userId;
            this.chatId = chatId;
        }

        @Override
        public void exec(@NotNull SQLiteDatabase db) {
            final ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("chat_id", chatId);
            db.insert("user_chats", null, values);
        }
    }

    private static final class LoadByChatId extends AbstractDbQuery<List<Chat>> {

        @NotNull
        private final String chatId;

        @NotNull
        private final ChatDao chatDao;

        private LoadByChatId(@NotNull Context context, @NotNull String chatId, @NotNull SQLiteOpenHelper sqliteOpenHelper, @NotNull ChatDao chatDao) {
            super(context, sqliteOpenHelper);
            this.chatId = chatId;
            this.chatDao = chatDao;
        }

        @NotNull
        @Override
        public Cursor createCursor(@NotNull SQLiteDatabase db) {
            return db.query("chats", null, "id = ? ", new String[]{String.valueOf(chatId)}, null, null, null);
        }

        @NotNull
        @Override
        public List<Chat> retrieveData(@NotNull Cursor cursor) {
            return new ListMapper<Chat>(new ChatMapper(chatDao)).convert(cursor);
        }
    }
}
