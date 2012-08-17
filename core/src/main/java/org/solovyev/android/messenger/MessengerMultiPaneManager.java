package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: serso
 * Date: 8/16/12
 * Time: 5:29 PM
 */
public interface MessengerMultiPaneManager {

    boolean isDualPane(@NotNull Activity activity);

    boolean isTriplePane(@NotNull Activity activity);

    boolean isFirstPane(@Nullable View parent);

    boolean isSecondPane(@Nullable View parent);

    boolean isThirdPane(@Nullable View parent);

    @NotNull
    ViewGroup getFirstPane(@NotNull Activity activity);

    @NotNull
    ViewGroup getSecondPane(@NotNull Activity activity);

    @NotNull
    ViewGroup getThirdPane(@NotNull Activity activity);

    void fillContentPane(@NotNull Activity activity, @Nullable View paneParent, @NotNull View pane);

    void fillLoadingLayout(@NotNull Activity activity, @Nullable View paneParent, @NotNull Resources resources, @NotNull LoadingLayout loadingView);
}
