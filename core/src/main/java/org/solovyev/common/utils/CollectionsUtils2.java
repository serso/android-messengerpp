package org.solovyev.common.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * User: serso
 * Date: 6/1/12
 * Time: 6:29 PM
 */
public class CollectionsUtils2 {
    @NotNull
    public static <E> List<List<E>> split(@NotNull List<E> list, int chunkSize) {
        final int size = list.size();

        final List<List<E>> result = new ArrayList<List<E>>(size / chunkSize + 1);

        int i = 0;
        int l = 0;
        int r = chunkSize;
        while( r <= size ) {
            result.add(list.subList(l, r));

            // update step
            i++;
            l = i * chunkSize;
            r = (i + 1) * chunkSize;
        }

        // if something left - add not full chunk
        if ( l < size ) {
            result.add(list.subList(l, size));
        }

        return result;
    }
}
