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

import static com.esotericsoftware.minlog.Log.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/** @author Nathan Sweet */
public class Util {
	static public final Random random = new Random();
	static private final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static private Charset ascii = Charset.forName("ASCII");
	static private Path uploadFile = new File(System.getProperty("user.home"), ".clippy/upload").toPath();

	static public final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread (Runnable runnable) {
			return new Thread(runnable, "Util");
		}
	});

	static public File extractFile (String sourcePath) throws IOException {
		File extractedFile = getTempFile(new File(sourcePath).getName());
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
	static private File getTempFile (String fileName) {
		// Temp directory with username in path.
		File idealFile = new File(System.getProperty("java.io.tmpdir") + "/clippy/" + System.getProperty("user.name"), fileName);
		if (canWrite(idealFile)) return idealFile;

		// System provided temp directory.
		try {
			File file = File.createTempFile("clippy", null);
			if (file.delete()) {
				file = new File(file, fileName);
				if (canWrite(file)) return file;
			}
		} catch (IOException ignored) {
		}

		// User home.
		File file = new File(System.getProperty("user.home") + "/.clippy/temp", fileName);
		if (canWrite(file)) return file;

		// Relative directory.
		file = new File(".temp/clippy", fileName);
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

	static public String id (int length) {
		int alphabetLength = alphabet.length();
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < length; i++)
			buffer.append(alphabet.charAt(random.nextInt(alphabetLength)));
		return buffer.toString();
	}

	static public String readFile (Path path) throws IOException {
		return new String(Files.readAllBytes(path), ascii);
	}

	static public void writeFile (Path path, String contents) throws IOException {
		Files.write(path, contents.getBytes(ascii));
	}

	static public File nextUploadFile (String name) {
		int number;
		try {
			synchronized (uploadFile) {
				if (Files.exists(uploadFile))
					number = Integer.parseInt(Util.readFile(uploadFile));
				else
					number = 1;
				Util.writeFile(uploadFile, Integer.toString(number + 1));
			}
		} catch (Exception ex) {
			if (WARN) warn("Upload ID error.", ex);
			number = 0;
		}

		if (number == 0) {
			if (name.charAt(0) == '.') name = Util.id(8) + name;
		} else {
			if (name.charAt(0) == '.')
				name = number + "-" + Util.id(4) + name;
			else
				name = number + "-" + name;
		}

		File file = getTempFile("clippy");
		File idFile = new File(file.getParent(), name);
		file.renameTo(idFile);
		return idFile;
	}

	static public void sleep (int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}
}
