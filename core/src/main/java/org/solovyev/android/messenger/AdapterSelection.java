package org.solovyev.android.messenger;

import android.os.Bundle;
import org.solovyev.android.list.ListAdapter;
import org.solovyev.android.list.ListItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AdapterSelection<LI> {

	/*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	private static final String BUNDLE_POSITION = "position";

	@Nonnull
	private static final String BUNDLE_ID = "id";

	static final int NOT_SELECTED = -1;

    /*
    **********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	private final int position;

	@Nullable
	private String id;

	@Nullable
	private LI item;

    /*
	**********************************************************************
    *
    *                           CONSTRUCTORS
    *
    **********************************************************************
    */

	private AdapterSelection(int position, @Nullable String id, @Nullable LI item) {
		this.position = position;
		this.id = id;
		this.item = item;
	}

	public static <LI extends ListItem & Identifiable> AdapterSelection<LI> newNotSelected() {
		return newSelection(NOT_SELECTED, null, null);
	}

	public static <LI extends ListItem> AdapterSelection<LI> newSelection(int position, @Nullable LI item, @Nullable String id) {
		return new AdapterSelection<LI>(position, id, item);
	}

	public static <LI extends ListItem & Identifiable> AdapterSelection<LI> newSelection(int position, @Nonnull LI item) {
		return new AdapterSelection<LI>(position, item.getId(), item);
	}

	@Nonnull
	public static <LI extends ListItem> AdapterSelection<LI> restoreSelection(@Nonnull Bundle savedInstanceState, int defaultPosition) {
		final int position = savedInstanceState.getInt(BUNDLE_POSITION, defaultPosition);
		final String id = savedInstanceState.getString(BUNDLE_ID);
		return AdapterSelection.newSelection(position, null, id);
	}

    /*
	**********************************************************************
    *
    *                           METHODS
    *
    **********************************************************************
    */

	public int getPosition() {
		return position;
	}

	@Nullable
	public String getId() {
		return id;
	}

	@Nullable
	public LI getItem() {
		return item;
	}

	public void saveState(@Nonnull Bundle outState) {
		if (position != NOT_SELECTED) {
			outState.putInt(BUNDLE_POSITION, position);
		}

		if (id != null) {
			outState.putString(BUNDLE_ID, id);
		}
	}

	public <LI extends Identifiable> boolean isAlreadySelected(@Nonnull ListAdapter<LI> adapter) {
		boolean alreadySelected = false;

		if (position >= 0 && position < adapter.getCount()) {
			if (adapter.getItem(position).getId().equals(id)) {
				alreadySelected = true;
			}
		}

		return alreadySelected;
	}

	@Override
	public String toString() {
		return "AdapterSelection{" +
				"position=" + position +
				", id='" + id + '\'' +
				", item=" + item +
				'}';
	}
}
