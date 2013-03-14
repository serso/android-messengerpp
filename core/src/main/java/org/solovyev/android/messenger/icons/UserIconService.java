package org.solovyev.android.messenger.icons;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import org.solovyev.android.messenger.users.User;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/13/13
 * Time: 9:46 PM
 */
public interface UserIconService {

    @Nonnull
    Drawable getDefaultUserIcon();

    void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView);

    void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView);
}
