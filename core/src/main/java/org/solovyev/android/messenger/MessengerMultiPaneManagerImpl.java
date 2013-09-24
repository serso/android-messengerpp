package org.solovyev.android.messenger;

import android.app.Activity;
import android.app.Application;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.Views;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.text.Strings;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

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
	public void onCreatePane(@Nonnull Activity activity, @Nullable View paneParent, @Nonnull View pane) {
		if (this.isDualPane(activity)) {
			if (this.isFirstPane(paneParent)) {
				pane.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mpp_border_right));
				// border may add padding => set to zeros
				pane.setPadding(0, 0, 0, 0);
			} else if (this.isSecondPane(paneParent)) {
				pane.setBackgroundColor(context.getResources().getColor(R.color.mpp_bg));
			} else if (this.isTriplePane(activity) && this.isThirdPane(paneParent)) {
				if (Views.getScreenOrientation(activity) == Configuration.ORIENTATION_LANDSCAPE) {
					pane.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.mpp_border_left));
				} else {
					pane.setBackgroundColor(context.getResources().getColor(R.color.mpp_bg));
				}
			}
		} else {
			pane.setBackgroundColor(context.getResources().getColor(R.color.mpp_bg));
		}
	}

	@Override
	public void onPaneCreated(@Nonnull Activity activity, @Nonnull View pane) {
		final TextView fragmentTitleTextView = (TextView) pane.findViewById(R.id.mpp_fragment_title);
		if (fragmentTitleTextView != null) {
			if (this.isDualPane(activity)) {
				final CharSequence fragmentTitle = fragmentTitleTextView.getText();
				if (Strings.isEmpty(fragmentTitle)) {
					fragmentTitleTextView.setVisibility(View.GONE);
				} else {
					fragmentTitleTextView.setText(String.valueOf(fragmentTitle).toUpperCase());
					fragmentTitleTextView.setVisibility(View.VISIBLE);
				}
			} else {
				fragmentTitleTextView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void fillLoadingLayout(@Nonnull Activity activity, @Nonnull Resources resources, @Nonnull LoadingLayout loadingView) {
		// todo serso: incorrect color of "Release to refresh caption"
		loadingView.setTextColor(ColorStateList.valueOf(resources.getColor(R.color.mpp_text)));
		loadingView.setBackgroundColor(resources.getColor(android.R.color.transparent));
	}
}
