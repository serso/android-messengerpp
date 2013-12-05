/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import android.widget.SectionIndexer;
import com.google.common.base.Predicate;
import org.solovyev.android.list.*;
import org.solovyev.android.messenger.accounts.AccountEvent;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.android.messenger.view.BaseMessengerListItem;
import org.solovyev.common.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class BaseListItemAdapter<LI extends ListItem & Identifiable> extends ListItemAdapter<LI> implements SectionIndexer {

	@Nonnull
	private final ListItemAdapterSelectionHelper<LI> selectionHelper = new ListItemAdapterSelectionHelper<LI>(this);

	@Nonnull
	private final SectionIndexer sectionIndexer;

	private final boolean saveSelection;

	public BaseListItemAdapter(@Nonnull Context context, @Nonnull List<? extends LI> listItems) {
		this(context, listItems, true, true);
	}

	public BaseListItemAdapter(@Nonnull Context context, @Nonnull List<? extends LI> listItems, boolean fastScrollEnabled, boolean saveSelection) {
		super(context, listItems);
		if (fastScrollEnabled) {
			sectionIndexer = AlphabetIndexer.createAndAttach(this);
		} else {
			sectionIndexer = EmptySectionIndexer.getInstance();
		}
		this.saveSelection = saveSelection;
	}

	public void onEvent(@Nonnull UserEvent event) {
	}

	public void onEvent(@Nonnull AccountEvent event) {
	}

	@Nullable
	protected Comparator<? super LI> getComparator() {
		return ListItemComparator.getInstance();
	}

	public void saveState(@Nonnull Bundle outState) {
		super.saveState(outState);

		if (saveSelection) {
			selectionHelper.saveState(outState);
		}
	}

	public boolean isSaveSelection() {
		return saveSelection;
	}

	@Override
	public Object[] getSections() {
		return sectionIndexer.getSections();
	}

	@Override
	public int getPositionForSection(int section) {
		return sectionIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		return sectionIndexer.getSectionForPosition(position);
	}

	public void unselect() {
		selectionHelper.unselect();
	}

	public int getPositionById(@Nonnull String id) {
		for (int i = 0; i < getCount(); i++) {
			final LI item = getItem(i);
			if (item.getId().equals(id)) {
				return i;
			}
		}

		return -1;
	}

	public static final class ListItemComparator implements Comparator<ListItem> {

		@Nonnull
		private static final ListItemComparator instance = new ListItemComparator();

		@Nonnull
		public static ListItemComparator getInstance() {
			return instance;
		}

		@Override
		public int compare(ListItem lhs, ListItem rhs) {
			return Objects.compare(lhs.toString(), rhs.toString());
		}
	}

	@Nullable
	public LI getSelectedItem() {
		return selectionHelper.getSelection().getItem();
	}

	@Override
	public long getItemId(int position) {
		return intToPositiveLong(getItem(position).getId().hashCode());
	}

	static long intToPositiveLong(int value) {
		if (value >= 0) {
			return value;
		} else {
			return (long) Integer.MAX_VALUE - value;
		}
	}

	@Nonnull
	public ListItemAdapterSelectionHelper<LI> getSelectionHelper() {
		return selectionHelper;
	}

	protected static <D extends EntityAware & Identifiable> void removeIf(@Nonnull final ListAdapter<? extends BaseMessengerListItem<D>> adapter, @Nonnull final Predicate<D> filter) {
		adapter.doWork(new Runnable() {
			@Override
			public void run() {
				for (int i = adapter.getCount() - 1; i >= 0; i--) {
					final BaseMessengerListItem<D> item = adapter.getItem(i);
					if (filter.apply(item.getData())) {
						adapter.removeAt(i);
					}
				}
			}
		});
	}
}
