package org.solovyev.android.messenger;

import android.os.Bundle;
import android.widget.Checkable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.solovyev.android.list.ListItem;

import static org.solovyev.android.messenger.AdapterSelection.newNotSelected;
import static org.solovyev.android.messenger.AdapterSelection.newSelection;

class ListItemAdapterSelectionHelper<LI extends ListItem> {

	@Nonnull
	private final BaseListItemAdapter<LI> adapter;

	@Nonnull
	private AdapterSelection<LI> selection;

	public ListItemAdapterSelectionHelper(@Nonnull BaseListItemAdapter<LI> adapter) {
		this.adapter = adapter;
		this.selection = newNotSelected();
	}

	@Nonnull
	public AdapterSelection<LI> getSelection() {
		return selection;
	}

	public void unselect() {
		selection = newNotSelected();
		findAndSelectItem(null);
	}

	private boolean findAndSelectItem(@Nullable LI toBeSelectedItem) {
		boolean selected = false;

		for (int i = 0; i < adapter.getCount(); i++) {
			final LI item = adapter.getItem(i);
			if(toBeSelectedItem == item) {
				selection = newSelection(i, toBeSelectedItem);
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
		selection.saveState(outState);
	}

	public int restoreSelectedPosition(@Nonnull Bundle savedInstanceState, int defaultPosition) {
		return selection.restoreSelectedPosition(savedInstanceState, defaultPosition);
	}

	void onNotifyDataSetChanged() {
		if (!adapter.isEmpty()) {
			final LI listItem = selection.getItem();
			if (listItem != null) {
				if (!isAlreadySelected()) {
					if (!findAndSelectItem(listItem)) {
						final int position = selection.getPosition();
						if (position >= 0 && position < adapter.getCount()) {
							onItemClick(position, false);
						} else if (!adapter.isEmpty()) {
							onItemClick(0, false);
						}
					}
				}
			}
		}
	}

	private boolean isAlreadySelected() {
		boolean alreadySelected = false;

		final int position = selection.getPosition();
		final LI listItem = selection.getItem();
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
		final LI listItem = selection.getItem();
		if (listItem != newListItem) {
			selectItem(newListItem, true);
			selectItem(listItem, false);

			selection = newSelection(newPosition, newListItem);

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
			return AdapterSelection.NOT_SELECTED;
		}
	}


	private static void selectItem(@Nullable ListItem item, boolean selected) {
		if (item instanceof Checkable) {
			((Checkable) item).setChecked(selected);
		}
	}
}
