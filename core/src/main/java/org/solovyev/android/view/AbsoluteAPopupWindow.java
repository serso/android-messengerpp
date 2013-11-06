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

import android.content.Context;
import android.view.View;

import javax.annotation.Nonnull;

public class AbsoluteAPopupWindow extends APopupWindow {

	public AbsoluteAPopupWindow(@Nonnull Context context, @Nonnull ViewBuilder<View> viewBuilder) {
		super(context, viewBuilder);
	}

	public void showLikePopDownMenu(@Nonnull View parentView, int gravity, int xOffset, int yOffset) {
		this.show();
		this.getWindow().setAnimationStyle(org.solovyev.android.messenger.core.R.style.pw_pop_down_menu);
		this.getWindow().showAtLocation(parentView, gravity, xOffset, yOffset);
	}
}
