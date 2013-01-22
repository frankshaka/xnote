package org.frankshaka.xnote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class Dialog extends Window {

	private Map<Integer, Button> buttons = new HashMap<Integer, Button>();

	private Listener buttonListener = new Listener() {
		public void handleEvent(Event event) {
			Object code = event.widget.getData();
			if (code instanceof Integer) {
				buttonPressed(((Integer) code).intValue());
			}
		}
	};

	public Dialog(Window parent) {
		super(parent);
	}

	protected Point computeWindowPosition(Point size) {
		Rectangle area = getParentWindow().getShell().getClientArea();
		return new Point(area.x + (area.width - size.x) / 2, area.y
			+ (area.height - size.y) / 2);
	}

	@Override
	protected int getShellStyle() {
		return SWT.DIALOG_TRIM;
	}

	@Override
	protected void createContents(Composite parent) {
		GridLayout parentLayout = new GridLayout(1, false);
		parentLayout.marginWidth = 0;
		parentLayout.marginHeight = 0;
		parentLayout.verticalSpacing = 0;
		parentLayout.horizontalSpacing = 0;
		parent.setLayout(parentLayout);

		Composite contentPane = new Composite(parent, SWT.NONE);
		contentPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createDialogContents(contentPane);

		createButtonBar(parent);
	}

	protected void createDialogContents(Composite parent) {
	}

	protected void createButtonBar(Composite parent) {
		Composite buttonBar = new Composite(parent, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout buttonBarLayout = new GridLayout(1, false);
		buttonBarLayout.marginWidth = 7;
		buttonBarLayout.marginHeight = 0;
		buttonBarLayout.marginBottom = 15;
		buttonBarLayout.verticalSpacing = 0;
		buttonBarLayout.horizontalSpacing = 7;
		buttonBar.setLayout(buttonBarLayout);

		Label blank = new Label(buttonBar, SWT.NONE);
		blank.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createButtons(buttonBar);

		int width = 100;
		for (Button b : buttons.values()) {
			width = Math.max(width, b.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
		}
		for (Button b : buttons.values()) {
			((GridData) b.getLayoutData()).widthHint = width;
		}
	}

	protected void createButtons(Composite buttonBar) {
	}

	protected Button createButton(Composite buttonBar, int code, String text,
		boolean defaultButton) {
		Button button = new Button(buttonBar, SWT.PUSH);
		button.setText(text);
		Integer Code = Integer.valueOf(code);
		button.setData(Code);
		buttons.put(Code, button);
		button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		if (defaultButton)
			buttonBar.getShell().setDefaultButton(button);
		((GridLayout) buttonBar.getLayout()).numColumns++;
		button.addListener(SWT.Selection, buttonListener);
		return button;
	}

	protected void buttonPressed(int code) {
		if (code == SWT.OK) {
			okPressed();
		} else if (code == SWT.CANCEL) {
			cancelPressed();
		}
	}

	protected void okPressed() {
		close();
	}

	protected void cancelPressed() {
		close();
	}

	protected Button getButton(int code) {
		return buttons.get(Integer.valueOf(code));
	}

}
