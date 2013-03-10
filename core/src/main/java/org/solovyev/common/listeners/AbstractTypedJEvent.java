package org.solovyev.common.listeners;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/10/13
 * Time: 2:22 PM
 */
public abstract class AbstractTypedJEvent<D, T> implements JEvent {

    @Nonnull
    private final D eventObject;

    @Nonnull
    private final T type;

    @Nullable
    private final Object data;

    protected AbstractTypedJEvent(@Nonnull D eventObject, @Nonnull T type, @Nullable Object data) {
        this.eventObject = eventObject;
        this.type = type;
        this.data = data;
    }

    /**
     * @return event object
     */
    @Nonnull
    protected final D getEventObject() {
        return eventObject;
    }

    @Nonnull
    public final T getType() {
        return type;
    }

    @Nullable
    public final Object getData() {
        return data;
    }

    public boolean isOfType(@Nonnull T... types) {
        for (T type : types) {
            if ( this.type.equals(type) ) {
                return true;
            }
        }

        return false;
    }
}
