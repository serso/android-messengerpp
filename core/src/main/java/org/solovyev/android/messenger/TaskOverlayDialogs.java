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

package org.solovyev.android.messenger;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: serso
 * Date: 4/3/13
 * Time: 11:01 PM
 */
public final class TaskOverlayDialogs {

	@Nonnull
	private final List<TaskOverlayDialog<?>> taskOverlayDialogs = new ArrayList<TaskOverlayDialog<?>>();

	public void addTaskOverlayDialog(@Nullable TaskOverlayDialog<?> t) {
		if (t != null) {
			taskOverlayDialogs.add(t);
		}
	}

	public void dismissAll() {
		for (TaskOverlayDialog<?> taskOverlayDialog : taskOverlayDialogs) {
			taskOverlayDialog.dismiss();
		}
		taskOverlayDialogs.clear();
	}
}
