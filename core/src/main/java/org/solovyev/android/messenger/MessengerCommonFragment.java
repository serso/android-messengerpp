package org.solovyev.android.messenger;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.Button;
import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 2:10 PM
 */
public interface MessengerCommonFragment {

    @Nonnull
    Button createFooterButton(int captionResId, @Nonnull Activity activity);

    @Nonnull
    ViewGroup getFooter(@Nonnull Fragment fragment);
}
