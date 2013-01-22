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
