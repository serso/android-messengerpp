package org.solovyev.android.messenger;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 7:43 PM
 */
public interface MessengerCommonActivity {

    void onCreate(@Nonnull MessengerFragmentActivity activity, @Nullable Bundle savedInstanceState);

    void onRestart(@Nonnull Activity activity);

    @Nonnull
    ImageButton createFooterImageButton(int imageResId, int contentDescriptionResId, @Nonnull Activity activity);

    @Nonnull
    Button createFooterButton(int captionResId, @Nonnull Activity activity);

    @Nonnull
    ViewGroup getFooterLeft(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getFooterCenter(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getFooterRight(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getHeaderLeft(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getHeaderCenter(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getHeaderRight(@Nonnull Activity activity);

    @Nonnull
    ViewGroup getCenter(@Nonnull Activity activity);

    void handleException(@Nonnull Exception e);

    @Nonnull
    ViewPager initTitleForViewPager(@Nonnull Activity activity,
                               @Nonnull ViewPager.OnPageChangeListener listener,
                               @Nonnull PagerAdapter adapter);

    void onSaveInstanceState(@Nonnull SherlockFragmentActivity activity, @Nonnull Bundle outState);
}
