package org.solovyev.android.messenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;
import org.solovyev.android.AThreads;
import org.solovyev.android.http.HttpRuntimeIoException;
import org.solovyev.android.messenger.chats.MessengerChatsFragment;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.messenger.http.IllegalJsonRuntimeException;
import org.solovyev.android.messenger.realms.MessengerRealmsFragment;
import org.solovyev.android.messenger.users.MessengerContactsFragment;
import org.solovyev.android.sherlock.tabs.ActionBarFragmentTabListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:41 PM
 */
public class MessengerCommonActivityImpl implements MessengerCommonActivity {

    /*
    **********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

    private static final String SELECTED_NAV = "selected_nav";


    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

    private int layoutId;

    private boolean showActionBarTabs = true;
    private boolean homeIcon = false;

    public MessengerCommonActivityImpl(int layoutId) {
        this.layoutId = layoutId;
    }

    public MessengerCommonActivityImpl(int layoutId, boolean showActionBarTabs, boolean homeIcon) {
        this.layoutId = layoutId;
        this.showActionBarTabs = showActionBarTabs;
        this.homeIcon = homeIcon;
    }

    @Override
    public void onCreate(@Nonnull final SherlockFragmentActivity activity, @Nullable Bundle savedInstanceState) {
        //activity.requestWindowFeature(Window.FEATURE_NO_TITLE);

        activity.setContentView(layoutId);

        final ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(homeIcon);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        if (showActionBarTabs) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            addTab(activity, "contacts", MessengerContactsFragment.class, null, R.string.mpp_tab_contacts);
            addTab(activity, "messages", MessengerChatsFragment.class, null, R.string.mpp_tab_messages);
            addTab(activity, "realms", MessengerRealmsFragment.class, null, R.string.mpp_tab_realms);

            // settings tab
            final ActionBar.Tab tab = actionBar.newTab();
            tab.setTag("settings");
            tab.setText(R.string.c_settings);
            tab.setTabListener(new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    activity.startActivity(new Intent(activity.getApplicationContext(), MessengerPreferencesActivity.class));
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                }
            });
            actionBar.addTab(tab);

            int navPosition = -1;
            if (savedInstanceState != null) {
                navPosition = savedInstanceState.getInt(SELECTED_NAV, -1);
            }

            if (navPosition >= 0) {
                activity.getSupportActionBar().setSelectedNavigationItem(navPosition);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@Nonnull SherlockFragmentActivity activity, @Nonnull Bundle outState) {
        outState.putInt(SELECTED_NAV, activity.getSupportActionBar().getSelectedNavigationIndex());
    }

    private void addTab(@Nonnull SherlockFragmentActivity activity,
                        @Nonnull String tag,
                        @Nonnull Class<? extends Fragment> fragmentClass,
                        @Nullable Bundle fragmentArgs,
                        int captionResId) {
        final ActionBar actionBar = activity.getSupportActionBar();
        final ActionBar.Tab tab = actionBar.newTab();
        tab.setTag(tag);
        tab.setText(captionResId);
        tab.setTabListener(new ActionBarFragmentTabListener(activity, tag, fragmentClass, fragmentArgs, R.id.content_first_pane));
        actionBar.addTab(tab);
    }

    @Override
    public void onRestart(@Nonnull Activity activity) {
    }

    @Override
    @Nonnull
    public ImageButton createFooterImageButton(int imageResId, int contentDescriptionResId, @Nonnull Activity activity) {
        final ImageButton result = FooterImageButtonBuilder.newInstance(imageResId, contentDescriptionResId).build(activity);
        result.setScaleType(ImageView.ScaleType.FIT_XY);
        return result;
    }

    @Nonnull
    @Override
    public Button createFooterButton(int captionResId, @Nonnull Activity activity) {
        return FooterButtonBuilder.newInstance(captionResId).build(activity);
    }

    @Override
    @Nonnull
    public ViewGroup getFooterLeft(@Nonnull Activity activity) {
        throw new UnsupportedOperationException();
        //return (ViewGroup) activity.findViewById(R.id.footer_left);
    }

    @Override
    @Nonnull
    public ViewGroup getFooterCenter(@Nonnull Activity activity) {
        throw new UnsupportedOperationException();
        //return (ViewGroup) activity.findViewById(R.id.footer_center);
    }

    @Override
    @Nonnull
    public ViewGroup getFooterRight(@Nonnull Activity activity) {
        throw new UnsupportedOperationException();
        //return (ViewGroup) activity.findViewById(R.id.footer_right);
    }

    @Nonnull
    @Override
    public ViewGroup getHeaderLeft(@Nonnull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.header_left);
    }

    @Nonnull
    @Override
    public ViewGroup getHeaderCenter(@Nonnull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.header_center);
    }

    @Nonnull
    @Override
    public ViewGroup getHeaderRight(@Nonnull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.header_right);
    }

    @Nonnull
    @Override
    public ViewGroup getCenter(@Nonnull Activity activity) {
        return (ViewGroup) activity.findViewById(R.id.center);
    }

    @Nonnull
    @Override
    public ViewPager initTitleForViewPager(@Nonnull Activity activity,
                                           @Nonnull ViewPager.OnPageChangeListener listener,
                                           @Nonnull PagerAdapter adapter) {
        final ViewPager pager = (ViewPager) activity.findViewById(R.id.viewpager);
        pager.setAdapter(adapter);

        final TitlePageIndicator titleIndicator = (TitlePageIndicator) activity.findViewById(R.id.viewpager_title);
        titleIndicator.setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.None);
        titleIndicator.setSelectedColor(R.color.text);
        titleIndicator.setTextColor(R.color.text);
        titleIndicator.setFooterColor(R.color.text);
        titleIndicator.setViewPager(pager);
        titleIndicator.setOnPageChangeListener(listener);
        if (adapter.getCount() <= 1) {
            titleIndicator.setVisibility(View.GONE);
        }

        return pager;
    }

    @Override
    public void handleException(@Nonnull Activity activity, @Nonnull Exception e) {
        handleExceptionStatic(activity, e);
    }

    public static void handleExceptionStatic(@Nonnull Context context, @Nonnull Exception e) {
        if (e instanceof HttpRuntimeIoException) {
            if (AThreads.isUiThread()) {
                Toast.makeText(context, "No internet connection available: connect to the network and try again!", Toast.LENGTH_LONG).show();
            }
            Log.d("Msg_NoInternet", e.getMessage(), e);
        } else if (e instanceof IllegalJsonRuntimeException) {
            if (AThreads.isUiThread()) {
                Toast.makeText(context, "The response from server is not valid!", Toast.LENGTH_LONG).show();
            }
            Log.e("Msg_InvalidJson", e.getMessage(), e);
        } else {
            if (AThreads.isUiThread()) {
                Toast.makeText(context, "Something is going wrong!", Toast.LENGTH_LONG).show();
            }
            Log.e("Msg_Exception", e.getMessage(), e);
        }
    }
}
