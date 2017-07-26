package com.gswtek.huyd.domain;

/**
 * Author: huyd
 * Date: 2017-07-19
 * Time: 20:09
 * Describe:
 */
public class MusicModel {
	private String title;
	private String autist;

	public MusicModel() {
	}

	public MusicModel(String title, String autist) {
		this.title = title;
		this.autist = autist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAutist() {
		return autist;
	}

	public void setAutist(String autist) {
		this.autist = autist;
	}
}
