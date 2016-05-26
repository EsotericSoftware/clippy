
package com.esotericsoftware.clippy;

import static com.esotericsoftware.clippy.util.Util.*;
import static com.esotericsoftware.minlog.Log.*;
import static com.esotericsoftware.scar.Scar.*;

import java.io.File;
import java.io.IOException;

import com.esotericsoftware.clippy.imgur.ImageResponse;
import com.esotericsoftware.clippy.imgur.ImgurAPI;
import com.esotericsoftware.clippy.imgur.ImgurUpload;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public interface Upload {
	static final Clippy clippy = Clippy.instance;

	public void uploadFile (File file, UploadListener callback);

	public interface UploadListener {
		public void complete (String url);

		public void failed ();
	}

	static public class Imgur implements Upload {
		public void uploadFile (File file, final UploadListener callback) {
			if (TRACE) trace("Uploading to imgur: " + file);
			final ImgurUpload upload = new ImgurUpload();
			upload.image = file;
			String clientID = "213cecec326ed89";
			RestAdapter rest = new RestAdapter.Builder().setEndpoint(ImgurAPI.server).build();
			// rest.setLogLevel(RestAdapter.LogLevel.FULL);
			rest.create(ImgurAPI.class).postImage("Client-ID " + clientID, upload.title, upload.description, upload.albumId, null,
				new TypedFile("image/*", upload.image), new Callback<ImageResponse>() {
					public void success (ImageResponse imageResponse, Response response) {
						if (TRACE) trace("Upload success: " + imageResponse.data.link);
						callback.complete(imageResponse.data.link);
					}

					public void failure (RetrofitError ex) {
						if (ERROR) error("Error uploading to imgur: ", ex);
						callback.failed();
					}
				});
		}
	}

	static public class Sftp implements Upload {
		public void uploadFile (final File file, final UploadListener callback) {
			if (clippy.config.ftpServer == null) {
				if (WARN) warn("SFTP upload is not configured.");
				return;
			}
			threadPool.submit(new Runnable() {
				public void run () {
					if (TRACE) trace("Uploading to SFTP: " + file);
					try {
						sftpUpload(clippy.config.ftpServer, clippy.config.ftpPort, clippy.config.ftpUser, clippy.config.ftpPassword,
							clippy.config.ftpDir, path(file.getAbsolutePath()));
						String url = clippy.config.ftpUrl + file.getName();
						if (TRACE) trace("Upload success: " + url);
						callback.complete(url);
					} catch (IOException ex) {
						if (ERROR) error("Error uploading to SFTP: ", ex);
						callback.failed();
					}
				}
			});
		}
	}

	static public class Ftp implements Upload {
		public void uploadFile (final File file, final UploadListener callback) {
			if (clippy.config.ftpServer == null) {
				if (WARN) warn("FTP upload is not configured.");
				return;
			}
			threadPool.submit(new Runnable() {
				public void run () {
					if (TRACE) trace("Uploading to FTP: " + file);
					try {
						ftpUpload(clippy.config.ftpServer, clippy.config.ftpUser, clippy.config.ftpPassword, clippy.config.ftpDir,
							path(file.getAbsolutePath()), true);
						String url = clippy.config.ftpUrl + file.getName();
						if (TRACE) trace("Upload success: " + url);
						callback.complete(url);
					} catch (IOException ex) {
						if (ERROR) error("Error uploading to FTP: ", ex);
						callback.failed();
					}
				}
			});
		}
	}
}
