package org.solovyev.android.messenger.about;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.messenger.BaseListItemAdapter;
import org.solovyev.android.messenger.BaseStaticListFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ListViewAwareOnRefreshListener;

public class AboutItemsFragment extends BaseStaticListFragment<AboutListItem> {

	public static final String FRAGMENT_TAG = "about-items";

	public AboutItemsFragment() {
		super(FRAGMENT_TAG, R.string.mpp_about, false, true);
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getTopPullRefreshListener() {
		return null;
	}

	@Nullable
	@Override
	protected ListViewAwareOnRefreshListener getBottomPullRefreshListener() {
		return null;
	}

	@Nonnull
	@Override
	protected BaseListItemAdapter<AboutListItem> createAdapter() {
		final List<AboutListItem> listItems = new ArrayList<AboutListItem>();
		for (AboutType aboutType : AboutType.values()) {
			listItems.add(new AboutListItem(aboutType));
		}
		return new BaseListItemAdapter<AboutListItem>(getThemeContext(), listItems);
	}
}
