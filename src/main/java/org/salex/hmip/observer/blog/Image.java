package org.salex.hmip.observer.blog;

public class Image {
	private final String id;
	private String full;
	private String thumbnail;
	private int thumbnailWidth;
	private int thumbnailHeight;
	public Image(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public String getFull() {
		return full;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setFull(String full) {
		this.full = full;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public int getThumbnailWidth() {
		return thumbnailWidth;
	}
	public void setThumbnailWidth(int thumbnailWidth) {
		this.thumbnailWidth = thumbnailWidth;
	}
	public int getThumbnailHeight() {
		return thumbnailHeight;
	}
	public void setThumbnailHeight(int thumbnailHieght) {
		this.thumbnailHeight = thumbnailHieght;
	}
}