
package com.esotericsoftware.clippy.util;

import java.io.File;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

public class Imgur {
	static public void upload (String clientID, ImgurUpload upload, final Callback<ImageResponse> callback) {
		RestAdapter rest = new RestAdapter.Builder().setEndpoint(ImgurAPI.server).build();
		// rest.setLogLevel(RestAdapter.LogLevel.FULL);
		rest.create(ImgurAPI.class).postImage("Client-ID " + clientID, upload.title, upload.description, upload.albumId, null,
			new TypedFile("image/*", upload.image), new Callback<ImageResponse>() {
				public void success (ImageResponse imageResponse, Response response) {
					callback.success(imageResponse, response);
				}

				public void failure (RetrofitError error) {
					callback.failure(error);
				}
			});
	}

	/** Created by AKiniyalocts on 2/23/15. This is our imgur API. It generates a rest API via Retrofit from Square inc. more here:
	 * http://square.github.io/retrofit/ */
	public interface ImgurAPI {
		String server = "https://api.imgur.com";

		/** @param auth #Type of authorization for upload
		 * @param title #Title of image
		 * @param description #Description of image
		 * @param albumId #ID for album (if the user is adding this image to an album)
		 * @param username username for upload
		 * @param file image
		 * @param cb Callback used for success/failures */
		@POST("/3/image")
		void postImage (@Header("Authorization") String auth, //
			@Query("title") String title, //
			@Query("description") String description, //
			@Query("album") String albumId, //
			@Query("account_url") String username, //
			@Body TypedFile file, //
			Callback<ImageResponse> cb);
	}

	/** Basic object for upload. */
	static public class ImgurUpload {
		public File image;
		public String title;
		public String description;
		public String albumId;
	}

	/** Response from imgur when uploading to the server. */
	static public class ImageResponse {
		public boolean success;
		public int status;
		public UploadedImage data;
	}

	static public class UploadedImage {
		public String id;
		public String title;
		public String description;
		public String type;
		public boolean animated;
		public int width;
		public int height;
		public int size;
		public int views;
		public int bandwidth;
		public String vote;
		public boolean favorite;
		public String account_url;
		public String deletehash;
		public String name;
		public String link;
	}
}
