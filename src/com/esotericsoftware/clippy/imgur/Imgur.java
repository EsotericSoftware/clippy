
package com.esotericsoftware.clippy.imgur;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
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
}
