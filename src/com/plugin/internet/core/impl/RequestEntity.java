package com.plugin.internet.core.impl;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;

class RequestEntity {
	
	public static final String REQUEST_CONTENT_TYPE_TEXT_PLAIN = "text/plain";
	
	public static final String REQUEST_CONTENT_TYPE_MUTIPART = "multipart/form-data";
	
	private Bundle basicParams;
	
	private String contentType;
	
	private ArrayList<MultipartFileItem> fileItems;

	/**
	 * will use file first
	 * @author hecao (he.cao@renren-inc.com)
	 *
	 */
	public static class MultipartFileItem {
		private String name;
		private String fileName;
		private File file;
		private byte[] data;
		private String contentType;
		
		public MultipartFileItem(String name, String fileName, File file, byte[] data, String contentType) {
			this.setName(name);
			this.setFileName(fileName);
			this.setFile(file);
			this.setData(data);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public File getFile() {
			return file;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public byte[] getData() {
			return data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}
	}

	public Bundle getBasicParams() {
		return basicParams;
	}

	public void setBasicParams(Bundle basicParams) {
		this.basicParams = basicParams;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public ArrayList<MultipartFileItem> getFileItems() {
		return fileItems;
	}

	public void setFileItems(ArrayList<MultipartFileItem> fileItems) {
		this.fileItems = fileItems;
	}
	
	public void addFileItem(MultipartFileItem fileItem) {
		if (this.fileItems == null) {
			this.fileItems = new ArrayList<RequestEntity.MultipartFileItem>();
		}
		this.fileItems.add(fileItem);
	}

}
