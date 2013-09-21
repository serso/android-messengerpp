package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import android.view.View;
import org.solovyev.common.listeners.AbstractTypedJEvent;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 5:18 PM
 */
public final class FragmentUiEvent extends AbstractTypedJEvent<Class<? extends Fragment>, FragmentUiEventType> {

	FragmentUiEvent(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull FragmentUiEventType type) {
		super(fragmentClass, type, View.NO_ID);
	}

	FragmentUiEvent(@Nonnull Class<? extends Fragment> fragmentClass, @Nonnull FragmentUiEventType type, int parentViewId) {
		super(fragmentClass, type, parentViewId);
	}

	@Nonnull
	public Class<? extends Fragment> getFragmentClass() {
		return getEventObject();
	}

	public int getParentViewId() {
		return (Integer) getData();
	}
}

