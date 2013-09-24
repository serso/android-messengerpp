package org.solovyev.android.messenger;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 6/9/12
 * Time: 8:17 PM
 */

/**
 * Result of merge of some data with data in persistence in storage.
 *
 * @param <T>  type of merged objects
 * @param <ID> type of ids of merged objects
 */
public interface MergeDaoResult<T, ID> {

	/**
	 * @return list of identifiers of removed objects
	 */
	@Nonnull
	List<ID> getRemovedObjectIds();

	/**
	 * Method returns list of added object to persistence storage, i.e. list of object which EXISTED before merge BUT were not added to parent object and LINKS to parent object WERE ADDED while merge
	 * <p/>
	 * NOTE: list returned by this method and list returned by {@link MergeDaoResult#getAddedObjects()} are mutually exclusive,
	 * i.e. no elements from one list contains in another and vice versa
	 *
	 * @return list of added objects links, empty if not applicable
	 */
	@Nonnull
	List<T> getAddedObjectLinks();

	/**
	 * Method returns list of added object to persistence storage, i.e. list of object which didn't exist before merge and were added while merge
	 * <p/>
	 * NOTE: list returned by this method and list returned by {@link MergeDaoResult#getAddedObjectLinks()} are mutually exclusive,
	 * i.e. no elements from one list contains in another and vice versa
	 *
	 * @return list of added objects
	 */
	@Nonnull
	List<T> getAddedObjects();

	/**
	 * Method returns list of update object in persistence storage, i.e. list of object which existed before merge and were update while merge
	 *
	 * @return list of updated objects
	 */
	@Nonnull
	List<T> getUpdatedObjects();
}
