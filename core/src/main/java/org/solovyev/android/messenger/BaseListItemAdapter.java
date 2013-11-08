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
import org.solovyev.android.list.AlphabetIndexer;
import org.solovyev.android.list.EmptySectionIndexer;
import org.solovyev.android.list.ListItem;
import org.solovyev.android.list.ListItemAdapter;
import org.solovyev.android.messenger.users.UserEvent;
import org.solovyev.common.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class BaseListItemAdapter<LI extends ListItem> extends ListItemAdapter<LI> implements SectionIndexer /*implements UserEventListener*/ {

	@Nonnull
	private final ListItemAdapterSelectionHelper<LI> selection = new ListItemAdapterSelectionHelper<LI>(this);

	@Nonnull
	private final SectionIndexer sectionIndexer;

	private final boolean saveSelection;

	@Nullable
	private Runnable onEmptyListListener;

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

	public void setOnEmptyListListener(@Nullable Runnable onEmptyListListener) {
		this.onEmptyListListener = onEmptyListListener;
	}

	/*@Override*/
	public void onEvent(@Nonnull UserEvent event) {
	}

	@Nullable
	protected Comparator<? super LI> getComparator() {
		return ListItemComparator.getInstance();
	}

	public void saveState(@Nonnull Bundle outState) {
		super.saveState(outState);

		if (saveSelection) {
			selection.saveState(outState);
		}
	}

	public int restoreSelectedPosition(@Nonnull Bundle savedInstanceState, int defaultPosition) {
		return selection.restoreSelectedPosition(savedInstanceState, defaultPosition);
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

	@Override
	public void notifyDataSetChanged() {
		if (isEmpty()) {
			if (onEmptyListListener != null) {
				onEmptyListListener.run();
			}
		}
		selection.onNotifyDataSetChanged();
		super.notifyDataSetChanged();
	}

	public void unselect() {
		selection.unselect();
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

	public int getSelectedItemPosition() {
		return selection.getPosition();
	}

	@Nullable
	public LI getSelectedItem() {
		return selection.getListItem();
	}

	@Nonnull
	public ListItemAdapterSelectionHelper<LI> getSelection() {
		return selection;
	}
}
