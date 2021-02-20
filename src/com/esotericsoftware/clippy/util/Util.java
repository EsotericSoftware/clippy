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

import static com.esotericsoftware.clippy.Win.User32.*;
import static com.esotericsoftware.minlog.Log.*;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

import com.esotericsoftware.clippy.Config.ColorTime;
import com.esotericsoftware.clippy.Config.ColorTimesReference;
import com.esotericsoftware.clippy.Win.POINT;
import com.esotericsoftware.clippy.Win.WTS_PROCESS_INFO;
import com.esotericsoftware.clippy.Win.Wtsapi32;
import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.jsonbeans.JsonException;
import com.esotericsoftware.jsonbeans.JsonSerializer;
import com.esotericsoftware.jsonbeans.JsonValue;
import com.esotericsoftware.jsonbeans.JsonValue.PrettyPrintSettings;
import com.esotericsoftware.jsonbeans.OutputType;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Robot;

/** @author Nathan Sweet */
public class Util {
	static public final Json json = new Json();
	static {
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);

		json.setSerializer(ColorTimesReference.class, new JsonSerializer<ColorTimesReference>() {
			public ColorTimesReference read (Json json, JsonValue value, Class type) {
				if (value.isNull()) return null;
				ColorTimesReference object = new ColorTimesReference();
				if (value.isString())
					object.name = value.asString();
				else if (value.isArray()) {
					object.times = json.readValue(ArrayList.class, ColorTime.class, value);
					Collections.sort(object.times);
				} else
					throw new JsonException("Invalid color timeline reference: " + value);
				return object;
			}

			public void write (Json json, ColorTimesReference object, Class type) {
				// Only written for default gamma config.
				if (object.name != null || object.times == null) throw new IllegalStateException();
				json.writeArrayStart();
				for (int i = 0, n = object.times.size(); i < n; i++)
					json.writeValue(object.times.get(i), ColorTime.class);
				json.writeArrayEnd();
			}
		});
	}

	static public final Random random = new Random();
	static private final String alphabet = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static private Path uploadFile = new File(System.getProperty("user.home"), ".clippy/upload").toPath();
	static private POINT mousePOINT = new POINT();

	static public Timer timer = new Timer("Clippy Timer", true);

	static public Robot robot;
	static {
		try {
			robot = new Robot(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
		} catch (Exception ex) {
			if (ERROR) error("Error creating robot.", ex);
		}
	}

	static public final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread (Runnable runnable) {
			return new Thread(runnable, "Util");
		}
	});

	static public Point getMouse (Point mousePoint) {
		if (GetCursorPos(mousePOINT)) {
			mousePoint.x = mousePOINT.x;
			mousePoint.y = mousePOINT.y;
		} else {
			mousePoint.x = Integer.MIN_VALUE;
			mousePoint.y = Integer.MIN_VALUE;
		}
		return mousePoint;
	}

	static public boolean setMouse (int x, int y) {
		return SetCursorPos(x, y);
	}

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

	static public void delete (File file) {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				delete(child);
		}
		file.delete();
	}

	static public void copy (File src, File dest) throws IOException {
		if (src.isDirectory()) {
			dest.mkdirs();
			for (File child : src.listFiles())
				copy(child, new File(dest, child.getName()));
			return;
		}
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1) break;
				out.write(buffer, 0, count);
			}
		} finally {
			close(in);
			close(out);
		}
	}

	static public void close (Closeable c) {
		try {
			if (c != null) c.close();
		} catch (Throwable ignored) {
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
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	static public void writeFile (Path path, String contents) throws IOException {
		Files.write(path, contents.getBytes(StandardCharsets.UTF_8));
	}

	static public int nextUploadID () {
		int number;
		try {
			synchronized (uploadFile) {
				if (Files.exists(uploadFile))
					number = Integer.parseInt(readFile(uploadFile));
				else
					number = 1;
				writeFile(uploadFile, Integer.toString(number + 1));
			}
		} catch (Exception ex) {
			if (WARN) warn("Upload ID error.", ex);
			number = 0;
		}
		return number;
	}

	static public File nextUploadFile (String name) {
		return nextUploadFile(nextUploadID(), name);
	}

	static public File nextUploadFile (int number, String name) {
		name = name.replaceAll("[ ,]", "-");
		name = name.replaceAll("-+", "-");
		name = name.replaceAll("[:\\\\/*\"?|<>']", "-");
		name = name.replaceAll("-$", "");

		if (number == 0) {
			if (name.charAt(0) == '.') name = id(8) + name;
		} else {
			if (name.charAt(0) == '.')
				name = number + "-" + id(4) + name;
			else
				name = number + "-" + name;
		}

		File file = getTempFile("clippy");
		File idFile = new File(file.getParent(), name);
		file.renameTo(idFile);
		return idFile;
	}

	static public void sleep (long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException ignored) {
		}
	}

	static public int clamp (int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public float clamp (float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public double clamp (double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public void writeJson (Object object, File file) throws IOException {
		PrettyPrintSettings pretty = new PrettyPrintSettings();
		pretty.outputType = OutputType.minimal;
		pretty.singleLineColumns = 130;
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
		writer.write(json.prettyPrint(object, pretty));
		writer.close();
	}

	/** @return May be null. */
	static public String getRunningProcess (String... names) {
		PointerByReference infosOut = new PointerByReference();
		IntByReference countOut = new IntByReference();
		if (!Wtsapi32.WTSEnumerateProcesses(Wtsapi32.WTS_CURRENT_SERVER_HANDLE, null, 1, infosOut, countOut)) return null;

		Pointer infos = infosOut.getValue();
		try {
			int size = WTS_PROCESS_INFO.size, offset = 0;
			for (int i = 0, n = countOut.getValue(); i < n; i++, offset += size) {
				String name = new WTS_PROCESS_INFO(infos.share(offset)).readField("pProcessName").toString();
				for (int ii = 0, nn = names.length; ii < nn; ii++)
					if (name.equalsIgnoreCase(names[ii])) return name;
			}
			return null;
		} finally {
			Wtsapi32.WTSFreeMemory(infos);
		}
	}

	static public void requireEDT () {
		if (!EventQueue.isDispatchThread()) throw new RuntimeException();
	}

	static public void edt (final Runnable runnable) {
		if (EventQueue.isDispatchThread())
			runnable.run();
		else
			EventQueue.invokeLater(runnable);
	}

	static public void edtWait (final Runnable runnable) {
		if (EventQueue.isDispatchThread())
			runnable.run();
		else {
			try {
				EventQueue.invokeAndWait(runnable);
			} catch (Exception ex) {
				throw new RuntimeException();
			}
		}
	}

	static public <T> T edtWait (final RunnableValue<T> runnable) {
		if (EventQueue.isDispatchThread()) return runnable.run();
		try {
			final AtomicReference ref = new AtomicReference();
			EventQueue.invokeAndWait(new Runnable() {
				public void run () {
					ref.set(runnable.run());
				}
			});
			return (T)ref.get();
		} catch (Exception ex) {
			throw new RuntimeException();
		}
	}

	static public void openFile (File file) {
		try {
			Desktop.getDesktop().open(file);
		} catch (Throwable ex) {
			if (WARN) warn("Unable to open file: " + file, ex);
		}
	}

	static public interface RunnableValue<T> {
		public T run ();
	}
}
