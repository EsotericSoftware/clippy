
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.clippy.util.Util.readFile;
import static com.esotericsoftware.minlog.Log.*;
import static com.esotericsoftware.scar.Scar.*;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import com.esotericsoftware.clippy.util.Imgur.ImageResponse;
import com.esotericsoftware.clippy.util.Imgur.ImgurAPI;
import com.esotericsoftware.clippy.util.Imgur.ImgurUpload;
import com.esotericsoftware.clippy.util.Util;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.scar.Scar;
import com.esotericsoftware.scar.Scar.ProgressMonitor;
import com.esotericsoftware.wildcard.Paths;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

// BOZO - Store upload history.

public abstract class Upload {
	static final Clippy clippy = Clippy.instance;

	final HashMap<File, ProgressBar> progressBars = new HashMap();

	public void upload (final File file, final boolean deleteAfterUpload, final UploadListener callback) {
		final ProgressBar progressBar = newProgressBar(file);
		threadPool.submit(new Runnable() {
			public void run () {
				try {
					callback.prepare();
					String url = upload(file);
					if (TRACE) trace("Upload success: " + url);
					if (progressBar != null) progressBar.done("Done!");
					callback.complete(url);
				} catch (Exception ex) {
					if (ERROR) error("Upload failed.", ex);
					if (progressBar != null) progressBar.failed("Failed!");
					callback.failed();
				} finally {
					if (deleteAfterUpload) file.delete();
					synchronized (progressBars) {
						progressBars.remove(file);
					}
				}
			}
		});
	}

	abstract protected String upload (File file) throws Exception;

	void setProgress (final File file, float progress) {
		ProgressBar progressBar = progressBars.get(file);
		if (progressBar == null) return;
		progressBar.setProgress(progress);
	}

	ProgressBar newProgressBar (final File file) {
		if (!clippy.config.uploadProgressBar) return null;
		if (!EventQueue.isDispatchThread()) {
			try {
				EventQueue.invokeAndWait(new Runnable() {
					public void run () {
						newProgressBar(file);
					}
				});
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			synchronized (progressBars) {
				return progressBars.get(file);
			}
		}
		ProgressBar progressBar = new ProgressBar(file.getName());
		synchronized (progressBars) {
			progressBars.put(file, progressBar);
		}
		return progressBar;
	}

	static public abstract class UploadListener {
		public void prepare () throws Exception {
		}

		public void complete (String url) {
		}

		public void failed () {
		}
	}

	static public class Imgur extends Upload {
		protected String upload (File file) {
			if (TRACE) trace("Uploading to imgur: " + file);
			ImgurUpload upload = new ImgurUpload();
			upload.image = file;
			String clientID = "213cecec326ed89";
			RestAdapter rest = new RestAdapter.Builder().setEndpoint(ImgurAPI.server).build();
			// rest.setLogLevel(RestAdapter.LogLevel.FULL);
			final CountDownLatch latch = new CountDownLatch(1);
			final AtomicReference<String> url = new AtomicReference();
			rest.create(ImgurAPI.class).postImage("Client-ID " + clientID, upload.title, upload.description, upload.albumId, null,
				new TypedFile("image/*", upload.image), new Callback<ImageResponse>() {
					public void success (ImageResponse imageResponse, Response response) {
						if (TRACE) trace("Upload success: " + imageResponse.data.link);
						url.set(imageResponse.data.link);
						latch.countDown();
					}

					public void failure (RetrofitError ex) {
						if (ERROR) error("Error uploading to imgur: ", ex);
						latch.countDown();
					}
				});
			try {
				latch.await();
			} catch (Exception ignored) {
			}
			return url.get();
		}
	}

	static public class Pastebin extends Upload {
		protected String upload (File file) throws Exception {
			if (clippy.config.pastebinDevKey == null) {
				if (WARN) warn("Pastebin is not configured.");
				return null;
			}

			String text = readFile(file.toPath());
			if (TRACE) trace("Uploading to pastebin: " + text);

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
			int total = bytes.length, remaining = total;
			while (remaining > 0) {
				output.write(bytes, total - remaining, Math.min(64, remaining));
				remaining -= 64;
				setProgress(file, 1 - remaining / (float)total);
			}
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

	static public class Sftp extends Upload {
		protected String upload (final File file) throws Exception {
			if (clippy.config.ftpServer == null) {
				if (WARN) warn("SFTP upload is not configured.");
				return null;
			}
			if (TRACE) trace("Uploading to SFTP: " + file);
			sftpUpload(clippy.config.ftpServer, clippy.config.ftpPort, clippy.config.ftpUser, clippy.config.ftpPassword,
				clippy.config.ftpDir, path(file.getAbsolutePath()), new ProgressMonitor() {
					public void progress (float fileProgress, float totalProgress) {
						setProgress(file, totalProgress);
					}
				});
			return clippy.config.ftpUrl + file.getName();
		}
	}

	static public class Ftp extends Upload {
		protected String upload (File file) throws Exception {
			if (clippy.config.ftpServer == null) {
				if (WARN) warn("FTP upload is not configured.");
				return null;
			}
			if (TRACE) trace("Uploading to FTP: " + file);
			ftpUpload(clippy.config.ftpServer, clippy.config.ftpUser, clippy.config.ftpPassword, clippy.config.ftpDir,
				path(file.getAbsolutePath()), true);
			return clippy.config.ftpUrl + file.getName();
		}
	}

	static public void uploadText (String text) {
		if (clippy.textUpload == null) return;
		File file = Util.nextUploadFile(".txt");
		try {
			if (TRACE) trace("Writing text file: " + file);
			Util.writeFile(file.toPath(), text);
			clippy.textUpload.upload(file, true, new UploadListener() {
				public void complete (String url) {
					if (clippy.config.pasteAfterUpload)
						clippy.paste(url);
					else
						clippy.clipboard.setContents(url);
					clippy.store(url);
				}
			});
		} catch (Exception ex) {
			if (Log.ERROR) error("Error writing text file.", ex);
		}
	}

	static public void uploadImage (BufferedImage image) {
		if (clippy.imageUpload == null) return;
		File file = null;
		try {
			file = Util.nextUploadFile(".png");
			if (TRACE) trace("Writing image file: " + file);
			ImageIO.write(image, "png", file);
		} catch (IOException ex) {
			if (ERROR) error("Error image file: " + file, ex);
			if (file != null) file.delete();
			return;
		}
		clippy.imageUpload.upload(file, true, new UploadListener() {
			public void complete (String url) {
				if (clippy.config.pasteAfterUpload)
					clippy.paste(url);
				else
					clippy.clipboard.setContents(url);
				clippy.store(url);
			}
		});
	}

	static public void uploadFiles (String[] files) {
		if (clippy.fileUpload == null) return;
		if (files.length == 1 && !new File(files[0]).isDirectory())
			uploadFile(new File(files[0]), false);
		else
			uploadZip(files);
	}

	static private void uploadZip (final String[] files) {
		String zipName = (files.length == 1 ? new File(files[0]) : new File(files[0]).getParentFile()).getName();
		final File zip = Util.nextUploadFile(zipName + ".zip");
		clippy.fileUpload.upload(zip, true, new UploadListener() {
			public void prepare () throws Exception {
				Paths paths = new Paths();
				for (String path : files) {
					File file = new File(path);
					paths.addFile(path);
					paths.glob(file.getParent(), file.getName() + "/**");
				}
				Scar.zip(paths, zip.getAbsolutePath());
			}

			public void complete (String url) {
				if (clippy.config.pasteAfterUpload)
					clippy.paste(url);
				else
					clippy.clipboard.setContents(url);
				clippy.store(url);
			}
		});
	}

	static private void uploadFile (final File file, final boolean deleteAfterUpload) {
		final String path = Util.nextUploadFile(file.getName()).getAbsolutePath();
		clippy.fileUpload.upload(new File(path), true, new UploadListener() {
			public void prepare () throws Exception {
				copyFile(file.getAbsolutePath(), path);
			}

			public void complete (String url) {
				if (clippy.config.pasteAfterUpload)
					clippy.paste(url);
				else
					clippy.clipboard.setContents(url);
				clippy.store(url);
			}
		});
	}
}
