package org.frankshaka.xnote;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends SheetDialog {

	private String input = "";

	private Text text;

	private Label messageBar;

	private IStringVerifier verifier;

	public InputDialog(Window parent) {
		super(parent);
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getInput() {
		return input;
	}

	public void setVerifier(IStringVerifier verifier) {
		this.verifier = verifier;
	}

	@Override
	protected void createDialogContents(Composite parent) {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 15;
		gridLayout.marginHeight = 15;
		gridLayout.verticalSpacing = 0;
		gridLayout.horizontalSpacing = 0;
		parent.setLayout(gridLayout);

		text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (input != null) {
			text.setText(input);
		}
		text.selectAll();
		text.setFocus();

		text.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				input = verify(text.getText());
			}
		});

		messageBar = new Label(parent, SWT.NONE);
		messageBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Display display = Display.getCurrent();
		final Color color = new Color(display, 250, 0, 0);
		messageBar.setForeground(color);
		final Font font = new Font(display, newFont(
			display.getSystemFont().getFontData(), -2));
		messageBar.setFont(font);
		messageBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				color.dispose();
				font.dispose();
			}
		});
	}

	private static FontData[] newFont(FontData[] fds, int deltaHeight) {
		for (FontData fd : fds) {
			fd.setHeight(fd.getHeight() + deltaHeight);
		}
		return fds;
	}

	private String verify(String string) {
		if (verifier != null) {
			String message = verifier.verify(string);
			if (message != null) {
				messageBar.setText(message);
				getButton(SWT.OK).setEnabled(false);
				return null;
			}
		}
		messageBar.setText("");
		boolean enabled = !"".equals(string);
		getButton(SWT.OK).setEnabled(enabled);
		return enabled ? string : null;
	}

	@Override
	protected void createButtons(Composite buttonBar) {
		createButton(buttonBar, SWT.CANCEL, "Cancel", false);
		createButton(buttonBar, SWT.OK, "OK", true);
	}

	@Override
	protected void cancelPressed() {
		input = null;
		super.cancelPressed();
	}

	@Override
	protected void handleShellClose() {
		input = null;
		super.handleShellClose();
	}

}
