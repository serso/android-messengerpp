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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.text.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class PropertyView {

	@Nonnull
	private final View view;

	@Nonnull
	private final Resources resources;

	@Nullable
	private TextView labelView;

	@Nullable
	private TextView valueView;

	@Nullable
	private ImageView iconView;

	@Nullable
	private FrameLayout widgetView;

	private PropertyView(int viewId, @Nonnull View parent) {
		view = parent.findViewById(viewId);
		resources = parent.getResources();
	}

	private PropertyView(@Nonnull View view) {
		this.view = view;
		resources = view.getResources();
	}

	@Nonnull
	public static PropertyView newPropertyView(int viewId, @Nonnull View view) {
		return new PropertyView(viewId, view);
	}

	@Nonnull
	public static PropertyView newPropertyView(@Nonnull Context context) {
		return new PropertyView(ViewFromLayoutBuilder.newInstance(R.layout.mpp_property).build(context));
	}

	public void fillViews(int labelResId, int valueResId, int iconResId) {
		setLabel(labelResId);
		setValue(valueResId);
		setIcon(iconResId);
	}

	@Nonnull
	public PropertyView setIcon(int iconResId) {
		if (iconResId != View.NO_ID) {
			setIcon(resources.getDrawable(iconResId));
		} else {
			setIcon(null);
		}

		return this;
	}

	@Nonnull
	public PropertyView setIcon(@Nullable Drawable icon) {
		final ImageView iconView = getIconView();
		iconView.setImageDrawable(icon);
		iconView.setVisibility(icon == null ? GONE : VISIBLE);

		return this;
	}

	@Nonnull
	public PropertyView setValue(@Nullable CharSequence value) {
		final TextView valueView = getValueView();
		valueView.setText(value);
		valueView.setVisibility(Strings.isEmpty(value) ? GONE : VISIBLE);
		return this;
	}

	@Nonnull
	public PropertyView setLabel(@Nullable CharSequence label) {
		getLabelView().setText(label);
		return this;
	}

	@Nonnull
	public PropertyView setValue(int valueResId) {
		setValue(resources.getString(valueResId));
		return this;
	}

	@Nonnull
	public PropertyView setLabel(int labelResId) {
		getLabelView().setText(labelResId);
		return this;
	}

	@Nonnull
	public PropertyView setWidget(@Nullable View widget) {
		final FrameLayout widgetView = getWidgetView();

		widgetView.removeAllViews();
		if (widget != null) {
			widgetView.addView(widget);
			widgetView.setVisibility(VISIBLE);
		} else {
			widgetView.setVisibility(GONE);
		}

		return this;
	}

	@Nonnull
	private TextView getLabelView() {
		if (labelView == null) {
			labelView = (TextView) view.findViewById(R.id.mpp_property_label);
		}
		return labelView;
	}

	@Nonnull
	private TextView getValueView() {
		if (valueView == null) {
			valueView = (TextView) view.findViewById(R.id.mpp_property_value);
		}
		return valueView;
	}

	@Nonnull
	public ImageView getIconView() {
		if (iconView == null) {
			iconView = (ImageView) view.findViewById(R.id.mpp_property_icon);
		}
		return iconView;
	}

	@Nonnull
	private FrameLayout getWidgetView() {
		if (widgetView == null) {
			widgetView = (FrameLayout) view.findViewById(R.id.mpp_property_widget);
		}
		return widgetView;
	}

	@Nonnull
	public View getView() {
		return view;
	}
}
