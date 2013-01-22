package org.frankshaka.xnote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

public class SafeRunner {

	public static boolean run(ISafeRunnable runnable) {
		return run(null, runnable, false);
	}

	public static boolean run(String title, ISafeRunnable runnable) {
		return run(title, runnable, false);
	}

	public static boolean run(final String title, ISafeRunnable runnable,
		boolean async) {
		try {
			runnable.run();
			return true;
		} catch (final Throwable e) {
			e.printStackTrace();
			Runnable showError = new Runnable() {
				public void run() {
					MessageBox err = new MessageBox(
						Display.getDefault().getActiveShell(), SWT.ICON_ERROR
							| SWT.OK);
					err.setText(title == null ? "XNote" : "XNote - " + title);
					String msg = e.getLocalizedMessage();
					if (msg == null)
						msg = e.getMessage();
					if (msg == null)
						msg = e.toString();
					err.setMessage(msg);
					err.open();
				}
			};
			if (async) {
				Display.getDefault().asyncExec(showError);
			} else {
				showError.run();
			}
			return false;
		}
	}

}
