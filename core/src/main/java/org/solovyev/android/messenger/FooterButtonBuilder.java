package org.solovyev.android.messenger;

import android.content.Context;
import android.widget.Button;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewBuilder;
import org.solovyev.android.view.ViewFromLayoutBuilder;

/**
 * User: serso
 * Date: 4/28/12
 * Time: 7:57 PM
 */
public class FooterButtonBuilder implements ViewBuilder<Button> {

    private int captionResId;

    private FooterButtonBuilder() {
    }

    @NotNull
    public static ViewBuilder<Button> newInstance(int captionResId) {
        final FooterButtonBuilder result = new FooterButtonBuilder();

        result.captionResId = captionResId;

        return result;
    }

    @NotNull
    @Override
    public Button build(@NotNull Context context) {
        final Button result = ViewFromLayoutBuilder.<Button>newInstance(R.layout.footer_button).build(context);

        result.setText(context.getText(captionResId));

        return result;
    }
}
