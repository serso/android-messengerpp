/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android.messenger.realms.sms;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;
import org.solovyev.android.FileCache;
import org.solovyev.android.http.ImageLoader;
import org.solovyev.android.http.MemoryCache;
import org.solovyev.android.http.OnImageLoadedListener;
import org.solovyev.common.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsIconLoader implements ImageLoader {

    /*
	**********************************************************************
    *
    *                           CONSTANTS
    *
    **********************************************************************
    */

	private static final String TAG = SmsIconLoader.class.getSimpleName();

	private static final int REQUIRED_SIZE = 70;

    /*
	**********************************************************************
    *
    *                           FIELDS
    *
    **********************************************************************
    */

	@Nonnull
	private final MemoryCache memoryCache = new MemoryCache();

	@Nonnull
	private final FileCache fileCache;

	@Nonnull
	private final Map<OnImageLoadedListener, String> imageViews = Collections.synchronizedMap(new WeakHashMap<OnImageLoadedListener, String>());

	@Nonnull
	private final ExecutorService executorService;

	@Nonnull
	private final Handler handler;

	@Nonnull
	private final Context context;

	public SmsIconLoader(@Nonnull Context context, @Nonnull String cacheFileName, @Nonnull Handler handler) {
		this.context = context;
		this.fileCache = new FileCache(context, cacheFileName);
		this.executorService = Executors.newFixedThreadPool(5);
		this.handler = handler;
	}

	@Override
	public void loadImage(@Nonnull String url, @Nonnull ImageView imageView, @Nullable Integer defaultImageId) {
		loadImage(url, new ImageViewImageLoadedListener(imageView, defaultImageId, handler));
	}

	@Override
	public void loadImage(@Nonnull String url, @Nonnull OnImageLoadedListener imageLoadedListener) {
		imageViews.put(imageLoadedListener, url);

		final Bitmap bitmapFromMemory = memoryCache.get(url);
		if (bitmapFromMemory != null) {
			// bitmap found in memory => set
			imageLoadedListener.onImageLoaded(bitmapFromMemory);
		} else {
			imageLoadedListener.setDefaultImage();

			// add to loading queue
			queuePhoto(url, imageLoadedListener);
		}
	}

	@Override
	public void loadImage(@Nonnull String url) {
		final Bitmap bitmapFromMemory = memoryCache.get(url);
		if (bitmapFromMemory != null) {
			// bitmap found in memory
		} else {
			// add to loading queue
			queuePhoto(url, EmptyImageLoadedListener.getInstance());
		}
	}

	private void queuePhoto(@Nonnull String url, @Nonnull OnImageLoadedListener imageLoadedListener) {
		executorService.submit(new PhotosLoader(new PhotoToLoad(url, imageLoadedListener)));
	}

	@Nullable
	private Bitmap getBitmap(@Nonnull String url) {
		try {
			final Integer contactId = Integer.valueOf(url);
			final Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
			final InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri);
			if (input == null) {
				return null;
			}
			return BitmapFactory.decodeStream(input);
		} catch (NumberFormatException e) {
			Log.e(SmsRealm.TAG, e.getMessage(), e);
		}

		return null;
	}

	@Nonnull
	private String createFilename(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	//Task for the queue
	private static class PhotoToLoad {

		@Nonnull
		public final String url;

		@Nonnull
		public final OnImageLoadedListener imageLoadedListener;

		public PhotoToLoad(@Nonnull String url, @Nonnull OnImageLoadedListener imageLoadedListener) {
			this.url = url;
			this.imageLoadedListener = imageLoadedListener;
		}
	}

	private class PhotosLoader implements Runnable {

		@Nonnull
		private final PhotoToLoad photoToLoad;

		private PhotosLoader(@Nonnull PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (!isNeedToLoad(photoToLoad)) {

				final Bitmap bmp = getBitmap(photoToLoad.url);
				if (bmp != null) {
					memoryCache.put(photoToLoad.url, bmp);
				}

				if (!isNeedToLoad(photoToLoad)) {
					photoToLoad.imageLoadedListener.onImageLoaded(bmp);
				}
			}
		}
	}

	private boolean isNeedToLoad(@Nonnull PhotoToLoad photoToLoad) {
		final String url = imageViews.get(photoToLoad.imageLoadedListener);
		if (url == null || !url.equals(photoToLoad.url)) {
			return true;
		} else {
			return false;
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

    /*
    **********************************************************************
    *
    *                           STATIC CLASSES
    *
    **********************************************************************
    */


	private static final class ImageViewImageLoadedListener implements OnImageLoadedListener {

		@Nonnull
		private final WeakReference<ImageView> imageViewRef;

		@Nullable
		private final Integer defaultImageId;

		@Nonnull
		private final Handler handler;

		private ImageViewImageLoadedListener(@Nonnull ImageView imageView, @Nullable Integer defaultImageId, @Nonnull Handler handler) {
			this.imageViewRef = new WeakReference<ImageView>(imageView);
			this.defaultImageId = defaultImageId;
			this.handler = handler;
		}

		@Override
		public void onImageLoaded(@Nullable final Bitmap image) {
			final ImageView imageView = imageViewRef.get();
			if (imageView != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (image != null) {
							imageView.setImageBitmap(image);
						} else {
							if (defaultImageId != null) {
								imageView.setImageResource(defaultImageId);
							}
						}
					}
				});
			}
		}

		@Override
		public void setDefaultImage() {
			final ImageView imageView = imageViewRef.get();
			if (imageView != null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (defaultImageId != null) {
							imageView.setImageResource(defaultImageId);
						}
					}
				});
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ImageViewImageLoadedListener)) return false;

			ImageViewImageLoadedListener that = (ImageViewImageLoadedListener) o;

			final ImageView thisImageView = this.imageViewRef.get();
			final ImageView thatImageView = that.imageViewRef.get();
			if (!Objects.areEqual(thisImageView, thatImageView)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			final ImageView imageView = imageViewRef.get();
			return imageView == null ? 0 : imageView.hashCode();
		}
	}


	private static final class EmptyImageLoadedListener implements OnImageLoadedListener {

		@Nonnull
		private static final OnImageLoadedListener instance = new EmptyImageLoadedListener();

		private EmptyImageLoadedListener() {
		}

		@Nonnull
		public static OnImageLoadedListener getInstance() {
			return instance;
		}

		@Override
		public void onImageLoaded(@Nullable Bitmap image) {
		}

		@Override
		public void setDefaultImage() {
		}
	}
}
