package org.solovyev.android.messenger;

import org.solovyev.android.messenger.events.AbstractTypedJEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 3/5/13
 * Time: 8:59 PM
 */
public final class GuiEvent extends AbstractTypedJEvent<Integer, GuiEventType> {

    public GuiEvent(@Nonnull GuiEventType type, @Nullable Object data) {
        super(0, type, data);
    }
}
