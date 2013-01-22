package org.frankshaka.xnote;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

public class TraverseDialog extends SheetDialog {

	private List list;

	private File[] files;

	private File selection;

	public TraverseDialog(NoteWindow parent) {
		super(parent);
	}

	public File getSelection() {
		return selection;
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.RESIZE;
	}

	@Override
	protected Point computeWindowSize() {
		return new Point(480, 240);
	}

	@Override
	protected void createContents(Composite parent) {
		super.createContents(parent);
		fillContents();
	}

	@Override
	protected void createDialogContents(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 15;
		gridLayout.marginHeight = 15;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		parent.setLayout(gridLayout);

		list = new List(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		list.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				itemSelected(list.getSelectionIndex());
			}
		});
	}

	private void fillContents() {
		File folder = new File(
			System.getProperty("org.frankshaka.xnote.notesPath"));
		files = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isFile() && !name.startsWith(".")
					&& name.endsWith(".txt");
			}
		});
		if (files == null)
			files = new File[0];
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return (int) (f2.lastModified() - f1.lastModified());
			}
		});

		list.setRedraw(false);
		for (File file : files) {
			list.add(String.format("%s  (%2$tF %2$tT)", toTitle(file),
				file.lastModified()));
		}
		list.setRedraw(true);
		list.setFocus();

		selection = null;
		getButton(SWT.OK).setEnabled(false);
	}

	private static String toTitle(File file) {
		String name = file.getName();
		if (name.endsWith(".txt"))
			name = name.substring(0, name.length() - 4);
		return name;
	}

	@Override
	protected void createButtons(Composite buttonBar) {
		createButton(buttonBar, SWT.CANCEL, "Cancel", false);
		createButton(buttonBar, SWT.OK, "Open", true);
	}

	protected void cancelPressed() {
		selection = null;
		super.cancelPressed();
	}

	@Override
	protected void handleShellClose() {
		selection = null;
		super.handleShellClose();
	}

	private void itemSelected(int index) {
		if (index >= 0 && index < files.length) {
			selection = files[index];
		} else {
			selection = null;
		}
		getButton(SWT.OK).setEnabled(selection != null);
	}

}
