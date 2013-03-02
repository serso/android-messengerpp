package org.solovyev.android.messenger;

import android.content.Context;
import com.actionbarsherlock.view.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.menu.IdentifiableMenuItem;
import org.solovyev.android.messenger.core.R;

import java.lang.ref.WeakReference;

/**
 * User: serso
 * Date: 8/13/12
 * Time: 8:52 PM
 */
public class ToggleFilterInputMenuItem implements IdentifiableMenuItem<MenuItem> {

    @NotNull
    private final WeakReference<AbstractMessengerListFragment<?, ?>> fragmentRef;

    public ToggleFilterInputMenuItem(@NotNull AbstractMessengerListFragment<?, ?> fragment) {
        this.fragmentRef = new WeakReference<AbstractMessengerListFragment<?, ?>>(fragment);
    }

    @NotNull
    @Override
    public Integer getItemId() {
        return R.id.toggle_filter_box;
    }

    @Override
    public void onClick(@NotNull MenuItem data, @NotNull Context context) {
        final AbstractMessengerListFragment<?, ?> fragment =  fragmentRef.get();
        if ( fragment != null ) {
            fragment.toggleFilterBox();
        }
    }
}
