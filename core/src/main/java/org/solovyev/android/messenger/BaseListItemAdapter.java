package org.solovyev.android.messenger;

import android.content.Context;
import android.os.Bundle;
import android.widget.Checkable;
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

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	private static final String POSITION = "position";

	private static final int NOT_SELECTED = -1;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private volatile boolean initialized = false;

	@Nullable
	private ListItem selectedItem = null;

	private int selectedItemPosition = NOT_SELECTED;

	@Nonnull
	private final SelectedItemListener selectedItemListener = new SelectedItemListener();

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

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
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

		final int selectedItemPosition = this.getSelectedItemPosition();
		if (saveSelection && selectedItemPosition != NOT_SELECTED) {
			outState.putInt(POSITION, selectedItemPosition);
		}
	}

	public void restoreState(@Nonnull Bundle savedInstanceState) {
		super.restoreState(savedInstanceState);
	}

	public int restoreSelectedPosition(@Nonnull Bundle savedInstanceState, int defaultPosition) {
		return savedInstanceState.getInt(POSITION, defaultPosition);
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
		if (selectedItem != null) {
			if (!isAlreadySelected()) {
				if (!findAndSelectItem(selectedItem)) {
					if (selectedItemPosition >= 0 && selectedItemPosition < getCount()) {
						selectedItemListener.onItemClick(selectedItemPosition, false);
					} else if (!isEmpty()) {
						selectedItemListener.onItemClick(0, false);
					}
				}
			}
		}
		super.notifyDataSetChanged();
	}

	private boolean isAlreadySelected() {
		boolean alreadySelected = false;
		if(selectedItemPosition >= 0 && selectedItemPosition < getCount()) {
			if(selectedItem == getItem(selectedItemPosition)) {
				alreadySelected = true;
			}
		}
		return alreadySelected;
	}

	private boolean findAndSelectItem(@Nullable ListItem selectedItem) {
		boolean selected = false;

		for (int i = 0; i < getCount(); i++) {
			final LI item = getItem(i);
			if(selectedItem == item) {
				selectedItemPosition = i;
				if(!isSelected(item)) {
					selectItem(item, true);
				}
				selected = true;
			} else if (isSelected(item)) {
				selectItem(item, false);
			}
		}

		return selected;
	}

	public void unselect() {
		selectedItem = null;
		selectedItemPosition = NOT_SELECTED;
		findAndSelectItem(null);
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

	@Nonnull
	public SelectedItemListener getSelectedItemListener() {
		return selectedItemListener;
	}

	public final class SelectedItemListener {

		public void onItemClick(int position) {
			onItemClick(position, true);
		}

		private void onItemClick(int position, boolean notifyChange) {
			final LI selectedItem = getItem(position);
			onItemClick(position, selectedItem, notifyChange);
		}

		private void onItemClick(int position, @Nonnull LI selectedItem, boolean notifyChange) {
			if (BaseListItemAdapter.this.selectedItem != selectedItem) {
				selectItem(selectedItem, true);
				selectItem(BaseListItemAdapter.this.selectedItem, false);

				BaseListItemAdapter.this.selectedItem = selectedItem;
				BaseListItemAdapter.this.selectedItemPosition = position;

				if (notifyChange) {
					notifyDataSetChanged();
				}
			}
		}

		public int onItemClick(@Nonnull ListItem selectedItem) {
			final LI selectedListItem = (LI) selectedItem;
			final int position = getPosition(selectedListItem);
			if (position >= 0) {
				onItemClick(position);
				return position;
			} else {
				return NOT_SELECTED;
			}
		}

	}

	public int getSelectedItemPosition() {
		return selectedItemPosition;
	}

	@Nullable
	public ListItem getSelectedItem() {
		return selectedItem;
	}

	private static void selectItem(@Nullable ListItem item, boolean selected) {
		if (item instanceof Checkable) {
			((Checkable) item).setChecked(selected);
		}
	}

	private static boolean isSelected(@Nullable ListItem item) {
		if (item instanceof Checkable) {
			return ((Checkable) item).isChecked();
		}
		return false;
	}


}
