package org.solovyev.android.messenger.users;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.R;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 3:52 PM
 */
public class MessengerFriendsActivity extends MessengerFragmentActivity implements ViewPager.OnPageChangeListener {

    private int pagerPosition = 0;

    public MessengerFriendsActivity() {
        super(R.layout.msg_main_view_pager);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerFriendsActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FriendsFragmentPagerAdapter adapter = new FriendsFragmentPagerAdapter(getSupportFragmentManager(), getString(R.string.c_friends), getString(R.string.c_online_friends));

        initTitleForViewPager(this, this, adapter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        this.pagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public static class FriendsFragmentPagerAdapter extends FragmentPagerAdapter {

        @NotNull
        private String friendsTitle;

        @NotNull
        private String onlineFriendsTitle;

        public FriendsFragmentPagerAdapter(@NotNull FragmentManager fm, @NotNull String friendsTitle, @NotNull String onlineFriendsTitle) {
            super(fm);
            this.friendsTitle = friendsTitle;
            this.onlineFriendsTitle = onlineFriendsTitle;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MessengerFriendsFragment();
                case 1:
                    return new MessengerOnlineFriendsFragment();
                default:
                    return null;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return friendsTitle;
                case 1:
                    return onlineFriendsTitle;
                default:
                    return null;
            }
        }
    }


}
