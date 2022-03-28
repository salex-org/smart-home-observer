package org.salex.hmip.observer.blog;

import com.fasterxml.jackson.annotation.*;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Media {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Details {
		private Map<String, Size> sizes;
		@JsonGetter("sizes")
		public Map<String, Size> getSizes() {
			return sizes;
		}
		@JsonSetter("sizes")
		public void setSizes(Map<String, Size> sizes) {
			this.sizes = sizes;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Size {
		private String url;
		private int width;
		private int height;
		@JsonGetter("source_url")
		public String getUrl() {
			return url;
		}
		@JsonSetter("source_url")
		public void setUrl(String url) {
			this.url = url;
		}
		@JsonGetter("width")
		public int getWidth() {
			return width;
		}
		@JsonSetter("width")
		public void setWidth(int width) {
			this.width = width;
		}
		@JsonGetter("height")
		public int getHeight() {
			return height;
		}
		@JsonSetter("height")
		public void setHeight(int height) {
			this.height = height;
		}
	}
	
	private final long id;
	private Details details;
	
	@JsonCreator
	public Media(@JsonProperty("id") long id) {
		this.id = id;
	}

	@JsonGetter("id")
	public long getId() {
		return id;
	}

	@JsonGetter("media_details")
	public Details getDetails() {
		return details;
	}
	
	@JsonSetter("media_details")
	public void setDetails(Details details) {
		this.details = details;
	}

}
