/*
 * Copyright (c) 2013 Frank Shaka
 * 
 * Licensed under GNU Lesser General Public License (LGPL).
 * http://www.gnu.org/licenses/lgpl.html
 */
package org.frankshaka.xnote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class AutoSaver implements Listener {

	private static final int DELAY = 1000;

	private NoteWindow window;

	private long lastModified = 0;

	public AutoSaver(NoteWindow window) {
		this.window = window;
	}

	public void activate() {
		Text text = window.getText();
		if (text != null && !text.isDisposed()) {
			text.addListener(SWT.Modify, this);
		}
		checkAndSave();
	}

	public void deactivate() {
		Text text = window.getText();
		if (text != null && !text.isDisposed()) {
			text.removeListener(SWT.Modify, this);
		}
	}

	@Override
	public void handleEvent(Event event) {
		schedule(event.display);
	}

	private void schedule(final Display display) {
		lastModified = System.currentTimeMillis();
		final long timestamp = lastModified;
		display.timerExec(DELAY, new Runnable() {
			public void run() {
				if (timestamp == lastModified) {
					checkAndSave();
				}
			}
		});
	}

	private void checkAndSave() {
		final Note note = window.getNote();
		if (note.isDirty()) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Throwable {
					note.save();
				}
			});
			window.updateWindowStatus();
		}
	}
}
