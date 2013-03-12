package org.solovyev.android.messenger.messages;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatMessage;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
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
    private ChatMessageDao chatMessageDao;


    @Nonnull
    @Override
    public List<ChatMessage> getChatMessages(@Nonnull RealmEntity realmChat) {
        return getChatMessageDao().loadChatMessages(realmChat.getEntityId());
    }

    @Override
    public void setMessageIcon(@Nonnull ImageView imageView, @Nonnull ChatMessage message, @Nonnull Chat chat, @Nonnull User user, @Nonnull Context context) {
        final Drawable defaultChatIcon = context.getResources().getDrawable(R.drawable.empty_icon);

        final User author = message.getAuthor();
        final String userIconUri = author.getPropertyValueByName("photo");
        if (!Strings.isEmpty(userIconUri)) {
            this.imageLoader.loadImage(userIconUri, imageView, R.drawable.empty_icon);
        } else {
            imageView.setImageDrawable(defaultChatIcon);
        }
    }

    @Nonnull
    private ChatMessageDao getChatMessageDao() {
        return chatMessageDao;
    }
}
