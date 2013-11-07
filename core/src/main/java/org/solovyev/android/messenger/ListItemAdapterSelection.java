package org.solovyev.android.messenger;

import android.os.Bundle;
import android.widget.Checkable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.ListItem;

class ListItemAdapterSelection<LI extends ListItem> {

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

	@Nonnull
	private final BaseListItemAdapter<LI> adapter;

	private int position;

	@Nullable
	private LI listItem;

	public ListItemAdapterSelection(@Nonnull BaseListItemAdapter<LI> adapter, int position, @Nullable LI listItem) {
		this.adapter = adapter;
		this.position = position;
		this.listItem = listItem;
	}

	public ListItemAdapterSelection(@Nonnull BaseListItemAdapter<LI> adapter) {
		this(adapter, NOT_SELECTED, null);
	}

	public int getPosition() {
		return position;
	}

	@Nullable
	public LI getListItem() {
		return listItem;
	}

	@Nonnull
	public BaseListItemAdapter<LI> getAdapter() {
		return adapter;
	}

	public void unselect() {
		position = NOT_SELECTED;
		listItem = null;
		findAndSelectItem(null);
	}

	public void setListItem(@Nullable LI listItem) {
		this.listItem = listItem;
	}

	private boolean findAndSelectItem(@Nullable ListItem selectedItem) {
		boolean selected = false;

		for (int i = 0; i < adapter.getCount(); i++) {
			final LI item = adapter.getItem(i);
			if(selectedItem == item) {
				position = i;
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

	public void saveState(@Nonnull Bundle outState) {
		if (position != NOT_SELECTED) {
			outState.putInt(POSITION, position);
		}
	}

	public int restoreSelectedPosition(@Nonnull Bundle savedInstanceState, int defaultPosition) {
		return savedInstanceState.getInt(POSITION, defaultPosition);
	}

	void onNotifyDataSetChanged() {
		if (listItem != null) {
			if (!isAlreadySelected()) {
				if (!findAndSelectItem(listItem)) {
					if (position >= 0 && position < adapter.getCount()) {
						onItemClick(position, false);
					} else if (!adapter.isEmpty()) {
						onItemClick(0, false);
					}
				}
			}
		}
	}

	private boolean isAlreadySelected() {
		boolean alreadySelected = false;
		if(position >= 0 && position < adapter.getCount()) {
			if(listItem == adapter.getItem(position)) {
				alreadySelected = true;
			}
		}
		return alreadySelected;
	}


	private static boolean isSelected(@Nullable ListItem item) {
		if (item instanceof Checkable) {
			return ((Checkable) item).isChecked();
		}
		return false;
	}

	public void onItemClick(int position) {
		onItemClick(position, true);
	}

	private void onItemClick(int position, boolean notifyChange) {
		final LI selectedItem = adapter.getItem(position);
		onItemClick(position, selectedItem, notifyChange);
	}

	private void onItemClick(int newPosition, @Nonnull LI newListItem, boolean notifyChange) {
		if (listItem != newListItem) {
			selectItem(newListItem, true);
			selectItem(listItem, false);

			listItem = newListItem;
			position = newPosition;

			if (notifyChange) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	public int onItemClick(@Nonnull ListItem selectedItem) {
		final LI selectedListItem = (LI) selectedItem;
		final int position = adapter.getPosition(selectedListItem);
		if (position >= 0) {
			onItemClick(position);
			return position;
		} else {
			return NOT_SELECTED;
		}
	}


	private static void selectItem(@Nullable ListItem item, boolean selected) {
		if (item instanceof Checkable) {
			((Checkable) item).setChecked(selected);
		}
	}
}
