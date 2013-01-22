package org.frankshaka.xnote;

import java.util.ArrayList;
import java.util.List;

public class UndoRedoManager {

	private static class TextModification {

		int start;

		String original;

		String replacement;

		long time;

		public TextModification(int start, String original, String replacement) {
			this.start = start;
			this.original = original;
			this.replacement = replacement;
			this.time = System.currentTimeMillis();
		}

		public boolean append(TextModification that) {
			if (start + replacement.length() == that.start
				&& that.original.length() == 0 && that.time - this.time <= 1000) {
				this.replacement = replacement + that.replacement;
				this.time = that.time;
				return true;
			}
			return false;
		}

	}

	private List<TextModification> modifications = new ArrayList<TextModification>(
		1000);

	private int cursor = -1;

	private boolean ignoreAppend = false;

	public String append(String content, int start, int end, String replacement) {
		return append(content, start, end, replacement, true);
	}

	public String append(String content, int start, int end,
		String replacement, boolean append) {
		// System.out.println("" + start + ", " + end + ", " + replacement);
		if (start == end && replacement.length() == 0)
			return content;

		TextModification modification = new TextModification(start,
			content.substring(start, end), replacement);
		if (modifications.isEmpty() || cursor < 0) {
			modifications.clear();
			modifications.add(modification);
			cursor = 0;
		} else {
			TextModification last = modifications.get(cursor);
			if (ignoreAppend || !append || !last.append(modification)) {
				ignoreAppend = false;
				cursor++;
				while (modifications.size() > cursor) {
					modifications.remove(modifications.size() - 1);
				}
				modifications.add(modification);
			}
		}
		return content.substring(0, start) + replacement
			+ content.substring(end);
	}

	public TextSelection undo(String content) {
		if (modifications.isEmpty() || cursor < 0)
			return null;

		TextModification last = modifications.get(cursor);
		String result = content.substring(0, last.start) + last.original
			+ content.substring(last.start + last.replacement.length());
		cursor--;
		ignoreAppend = true;
		return new TextSelection(result, last.start, last.start
			+ last.original.length());
	}

	public TextSelection redo(String content) {
		if (modifications.isEmpty() || cursor >= modifications.size() - 1)
			return null;

		cursor++;
		TextModification next = modifications.get(cursor);
		String result = content.substring(0, next.start) + next.replacement
			+ content.substring(next.start + next.original.length());
		ignoreAppend = true;
		return new TextSelection(result, next.start, next.start
			+ next.replacement.length());
	}

	public void reset() {
		modifications.clear();
		cursor = -1;
		ignoreAppend = false;
	}

}
