/*
 * Copyright (c) 2013 Frank Shaka
 * 
 * Licensed under GNU Lesser General Public License (LGPL).
 * http://www.gnu.org/licenses/lgpl.html
 */
package org.frankshaka.xnote;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class KeyboardFilter implements Listener {

	public void handleEvent(Event event) {
		Shell shell = event.display.getActiveShell();
		if (shell != null) {
			Object window = shell.getData();
			if (window instanceof Window) {
				boolean dispatched = ((Window) window).triggerKeyboardAction(
					event.keyCode, event.stateMask);
				if (dispatched)
					event.doit = false;
			}
		}
	}

}
