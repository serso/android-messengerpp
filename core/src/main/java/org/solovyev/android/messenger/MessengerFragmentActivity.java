package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.fragments.MessengerFragmentService;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.UserService;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:28 PM
 */
public abstract class MessengerFragmentActivity extends RoboSherlockFragmentActivity {

    /*
    **********************************************************************
    *
    *                           CONSTATS
    *
    **********************************************************************
    */

    protected final String TAG = this.getClass().getSimpleName();


    /*
    **********************************************************************
    *
    *                           AUTO INJECTED FIELDS
    *
    **********************************************************************
    */

    @Inject
    @Nonnull
    private UserService userService;

    @Inject
    @Nonnull
    private ChatService chatService;

    @Inject
    @Nonnull
    private RealmService realmService;

    @Inject
    @Nonnull
    private EventManager eventManager;


    /*
    **********************************************************************
    *
    *                           OWN FIELDS
    *
    **********************************************************************
    */

    @Nullable
    private ViewGroup firstPane;

    @Nullable
    private ViewGroup secondPane;

    @Nullable
    private ViewGroup thirdPane;

    @Nonnull
    private final MessengerCommonActivity activity;

    @Nonnull
    private final MessengerFragmentService fragmentService;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    protected MessengerFragmentActivity(int layoutId) {
        activity = new MessengerCommonActivityImpl(layoutId);
        fragmentService = new MessengerFragmentService(this);
    }

    protected MessengerFragmentActivity(int layoutId, boolean showActionBarTabs, boolean homeIcon) {
        activity = new MessengerCommonActivityImpl(layoutId, showActionBarTabs, homeIcon);
        fragmentService = new MessengerFragmentService(this);
    }

    /*
    **********************************************************************
    *
    *                           GETTERS/SETTERS
    *
    **********************************************************************
    */

    @Nonnull
    protected UserService getUserService() {
        return userService;
    }

    @Nonnull
    public ChatService getChatService() {
        return chatService;
    }

    @Nonnull
    public EventManager getEventManager() {
        return eventManager;
    }


    @Nonnull
    public RealmService getRealmService() {
        return realmService;
    }


    public boolean isDualPane() {
        return this.secondPane != null;
    }

    public boolean isTriplePane() {
        return this.thirdPane != null;
    }

    @Nonnull
    public ViewGroup getFooterCenter() {
        return activity.getFooterCenter(this);
    }

    @Nonnull
    public ViewGroup getFooterRight() {
        return activity.getFooterRight(this);
    }

    @Nonnull
    public ViewGroup getFooterLeft() {
        return activity.getFooterLeft(this);
    }

    @Nonnull
    public ViewGroup getHeaderLeft() {
        return this.activity.getHeaderLeft(this);
    }

    @Nonnull
    public ViewGroup getHeaderCenter() {
        return this.activity.getHeaderCenter(this);
    }

    @Nonnull
    public ViewGroup getHeaderRight() {
        return this.activity.getHeaderRight(this);
    }

    @Nonnull
    public ViewGroup getCenter(@Nonnull Activity activity) {
        return this.activity.getCenter(activity);
    }

    @Nonnull
    public ImageButton createFooterImageButton(int imageResId, int contentDescriptionResId) {
        return activity.createFooterImageButton(imageResId, contentDescriptionResId, this);
    }

    @Nonnull
    public Button createFooterButton(int captionResId) {
        return this.activity.createFooterButton(captionResId, this);
    }

    @Nonnull
    public ViewPager initTitleForViewPager(@Nonnull Activity activity, @Nonnull ViewPager.OnPageChangeListener listener, @Nonnull PagerAdapter adapter) {
        return this.activity.initTitleForViewPager(activity, listener, adapter);
    }

    @Nonnull
    public MessengerFragmentService getFragmentService() {
        return fragmentService;
    }

    /*
    **********************************************************************
    *
    *                           LIFECYCLE
    *
    **********************************************************************
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity.onCreate(this, savedInstanceState);

        this.firstPane = (ViewGroup) findViewById(R.id.content_first_pane);
        this.secondPane = (ViewGroup) findViewById(R.id.content_second_pane);
        this.thirdPane = (ViewGroup) findViewById(R.id.content_third_pane);
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

}
