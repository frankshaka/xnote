/*
 * Copyright (c) 2013 Frank Shaka
 * 
 * Licensed under GNU Lesser General Public License (LGPL).
 * http://www.gnu.org/licenses/lgpl.html
 */
package org.frankshaka.xnote;

public class Main {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XNoteApp app = new XNoteApp();
		app.start();
		while(app.isRunning()) {
			app.sleep();
		}
		app.end();
	}

}
