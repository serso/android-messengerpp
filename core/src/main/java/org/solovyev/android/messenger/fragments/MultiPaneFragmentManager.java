package org.solovyev.android.messenger.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import org.solovyev.android.Activities;
import org.solovyev.android.messenger.core.R;
import org.solovyev.common.Builder;
import org.solovyev.common.JPredicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/7/13
 * Time: 9:02 PM
 */
public class MultiPaneFragmentManager {

    @Nonnull
    private static final String TAG = "MultiPaneFragmentManager";

    @Nonnull
    private final FragmentActivity activity;

    private final int mainPaneViewId;

    @Nonnull
    private final Class<? extends Fragment> emptyFragmentClass;

    @Nonnull
    private final String emptyFragmentTag;

    public MultiPaneFragmentManager(@Nonnull FragmentActivity activity,
                                    int mainPaneViewId,
                                    @Nonnull Class<? extends Fragment> emptyFragmentClass,
                                    @Nonnull String emptyFragmentTag) {
        this.activity = activity;
        this.mainPaneViewId = mainPaneViewId;
        this.emptyFragmentClass = emptyFragmentClass;
        this.emptyFragmentTag = emptyFragmentTag;
    }

    /*
    **********************************************************************
    *
    *                           GETTER
    *
    **********************************************************************
    */

    @Nonnull
    public FragmentActivity getActivity() {
        return activity;
    }

    /*
    **********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */


    @Nonnull
    private MultiPaneFragmentDef createEmptyMultiPaneFragmentDef(int paneViewId) {
        return MultiPaneFragmentDef.forClass(getEmptyFragmentTag(paneViewId), false, emptyFragmentClass, activity, null);
    }

    @Nonnull
    private String getEmptyFragmentTag(int paneViewId) {
        return emptyFragmentTag + "-" + paneViewId;
    }

    protected void setFragment(int fragmentViewId, @Nonnull MultiPaneFragmentDef mpfd) {
        final FragmentManager fm = activity.getSupportFragmentManager();

        final FragmentTransaction ft = fm.beginTransaction();

        setFragment(fragmentViewId, mpfd, fm, ft);

        ft.commit();

        // we cannot wait until android will execute pending transactions as some logic rely on added/attached transactions
        executePendingTransactions(fm);
    }

    private void setFragment(final int fragmentViewId,
                             @Nonnull MultiPaneFragmentDef mpfd,
                             @Nonnull FragmentManager fm,
                             @Nonnull FragmentTransaction ft) {
        hideKeyboard();

        // in some cases we cannot execute pending transactions after commit (e.g. transactions from action bar => we need to try to execute them now)
        boolean canContinue = executePendingTransactions(fm);

        if (canContinue) {
            ft.setCustomAnimations(R.anim.mpp_fragment_fade_in, R.anim.mpp_fragment_fade_out);

            if (mpfd.isAddToBackStack()){
                ft.addToBackStack(mpfd.getTag());
            }
            /**
             * Fragments identified by tag defines set of fragments of the same TYPE
             */
            final Fragment fragmentByTag = fm.findFragmentByTag(mpfd.getTag());

            /**
             * Fragments identified by tag defines set of fragments of the same LOCATION in view
             */
            final Fragment fragmentById = fm.findFragmentById(fragmentViewId);

            if (fragmentByTag != null) {
                // found fragment of known type - reuse?
                if (mpfd.canReuse(fragmentByTag)) {
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
                            ft.add(fragmentViewId, mpfd.build(), mpfd.getTag());
                        }
                    }
                } else {
                    // fragment cannot be reused
                    tryToPreserveFragment(ft, fragmentByTag);
                    if (fragmentById != null) {
                        tryToPreserveFragment(ft, fragmentById);
                    }
                    // add new fragment
                    ft.add(fragmentViewId, mpfd.build(), mpfd.getTag());
                }

            } else {
                if (fragmentById != null) {
                    tryToPreserveFragment(ft, fragmentById);
                }
                ft.add(fragmentViewId, mpfd.build(), mpfd.getTag());
            }
        }
    }

    private void hideKeyboard() {
        final View focusedView = activity.getCurrentFocus();

        if (focusedView != null) {
            final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    /**
     * Method executes all pending transactions in {@link FragmentManager}.
     * @param fm fragment manager
     * @return true if pending transactions were successfully executed, false otherwise
     */
    private boolean executePendingTransactions(@Nonnull FragmentManager fm) {
        boolean success;
        // we must run all pending transactions to be sure that no fragments for same tags are in pending list
        try {
            fm.executePendingTransactions();
            success = true;
        } catch (RuntimeException e) {
            success = false;
            /**
             * May be thrown by {@link android.support.v4.app.FragmentManager#executePendingTransactions()}.
             */
            Log.e(TAG, e.getMessage(), e);

            // we cannot work with the same UI anymore => restart activity (as persistence data is OK, only UI is broken)
            Activities.restartActivity(activity);
        }
        return success;
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

    private void goBackTillStart(@Nonnull FragmentManager fm) {
        if (!activity.isFinishing()) {
            int backStackEntryCount = fm.getBackStackEntryCount();
            for ( int i = 0; i < backStackEntryCount; i++) {
                fm.popBackStack();
            }
        }
    }
    public void goBackTillStart() {
        goBackTillStart(activity.getSupportFragmentManager());
    }

    public void goBack() {
        activity.getSupportFragmentManager().popBackStack();
    }

    public boolean goBackImmediately() {
        return activity.getSupportFragmentManager().popBackStackImmediate();
    }

    public void goBack(@Nonnull String tag) {
        activity.getSupportFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public boolean isFragmentShown(@Nonnull String fragmentTag) {
        final FragmentManager fm = activity.getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentByTag(fragmentTag);
        if ( fragment != null && fragment.isAdded() && !fragment.isDetached() ){
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    public <F extends Fragment> F getFragment(@Nonnull String fragmentTag) {
        final FragmentManager fm = activity.getSupportFragmentManager();
        return (F) fm.findFragmentByTag(fragmentTag);
    }

    protected void emptifyFragmentPane(int paneViewId) {
        setFragment(paneViewId, createEmptyMultiPaneFragmentDef(paneViewId));
    }

    /*
    **********************************************************************
    *
    *                           MAIN PANE
    *
    **********************************************************************
    */

    @Deprecated
    public void setMainFragment(@Nonnull Class<? extends Fragment> fragmentClass,
                                @Nullable Bundle fragmentArgs,
                                @Nullable JPredicate<Fragment> reuseCondition,
                                @Nonnull String fragmentTag,
                                boolean addToBackStack) {
        setMainFragment(MultiPaneFragmentDef.newInstance(fragmentTag, addToBackStack, ReflectionFragmentBuilder.forClass(activity, fragmentClass, fragmentArgs), reuseCondition));
    }

    @Deprecated
    public void setMainFragment(@Nonnull Builder<Fragment> fragmentBuilder,
                                @Nullable JPredicate<Fragment> reuseCondition,
                                @Nonnull String fragmentTag) {
        setMainFragment(fragmentBuilder, reuseCondition, fragmentTag, false);
    }

    @Deprecated
    public void setMainFragment(@Nonnull Builder<Fragment> fragmentBuilder,
                                @Nullable JPredicate<Fragment> reuseCondition,
                                @Nonnull String fragmentTag,
                                boolean addToBackStack) {
        setMainFragment(MultiPaneFragmentDef.newInstance(fragmentTag, addToBackStack, fragmentBuilder, reuseCondition));
    }

    public void setMainFragment(@Nonnull MultiPaneFragmentDef mpfd) {
        setFragment(mainPaneViewId, mpfd);
    }

    protected void emptifyMainFragment() {
        setMainFragment(createEmptyMultiPaneFragmentDef(mainPaneViewId));
    }

    public void setMainFragment(@Nonnull FragmentDef fragmentDef,
                                @Nonnull FragmentManager fm,
                                @Nonnull FragmentTransaction ft) {
        goBackTillStart(fm);

        setFragment(mainPaneViewId, MultiPaneFragmentDef.fromFragmentDef(fragmentDef, null, activity), fm, ft);
    }

    public void setMainFragment(@Nonnull FragmentDef fragmentDef, @Nullable Bundle fragmentArgs) {
        goBackTillStart(activity.getSupportFragmentManager());

        setFragment(mainPaneViewId, MultiPaneFragmentDef.fromFragmentDef(fragmentDef, fragmentArgs, activity));
    }

    public void setMainFragment(@Nonnull FragmentDef fragmentDef) {
        setMainFragment(fragmentDef, null);
    }
}
