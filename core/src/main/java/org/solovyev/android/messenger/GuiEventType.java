package org.solovyev.android.messenger;

import javax.annotation.Nonnull;

/**
 * User: serso
 * Date: 3/8/13
 * Time: 10:29 PM
 */
public enum GuiEventType {

    show_realm_defs;

    @Nonnull
    private static final GuiEvent showRealmDefsEvent = new GuiEvent(show_realm_defs, null);

    @Nonnull
    public static GuiEvent newShowRealmDefsEvent() {
        return showRealmDefsEvent;
    }
}
