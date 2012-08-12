package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.http.RemoteFileService;
import org.solovyev.android.messenger.R;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.roboguice.RoboGuiceUtils;
import org.solovyev.common.text.StringUtils;

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
    @NotNull
    private RemoteFileService remoteFileService;

    @Inject
    @NotNull
    private Provider<ChatMessageDao> chatMessageDaoProvider;


    @NotNull
    @Override
    public List<ChatMessage> getChatMessages(@NotNull String chatId, @NotNull Context context) {
        return getChatMessageDao(context).loadChatMessages(chatId);
    }

    @Override
    public void setMessageIcon(@NotNull ImageView imageView, @NotNull ChatMessage message, @NotNull Chat chat, @NotNull User user, @NotNull Context context) {
        final Drawable defaultChatIcon = context.getResources().getDrawable(R.drawable.empty_icon);

        final User author = message.getAuthor();
        final String userIconUri = author.getPropertyValueByName("photo");
        if (!StringUtils.isEmpty(userIconUri)) {
            this.remoteFileService.loadImage(userIconUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultChatIcon);
        }
    }

    @NotNull
    private ChatMessageDao getChatMessageDao(@NotNull Context context) {
        return RoboGuiceUtils.getInContextScope(context, chatMessageDaoProvider);
    }
}
