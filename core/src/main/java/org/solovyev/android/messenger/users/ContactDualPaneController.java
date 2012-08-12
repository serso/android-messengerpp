package org.solovyev.android.messenger.users;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.messenger.AbstractMessengerListFragment;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * User: serso
 * Date: 7/26/12
 * Time: 6:32 PM
 */
public class ContactDualPaneController {

    @NotNull
    private static final ContactDualPaneController instance = new ContactDualPaneController();

    @NotNull
    private final Map<Context, AbstractMessengerListFragment<?>> map = new WeakHashMap<Context, AbstractMessengerListFragment<?>>();

    private ContactDualPaneController() {
    }

    @NotNull
    public static ContactDualPaneController getInstance() {
        return instance;
    }

    public void registerDualPaneFragment(@NotNull AbstractMessengerListFragment<?> fragment) {
        synchronized (map) {
            map.put(fragment.getActivity(), fragment);
        }
    }

    public void unregisterDualPaneFragment(@NotNull AbstractMessengerListFragment<?> fragment) {
        synchronized (map) {
            map.remove(fragment.getActivity());
        }
    }

    @Nullable
    public AbstractMessengerListFragment<?> getDualPaneFragment(@NotNull Context context) {
        synchronized (map) {
            return map.get(context);
        }
    }


}
