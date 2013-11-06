/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.view;

import android.content.Context;
import android.util.AttributeSet;

import com.handmark.pulltorefresh.library.PullToRefreshListView;

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