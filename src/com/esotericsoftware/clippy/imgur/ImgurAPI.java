
package com.esotericsoftware.clippy.imgur;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

/** Created by AKiniyalocts on 2/23/15.
 * <p/>
 * This is our imgur API. It generates a rest API via Retrofit from Square inc.
 * <p/>
 * more here: http://square.github.io/retrofit/ */
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
