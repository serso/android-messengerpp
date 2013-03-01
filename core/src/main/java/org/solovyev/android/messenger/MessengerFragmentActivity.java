package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.common.JPredicate;
import roboguice.event.EventManager;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:28 PM
 */
public abstract class MessengerFragmentActivity extends RoboSherlockFragmentActivity {

    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @NotNull
    private UserService userService;

    @Inject
    @NotNull
    private ChatService chatService;

    @Inject
    @NotNull
    private RealmService realmService;

    @Inject
    @NotNull
    private EventManager eventManager;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */
    @Nullable
    private ViewGroup secondPane;

    @Nullable
    private ViewGroup thirdPane;

    @NotNull
    private final MessengerCommonActivity activity;

    protected MessengerFragmentActivity(int layoutId) {
        activity = new MessengerCommonActivityImpl(layoutId);
    }

    protected MessengerFragmentActivity(int layoutId, boolean showActionBarTabs, boolean homeIcon) {
        activity = new MessengerCommonActivityImpl(layoutId, showActionBarTabs, homeIcon);
    }

    @NotNull
    protected UserService getUserService() {
        return userService;
    }

    @NotNull
    protected ChatService getChatService() {
        return chatService;
    }

    @NotNull
    protected EventManager getEventManager() {
        return eventManager;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity.onCreate(this, savedInstanceState);

        this.secondPane = (ViewGroup) findViewById(R.id.content_second_pane);
        this.thirdPane = (ViewGroup) findViewById(R.id.content_third_pane);
    }

    protected boolean isDualPane() {
        return this.secondPane != null;
    }

    protected boolean isTriplePane() {
        return this.thirdPane != null;
    }

    @NotNull
    public ViewGroup getFooterCenter() {
        return activity.getFooterCenter(this);
    }

    @NotNull
    public ViewGroup getFooterRight() {
        return activity.getFooterRight(this);
    }

    @NotNull
    public ViewGroup getFooterLeft() {
        return activity.getFooterLeft(this);
    }

    @NotNull
    public ViewGroup getHeaderLeft() {
        return this.activity.getHeaderLeft(this);
    }

    @NotNull
    public ViewGroup getHeaderCenter() {
        return this.activity.getHeaderCenter(this);
    }

    @NotNull
    public ViewGroup getHeaderRight() {
        return this.activity.getHeaderRight(this);
    }

    @NotNull
    public ViewGroup getCenter(@NotNull Activity activity) {
        return this.activity.getCenter(activity);
    }

    @NotNull
    public ImageButton createFooterImageButton(int imageResId, int contentDescriptionResId) {
        return activity.createFooterImageButton(imageResId, contentDescriptionResId, this);
    }

    @NotNull
    public Button createFooterButton(int captionResId) {
        return this.activity.createFooterButton(captionResId, this);
    }

    @NotNull
    public ViewPager initTitleForViewPager(@NotNull Activity activity, @NotNull ViewPager.OnPageChangeListener listener, @NotNull PagerAdapter adapter) {
        return this.activity.initTitleForViewPager(activity, listener, adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        this.activity.onSaveInstanceState(this, outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.activity.onRestart(this);
    }

    protected void setFragment(int fragmentContainerViewId, @NotNull Fragment fragment, @Nullable String tag) {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        boolean oldFragmentUsed = false;
        if (tag != null) {
            final Fragment oldFragment = fragmentManager.findFragmentByTag(tag);
            if ( oldFragment != null ) {
                if ( oldFragment.isDetached() ) {
                    fragmentTransaction.attach(oldFragment);
                    oldFragmentUsed = true;
                } else if ( !oldFragment.isAdded() ) {
                    fragmentTransaction.add(fragmentContainerViewId, oldFragment, tag);
                    oldFragmentUsed = true;
                }
            }
        }

        if (!oldFragmentUsed) {
            fragmentTransaction.add(fragmentContainerViewId, fragment, tag);
        }

        fragmentTransaction.commit();
    }

    protected void setFragment(int fragmentViewId, @NotNull Class<? extends Fragment> fragmentClass, @NotNull String fragmentTag, @Nullable Bundle fragmentArgs) {
        trySetFragment(fragmentViewId, fragmentClass, fragmentTag, fragmentArgs, null, false, null);
    }

    /**
     * @param fragmentViewId
     * @param fragmentClass
     * @param fragmentTag
     * @param fragmentArgs
     * @param reuseCondition true if fragment can be reused
     */
    protected void trySetFragment(int fragmentViewId,
                                  @NotNull Class<? extends Fragment> fragmentClass,
                                  @NotNull String fragmentTag,
                                  @Nullable Bundle fragmentArgs,
                                  @Nullable JPredicate<Fragment> reuseCondition,
                                  boolean addToBackStack,
                                  @Nullable String emptyFragmentTag) {
        final FragmentManager fm = getSupportFragmentManager();

        final Fragment oldEmptyFragment = fm.findFragmentByTag(emptyFragmentTag);

        final FragmentTransaction ft = fm.beginTransaction();

        final Fragment oldFragment = fm.findFragmentByTag(fragmentTag);
        if (oldFragment != null) {
            if (reuseCondition == null || reuseCondition.apply(oldFragment)) {
                if (!oldFragment.isAdded()) {
                    ft.add(fragmentViewId, oldFragment, fragmentTag);
                    if ( addToBackStack ) {
                        ft.addToBackStack(null);
                    }
                }
            } else {
                if (oldFragment.isAdded()) {
                    ft.remove(oldFragment);
                }

                ft.add(fragmentViewId, Fragment.instantiate(this, fragmentClass.getName(), fragmentArgs), fragmentTag);
                if ( addToBackStack ) {
                    ft.addToBackStack(null);
                }
            }

        } else {
            if (oldEmptyFragment != null && oldEmptyFragment.isAdded()) {
                ft.remove(oldEmptyFragment);
            }
            ft.add(fragmentViewId, Fragment.instantiate(this, fragmentClass.getName(), fragmentArgs), fragmentTag);
            if ( addToBackStack ) {
                ft.addToBackStack(null);
            }
        }

        ft.commit();
    }

    @NotNull
    protected RealmService getRealmService() {
        return realmService;
    }
}
