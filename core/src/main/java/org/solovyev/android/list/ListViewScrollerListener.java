package org.solovyev.android.list;

/**
 * User: serso
 * Date: 6/4/13
 * Time: 7:56 PM
 */
public interface ListViewScrollerListener {

	void onItemReachedFromTop(int position);

	void onItemReachedFromBottom(int position);

	void onBottomReached();

	void onTopReached();
}
