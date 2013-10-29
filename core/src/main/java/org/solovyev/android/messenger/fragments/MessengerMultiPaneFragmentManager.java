package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.fragments.MultiPaneFragmentManager;
import org.solovyev.android.messenger.BaseFragmentActivity;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.EmptyFragment;
import org.solovyev.common.Builder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.solovyev.android.messenger.fragments.PrimaryFragment.*;

public class MessengerMultiPaneFragmentManager extends MultiPaneFragmentManager {

	public static final List<PrimaryFragment> tabFragments;

	static {
		final List<PrimaryFragment> mutableTabFragments = new ArrayList<PrimaryFragment>();
		mutableTabFragments.add(contacts);
		mutableTabFragments.add(messages);
		mutableTabFragments.add(accounts);
		mutableTabFragments.add(settings);
		tabFragments = unmodifiableList(mutableTabFragments);
	}

	public MessengerMultiPaneFragmentManager(@Nonnull BaseFragmentActivity activity) {
		super(activity, R.id.content_first_pane, EmptyFragment.class, EmptyFragment.FRAGMENT_TAG);
	}

	@Nonnull
	@Override
	public BaseFragmentActivity getActivity() {
		return (BaseFragmentActivity) super.getActivity();
	}

	public void setSecondFragment(@Nonnull MultiPaneFragmentDef fragmentDef) {
		setFragment(R.id.content_second_pane, fragmentDef);
	}

	public void emptifySecondFragment() {
		emptifyFragmentPane(R.id.content_second_pane);
	}

	public void setThirdFragment(@Nonnull Builder<Fragment> fragmentBuilder,
								 @Nullable JPredicate<Fragment> reuseCondition,
								 @Nonnull String fragmentTag) {
		setFragment(R.id.content_third_pane, MultiPaneFragmentDef.newInstance(fragmentTag, false, fragmentBuilder, reuseCondition));
	}

	public void emptifyThirdFragment() {
		emptifyFragmentPane(R.id.content_third_pane);
	}

	public void clearBackStack() {
		final FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
	}
}
