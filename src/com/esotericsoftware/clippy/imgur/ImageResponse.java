
package com.esotericsoftware.clippy.imgur;

/** Created by AKiniyalocts on 1/14/15.
 *
 * Response from imgur when uploading to the server. */
public class ImageResponse {
	public boolean success;
	public int status;
	public UploadedImage data;

	public static class UploadedImage {
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
