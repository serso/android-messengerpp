package org.solovyev.android.messenger.view;

import android.view.View;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * User: serso
 * Date: 3/6/13
 * Time: 11:03 PM
 */
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
