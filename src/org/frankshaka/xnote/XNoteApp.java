package org.frankshaka.xnote;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class XNoteApp {

	private Display display;

	private Window window;

	public void start() {
		initializeWorkspacePath();
		Pref.initialize();
		initializeNotesPath();
		Display.setAppName("XNote");
		display = new Display();
		display.addFilter(SWT.KeyDown, new KeyboardFilter());
		window = new NoteWindow();
		window.open();
	}

	private void initializeWorkspacePath() {
		File workspacePath = new File(System.getProperty("user.home"),
			"Library/Application Support/XNote");
		System.setProperty("org.frankshaka.xnote.workspace",
			workspacePath.getAbsolutePath());
	}

	private void initializeNotesPath() {
		String notesPath = Pref.get(Pref.NOTES_PATH);
		if (notesPath == null) {
			notesPath = Pref.getDefaultNotesPath();
		}
		System.setProperty("org.frankshaka.xnote.notesPath", notesPath);
	}

	public void end() {
		window.close();
		display.dispose();
		Pref.save();
	}

	public boolean isRunning() {
		return !display.isDisposed() && !window.isClosed();
	}

	public void sleep() {
		if (!display.readAndDispatch())
			display.sleep();
	}

}
