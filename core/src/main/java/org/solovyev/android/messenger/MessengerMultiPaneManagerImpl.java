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
import org.solovyev.android.Views;
import org.solovyev.android.messenger.core.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 2/28/13
 * Time: 11:22 PM
 */
@Singleton
public class MessengerMultiPaneManagerImpl implements MessengerMultiPaneManager {

    @Nonnull
    private final Application context;

    @Inject
    public MessengerMultiPaneManagerImpl(@Nonnull Application context) {
        this.context = context;
    }

    @Override
    public boolean isDualPane(@Nonnull Activity activity) {
        if (activity.findViewById(R.id.content_second_pane) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isTriplePane(@Nonnull Activity activity) {
        if (activity.findViewById(R.id.content_third_pane) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public ViewGroup getFirstPane(@Nonnull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.content_first_pane);
    }

    @Nonnull
    @Override
    public ViewGroup getSecondPane(@Nonnull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.content_second_pane);
    }

    @Nonnull
    @Override
    public ViewGroup getThirdPane(@Nonnull Activity activity) {
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
    public void fillContentPane(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull View pane) {
        if (this.isDualPane(activity)) {
            if (this.isFirstPane(paneParent)) {
                pane.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mpp_border_right));
            } else if (this.isSecondPane(paneParent)) {
                pane.setBackgroundColor(context.getResources().getColor(R.color.mpp_bg));
            } else if (this.isTriplePane(activity) && this.isThirdPane(paneParent)) {
                if (Views.getScreenOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE) {
                    pane.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mpp_border_left));
                } else {
                    pane.setBackgroundColor(context.getResources().getColor(R.color.mpp_bg));
                }
            }
        } else if (this.isFirstPane(paneParent)) {
            pane.setBackgroundColor(context.getResources().getColor(R.color.mpp_bg));
        }
    }

    @Override
    public void fillLoadingLayout(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull Resources resources, @Nonnull LoadingLayout loadingView) {
        loadingView.setTextColor(resources.getColor(R.color.mpp_text));
        loadingView.setBackgroundColor(resources.getColor(android.R.color.transparent));
    }}
