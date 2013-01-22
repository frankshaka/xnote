package org.frankshaka.xnote;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;

public class KeyStroke {

	private static Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();

	static {
		mapping.put(SWT.KEYPAD_0, (int) '0');
		mapping.put(SWT.KEYPAD_1, (int) '1');
		mapping.put(SWT.KEYPAD_2, (int) '2');
		mapping.put(SWT.KEYPAD_3, (int) '3');
		mapping.put(SWT.KEYPAD_4, (int) '4');
		mapping.put(SWT.KEYPAD_5, (int) '5');
		mapping.put(SWT.KEYPAD_6, (int) '6');
		mapping.put(SWT.KEYPAD_7, (int) '7');
		mapping.put(SWT.KEYPAD_8, (int) '8');
		mapping.put(SWT.KEYPAD_9, (int) '9');
		mapping.put(SWT.KEYPAD_ADD, (int) '+');
		mapping.put(SWT.KEYPAD_CR, (int) SWT.CR);
		mapping.put(SWT.KEYPAD_DECIMAL, (int) '.');
		mapping.put(SWT.KEYPAD_DIVIDE, (int) '/');
		mapping.put(SWT.KEYPAD_EQUAL, (int) '=');
		mapping.put(SWT.KEYPAD_MULTIPLY, (int) '*');
		mapping.put(SWT.KEYPAD_SUBTRACT, (int) '-');
	}

	private int keyCode;

	private int stateMask;

	public KeyStroke(int keyCode, int stateMask) {
		this.keyCode = keyCode;
		this.stateMask = stateMask;
	}

	@Override
	public int hashCode() {
		return keyCode ^ stateMask;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof KeyStroke))
			return false;
		KeyStroke that = (KeyStroke) obj;
		return this.keyCode == that.keyCode && this.stateMask == that.stateMask;
	}

	public static KeyStroke valueOf(int keyCode, int stateMask) {
		if (keyCode >= 'a' && keyCode <= 'z') {
			keyCode = keyCode - 32;
		} else {
			Integer c = mapping.get(keyCode);
			if (c != null) {
				keyCode = c.intValue();
			}
		}
		return new KeyStroke(keyCode, stateMask);
	}

}
