package org.solovyev.android.messenger.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.messages.MessengerEmptyFragment;
import org.solovyev.android.messenger.realms.SimpleFragmentReuseCondition;
import org.solovyev.common.Builder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 9:02 PM
 */
public class MessengerFragmentService {

    @Nonnull
    private static final String TAG = "M++/FragmentManager";

    @Nonnull
    private final FragmentActivity activity;

    public MessengerFragmentService(@Nonnull FragmentActivity activity) {
        this.activity = activity;
    }

    public void setFirstFragment(@Nonnull Class<? extends Fragment> fragmentClass,
                                    @Nullable Bundle fragmentArgs,
                                    @Nullable JPredicate<Fragment> reuseCondition,
                                    @Nonnull String fragmentTag) {
        setFragment(R.id.content_first_pane, fragmentTag, ReflectionFragmentBuilder.newInstance(activity, fragmentClass, fragmentArgs), reuseCondition);
    }

    public void setFirstFragment(@Nonnull Builder<Fragment> fragmentBuilder,
                                    @Nullable JPredicate<Fragment> reuseCondition,
                                    @Nonnull String fragmentTag) {
        setFragment(R.id.content_first_pane, fragmentTag, fragmentBuilder, reuseCondition);
    }

    protected void emptifyFirstFragment() {
        setFirstFragment(MessengerEmptyFragment.class, null, EmptyFragmentReuseCondition.getInstance(), MessengerEmptyFragment.FRAGMENT_TAG + "-1");
    }

    public void setSecondFragment(@Nonnull Class<? extends Fragment> fragmentClass,
                                  @Nullable Bundle fragmentArgs,
                                  @Nullable JPredicate<Fragment> reuseCondition,
                                  @Nonnull String fragmentTag) {
        setFragment(R.id.content_second_pane, fragmentTag, ReflectionFragmentBuilder.newInstance(activity, fragmentClass, fragmentArgs), reuseCondition);
    }

    public void setSecondFragment(@Nonnull Builder<Fragment> fragmentBuilder,
                                  @Nullable JPredicate<Fragment> reuseCondition,
                                  @Nonnull String fragmentTag) {
        setFragment(R.id.content_second_pane, fragmentTag, fragmentBuilder, reuseCondition);
    }

    public void emptifySecondFragment() {
        setSecondFragment(MessengerEmptyFragment.class, null, EmptyFragmentReuseCondition.getInstance(), MessengerEmptyFragment.FRAGMENT_TAG + "-2");
    }

    public void setThirdFragment(@Nonnull Class<? extends Fragment> fragmentClass,
                                 @Nullable Bundle fragmentArgs,
                                 @Nullable JPredicate<Fragment> reuseCondition,
                                 @Nonnull String fragmentTag) {
        setFragment(R.id.content_third_pane, fragmentTag, ReflectionFragmentBuilder.newInstance(activity, fragmentClass, fragmentArgs), reuseCondition);
    }

    public void setThirdFragment(@Nonnull Builder<Fragment> fragmentBuilder,
                                 @Nullable JPredicate<Fragment> reuseCondition,
                                 @Nonnull String fragmentTag) {
        setFragment(R.id.content_third_pane, fragmentTag, fragmentBuilder, reuseCondition);
    }

    public void emptifyThirdFragment() {
        setThirdFragment(MessengerEmptyFragment.class, null, EmptyFragmentReuseCondition.getInstance(), MessengerEmptyFragment.FRAGMENT_TAG + "-3");
    }

    protected void setFragment(int fragmentViewId, @Nonnull Class<? extends Fragment> fragmentClass, @Nonnull String fragmentTag, @Nullable Bundle fragmentArgs) {
        setFragment(fragmentViewId, fragmentTag, ReflectionFragmentBuilder.newInstance(activity, fragmentClass, fragmentArgs), null);
    }

    /**
     * @param fragmentViewId
     * @param fragmentTag
     * @param fragmentBuilder
     * @param reuseCondition  true if fragment can be reused
     */
    private void setFragment(int fragmentViewId,
                             @Nonnull String fragmentTag,
                             @Nonnull Builder<Fragment> fragmentBuilder,
                             @Nullable JPredicate<Fragment> reuseCondition) {
        final FragmentManager fm = activity.getSupportFragmentManager();

        // we must run all pending transactions to be sure that no fragments for same tags are in pending list
        try {
            fm.executePendingTransactions();

            final FragmentTransaction ft = fm.beginTransaction();

            setFragment(fragmentViewId, fragmentTag, fragmentBuilder, reuseCondition, fm, ft);

            ft.commit();
        } catch (IllegalStateException e) {
            /**
             * May be thrown by {@link android.support.v4.app.FragmentManager#executePendingTransactions()}.
             */
            Log.e(TAG, e.getMessage(), e);
        }
    }

    void setFragment(int fragmentViewId,
                     @Nonnull String fragmentTag,
                     @Nonnull Builder<Fragment> fragmentBuilder,
                     @Nullable JPredicate<Fragment> reuseCondition,
                     @Nonnull FragmentManager fm,
                     @Nonnull FragmentTransaction ft) {
        ft.setCustomAnimations(R.anim.mpp_fragment_fade_in, R.anim.mpp_fragment_fade_out);

        /**
         * Fragments identified by tag defines set of fragments of the same TYPE
         */
        final Fragment fragmentByTag = fm.findFragmentByTag(fragmentTag);

        /**
         * Fragments identified by tag defines set of fragments of the same LOCATION in view
         */
        final Fragment fragmentById = fm.findFragmentById(fragmentViewId);
        if (fragmentByTag != null) {
            // found fragment of known type - reuse?
            if (reuseCondition != null && reuseCondition.apply(fragmentByTag)) {
                // fragment can be reused
                if (fragmentByTag.isDetached()) {
                    if (fragmentById != null) {
                        tryToPreserveFragment(ft, fragmentById);
                    }
                    // fragment is detached and can be simple reused
                    ft.attach(fragmentByTag);
                } else {
                    // fragment is not free - it is already shown on the view
                    // let's check that it is shown in the right place
                    if (fragmentByTag.equals(fragmentById)) {
                        // yes, fragment is shown under the view with same ID
                    } else {
                        // no, fragment is shown somewhere else, but that's bad - either fragment was not correctly detached or it has been already added
                        if (fragmentById != null) {
                            tryToPreserveFragment(ft, fragmentById);
                        }
                        // add new fragment
                        ft.add(fragmentViewId, fragmentBuilder.build(), fragmentTag);
                    }
                }
            } else {
                // fragment cannot be reused
                tryToPreserveFragment(ft, fragmentByTag);
                if (fragmentById != null) {
                    tryToPreserveFragment(ft, fragmentById);
                }
                // add new fragment
                ft.add(fragmentViewId, fragmentBuilder.build(), fragmentTag);
            }

        } else {
            if (fragmentById != null) {
                tryToPreserveFragment(ft, fragmentById);
            }
            ft.add(fragmentViewId, fragmentBuilder.build(), fragmentTag);
        }
    }

    private void tryToPreserveFragment(@Nonnull FragmentTransaction ft, @Nonnull Fragment fragment) {
        // let's see if we can preserve old fragment for further use
        if (fragment instanceof DetachableFragment) {
            // yes, we can => detach if not detached yet
            if (!fragment.isDetached()) {
                ft.detach(fragment);
            }
        } else {
            // no, we cannot => remove
            if (fragment.isAdded()) {
                ft.remove(fragment);
            }
        }
    }

    public void setPrimaryFragment(@Nonnull MessengerPrimaryFragment primaryFragment,
                                   @Nonnull FragmentManager fm,
                                   @Nonnull FragmentTransaction ft) {
        final Class<? extends Fragment> fragmentClass = primaryFragment.getFragmentClass();
        final Builder<Fragment> fragmentBuilder = ReflectionFragmentBuilder.newInstance(activity, fragmentClass, null);
        final JPredicate<Fragment> fragmentReuseCondition = SimpleFragmentReuseCondition.forClass(fragmentClass);
        setFragment(R.id.content_first_pane, primaryFragment.getTag(), fragmentBuilder, fragmentReuseCondition, fm, ft);
    }

    public void setPrimaryFragment(@Nonnull MessengerPrimaryFragment primaryFragment) {
        final Class<? extends Fragment> fragmentClass = primaryFragment.getFragmentClass();
        final Builder<Fragment> fragmentBuilder = ReflectionFragmentBuilder.newInstance(activity, fragmentClass, null);
        final JPredicate<Fragment> fragmentReuseCondition = SimpleFragmentReuseCondition.forClass(fragmentClass);
        setFragment(R.id.content_first_pane, primaryFragment.getTag(), fragmentBuilder, fragmentReuseCondition);
    }

    private static final class EmptyFragmentReuseCondition implements JPredicate<Fragment> {

        @Nonnull
        private static final EmptyFragmentReuseCondition instance = new EmptyFragmentReuseCondition();

        private EmptyFragmentReuseCondition() {
        }

        @Nonnull
        public static EmptyFragmentReuseCondition getInstance() {
            return instance;
        }

        @Override
        public boolean apply(@Nullable Fragment fragment) {
            return fragment instanceof MessengerEmptyFragment;
        }
    }

}