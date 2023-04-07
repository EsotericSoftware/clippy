
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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

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
					if (progressBar != null) progressBar.done("Done!", 1000);
					callback.complete(url);
				} catch (Exception ex) {
					if (ERROR) error("Upload failed.", ex);
					if (progressBar != null) progressBar.failed("Failed!", 20_000);
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
			edtWait(new Runnable() {
				public void run () {
					newProgressBar(file);
				}
			});
			synchronized (progressBars) {
				return progressBars.get(file);
			}
		}
		ProgressBar progressBar = new ProgressBar(file.getName());
		progressBar.setVisible(true);
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
				}, false);
			return clippy.config.ftpUrl + URLEncoder.encode(file.getName(), "UTF-8");
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
			return clippy.config.ftpUrl + URLEncoder.encode(file.getName(), "UTF-8");
		}
	}

	static public void uploadText (String text) {
		if (clippy.textUpload == null) return;
		File file = nextUploadFile(".txt");
		try {
			if (TRACE) trace("Writing text file: " + file);
			writeFile(file.toPath(), text);
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

	static public void uploadImage (BufferedImage image, boolean forcePNG) {
		if (clippy.imageUpload == null) return;
		int number = nextUploadID();
		File filePNG = null;
		try {
			filePNG = nextUploadFile(number, ".png");
			if (TRACE) trace("Writing PNG file: " + filePNG);
			ImageIO.write(image, "png", filePNG);
		} catch (IOException ex) {
			if (ERROR) error("Error writing PNG file: " + filePNG, ex);
			if (filePNG != null) filePNG.delete();
			filePNG = null;
		}

		File fileJPG = null;
		if (!forcePNG) {
			try {
				fileJPG = nextUploadFile(number, ".jpg");
				if (TRACE) trace("Writing JPG file: " + fileJPG);

				JPEGImageWriteParam param = new JPEGImageWriteParam(null);
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				param.setCompressionQuality(1);

				FileImageOutputStream output = new FileImageOutputStream(fileJPG);
				ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				writer.setOutput(output);
				writer.write(null, new IIOImage(image, null, null), param);
				writer.dispose();
				output.close();
			} catch (IOException ex) {
				if (ERROR) error("Error writing JPG file: " + fileJPG, ex);
				if (fileJPG != null) fileJPG.delete();
				fileJPG = null;
			}
		}

		File file;
		if (filePNG == null) {
			if (fileJPG == null) return;
			file = fileJPG;
		} else if (fileJPG == null)
			file = filePNG;
		else {
			long lengthPNG = filePNG.length();
			if (lengthPNG > 350_000 && fileJPG.length() / (double)lengthPNG < 0.66) {
				file = fileJPG;
				filePNG.delete();
			} else {
				file = filePNG;
				fileJPG.delete();
			}
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
		final File zip = nextUploadFile(zipName + ".zip");
		clippy.fileUpload.upload(zip, true, new UploadListener() {
			public void prepare () throws Exception {
				Paths paths = new Paths();
				for (String path : files) {
					File file = new File(path);
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
		final String path = nextUploadFile(file.getName()).getAbsolutePath();
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
