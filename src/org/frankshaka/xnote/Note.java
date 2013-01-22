package org.frankshaka.xnote;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class Note {

	private String content = null;

	private String path;

	private String title = null;

	private String savedContent = null;

	private boolean dirty = false;

	public Note(String content) {
		this(createNotePath(), content);
	}

	protected Note(String path, String content) {
		if (content == null || path == null)
			throw new IllegalArgumentException();
		this.content = content;
		this.savedContent = content;
		this.path = path;
	}

	public void setContent(String content) {
		if (content == null)
			content = "";
		this.content = content;
		this.dirty = !content.equals(savedContent);
	}

	private void markSaved() {
		this.savedContent = this.content;
		this.dirty = false;
		this.title = null;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public String getContent() {
		return this.content;
	}

	public boolean isTemporary() {
		return !new File(path).exists();
	}

	public String getTitle() {
		if (title == null)
			title = computeTitle();
		return title;
	}

	public String getPath() {
		return path;
	}

	protected String computeTitle() {
		String name = new File(path).getName();
		if (name.endsWith(".txt"))
			name = name.substring(0, name.length() - 4);
		return name;
	}

	public synchronized void save() throws IOException {
		File file = new File(path);
		saveTo(file);
		markSaved();
	}

	private void saveTo(File file) throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			if (!file.getParentFile().isDirectory())
				throw new FileNotFoundException("Can't create parent folder.");
		} else if (file.isDirectory())
			throw new FileNotFoundException();

		FileWriter writer = new FileWriter(file);
		try {
			StringReader reader = new StringReader(new String(
				content.getBytes("utf-8")));
			try {
				char[] buffer = new char[1024];
				int len = reader.read(buffer);
				while (len >= 0) {
					writer.write(buffer, 0, len);
					len = reader.read(buffer);
				}
			} finally {
				reader.close();
			}
		} finally {
			writer.close();
		}
	}

	public void saveAs(String path) throws IOException {
		this.path = path;
		save();
	}

	public void rename(String newName) throws IOException {
		File file = new File(path);
		File newFile = new File(file.getParentFile(), newName + ".txt");
		if (file.exists()) {
			file.renameTo(newFile);
		}
		if (!newFile.exists()) {
			saveTo(newFile);
		}
		this.path = newFile.getAbsolutePath();
		this.title = null;
	}

	public void move(String newPath) throws IOException {
		File file = new File(path);
		File newFile = new File(newPath);
		if (!move(file, newFile)) {
			saveTo(newFile);
			if (file.exists())
				file.delete();
		}
		this.path = newFile.getAbsolutePath();
		this.title = null;
	}

	private boolean move(File file, File newFile) {
		if (file.exists()) {
			if (newFile.exists()) {
				return newFile.delete() && file.renameTo(newFile);
			} else {
				return newFile.getParentFile().mkdirs()
					&& file.renameTo(newFile);
			}
		}
		return false;
	}

	private static String createNotePath() {
		File folder = new File(
			System.getProperty("org.frankshaka.xnote.notesPath"));
		int index = 1;
		String[] names = folder.list();
		if (names != null) {
			for (String name : names) {
				if (name.startsWith("Note ") && name.endsWith(".txt")) {
					String number = name.substring(5, name.length() - 4).trim();
					try {
						index = Math.max(index, Integer.parseInt(number) + 1);
					} catch (NumberFormatException e) {
						// ignore
					}
				}
			}
		}
		return new File(folder, "Note " + index + ".txt").getAbsolutePath();
	}

	public static Note open(String path) throws IOException {
		File file = new File(path);
		if (!file.exists() || !file.isFile())
			throw new FileNotFoundException();

		String content;
		FileReader reader = new FileReader(file);
		try {
			StringWriter writer = new StringWriter();
			try {
				char[] buffer = new char[1024];
				int len = reader.read(buffer);
				while (len >= 0) {
					writer.write(buffer, 0, len);
					len = reader.read(buffer);
				}
				content = new String(writer.toString().getBytes(), "utf-8");
			} finally {
				writer.close();
			}
		} finally {
			reader.close();
		}
		return new Note(path, content);
	}
}
