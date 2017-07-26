package com.gswtek.huyd.mymusicplayer;

import java.io.File;
import java.io.Serializable;

public class MyFile implements Serializable {

	public boolean ischecked = false;
	public File file;
	public boolean isPusing = false;
	public boolean isthisSong = false;

	public MyFile(boolean ischecked, File file) {
		super();
		this.ischecked = ischecked;
		this.file = file;
	}


}
