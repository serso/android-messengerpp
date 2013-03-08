package org.solovyev.android.messenger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public class GuiEvent {

    @Nonnull
    private final GuiEventType type;

    @Nullable
    private final Object data;

    public GuiEvent(@Nonnull GuiEventType type, @Nullable Object data) {
        this.type = type;
        this.data = data;
    }

    @Nonnull
    public GuiEventType getType() {
        return type;
    }


    @Nullable
    public Object getData() {
        return data;
    }
}
