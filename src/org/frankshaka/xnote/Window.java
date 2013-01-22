package org.frankshaka.xnote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class Window {

	private Window parent;

	private Shell shell;

	private Map<KeyStroke, Runnable> keyboardActions = new HashMap<KeyStroke, Runnable>();

	private boolean block = false;

	public Window(Window parent) {
		this.parent = parent;
	}

	public Window() {
		this.parent = null;
	}

	public Shell getShell() {
		return shell;
	}

	public Window getParentWindow() {
		return parent;
	}

	protected void setBlockOnOpen(boolean block) {
		this.block = block;
	}

	protected boolean getBlockOnOpen() {
		return this.block;
	}

	public void open() {
		if (shell == null || shell.isDisposed()) {
			shell = createShell();
			createContents(shell);
			registerKeyboardActions();
			constrainWindowSize();
			constrainWindowPosition();
		}
		if (shell != null && !shell.isDisposed()) {
			shell.open();
		}

		if (block) {
			Display display = shell.getDisplay();
			while (shell != null && !shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		}
	}

	private void constrainWindowSize() {
		if (shell != null && !shell.isDisposed()) {
			Point size = computeWindowSize();
			if (size == null)
				shell.pack();
			else
				shell.setSize(size);
		}
	}

	private void constrainWindowPosition() {
		if (shell != null && !shell.isDisposed()) {
			Point loc = computeWindowPosition(shell.getSize());
			if (loc != null)
				shell.setLocation(loc);
		}
	}

	protected Point computeWindowPosition(Point size) {
		return null;
	}

	protected Point computeWindowSize() {
		return null;
	}

	protected Shell createShell() {
		Shell shell;
		if (parent != null) {
			shell = new Shell(parent.getShell(), getShellStyle());
		} else {
			shell = new Shell(getShellStyle());
		}
		shell.setData(this);
		shell.setText("XNote");

		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				handleShellClose();
			}
		});
		shell.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event event) {
				handleShellDispose();
			}
		});
		return shell;
	}

	protected void handleShellClose() {
		close();
	}

	protected void handleShellDispose() {
	}

	protected int getShellStyle() {
		return SWT.SHELL_TRIM;
	}

	protected void createContents(Composite parent) {
	}

	public void close() {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
	}

	public boolean isClosed() {
		return shell == null || shell.isDisposed();
	}

	protected void registerKeyboardActions() {
		keyboardActions.clear();
	}

	protected void addKeyboardAction(int keyCode, int stateMask, Runnable action) {
		keyboardActions.put(KeyStroke.valueOf(keyCode, stateMask), action);
	}

	public boolean triggerKeyboardAction(int keyCode, int stateMask) {
		final Runnable action = keyboardActions.get(KeyStroke.valueOf(keyCode,
			stateMask));
		if (action == null)
			return false;

		return SafeRunner.run(new ISafeRunnable() {
			public void run() throws Throwable {
				action.run();
			}
		});
	}

}
