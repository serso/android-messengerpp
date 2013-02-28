package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.Views;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 11:22 PM
 */
@Singleton
public class MessengerMultiPaneManagerImpl implements MessengerMultiPaneManager {

    @NotNull
    private final Application context;

    @Inject
    public MessengerMultiPaneManagerImpl(@NotNull Application context) {
        this.context = context;
    }

    @Override
    public boolean isDualPane(@NotNull Activity activity) {
        if (activity.findViewById(R.id.content_second_pane) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isTriplePane(@NotNull Activity activity) {
        if (activity.findViewById(R.id.content_third_pane) != null) {
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public ViewGroup getFirstPane(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.content_first_pane);
    }

    @NotNull
    @Override
    public ViewGroup getSecondPane(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.content_second_pane);
    }

    @NotNull
    @Override
    public ViewGroup getThirdPane(@NotNull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.content_third_pane);
    }

    @Override
    public boolean isFirstPane(@Nullable View parent) {
        return parent != null && parent.getId() == R.id.content_first_pane;
    }

    @Override
    public boolean isSecondPane(@Nullable View parent) {
        return parent != null && parent.getId() == R.id.content_second_pane;
    }

    @Override
    public boolean isThirdPane(@Nullable View parent) {
        return parent != null && parent.getId() == R.id.content_third_pane;
    }

    @Override
    public void fillContentPane(@NotNull Activity activity, @Nullable View paneParent, @NotNull View pane) {
        if (this.isDualPane(activity)) {
            if (this.isFirstPane(paneParent)) {
                pane.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.right_border));
                pane.setPadding(0, 0, 0, 0);
            } else if (this.isSecondPane(paneParent)) {
                pane.setBackgroundColor(context.getResources().getColor(R.color.base_bg_lighter));
            } else if (this.isTriplePane(activity) && this.isThirdPane(paneParent)) {
                if (Views.getScreenOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE) {
                    pane.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.left_border));
                } else {
                    pane.setBackgroundColor(context.getResources().getColor(R.color.base_bg_lighter));
                }
            }
        } else if (this.isFirstPane(paneParent)) {
            pane.setBackgroundColor(context.getResources().getColor(R.color.base_bg_lighter));
        }
    }

    @Override
    public void fillLoadingLayout(@NotNull Activity activity, @Nullable View paneParent, @NotNull Resources resources, @NotNull LoadingLayout loadingView) {
        loadingView.setTextColor(resources.getColor(R.color.text));
        if (this.isDualPane(activity)) {
            if (this.isFirstPane(paneParent)) {
                loadingView.setBackgroundColor(resources.getColor(R.color.base_bg));
            } else if (this.isSecondPane(paneParent)) {
                loadingView.setBackgroundColor(resources.getColor(R.color.base_bg_lighter));
            } else if (this.isTriplePane(activity) && this.isThirdPane(paneParent)) {
                if (Views.getScreenOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE) {
                    loadingView.setBackgroundColor(resources.getColor(R.color.base_bg));
                } else {
                    loadingView.setBackgroundColor(resources.getColor(R.color.base_bg_lighter));
                }
            }
        } else if (this.isFirstPane(paneParent)) {
            loadingView.setBackgroundColor(resources.getColor(R.color.base_bg_lighter));
        }
    }}
