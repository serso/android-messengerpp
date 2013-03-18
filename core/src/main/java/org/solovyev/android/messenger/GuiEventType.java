package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 10:29 PM
 */
public enum GuiEventType {
    show_realm_defs,
    app_exit;

    @Nonnull
    private GuiEvent guiEvent;

    private GuiEventType() {
        this.guiEvent = new GuiEvent(this, null);
    }

    @Nonnull
    public GuiEvent newEvent() {
        return guiEvent;
    }
}
