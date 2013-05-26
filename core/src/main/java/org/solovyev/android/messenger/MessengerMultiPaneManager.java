package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
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

	void onCreatePane(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull View pane);

	void onPaneCreated(@Nonnull Activity activity, @Nonnull View pane);

	void fillLoadingLayout(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull Resources resources, @Nonnull LoadingLayout loadingView);
}
