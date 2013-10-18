package org.solovyev.android.messenger;

import android.content.Context;

import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.messenger.core.R;

import com.actionbarsherlock.view.MenuItem;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 8:52 PM
 */
public class ToggleFilterInputMenuItem implements IdentifiableMenuItem<MenuItem> {

	@Nonnull
	private final WeakReference<BaseListFragment<?, ?>> fragmentRef;

	public ToggleFilterInputMenuItem(@Nonnull BaseListFragment<?, ?> fragment) {
		this.fragmentRef = new WeakReference<BaseListFragment<?, ?>>(fragment);
	}

	@Nonnull
	@Override
	public Integer getItemId() {
		return R.id.mpp_menu_toggle_filter_box;
	}

	@Override
	public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
		final BaseListFragment<?, ?> fragment = fragmentRef.get();
		if (fragment != null) {
			fragment.toggleFilterBox();
		}
	}
}
