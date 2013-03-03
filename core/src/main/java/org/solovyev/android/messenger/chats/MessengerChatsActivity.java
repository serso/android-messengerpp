package org.solovyev.android.messenger.chats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MessengerFragmentActivity;
import org.solovyev.android.messenger.core.R;

/**
 * User: serso
 * Date: 5/25/12
 * Time: 9:44 PM
 */
public class MessengerChatsActivity extends MessengerFragmentActivity implements ViewPager.OnPageChangeListener {

    public static void startActivity(@Nonnull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerChatsActivity.class);
        activity.startActivity(result);
    }

    private int pagerPosition = 0;

    @Nullable
    private ViewPager pager;

    public MessengerChatsActivity() {
        super(R.layout.msg_main_view_pager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ChatsFragmentPagerAdapter adapter = new ChatsFragmentPagerAdapter(getSupportFragmentManager(),
                getString(R.string.c_chats));

        pager = initTitleForViewPager(this, this, adapter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static class ChatsFragmentPagerAdapter extends FragmentPagerAdapter {

        @Nonnull
        private String chatsTitle;

        public ChatsFragmentPagerAdapter(@Nonnull FragmentManager fm, @Nonnull String chatsTitle) {
            super(fm);
            this.chatsTitle = chatsTitle;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MessengerChatsFragment();
                default:
                    return null;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return chatsTitle;
                default:
                    return null;
            }
        }
    }
}
