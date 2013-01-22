package org.frankshaka.xnote;

import org.eclipse.swt.SWT;

public class SheetDialog extends Dialog {

	public SheetDialog(Window parent) {
		super(parent);
		setBlockOnOpen(true);
	}

	@Override
	protected int getShellStyle() {
		return super.getShellStyle() | SWT.SHEET;
	}

}
