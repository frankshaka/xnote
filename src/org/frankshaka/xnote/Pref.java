/*
 * Copyright (c) 2013 Frank Shaka
 * 
 * Licensed under GNU Lesser General Public License (LGPL).
 * http://www.gnu.org/licenses/lgpl.html
 */
package org.frankshaka.xnote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Pref {

	public final static String FONT_SIZE = "FONT_SIZE";
	
	public final static String AUTO_SAVE = "AUTO_SAVE";
	
	public final static String NOTES_PATH = "NOTES_PATH";
	
	public static final String DEFAULT_NAME = "DEFAULT_NAME";
	
	private static Properties contents;

	private static File prefFile;

	private Pref() {
	}

	public static void initialize() {
		if (contents == null) {
			contents = new Properties();
			prefFile = new File(
				System.getProperty("org.frankshaka.xnote.workspace"),
				"pref.properties");
			loadContents();
		}
	}

	public static String get(String key) {
		return contents.getProperty(key);
	}

	public static Integer getInt(String key) {
		String value = get(key);
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static boolean getBool(String key) {
		return Boolean.parseBoolean(get(key));
	}

	public static void set(String key, String value) {
		contents.setProperty(key, value);
	}

	public static void save() {
		if (!prefFile.exists()) {
			prefFile.getParentFile().mkdirs();
		}
		try {
			FileOutputStream out = new FileOutputStream(prefFile);
			try {
				contents.store(out, "Generated by XNote 1.0");
			} finally {
				out.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadContents() {
		if (prefFile.exists() && prefFile.isFile()) {
			try {
				FileInputStream in = new FileInputStream(prefFile);
				try {
					contents.load(in);
				} finally {
					in.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getDefaultNotesPath() {
		return new File(System.getProperty("org.frankshaka.xnote.workspace"),
			"notes").getAbsolutePath();
	}

}
