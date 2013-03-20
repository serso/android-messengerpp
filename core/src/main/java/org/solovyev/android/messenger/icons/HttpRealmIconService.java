package org.solovyev.android.messenger.icons;

import android.content.Context;
import android.widget.ImageView;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.messenger.users.User;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * User: serso
 * Date: 3/14/13
 * Time: 8:07 PM
 */
public final class HttpRealmIconService implements RealmIconService {

    @Nonnull
    private final Context context;

    @Nonnull
    private final ImageLoader imageLoader;

    private final int defaultUserIconResId;

    private final int defaultUsersIconResId;

    @Nonnull
    private final UrlGetter iconUrlGetter;

    @Nonnull
    private final UrlGetter photoUrlGetter;

    public HttpRealmIconService(@Nonnull Context context,
                                @Nonnull ImageLoader imageLoader,
                                int defaultUserIconResId,
                                int defaultUsersIconResId,
                                @Nonnull UrlGetter iconUrlGetter,
                                @Nonnull UrlGetter photoUrlGetter) {
        this.context = context;
        this.imageLoader = imageLoader;
        this.defaultUserIconResId = defaultUserIconResId;
        this.defaultUsersIconResId = defaultUsersIconResId;
        this.iconUrlGetter = iconUrlGetter;
        this.photoUrlGetter = photoUrlGetter;
    }

    @Override
    public void setUserIcon(@Nonnull User user, @Nonnull ImageView imageView) {
        final String userIconUrl = iconUrlGetter.getUrl(user);
        if (!Strings.isEmpty(userIconUrl)) {
            assert userIconUrl != null;
            this.imageLoader.loadImage(userIconUrl, imageView, defaultUserIconResId);
        } else {
            imageView.setImageDrawable(context.getResources().getDrawable(defaultUserIconResId));
        }
    }

    @Override
    public void setUserPhoto(@Nonnull User user, @Nonnull ImageView imageView) {
        final String userPhotoUrl = photoUrlGetter.getUrl(user);
        if (!Strings.isEmpty(userPhotoUrl)) {
            assert userPhotoUrl != null;
            this.imageLoader.loadImage(userPhotoUrl, imageView, defaultUserIconResId);
        } else {
            imageView.setImageDrawable(context.getResources().getDrawable(defaultUserIconResId));
        }
    }

    @Override
    public void fetchUsersIcons(@Nonnull List<User> users) {
        for (User contact : users) {
            fetchUserIcon(contact);
        }
    }

    @Override
    public void setUsersIcon(@Nonnull List<User> users, @Nonnull ImageView imageView) {
        imageView.setImageDrawable(context.getResources().getDrawable(defaultUsersIconResId));
    }

    public void fetchUserIcon(@Nonnull User user) {
        final String userIconUrl = iconUrlGetter.getUrl(user);
        if (!Strings.isEmpty(userIconUrl)) {
            assert userIconUrl != null;
            this.imageLoader.loadImage(userIconUrl);
        }
    }

    /*
    **********************************************************************
    *
    *                           STATIC
    *
    **********************************************************************
    */

    public static interface UrlGetter {

        @Nullable
        String getUrl(@Nonnull User user);

    }

    private static final class UrlFromPropertyGetter implements UrlGetter {

        @Nonnull
        private final String propertyName;

        private UrlFromPropertyGetter(@Nonnull String propertyName) {
            this.propertyName = propertyName;
        }

        @Nullable
        @Override
        public String getUrl(@Nonnull User user) {
            return user.getPropertyValueByName(propertyName);
        }
    }

    @Nonnull
    public static UrlGetter newUrlFromPropertyGetter(@Nonnull String propertyName) {
        return new UrlFromPropertyGetter(propertyName);
    }
}
