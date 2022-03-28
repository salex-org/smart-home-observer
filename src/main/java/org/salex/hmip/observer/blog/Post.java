package org.salex.hmip.observer.blog;

import com.fasterxml.jackson.annotation.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Post {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Content {
		private String text;
		@JsonGetter("rendered")
		public String getText() {
			return text;
		}
		@JsonSetter("rendered")
		public void setText(String text) {
			this.text = text;
		}
	}
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Meta {
		private String referencedImages;
		@JsonGetter("referenced_images")
		public String getReferencedImages() {
			return referencedImages;
		}
		@JsonSetter("referenced_images")
		public void setReferencedImages(String referencedImages) {
			this.referencedImages = referencedImages;
		}
	}
	
	private final long id;
	private String content;
	private Meta meta;

	@JsonCreator
	public Post(@JsonProperty("id") long id) {
		this.id = id;
	}

	@JsonGetter("id")
	public long getId() {
		return id;
	}

	@JsonGetter("content")
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@JsonSetter("content")
	public void setContent(Content content) {
		setContent(content.getText());
	}

	@JsonGetter("meta")
	public Meta getMeta() {
		return meta;
	}

	@JsonSetter("meta")
	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	
}
