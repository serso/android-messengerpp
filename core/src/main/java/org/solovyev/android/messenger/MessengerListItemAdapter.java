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

/**
 * User: serso
 * Date: 6/7/12
 * Time: 5:58 PM
 */
public class MessengerListItemAdapter<LI extends ListItem> extends ListItemAdapter<LI> implements SectionIndexer /*implements UserEventListener*/ {

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

	public MessengerListItemAdapter(@Nonnull Context context, @Nonnull List<? extends LI> listItems) {
		this(context, listItems, true);
	}

	public MessengerListItemAdapter(@Nonnull Context context, @Nonnull List<? extends LI> listItems, boolean fastScrollEnabled) {
		super(context, listItems);
		if (fastScrollEnabled) {
			sectionIndexer = AlphabetIndexer.createAndAttach(this);
		} else {
			sectionIndexer = EmptySectionIndexer.getInstance();
		}
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

	@Deprecated
	protected void addListItem(@Nonnull LI listItem) {
		this.add(listItem);
	}

	@Deprecated
	protected void removeListItem(@Nonnull LI listItem) {
		this.remove(listItem);
	}

	@Deprecated
	protected void addListItems(@Nonnull List<LI> listItems) {
		this.addAll(listItems);
	}

	@Nullable
	protected Comparator<? super LI> getComparator() {
		return ListItemComparator.getInstance();
	}

	public void saveState(@Nonnull Bundle outState) {
		super.saveState(outState);

		final int selectedItemPosition = this.getSelectedItemPosition();
		if (selectedItemPosition != NOT_SELECTED) {
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
				findAndSelectItem(selectedItem);
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

	private void findAndSelectItem(@Nonnull ListItem selectedItem) {
		for (int i = 0; i < getCount(); i++) {
			final LI item = getItem(i);
			if(selectedItem == item) {
				selectedItemPosition = i;
				if(!isSelected(item)) {
					selectItem(item, true);
				}
			} else if (isSelected(item)) {
				selectItem(item, false);
			}
		}
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
			final LI selectedItem = getItem(position);
			onItemClick(position, selectedItem);
		}

		private void onItemClick(int position, @Nonnull LI selectedItem) {
			if (MessengerListItemAdapter.this.selectedItem != selectedItem) {
				selectItem(selectedItem, true);
				selectItem(MessengerListItemAdapter.this.selectedItem, false);

				MessengerListItemAdapter.this.selectedItem = selectedItem;
				MessengerListItemAdapter.this.selectedItemPosition = position;

				notifyDataSetChanged();
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
