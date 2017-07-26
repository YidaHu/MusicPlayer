package com.gswtek.huyd.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * Author: huyd
 * Date: 2017-07-19
 * Time: 20:39
 * Describe:音乐播放器播放服务
 */
public class PlayerService extends Service {


	private MediaPlayer mediaPlayer = new MediaPlayer();
	private final IBinder iBinder = new MusicBinder();
	int SongTime = 0;
	long currentPosition = 0;
	private Callback callback;
	private boolean connection = false;


	public PlayerService() {

	}


	/**
	 * MusicBinder 提供了getService方法来获得当前MusicService的实例
	 */
	public class MusicBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}

	}

	@Override
	public void onCreate() {
		super.onCreate();

		connection = true;
		new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				while (connection) {
					currentPosition = currentPosition + 1000;
					try {
						Thread.sleep(1000);
						showPosition();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();


	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.


		return iBinder;
	}


	/**
	 * 播放音乐的方法
	 *
	 * @param currentPath 音乐文件路径
	 */
	public void playMusic(String currentPath, String flag) {


		//播放
		if (flag.equals("0")) {
//			currentPosition = 0;
			try {
				if (mediaPlayer.isPlaying()) {//如果当前正在播放音乐，则先停止
					mediaPlayer.stop();
				}
				mediaPlayer.reset();//重置播放器z状态
				mediaPlayer.setDataSource(currentPath);
				mediaPlayer.prepare();
				SongTime = mediaPlayer.getDuration();
				showPosition();
				mediaPlayer.start();


			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (flag.equals("1")) {//暂停
			currentPosition = mediaPlayer.getCurrentPosition();//暂停时获取当前播放进度
			mediaPlayer.pause();

		} else {
		}


	}

	//从滑动条出播放
	public void seekPlay(int progress) {
		mediaPlayer.seekTo(progress);
	}

	public void startPlay(int progress) {
		mediaPlayer.seekTo(progress);
		mediaPlayer.start();
	}


	public void setCallback(Callback callback) {
		this.callback = callback;

	}

	public interface Callback {
		void onDataChange(String data);
	}


	public void showPosition() {
		//获取当前歌曲播放长度
		currentPosition = mediaPlayer.getCurrentPosition();
		Log.i("currentPosition", String.valueOf(currentPosition) + "------------");


		//字符串以@符号分割,前面为歌曲总长度,后面为当前播放长度
		String data = String.valueOf(SongTime) + "@" + String.valueOf(currentPosition);
		callback.onDataChange(data);
	}

}
