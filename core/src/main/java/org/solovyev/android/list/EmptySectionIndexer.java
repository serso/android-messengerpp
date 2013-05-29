package org.solovyev.android.list;

import android.widget.SectionIndexer;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 5/30/13
 * Time: 12:28 AM
 */
public final class EmptySectionIndexer implements SectionIndexer {

	private static final Object[] SECTIONS = new Object[0];

	@Nonnull
	private static final EmptySectionIndexer instance = new EmptySectionIndexer();

	private EmptySectionIndexer() {
	}

	@Nonnull
	public static EmptySectionIndexer getInstance() {
	    return instance;
	}

	@Override
	public Object[] getSections() {
		return SECTIONS;
	}

	@Override
	public int getPositionForSection(int section) {
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}
}
