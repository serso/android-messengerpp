package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 5:29 PM
 */
public interface MessengerMultiPaneManager {

    boolean isDualPane(@Nonnull Activity activity);

    boolean isTriplePane(@Nonnull Activity activity);

    boolean isFirstPane(@Nullable View parent);

    boolean isSecondPane(@Nullable View parent);

    boolean isThirdPane(@Nullable View parent);

    @Nonnull
    ViewGroup getFirstPane(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getSecondPane(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getThirdPane(@Nonnull Activity activity);

    void fillContentPane(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull View pane);

    void fillLoadingLayout(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull Resources resources, @Nonnull LoadingLayout loadingView);
}
