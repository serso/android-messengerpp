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

import android.view.View;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public final class ViewAwareTag {

	@Nonnull
	private final Map<Integer, View> views = new HashMap<Integer, View>();

	@Nonnull
	private String tag;

	@Nonnull
	private final View view;

	public ViewAwareTag(@Nonnull String tag, @Nonnull View view) {
		this.tag = tag;
		this.view = view;
	}

	public <V extends View> V getViewById(int viewId) {
		V result = (V) views.get(viewId);
		if (result == null) {
			result = (V) view.findViewById(viewId);
			if (result != null) {
				views.put(viewId, result);
			}
		}
		return result;
	}

	@Nonnull
	public String getTag() {
		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ViewAwareTag)) return false;

		ViewAwareTag that = (ViewAwareTag) o;

		if (!tag.equals(that.tag)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return tag.hashCode();
	}

	public void update(@Nonnull ViewAwareTag that) {
		this.tag = that.tag;
	}

	@Nonnull
	public View getView() {
		return view;
	}
}
