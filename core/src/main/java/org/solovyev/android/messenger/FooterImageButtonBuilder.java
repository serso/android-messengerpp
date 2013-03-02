package org.solovyev.android.messenger;

import android.content.Context;
import android.widget.ImageButton;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.ViewBuilder;
import org.solovyev.android.view.ViewFromLayoutBuilder;

/**
 * User: serso
 * Date: 4/28/12
 * Time: 7:50 PM
 */
public class FooterImageButtonBuilder implements ViewBuilder<ImageButton> {

    private int imageResId;

    private int contentDescriptionResId;

    private FooterImageButtonBuilder() {
    }

    @NotNull
    public static ViewBuilder<ImageButton> newInstance(int imageResId, int contentDescriptionResId) {
        final FooterImageButtonBuilder result = new FooterImageButtonBuilder();

        result.imageResId = imageResId;
        result.contentDescriptionResId = contentDescriptionResId;

        return result;
    }

    @NotNull
    @Override
    public ImageButton build(@NotNull Context context) {
        final ImageButton result = ViewFromLayoutBuilder.<ImageButton>newInstance(R.layout.footer_image_button).build(context);

        result.setImageDrawable(context.getResources().getDrawable(imageResId));
        result.setContentDescription(context.getString(contentDescriptionResId));

        return result;
    }
}

