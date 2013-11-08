package org.solovyev.android.messenger;

import android.os.Bundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AdapterSelection<I> {

	/*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	@Nonnull
	private static final String POSITION = "position";

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
	private final I item;

	private AdapterSelection(int position, @Nullable I item) {
		this.position = position;
		this.item = item;
	}

	public static <I> AdapterSelection<I> newNotSelected() {
		return newSelection(NOT_SELECTED, null);
	}

	public static <I> AdapterSelection<I> newSelection(int position, @Nullable I item) {
		return new AdapterSelection<I>(position, item);
	}

	public int getPosition() {
		return position;
	}

	@Nullable
	public I getItem() {
		return item;
	}

	public void saveState(@Nonnull Bundle outState) {
		if (position != NOT_SELECTED) {
			outState.putInt(POSITION, position);
		}
	}

	public int restoreSelectedPosition(@Nonnull Bundle savedInstanceState, int defaultPosition) {
		return savedInstanceState.getInt(POSITION, defaultPosition);
	}
}
