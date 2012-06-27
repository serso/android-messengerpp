package org.solovyev.android.messenger.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import org.jetbrains.annotations.NotNull;
import org.solovyev.android.*;
import org.solovyev.android.http.DownloadFileAsyncTask;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.messenger.R;
import org.solovyev.android.view.DialogBuilder;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.utils.CollectionsUtils;

import java.util.List;

/**
 * User: serso
 * Date: 5/29/12
 * Time: 9:39 PM
 */
public class CaptchaViewBuilder implements DialogBuilder<AlertDialog> {

    @NotNull
    private Context context;

    @NotNull
    private Captcha captcha;

    @NotNull
    private CaptchaEnteredListener captchaEnteredListener;

    public CaptchaViewBuilder(@NotNull Context context, @NotNull Captcha captcha, @NotNull CaptchaEnteredListener captchaEnteredListener) {
        this.context = context;
        this.captcha = captcha;
        this.captchaEnteredListener = captchaEnteredListener;
    }

    @NotNull
    @Override
    public AlertDialog build() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(R.string.c_captcha);
        final View view = ViewFromLayoutBuilder.newInstance(R.layout.captcha).build(context);

        final ImageView captchaImage = (ImageView) view.findViewById(R.id.captcha_image);
        final EditText captchaCodeInput = (EditText) view.findViewById(R.id.captcha_code);

        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                captchaEnteredListener.onCaptchaEntered(captcha.resolve(captchaCodeInput.getText().toString()));
            }
        });

        final AlertDialog result = builder.create();
        if (context instanceof Activity) {
            ActivityDestroyerController.getInstance().put((Activity) context, new DialogOnActivityDestroyedListener(result));
        }

        // at the end schedule captcha download
        new DownloadFileAsyncTask(context, new DownloadFileAsyncTask.OnPostExecute<List<Object>>() {
            @Override
            public void onPostExecute(@NotNull final List<Object> result) {
                if (!CollectionsUtils.isEmpty(result)) {
                    captchaImage.setImageDrawable((Drawable) result.get(0));
                }
            }
        }).execute(new DownloadFileAsyncTask.Input(captcha.getCaptchaImage(), HttpMethod.GET, DrawableFromIsConverter.getInstance()));

        return result;
    }

    public static interface CaptchaEnteredListener {
        void onCaptchaEntered(@NotNull ResolvedCaptcha resolvedCaptcha);
    }
}
