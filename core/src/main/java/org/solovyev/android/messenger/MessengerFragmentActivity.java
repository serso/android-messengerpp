package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import org.solovyev.android.menu.ActivityMenu;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.menu.ListActivityMenu;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatGuiEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.fragments.MessengerMultiPaneFragmentManager;
import org.solovyev.android.messenger.fragments.MessengerPrimaryFragment;
import org.solovyev.android.messenger.messages.UnreadMessagesCounter;
import org.solovyev.android.messenger.realms.RealmService;
import org.solovyev.android.messenger.users.UserService;
import org.solovyev.android.sherlock.menu.SherlockMenuHelper;
import org.solovyev.common.listeners.AbstractJEventListener;
import org.solovyev.common.listeners.JEventListener;
import roboguice.RoboGuice;
import roboguice.event.EventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:28 PM
 */
public abstract class MessengerFragmentActivity extends RoboSherlockFragmentActivity {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    private static final String SELECTED_NAV = "selected_nav";

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
    private MessengerMultiPaneManager multiPaneManager;

    @Inject
    @Nonnull
    private MessengerListeners messengerListeners;

    @Inject
    @Nonnull
    private UnreadMessagesCounter unreadMessagesCounter;

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

    private int layoutId;

    private boolean showActionBarTabs = true;

    private boolean actionBarIconAsUp = true;

    @Nullable
    private ViewGroup secondPane;

    @Nullable
    private ViewGroup thirdPane;

    @Nonnull
    private final MessengerMultiPaneFragmentManager multiPaneFragmentManager;

    private ActivityMenu<Menu, MenuItem> menu;

    @Nullable
    private JEventListener<MessengerEvent> messengerEventListener;

    /*
    **********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

    protected MessengerFragmentActivity(int layoutId) {
        this.layoutId = layoutId;
        this.multiPaneFragmentManager = new MessengerMultiPaneFragmentManager(this);
    }

    protected MessengerFragmentActivity(int layoutId, boolean showActionBarTabs, boolean actionBarIconAsUp) {
        this.layoutId = layoutId;
        this.showActionBarTabs = showActionBarTabs;
        this.actionBarIconAsUp = actionBarIconAsUp;
        this.multiPaneFragmentManager = new MessengerMultiPaneFragmentManager(this);
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

    @Nonnull
    public MessengerListeners getMessengerListeners() {
        return messengerListeners;
    }

    @Nonnull
    public UnreadMessagesCounter getUnreadMessagesCounter() {
        return unreadMessagesCounter;
    }

    @Nonnull
    public MessengerMultiPaneManager getMultiPaneManager() {
        return multiPaneManager;
    }

    public boolean isDualPane() {
        return this.secondPane != null;
    }

    public boolean isTriplePane() {
        return this.thirdPane != null;
    }

    @Nonnull
    public MessengerMultiPaneFragmentManager getMultiPaneFragmentManager() {
        return multiPaneFragmentManager;
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

        setContentView(layoutId);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(actionBarIconAsUp);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        if (showActionBarTabs) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            addTab(MessengerPrimaryFragment.contacts);
            addTab(MessengerPrimaryFragment.messages);
            addTab(MessengerPrimaryFragment.realms);
            addTab(MessengerPrimaryFragment.settings);

            int navPosition = -1;
            if (savedInstanceState != null) {
                navPosition = savedInstanceState.getInt(SELECTED_NAV, -1);
            }

            if (navPosition >= 0) {
                getSupportActionBar().setSelectedNavigationItem(navPosition);
            }
        }

        this.secondPane = (ViewGroup) findViewById(R.id.content_second_pane);
        this.thirdPane = (ViewGroup) findViewById(R.id.content_third_pane);

        this.messengerEventListener = UiThreadEventListener.wrap(this, new MessengerEventListener());
        this.messengerListeners.addListener(messengerEventListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_NAV, getSupportActionBar().getSelectedNavigationIndex());
    }


    private void addTab(@Nonnull final MessengerPrimaryFragment messengerPrimaryFragment) {
        final String fragmentTag = messengerPrimaryFragment.getFragmentTag();

        final ActionBar actionBar = getSupportActionBar();
        final ActionBar.Tab tab = actionBar.newTab();
        tab.setTag(fragmentTag);
        tab.setText(messengerPrimaryFragment.getTitleResId());
        tab.setTabListener(new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                emptifyNotPrimaryPanes();
                getMultiPaneFragmentManager().setMainFragment(messengerPrimaryFragment, getSupportFragmentManager(), ft);
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                emptifyNotPrimaryPanes();
                // in some cases we reuse pane for another fragment under same tab -> we need to reset fragment (in case if fragment has not been changed nothing is done)
                getMultiPaneFragmentManager().setMainFragment(messengerPrimaryFragment, getSupportFragmentManager(), ft);
            }
        });
        actionBar.addTab(tab);
    }

    private void emptifyNotPrimaryPanes() {
        if (isDualPane()) {
            getMultiPaneFragmentManager().emptifySecondFragment();
            if (isTriplePane()) {
                getMultiPaneFragmentManager().emptifyThirdFragment();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if ( this.messengerEventListener != null ) {
            this.messengerListeners.removeListener(messengerEventListener);
        }

        super.onDestroy();
    }

    /*
    **********************************************************************
    *
    *                           MENU
    *
    **********************************************************************
    */

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean result = this.menu.onPrepareOptionsMenu(this, menu);

        onUnreadMessagesCountChanged(menu, unreadMessagesCounter.getUnreadMessagesCount());

        return result;
    }

    private void onUnreadMessagesCountChanged(@Nonnull Menu menu, int unreadMessagesCount) {
        final MenuItem menuItem = menu.findItem(R.id.mpp_menu_unread_messages_counter);
        if (unreadMessagesCount == 0) {
            menuItem.setVisible(false);
            menuItem.setEnabled(false);
        } else {
            menuItem.setTitle(String.valueOf(unreadMessagesCount));
            menuItem.setVisible(true);
            menuItem.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        if ( this.menu == null ) {
            final List<IdentifiableMenuItem<MenuItem>> menuItems = new ArrayList<IdentifiableMenuItem<MenuItem>>(1);
            menuItems.add(new MenuItemAppExitMenuItem(this));
            menuItems.add(new UnreadMessagesCounterMenuItem(this));

            this.menu = ListActivityMenu.fromResource(R.menu.mpp_menu_main, menuItems, SherlockMenuHelper.getInstance());
        }
        return this.menu.onCreateOptionsMenu(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (actionBarIconAsUp) {
                    if ( !multiPaneFragmentManager.goBackImmediately() ) {
                        final ActionBar.Tab tab = findTabByTag(MessengerPrimaryFragment.contacts.getFragmentTag());
                        if ( tab != null ) {
                            tab.select();
                        }
                    }
                }
                return true;
            default:
                return this.menu.onOptionsItemSelected(this, item);
        }
    }

    /*@Nullable*/
    public ActionBar.Tab findTabByTag(/*@NotNull*/ String tag) {
        final ActionBar actionBar = getSupportActionBar();
        if ( actionBar != null ) {
            for ( int i = 0; i < actionBar.getTabCount(); i++ ) {
                final ActionBar.Tab tab = actionBar.getTabAt(i);
                if ( tab != null && tag.equals(tab.getTag()) ) {
                    return tab;
                }
            }
        }

        return null;
    }

    private static class MenuItemAppExitMenuItem implements IdentifiableMenuItem<MenuItem> {

        @Nonnull
        private final Activity activity;

        private MenuItemAppExitMenuItem(@Nonnull Activity activity) {
            this.activity = activity;
        }

        @Nonnull
        @Override
        public Integer getItemId() {
            return R.id.mpp_menu_app_exit;
        }

        @Override
        public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
            MessengerApplication.getApp().exit(activity);
        }
    }

    private static class UnreadMessagesCounterMenuItem implements IdentifiableMenuItem<MenuItem> {

        @Nonnull
        private final MessengerFragmentActivity activity;

        private UnreadMessagesCounterMenuItem(@Nonnull MessengerFragmentActivity activity) {
            this.activity = activity;
        }

        @Nonnull
        @Override
        public Integer getItemId() {
            return R.id.mpp_menu_unread_messages_counter;
        }

        @Override
        public void onClick(@Nonnull MenuItem data, @Nonnull Context context) {
            final Entity chatEntity = activity.getUnreadMessagesCounter().getUnreadChat();
            if (chatEntity != null) {
                final Chat chat = activity.getChatService().getChatById(chatEntity);
                if (chat != null) {
                    final EventManager eventManager = RoboGuice.getInjector(context).getInstance(EventManager.class);
                    eventManager.fire(ChatGuiEventType.chat_open_requested.newEvent(chat));
                }
            }
        }
    }

    private class MessengerEventListener extends AbstractJEventListener<MessengerEvent> {

        protected MessengerEventListener() {
            super(MessengerEvent.class);
        }

        @Override
        public void onEvent(@Nonnull MessengerEvent event) {
            switch (event.getType()) {
                case unread_messages_count_changed:
                    invalidateOptionsMenu();
                    break;
            }
        }
    }
}
