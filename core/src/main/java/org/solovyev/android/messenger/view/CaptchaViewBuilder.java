/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import javax.annotation.Nonnull;

import org.solovyev.android.captcha.Captcha;
import org.solovyev.android.captcha.ResolvedCaptcha;
import org.solovyev.android.http.DownloadFileAsyncTask;
import org.solovyev.android.http.HttpMethod;
import org.solovyev.android.messenger.api.MessengerAsyncTask;
import org.solovyev.android.messenger.core.R;
import org.solovyev.android.view.DrawableFromIsConverter;
import org.solovyev.android.view.ViewFromLayoutBuilder;
import org.solovyev.common.Builder;
import org.solovyev.common.collections.Collections;

public class CaptchaViewBuilder implements Builder<AlertDialog> {

	@Nonnull
	private Context context;

	@Nonnull
	private Captcha captcha;

	@Nonnull
	private CaptchaEnteredListener captchaEnteredListener;

	public CaptchaViewBuilder(@Nonnull Context context, @Nonnull Captcha captcha, @Nonnull CaptchaEnteredListener captchaEnteredListener) {
		this.context = context;
		this.captcha = captcha;
		this.captchaEnteredListener = captchaEnteredListener;
	}

	@Nonnull
	@Override
	public AlertDialog build() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(R.string.mpp_captcha_title);
		final View view = ViewFromLayoutBuilder.newInstance(R.layout.mpp_captcha).build(context);

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

		// todo serso: fragment dialog!

		// at the end schedule captcha download
		MessengerAsyncTask.executeInParallel(new DownloadFileAsyncTask(context, new DownloadFileAsyncTask.OnPostExecute<List<Object>>() {
			@Override
			public void onPostExecute(@Nonnull final List<Object> result) {
				if (!Collections.isEmpty(result)) {
					captchaImage.setImageDrawable((Drawable) result.get(0));
				}
			}
		}), new DownloadFileAsyncTask.Input(captcha.getCaptchaImage(), HttpMethod.GET, DrawableFromIsConverter.getInstance()));

		return result;
	}

	public static interface CaptchaEnteredListener {
		void onCaptchaEntered(@Nonnull ResolvedCaptcha resolvedCaptcha);
	}
}
