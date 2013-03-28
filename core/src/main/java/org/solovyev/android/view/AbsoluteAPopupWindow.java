package org.solovyev.android.view;

import android.content.Context;
import android.view.View;

import javax.annotation.Nonnull;

public class AbsoluteAPopupWindow extends APopupWindow {

    public AbsoluteAPopupWindow(@Nonnull Context context, @Nonnull ViewBuilder<View> viewBuilder) {
        super(context, viewBuilder);
    }

    public void showLikePopDownMenu(@Nonnull View parentView, int gravity, int xOffset, int yOffset) {
        this.show();
        this.getWindow().setAnimationStyle(org.solovyev.android.messenger.core.R.style.pw_pop_down_menu);
        this.getWindow().showAtLocation(parentView, gravity, xOffset, yOffset);
    }
}
