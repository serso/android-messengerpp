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
public class MessengerContactsActivity extends MessengerFragmentActivity implements ViewPager.OnPageChangeListener {

    private int pagerPosition = 0;

    public MessengerContactsActivity() {
        super(R.layout.msg_main_view_pager);
    }

    public static void startActivity(@NotNull Activity activity) {
        final Intent result = new Intent();
        result.setClass(activity, MessengerContactsActivity.class);
        activity.startActivity(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContactsFragmentPagerAdapter adapter = new ContactsFragmentPagerAdapter(getSupportFragmentManager(), getString(R.string.c_contacts), getString(R.string.c_online_contacts));

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

    public static class ContactsFragmentPagerAdapter extends FragmentPagerAdapter {

        @NotNull
        private String contactsTitle;

        @NotNull
        private String onlineContactsTitle;

        public ContactsFragmentPagerAdapter(@NotNull FragmentManager fm,
                                            @NotNull String contactsTitle,
                                            @NotNull String onlineContactsTitle) {
            super(fm);
            this.contactsTitle = contactsTitle;
            this.onlineContactsTitle = onlineContactsTitle;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new MessengerContactsFragment();
                case 1:
                    return new MessengerOnlineContactsFragment();
                default:
                    return null;
            }

        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return contactsTitle;
                case 1:
                    return onlineContactsTitle;
                default:
                    return null;
            }
        }
    }


}
