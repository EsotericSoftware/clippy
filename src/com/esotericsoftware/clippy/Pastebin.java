
package com.esotericsoftware.clippy;

import static com.esotericsoftware.minlog.Log.*;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Pastebin {
	static final Clippy clippy = Clippy.instance;
	static private final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
		public Thread newThread (Runnable runnable) {
			return new Thread(runnable, "Pastebin");
		}
	});

	static public void save (final String text) {
		if (clippy.config.pastebinDevKey == null) {
			if (WARN) warn("Pastebin not configured.");
			return;
		}

		threadPool.submit(new Runnable() {
			public void run () {
				if (TRACE) warn("Save to pastebin: " + text);
				try {
					clippy.popup.hidePopup();
					clippy.paste(post(text));
					clippy.store(text);
				} catch (IOException ex) {
					if (ERROR) error("Unable to save pastebin.", ex);
				}
			}
		});
	}

	static String post (String text) throws IOException {
		StringBuilder params = new StringBuilder();
		params.append("api_option=paste&api_dev_key=");
		params.append(URLEncoder.encode(clippy.config.pastebinDevKey, "UTF-8"));
		params.append("&api_paste_format=");
		params.append(URLEncoder.encode(clippy.config.pastebinFormat, "UTF-8"));
		params.append("&api_paste_private=");
		params.append(URLEncoder.encode(clippy.config.pastebinPrivate, "UTF-8"));
		params.append("&api_paste_expire_date=");
		params.append(URLEncoder.encode(clippy.config.pastebinExpire, "UTF-8"));
		params.append("&api_paste_code=");
		params.append(URLEncoder.encode(text, "UTF-8"));
		byte[] bytes = params.toString().getBytes();

		URL url = new URL("http://pastebin.com/api/api_post.php");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", "" + Integer.toString(bytes.length));
		conn.setUseCaches(false);

		OutputStream output = conn.getOutputStream();
		output.write(bytes);
		output.close();

		BufferedInputStream input = new BufferedInputStream(conn.getInputStream(), 256);
		ByteArrayOutputStream response = new ByteArrayOutputStream();
		byte[] buffer = new byte[256];
		while (true) {
			int count = input.read(buffer);
			if (count == -1) break;
			response.write(buffer, 0, count);
		}
		input.close();

		conn.disconnect();

		String result = new String(response.toByteArray(), "UTF-8");
		if (!result.startsWith("http://pastebin.com/")) throw new RuntimeException(result);
		if (!clippy.config.pastebinRaw) return result;
		return "http://pastebin.com/raw.php?i=" + result.substring(20);
	}
}
