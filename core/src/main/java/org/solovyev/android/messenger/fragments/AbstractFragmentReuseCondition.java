package org.solovyev.android.messenger.fragments;

import android.support.v4.app.Fragment;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 1:35 PM
 */
public abstract class AbstractFragmentReuseCondition<F extends Fragment> implements JPredicate<Fragment> {

    @Nonnull
    private final Class<F> fragmentClass;

    public AbstractFragmentReuseCondition(@Nonnull Class<F> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    @Override
    public final boolean apply(@Nullable Fragment f) {
        if ( f != null && fragmentClass.isAssignableFrom(f.getClass()) ) {
            return canReuseFragment((F) f);
        } else {
            return false;
        }
    }

    /**
     * @param fragment typed for fragment on which reuse check must be done
     * @return true if <var>fragment</var> can be reused
     */
    protected abstract boolean canReuseFragment(@Nonnull F fragment);
}
