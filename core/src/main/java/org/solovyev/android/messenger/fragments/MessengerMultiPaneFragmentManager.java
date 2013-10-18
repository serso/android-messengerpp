package org.solovyev.android.messenger.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.solovyev.android.fragments.MultiPaneFragmentDef;
import org.solovyev.android.fragments.MultiPaneFragmentManager;
import org.solovyev.android.fragments.ReflectionFragmentBuilder;
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
		super(activity, R.id.content_first_pane, EmptyFragment.class, EmptyFragment.FRAGMENT_TAG, R.anim.mpp_fragment_fade_in, R.anim.mpp_fragment_fade_out);
	}

	@Nonnull
	@Override
	public BaseFragmentActivity getActivity() {
		return (BaseFragmentActivity) super.getActivity();
	}

	public void setSecondFragment(@Nonnull Class<? extends Fragment> fragmentClass,
								  @Nullable Bundle fragmentArgs,
								  @Nullable JPredicate<Fragment> reuseCondition,
								  @Nonnull String fragmentTag,
								  boolean addToBackStack) {
		if (!addToBackStack) {
			goBackTillStart();
		}
		setFragment(R.id.content_second_pane, MultiPaneFragmentDef.newInstance(fragmentTag, addToBackStack, ReflectionFragmentBuilder.forClass(getActivity(), fragmentClass, fragmentArgs), reuseCondition));
	}

	public void setSecondFragment(@Nonnull Builder<Fragment> fragmentBuilder,
								  @Nullable JPredicate<Fragment> reuseCondition,
								  @Nonnull String fragmentTag) {
		goBackTillStart();
		setFragment(R.id.content_second_pane, MultiPaneFragmentDef.newInstance(fragmentTag, false, fragmentBuilder, reuseCondition));
	}

	public void emptifySecondFragment() {
		goBackTillStart();
		emptifyFragmentPane(R.id.content_second_pane);
	}

	public void setThirdFragment(@Nonnull Class<? extends Fragment> fragmentClass,
								 @Nullable Bundle fragmentArgs,
								 @Nullable JPredicate<Fragment> reuseCondition,
								 @Nonnull String fragmentTag) {
		goBackTillStart();
		setFragment(R.id.content_third_pane, MultiPaneFragmentDef.newInstance(fragmentTag, false, ReflectionFragmentBuilder.forClass(getActivity(), fragmentClass, fragmentArgs), reuseCondition));
	}

	public void setThirdFragment(@Nonnull Builder<Fragment> fragmentBuilder,
								 @Nullable JPredicate<Fragment> reuseCondition,
								 @Nonnull String fragmentTag) {
		goBackTillStart();
		setFragment(R.id.content_third_pane, MultiPaneFragmentDef.newInstance(fragmentTag, false, fragmentBuilder, reuseCondition));
	}

	public void emptifyThirdFragment() {
		goBackTillStart();
		emptifyFragmentPane(R.id.content_third_pane);
	}

	public void setSecondOrMainFragment(Class<? extends Fragment> fragmentClass, Bundle fragmentArgs, String fragmentTag) {
		if (getActivity().isDualPane()) {
			setSecondFragment(fragmentClass, fragmentArgs, null, fragmentTag, true);
		} else {
			setMainFragment(fragmentClass, fragmentArgs, null, fragmentTag, false);
		}
	}
}
