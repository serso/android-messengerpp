package org.solovyev.android.messenger.vk.messages;

import android.content.Context;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.solovyev.android.messenger.chats.*;
import org.solovyev.android.messenger.http.IllegalJsonException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/6/12
 * Time: 1:07 PM
 */
public class JsonMessage {

    @Nullable
    private String mid;

    @Nullable
    private String uid;

    @Nullable
    private String date;

    @Nullable
    private Integer read_state;

    @Nullable
    private Integer out;

    @Nullable
    private String title;

    @Nullable
    private String body;

    @Nullable
    private JsonMessageTypedAttachment[] attachments;

    @Nullable
    private JsonMessage[] fwd_messages;

    @Nullable
    private Integer chat_id;

    @Nullable
    private String chat_active;

    @Nullable
    private Integer users_count;

    @Nullable
    private Integer admin_id;

    @Nullable
    public String getMid() {
        return mid;
    }

    @Nullable
    public String getUid() {
        return uid;
    }

    @Nullable
    public String getDate() {
        return date;
    }

    @Nullable
    public Integer getRead_state() {
        return read_state;
    }

    @Nullable
    public Integer getOut() {
        return out;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getBody() {
        return body;
    }

    @Nullable
    public JsonMessageTypedAttachment[] getAttachments() {
        return attachments;
    }

    @Nullable
    public JsonMessage[] getFwd_messages() {
        return fwd_messages;
    }

    @Nullable
    public Integer getChat_id() {
        return chat_id;
    }

    @Nullable
    public String getChat_active() {
        return chat_active;
    }

    @Nullable
    public Integer getUsers_count() {
        return users_count;
    }

    @Nullable
    public Integer getAdmin_id() {
        return admin_id;
    }

    @NotNull
    public LiteChatMessage toLiteChatMessage(@NotNull User user,
                                             @Nullable String explicitUserId,
                                             @NotNull UserService userService,
                                             @NotNull Context context) throws IllegalJsonException {
        if (mid == null || uid == null || date == null) {
            throw new IllegalJsonException();
        }

        final LiteChatMessageImpl result = LiteChatMessageImpl.newInstance(mid);

        final MessageDirection messageDirection = getMessageDirection();
        if (messageDirection == MessageDirection.out) {
            result.setAuthor(user);
            result.setRecipient(userService.getUserById(explicitUserId == null ? uid : explicitUserId, context));
        } else if ( messageDirection == MessageDirection.in ) {
            result.setAuthor(userService.getUserById(explicitUserId == null ? uid : explicitUserId, context));
            result.setRecipient(user);
        } else {
            result.setAuthor(userService.getUserById(uid, context));
            if ( explicitUserId != null ) {
                result.setRecipient(userService.getUserById(explicitUserId, context));
            }
        }

        DateTime sendDate;
        try {
            sendDate = new DateTime(Long.valueOf(date) * 1000L);
        } catch (NumberFormatException e) {
            Log.e(this.getClass().getSimpleName(), "Date could not be parsed for message: " + mid + ", date: " + date);
            sendDate = DateTime.now();
        }
        result.setSendDate(sendDate);
        result.setBody(StringUtils.getNotEmpty(body, ""));
        result.setTitle(StringUtils.getNotEmpty(title, ""));

        return result;
    }

    @NotNull
    public ChatMessage toChatMessage(@NotNull User user, @Nullable String explicitUserId, @NotNull UserService userService, @NotNull Context context) throws IllegalJsonException {
        if (read_state == null || out == null) {
            throw new IllegalJsonException();
        }

        final ChatMessageImpl result = new ChatMessageImpl(toLiteChatMessage(user, explicitUserId, userService, context));
        result.setRead(isRead());
        result.setDirection(getNotNullMessageDirection());
        for (LiteChatMessage fwdMessage : getFwdMessages(user, userService, context)) {
            result.addFwdMessage(fwdMessage);
        }

        return result;
    }

    @NotNull
    private List<LiteChatMessage> getFwdMessages(@NotNull User user, @NotNull UserService userService, @NotNull Context context) throws IllegalJsonException {
        if (fwd_messages == null) {
            return Collections.emptyList();
        } else {
            final List<LiteChatMessage> result = new ArrayList<LiteChatMessage>(fwd_messages.length);

            for (JsonMessage fwd_message : fwd_messages) {
                // todo serso: think about explicit user id
                result.add(fwd_message.toLiteChatMessage(user, null, userService, context));
            }

            return result;
        }
    }

    @NotNull
    private MessageDirection getNotNullMessageDirection() {
        if (Integer.valueOf(1).equals(out)) {
            return MessageDirection.out;
        } else {
            return MessageDirection.in;
        }
    }

    @Nullable
    private MessageDirection getMessageDirection() {
        if (Integer.valueOf(1).equals(out)) {
            return MessageDirection.out;
        } else if (Integer.valueOf(0).equals(out)) {
            return MessageDirection.in;
        } else {
            return null;
        }
    }

    private boolean isRead() {
        if (Integer.valueOf(1).equals(read_state)) {
            return true;
        } else {
            return false;
        }
    }
}
