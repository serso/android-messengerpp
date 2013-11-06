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

package org.solovyev.android.view;

import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import javax.annotation.Nonnull;

public class AnchorAPopupWindow extends APopupWindow {

	@Nonnull
	private final View anchor;

	/**
	 * Create a BetterPopupWindow
	 *
	 * @param anchor      the view that the BetterPopupWindow will be displaying 'from'
	 * @param viewBuilder
	 */
	public AnchorAPopupWindow(@Nonnull View anchor, @Nonnull ViewBuilder<View> viewBuilder) {
		super(anchor.getContext(), viewBuilder);
		this.anchor = anchor;
	}

	/**
	 * Displays like a popdown menu from the anchor view
	 */
	public void showLikePopDownMenu() {
		this.showLikePopDownMenu(0, 0);
	}

	/**
	 * Displays like a popdown menu from the anchor view.
	 *
	 * @param xOffset offset in X direction
	 * @param yOffset offset in Y direction
	 */
	public void showLikePopDownMenu(int xOffset, int yOffset) {
		this.show();
		this.getWindow().setAnimationStyle(org.solovyev.android.messenger.core.R.style.pw_grow_from_top);
		this.getWindow().showAsDropDown(this.anchor, xOffset, yOffset);
	}

	/**
	 * Displays like a QuickAction from the anchor view.
	 */
	public void showLikeQuickAction() {
		this.showLikeQuickAction(0, 0);
	}

	/**
	 * Displays like a QuickAction from the anchor view.
	 *
	 * @param xOffset offset in the X direction
	 * @param yOffset offset in the Y direction
	 */
	public void showLikeQuickAction(int xOffset, int yOffset) {
		final View root = this.show();

		this.getWindow().setAnimationStyle(org.solovyev.android.messenger.core.R.style.pw_grow_from_bottom);

		int[] location = new int[2];
		this.anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0] + this.anchor.getWidth(), location[1] + this.anchor.getHeight());

		root.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		int rootWidth = root.getMeasuredWidth();
		int rootHeight = root.getMeasuredHeight();

		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

		int xPos = ((screenWidth - rootWidth) / 2) + xOffset;
		int yPos = anchorRect.top - rootHeight + yOffset;

		// display on bottom
		if (rootHeight > anchorRect.top) {
			yPos = anchorRect.bottom + yOffset;
			getWindow().setAnimationStyle(org.solovyev.android.messenger.core.R.style.pw_grow_from_top);
		}

		getWindow().showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
}
