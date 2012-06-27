package org.solovyev.android.messenger;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
* User: serso
* Date: 6/9/12
* Time: 8:17 PM
*/
public interface MergeDaoResult<T, ID> {

    @NotNull
    List<ID> getRemovedObjectIds();

    @NotNull
    List<T> getAddedObjectLinks();

    @NotNull
    List<T> getAddedObjects();

    @NotNull
    List<T> getUpdatedObjects();
}
