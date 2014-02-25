/*
 * Copyright 2014 serso aka se.solovyev
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
import android.os.Build;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import javax.annotation.Nonnull;

public final class SwitchView {

	@Nonnull
	private final CompoundButton view;

	public SwitchView(@Nonnull Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			view = new Switch(context);
		} else {
			view = new CheckBox(context);
		}
	}

	public void setOnCheckedChangeListener(@Nonnull CompoundButton.OnCheckedChangeListener listener) {
		view.setOnCheckedChangeListener(listener);
	}

	public void setChecked(boolean checked) {
		view.setChecked(checked);
	}

	@Nonnull
	public View getView() {
		return view;
	}
}
