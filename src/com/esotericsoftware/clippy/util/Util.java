/* Copyright (c) 2014, Esoteric Software
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.esotericsoftware.clippy.util;

import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/** @author Nathan Sweet */
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
