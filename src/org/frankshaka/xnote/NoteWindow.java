package org.frankshaka.xnote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NoteWindow extends Window {

	private final static int DEFAULT_FONT_SIZE = 16;

	private Button autoSaveCheck;

	private Text text;

	private int fontSize = DEFAULT_FONT_SIZE;

	private Font font;

	private boolean modifyingText = false;

	private UndoRedoManager undoRedoManager = new UndoRedoManager();

	private AutoSaver autoSaver = null;

	private Listener eventListener = new Listener() {

		public void handleEvent(Event event) {
			if (event.widget == text) {
				if (event.type == SWT.Modify) {
					if (!modifyingText)
						textModified();
				} else if (event.type == SWT.Verify) {
					if (!modifyingText)
						appendTextChange(event);
				}
			} else if (event.widget == autoSaveCheck) {
				if (event.type == SWT.Selection) {
					autoSaveChanged(autoSaveCheck.getSelection());
				}
			} else if (event.widget instanceof MenuItem) {
				final Object action = event.widget.getData();
				if (action instanceof Runnable) {
					SafeRunner.run(new ISafeRunnable() {
						public void run() throws Throwable {
							((Runnable) action).run();
						}
					});
				}
			}
		}
	};

	private Note note;

	protected void createContents(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginWidth = 0;
		parentLayout.marginHeight = 0;
		parentLayout.verticalSpacing = 0;
		parentLayout.horizontalSpacing = 0;
		parent.setLayout(parentLayout);

		createToolBar(parent);
		createText(parent);

		createMenuBar(parent.getShell());

		SafeRunner.run("Initialize", new ISafeRunnable() {
			public void run() throws Throwable {
				setNote(openDefaultNote());
			}
		}, true);
	}

	private void createText(Composite parent) {
		text = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Integer FontSize = Pref.getInt(Pref.FONT_SIZE);
		if (FontSize == null) {
			FontSize = Integer.valueOf(DEFAULT_FONT_SIZE);
			Pref.set(Pref.FONT_SIZE, String.valueOf(DEFAULT_FONT_SIZE));
		}
		fontSize = FontSize.intValue();
		setTextFont();
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (font != null) {
					font.dispose();
					font = null;
				}
			}
		});

		text.setFocus();
		text.addListener(SWT.Modify, eventListener);
		text.addListener(SWT.Verify, eventListener);
	}

	private void setTextFont() {
		if (font != null) {
			font.dispose();
		}
		font = new Font(Display.getCurrent(), "Arial", fontSize, SWT.NORMAL);
		text.setFont(font);
	}

	private void createToolBar(final Composite parent) {
		Composite toolbar = new Composite(parent, SWT.NONE);
		GridLayout toolbarLayout = new GridLayout(1, false);
		toolbarLayout.marginWidth = 15;
		toolbarLayout.marginHeight = 5;
		toolbarLayout.verticalSpacing = 0;
		toolbarLayout.horizontalSpacing = 10;
		toolbar.setLayout(toolbarLayout);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		autoSaveCheck = new Button(toolbar, SWT.CHECK);
		autoSaveCheck.setText("Auto save (Command+E)");
		autoSaveCheck.setLayoutData(new GridData(SWT.END, SWT.CENTER, true,
			false));
		autoSaveCheck.addListener(SWT.Selection, eventListener);

		Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	protected Point computeWindowSize() {
		return new Point(600, 800);
	}

	@Override
	protected Point computeWindowPosition(Point size) {
		Rectangle area = Display.getCurrent().getPrimaryMonitor().getClientArea();
		return new Point(area.x + area.width - size.x - 20, area.y + 20);
	}

	public void setNote(Note doc) {
		if (doc == null)
			throw new IllegalArgumentException();

		if (autoSaver != null) {
			autoSaver.deactivate();
		}
		this.note = doc;
		undoRedoManager.reset();
		updateContents();
		updateWindowTitle();
		updateWindowStatus();
		updateAutoSave();
	}

	public Note getNote() {
		return note;
	}

	protected void updateContents() {
		setModifyingText(true);
		text.setText(note.getContent());
		text.setSelection(text.getCharCount());
		setModifyingText(false);
	}

	protected void updateWindowTitle() {
		getShell().setText(note.getTitle());
	}

	protected void updateWindowStatus() {
		getShell().setModified(note.isDirty());
	}

	protected void updateAutoSave() {
		boolean autoSave;
		if (isDefaultNote()) {
			autoSave = Pref.getBool(Pref.AUTO_SAVE);
		} else {
			autoSave = true;
		}
		autoSaveCheck.setSelection(autoSave);
		autoSaveChanged(autoSave);
	}

	private boolean isDefaultNote() {
		return new File(note.getPath()).getName().equals(getDefaultName());
	}

	protected void textModified() {
		updateWindowStatus();
	}

	protected Text getText() {
		return text;
	}

	private void appendTextChange(Event event) {
		if (!note.getContent().equals(text.getText()))
			return;
		String newContent = undoRedoManager.append(note.getContent(),
			event.start, event.end, event.text);
		note.setContent(newContent);
	}

	protected void setModifyingText(boolean modifyingText) {
		this.modifyingText = modifyingText;
	}

	private void autoSaveChanged(boolean autoSave) {
		if (autoSave) {
			if (autoSaver == null)
				autoSaver = new AutoSaver(this);
			autoSaver.activate();
		} else if (autoSaver != null) {
			autoSaver.deactivate();
		}
		if (isDefaultNote()) {
			Pref.set(Pref.AUTO_SAVE, Boolean.toString(autoSave));
		}
	}

	@Override
	protected void handleShellDispose() {
		if (autoSaver != null) {
			autoSaver.deactivate();
			autoSaver = null;
		}
		if (note.isDirty()) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Throwable {
					note.save();
				}
			});
		}
		super.handleShellDispose();
	}

	private Runnable selectAllAction = new Runnable() {
		public void run() {
			text.selectAll();
		}
	};

	private Runnable cutAction = new Runnable() {
		public void run() {
			text.cut();
		}
	};

	private Runnable copyAction = new Runnable() {
		public void run() {
			text.copy();
		}
	};

	private Runnable pasteAction = new Runnable() {
		public void run() {
			text.paste();
		}
	};

	private Runnable undoAction = new Runnable() {
		public void run() {
			TextSelection result = undoRedoManager.undo(text.getText());
			if (result != null) {
				note.setContent(result.text);
				setModifyingText(true);
				text.setText(result.text);
				text.setSelection(result.start, result.end);
				setModifyingText(false);
				updateWindowStatus();
			}
		}
	};

	private Runnable redoAction = new Runnable() {
		public void run() {
			TextSelection result = undoRedoManager.redo(text.getText());
			if (result != null) {
				note.setContent(result.text);
				setModifyingText(true);
				text.setText(result.text);
				text.setSelection(result.start, result.end);
				setModifyingText(false);
				updateWindowStatus();
			}
		}
	};

	private Runnable closeDocumentAction = new Runnable() {
		public void run() {
			closeNote();
		}
	};

	private Runnable newDocumentAction = new Runnable() {
		public void run() {
			createNote();
		}
	};

	private Runnable deleteDocumentAction = new Runnable() {
		public void run() {
			deleteNote();
		}
	};

	private Runnable traverseDocumentAction = new Runnable() {
		public void run() {
			traverse();
		}
	};

	private Runnable openDocumentAction = new Runnable() {
		public void run() {
			System.out.println("open");
		}
	};

	private Runnable renameDocumentAction = new Runnable() {
		public void run() {
			renameNote();
		}
	};

	private Runnable moveDocumentAction = new Runnable() {
		public void run() {
			System.out.println("move");
		}
	};

	private Runnable saveDocumentAction = new Runnable() {
		public void run() {
			save();
		}
	};

	private Runnable saveDocumentAsAction = new Runnable() {
		public void run() {
			System.out.println("save as");
		}
	};

	private Runnable autoSaveAction = new Runnable() {
		public void run() {
			boolean autoSave = !autoSaveCheck.getSelection();
			autoSaveCheck.setSelection(autoSave);
			autoSaveChanged(autoSave);
		}
	};

	private Runnable biggerCharSizeAction = new Runnable() {
		public void run() {
			if (fontSize >= 144)
				return;
			fontSize++;
			Pref.set(Pref.FONT_SIZE, String.valueOf(fontSize));
			setTextFont();
		}
	};

	private Runnable smallerCharSizeAction = new Runnable() {
		public void run() {
			if (fontSize <= 8)
				return;
			fontSize--;
			Pref.set(Pref.FONT_SIZE, String.valueOf(fontSize));
			setTextFont();
		}
	};

	private Runnable resetCharSizeAction = new Runnable() {
		public void run() {
			fontSize = DEFAULT_FONT_SIZE;
			Pref.set(Pref.FONT_SIZE, String.valueOf(fontSize));
			setTextFont();
		}
	};

	private Runnable changeNotesPathAction = new Runnable() {
		public void run() {
			String path;
			do {
				DirectoryDialog dialog = new DirectoryDialog(getShell());
				path = dialog.open();
			} while (path != null && !changeNotesPath(path));
		}
	};

	private Runnable resetNotesPathAction = new Runnable() {
		public void run() {
			changeNotesPath(Pref.getDefaultNotesPath());
		}
	};

	@Override
	protected void registerKeyboardActions() {
		super.registerKeyboardActions();
		// addKeyboardAction('a', SWT.MOD1, selectAllAction);
		// addKeyboardAction('z', SWT.MOD1, undoAction);
		// addKeyboardAction('z', SWT.MOD1 | SWT.MOD2, redoAction);
		// addKeyboardAction('w', SWT.MOD1, closeDocumentAction);
		// addKeyboardAction('n', SWT.MOD1, newDocumentAction);
		// addKeyboardAction(SWT.BS, SWT.MOD1, deleteDocumentAction);
		// addKeyboardAction('t', SWT.MOD1, traverseDocumentAction);
		// addKeyboardAction('o', SWT.MOD1, openDocumentAction);
		// addKeyboardAction('r', SWT.MOD1 | SWT.MOD2, renameDocumentAction);
		// addKeyboardAction('m', SWT.MOD1 | SWT.MOD2, moveDocumentAction);
		// addKeyboardAction('s', SWT.MOD1, saveDocumentAction);
		// addKeyboardAction('s', SWT.MOD1 | SWT.MOD2, saveDocumentAsAction);
		addKeyboardAction('e', SWT.MOD1, autoSaveAction);
		// addKeyboardAction('+', SWT.MOD1, biggerCharSizeAction);
		// addKeyboardAction('=', SWT.MOD1, biggerCharSizeAction);
		// addKeyboardAction('-', SWT.MOD1, smallerCharSizeAction);
		// addKeyboardAction('0', SWT.MOD1, resetCharSizeAction);
	}

	private void createMenuBar(Shell shell) {
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);

		Menu fileMenu = createMenu(menuBar, "File");
		addMenuAction(fileMenu, "New Note", SWT.MOD1 | 'n', newDocumentAction);
		addMenuAction(fileMenu, "Open Note...", SWT.MOD1 | 'o',
			openDocumentAction).setEnabled(false);
		addMenuAction(fileMenu, "Browse Notes...", SWT.MOD1 | 't',
			traverseDocumentAction);
		addMenuAction(fileMenu, "Close Note", SWT.MOD1 | 'w',
			closeDocumentAction);
		new MenuItem(fileMenu, SWT.SEPARATOR);
		addMenuAction(fileMenu, "Save Note", SWT.MOD1 | 's', saveDocumentAction);
		addMenuAction(fileMenu, "Save Note As...", SWT.MOD1 | SWT.MOD2 | 's',
			saveDocumentAsAction).setEnabled(false);
		new MenuItem(fileMenu, SWT.SEPARATOR);
		addMenuAction(fileMenu, "Rename Note", SWT.MOD1 | SWT.MOD2 | 'r',
			renameDocumentAction);
		addMenuAction(fileMenu, "Move Note", SWT.MOD1 | SWT.MOD2 | 'm',
			moveDocumentAction).setEnabled(false);
		new MenuItem(fileMenu, SWT.SEPARATOR);
		addMenuAction(fileMenu, "Delete Note...", SWT.MOD1 | SWT.BS,
			deleteDocumentAction);
		new MenuItem(fileMenu, SWT.SEPARATOR);
		addMenuAction(fileMenu, "Change Notes Path...", 0,
			changeNotesPathAction);
		addMenuAction(fileMenu, "Reset Notes Path", 0, resetNotesPathAction);

		Menu editMenu = createMenu(menuBar, "Edit");
		addMenuAction(editMenu, "Undo", SWT.MOD1 | 'z', undoAction);
		addMenuAction(editMenu, "Redo", SWT.MOD1 | SWT.MOD2 | 'z', redoAction);
		new MenuItem(editMenu, SWT.SEPARATOR);
		addMenuAction(editMenu, "Cut", SWT.MOD1 | 'x', cutAction);
		addMenuAction(editMenu, "Copy", SWT.MOD1 | 'c', copyAction);
		addMenuAction(editMenu, "Paste", SWT.MOD1 | 'v', pasteAction);
		new MenuItem(editMenu, SWT.SEPARATOR);
		addMenuAction(editMenu, "Select All", SWT.MOD1 | 'a', selectAllAction);

		Menu viewMenu = createMenu(menuBar, "View");
		addMenuAction(viewMenu, "Bigger Character Size", SWT.MOD1 | '=',
			biggerCharSizeAction);
		addMenuAction(viewMenu, "Smaller Character Size", SWT.MOD1 | '-',
			smallerCharSizeAction);
		addMenuAction(viewMenu, "Reset Character Size", SWT.MOD1 | '0',
			resetCharSizeAction);
	}

	private Menu createMenu(Menu parent, String text) {
		MenuItem item = new MenuItem(parent, SWT.CASCADE);
		item.setText(text);

		Menu menu = new Menu(item);
		item.setMenu(menu);
		return menu;
	}

	private MenuItem addMenuAction(Menu menu, String text, int accelerator,
		Runnable action) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(text);
		if (accelerator > 0)
			item.setAccelerator(accelerator);
		item.setData(action);
		item.addListener(SWT.Selection, eventListener);
		return item;
	}

	protected void createNote() {
		if (note.isDirty()) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Throwable {
					note.save();
				}
			});
		}
		setNote(new Note(""));
	}

	protected void deleteNote() {
		if (isDefaultNote())
			return;

		MessageBox confirm = new MessageBox(getShell(), SWT.ICON_QUESTION
			| SWT.OK | SWT.CANCEL);
		confirm.setText("XNote - Delete Note");
		confirm.setMessage("Are you sure to delete " + note.getTitle()
			+ "?\nThis operation can NOT be undone!");
		if (confirm.open() == SWT.OK) {
			doDeleteNote();
		}
	}

	private void doDeleteNote() {
		File file = new File(note.getPath());
		if (!file.exists() || file.delete()) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Throwable {
					setNote(openDefaultNote());
				}
			});
		} else {
			MessageBox err = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			err.setText("XNote - Delete Note");
			err.setMessage("Unable to delete file: " + note.getTitle()
				+ "\nPlease check this path: " + note.getPath());
			err.open();
		}
	}

	protected void save() {
		SafeRunner.run("Save", new ISafeRunnable() {
			public void run() throws Throwable {
				note.save();
			}
		});
		updateWindowStatus();
	}

	protected void traverse() {
		TraverseDialog dialog = new TraverseDialog(this);
		dialog.open();
		final File file = dialog.getSelection();
		if (file != null) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Throwable {
					if (note.isDirty())
						note.save();
					setNote(Note.open(file.getAbsolutePath()));
				}
			});
		}
	}

	protected void closeNote() {
		if (isDefaultNote())
			return;

		SafeRunner.run(new ISafeRunnable() {
			public void run() throws Throwable {
				if (note.isDirty())
					note.save();
				setNote(openDefaultNote());
			}
		});
	}

	protected void renameNote() {
		if (isDefaultNote())
			return;

		InputDialog dialog = new InputDialog(this);
		dialog.setInput(note.getTitle());
		dialog.setVerifier(new IStringVerifier() {
			public String verify(String string) {
				if (string.indexOf('/') >= 0)
					return "Invalid characters: '/'";
				if (new File(
					System.getProperty("org.frankshaka.xnote.notesPath"),
					string + ".txt").exists())
					return "Name already exists.";
				return null;
			}
		});
		dialog.open();
		final String newName = dialog.getInput();
		if (newName != null) {
			SafeRunner.run(new ISafeRunnable() {
				public void run() throws Throwable {
					note.rename(newName);
				}
			});
			updateWindowTitle();
			updateWindowStatus();
		}
	}

	private static Note openDefaultNote() throws IOException {
		String path = new File(
			System.getProperty("org.frankshaka.xnote.notesPath"),
			getDefaultName() + ".txt").getAbsolutePath();
		if (new File(path).isFile()) {
			return Note.open(path);
		} else {
			Note doc = new Note("");
			doc.saveAs(path);
			return doc;
		}
	}

	static String getDefaultName() {
		String defaultName = Pref.get(Pref.DEFAULT_NAME);
		if (defaultName == null) {
			defaultName = "Default";
		}
		return defaultName;
	}

	private boolean changeNotesPath(final String newPath) {
		return SafeRunner.run(new ISafeRunnable() {
			public void run() throws Throwable {
				File newFolder = new File(newPath);
				if (newFolder.exists() && !newFolder.isDirectory()) {
					throw new FileNotFoundException("Not a valid directory!");
				}

				File oldFolder = new File(
					System.getProperty("org.frankshaka.xnote.notesPath"));
				moveFolder(newFolder, oldFolder);
				Pref.set(Pref.NOTES_PATH, newPath);
				System.setProperty("org.frankshaka.xnote.notesPath", newPath);
			}

			private void moveFolder(File newFolder, File oldFolder)
				throws IOException {
				if (!newFolder.exists()) {
					newFolder.getParentFile().mkdirs();
					if (oldFolder.renameTo(newFolder))
						return;
				}
				for (File oldFile : oldFolder.listFiles()) {
					File newFile = new File(newFolder, oldFile.getName());
					moveFile(newFile, oldFile);
				}
			}

			private void moveFile(File newFile, File oldFile) {
				if (newFile.exists()) {
					newFile.delete();
				}
				oldFile.renameTo(newFile);
			}

		});
	}

}
