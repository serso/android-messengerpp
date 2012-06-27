package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.users.User;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:28 PM
 */
public abstract class MessengerFragmentActivity extends FragmentActivity {

    @NotNull
    private final MessengerCommonActivity activity;

    protected MessengerFragmentActivity(int layoutId) {
        activity = new MessengerCommonActivityImpl(layoutId, null);
    }

    protected MessengerFragmentActivity(int layoutId, boolean createFooterButtons) {
        activity = new MessengerCommonActivityImpl(layoutId, null, createFooterButtons);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity.onCreate(this);
    }

    @NotNull
    public User getUser() {
        return activity.getUser();
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
    public ServiceLocator getServiceLocator() {
        return activity.getServiceLocator();
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
    protected void onRestart() {
        super.onRestart();
        this.activity.onRestart(this);
    }
}
