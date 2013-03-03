package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

import java.util.List;

/**
* User: serso
* Date: 6/9/12
* Time: 8:17 PM
*/
public interface MergeDaoResult<T, ID> {

    @Nonnull
    List<ID> getRemovedObjectIds();

    @Nonnull
    List<T> getAddedObjectLinks();

    @Nonnull
    List<T> getAddedObjects();

    @Nonnull
    List<T> getUpdatedObjects();
}
