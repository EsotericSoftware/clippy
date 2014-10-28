
package com.esotericsoftware.clippy.util;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Util {
	static public File extractFile (String sourcePath, String appName) throws IOException {
		File extractedFile = getExtractPath(appName, new File(sourcePath).getName());
		InputStream input = Util.class.getResourceAsStream("/" + sourcePath);
		if (input == null) throw new IOException("Unable to read file for extraction: " + sourcePath);
		try {
			extractedFile.getParentFile().mkdirs();
			FileOutputStream output = new FileOutputStream(extractedFile);
			byte[] buffer = new byte[4096];
			while (true) {
				int length = input.read(buffer);
				if (length == -1) break;
				output.write(buffer, 0, length);
			}
			input.close();
			output.close();
		} catch (IOException ex) {
			throw new IOException("Error extracting file: " + sourcePath + "\nTo: " + extractedFile.getAbsolutePath(), ex);
		}
		return extractedFile;
	}

	/** Returns a path to a file that can be written. Tries multiple locations and verifies writing succeeds. */
	static private File getExtractPath (String appName, String fileName) {
		// Temp directory with username in path.
		File idealFile = new File(System.getProperty("java.io.tmpdir") + "/" + appName + "/" + System.getProperty("user.name"),
			fileName);
		if (canWrite(idealFile)) return idealFile;

		// System provided temp directory.
		try {
			File file = File.createTempFile(appName, null);
			if (file.delete()) {
				file = new File(file, fileName);
				if (canWrite(file)) return file;
			}
		} catch (IOException ignored) {
		}

		// User home.
		File file = new File(System.getProperty("user.home") + "/." + appName, fileName);
		if (canWrite(file)) return file;

		// Relative directory.
		file = new File(".temp/" + appName, fileName);
		if (canWrite(file)) return file;

		return idealFile; // Will likely fail, but we did our best.
	}

	/** Returns true if the parent directories of the file can be created and the file can be written. */
	static private boolean canWrite (File file) {
		File parent = file.getParentFile();
		File testFile;
		if (file.exists()) {
			if (!file.canWrite()) return false;
			testFile = new File(parent, UUID.randomUUID().toString());
		} else {
			parent.mkdirs();
			if (!parent.isDirectory()) return false;
			testFile = file;
		}
		try {
			new FileOutputStream(testFile).close();
			return true;
		} catch (Throwable ex) {
			return false;
		} finally {
			testFile.delete();
		}
	}
}
