package org.solovyev.android.messenger.view;

import android.content.Context;
import android.util.AttributeSet;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:44 PM
 */

/**
 * Main goal of this class to provide access to protected members/method of {@link PullToRefreshListView}
 */
public class PublicPullToRefreshListView extends PullToRefreshListView {

	public PublicPullToRefreshListView(Context context) {
		super(context);
	}

	public PublicPullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PublicPullToRefreshListView(Context context, Mode mode) {
		super(context, mode);
	}

	/*
	 * Method scope visibility changed
	 */
	@Override
	public void setRefreshingInternal(boolean doScroll) {
		super.setRefreshingInternal(doScroll);
	}
}