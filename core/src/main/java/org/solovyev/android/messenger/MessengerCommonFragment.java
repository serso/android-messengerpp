package org.solovyev.android.messenger;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.widget.Button;
import org.jetbrains.annotations.NotNull;

/**
 * User: serso
 * Date: 8/17/12
 * Time: 2:10 PM
 */
public interface MessengerCommonFragment {

    @NotNull
    Button createFooterButton(int captionResId, @NotNull Activity activity);

    @NotNull
    ViewGroup getFooter(@NotNull Fragment fragment);
}
