package org.solovyev.android.messenger;

import android.content.Context;
import android.view.View;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemArrayAdapter;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.users.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:22 PM
 */
public abstract class AbstractAsyncLoader<R> extends MessengerAsyncTask<Void, Void, List<R>> {

    @NotNull
    private User user;

    @NotNull
    private ListItemArrayAdapter adapter;

    @Nullable
    private Runnable onPostExecute;

    public AbstractAsyncLoader(@NotNull User user,
                               @NotNull Context context,
                               @NotNull ListItemArrayAdapter adapter,
                               @Nullable Runnable onPostExecute) {
        super(context);
        this.user = user;
        this.adapter = adapter;
        this.onPostExecute = onPostExecute;
    }


    @Override
    protected List<R> doWork(@NotNull List<Void> voids) {
        final Context context = getContext();
        if (context != null) {
            return getElements(context);
        }

        return Collections.emptyList();
    }

    @NotNull
    protected abstract List<R> getElements(@NotNull Context context);

    @NotNull
    protected User getUser() {
        return user;
    }

    @Override
    protected void onSuccessPostExecute(@Nullable final List<R> elements) {

        if (elements != null) {
            adapter.doWork(new Runnable() {
                @Override
                public void run() {
                    for (R element : elements) {
                        adapter.add(createListItem(element));
                    }
                }
            });

            final Comparator<? super ListItem<? extends View>> comparator = getComparator();
            if (comparator != null) {
                adapter.sort(comparator);
            }
        }

        if (onPostExecute != null) {
            onPostExecute.run();
        }
    }

    @Nullable
    protected abstract Comparator<? super ListItem<? extends View>> getComparator();

    @NotNull
    protected abstract ListItem<?> createListItem(@NotNull R element);
}
