/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	 * Method returns list of added object to persistence storage, i.e. list of object which didn't exist before merge and were added while merge
	 * <p/>
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
