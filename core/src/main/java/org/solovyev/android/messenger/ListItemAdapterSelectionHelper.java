package org.solovyev.android.messenger;

import android.os.Bundle;
import android.widget.Checkable;
import org.solovyev.android.list.ListItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static org.solovyev.android.messenger.AdapterSelection.newNotSelected;
import static org.solovyev.android.messenger.AdapterSelection.newSelection;

public class ListItemAdapterSelectionHelper<LI extends ListItem & Identifiable> {

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

	boolean findAndSelectItem(@Nullable LI toBeSelectedItem) {
		boolean selected = false;

		for (int i = 0; i < adapter.getCount(); i++) {
			final LI item = adapter.getItem(i);
			if (toBeSelectedItem == item) {
				assert toBeSelectedItem != null;
				selection = newSelection(i, toBeSelectedItem);
				if (!isSelected(item)) {
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
		final LI newItem = adapter.getItem(position);
		onItemClick(position, newItem, notifyChange);
	}

	private void onItemClick(int newPosition, @Nonnull LI newItem, boolean notifyChange) {
		final String id = selection.getId();
		final LI item = selection.getItem();
		if (!newItem.getId().equals(id)) {
			selectItem(newItem, true);
			selectItem(item, false);

			selection = newSelection(newPosition, newItem);

			if (notifyChange) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	public int onItemClick(@Nonnull ListItem item) {
		final LI selectedListItem = (LI) item;
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
