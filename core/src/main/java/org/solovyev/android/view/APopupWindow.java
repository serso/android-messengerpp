package org.solovyev.android.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import javax.annotation.Nonnull;

public abstract class APopupWindow {

	@Nonnull
	private final PopupWindow window;

	@Nonnull
	private final WindowManager windowManager;

	@Nonnull
	private final Context context;

	@Nonnull
	private View root;

	@Nonnull
	private final ViewBuilder<View> viewBuilder;

	public APopupWindow(@Nonnull Context context, @Nonnull ViewBuilder<View> viewBuilder) {
		this.context = context;
		this.viewBuilder = viewBuilder;
		this.window = new PopupWindow(context);

		// when a touch even happens outside of the window
		// make the window go away
		this.window.setTouchInterceptor(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					APopupWindow.this.window.dismiss();
					return true;
				}
				return false;
			}
		});

		this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}

	@Nonnull
	protected PopupWindow getWindow() {
		return window;
	}

	@Nonnull
	protected WindowManager getWindowManager() {
		return windowManager;
	}

	/**
	 * Anything you want to have happen when created. Probably should create a view and setup the event listeners on
	 * child views.
	 *
	 * @param context
	 */
	@Nonnull
	private View onCreateView(@Nonnull Context context) {
		return viewBuilder.build(context);
	}

	/**
	 * In case there is stuff to do right before displaying.
	 */
	protected void onShow() {
	}

	@Nonnull
	protected View show() {

		this.root = onCreateView(context);
		this.window.setContentView(this.root);

		onShow();

		prepareWindow(this.window);

		// if using PopupWindow#setBackgroundDrawable this is the only values of the width and hight that make it work
		// otherwise you need to set the background of the root viewgroup
		// and set the popupwindow background to an empty BitmapDrawable
		this.window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		this.window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		this.window.setTouchable(true);
		this.window.setFocusable(true);
		this.window.setOutsideTouchable(true);

		this.window.setContentView(this.root);

		return this.root;
	}

	private void prepareWindow(@Nonnull PopupWindow window) {
		window.setBackgroundDrawable(new BitmapDrawable());
	}

	/**
	 * If you want to do anything when {@link APopupWindow#dismiss()} is called
	 *
	 * @param listener
	 */
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		this.window.setOnDismissListener(listener);
	}

	public void dismiss() {
		this.window.dismiss();
	}
}

