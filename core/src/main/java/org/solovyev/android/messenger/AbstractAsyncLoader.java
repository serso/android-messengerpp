package org.solovyev.android.messenger;

import android.content.Context;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.messenger.api.MessengerAsyncTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * User: serso
 * Date: 6/2/12
 * Time: 5:22 PM
 */
public abstract class AbstractAsyncLoader<R, LI extends ListItem> extends MessengerAsyncTask<Void, Void, List<R>> {

	@Nonnull
	private ListAdapter<LI> adapter;

	@Nullable
	private Runnable onPostExecute;

	public AbstractAsyncLoader(@Nonnull Context context,
							   @Nonnull ListAdapter<LI> adapter,
							   @Nullable Runnable onPostExecute) {
		super(context);
		this.adapter = adapter;
		this.onPostExecute = onPostExecute;
	}


	@Override
	protected List<R> doWork(@Nonnull List<Void> voids) {
		final Context context = getContext();
		if (context != null) {
			return getElements(context);
		}

		return Collections.emptyList();
	}

	@Nonnull
	protected abstract List<R> getElements(@Nonnull Context context);

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
		}

		if (onPostExecute != null) {
			onPostExecute.run();
		}
	}

	@Nonnull
	protected abstract LI createListItem(@Nonnull R element);
}
