package com.gswtek.huyd.mymusicplayer;

import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gswtek.huyd.broadcastreceiver.LrcBroadcastReceiver;

import java.io.IOException;
import java.io.InputStream;

import me.wcy.lrcview.LrcView;
/**
 * Author: huyd
 * Date: 2017-07-20
 * Time: 20:12
 * Describe:显示歌词
 */
public class LrcActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, LrcBroadcastReceiver.Message {

	private LrcView lrcBig;
	private MediaPlayer mediaPlayer = new MediaPlayer();
	private Handler handler = new Handler();
	LrcBroadcastReceiver lrcBroadcastReceiver;
	int currentPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lrc);

		lrcBig = (LrcView) findViewById(R.id.lrc_big);

		mediaPlayer.setOnCompletionListener(this);

		AssetManager am = getAssets();
		try {
			mediaPlayer.reset();
			AssetFileDescriptor fileDescriptor = am.openFd("cbg.mp3");
			mediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
			mediaPlayer.prepareAsync();
		} catch (IOException e) {
			e.printStackTrace();
		}

		lrcBig.loadLrc(getLrcText("cbg.lrc"));

		Log.i("msgInfo2", String.valueOf(currentPosition));

		//注册广播接收器
		lrcBroadcastReceiver = new LrcBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.example.broadcasttest.NOTIFICATOIN_LRC");
		registerReceiver(lrcBroadcastReceiver, intentFilter);

		//因为这里需要注入Message，所以不能在AndroidManifest文件中静态注册广播接收器
		lrcBroadcastReceiver.setMessage(this);


		handler.post(runnable);
	}

	private String getLrcText(String fileName) {
		String lrcText = null;
		try {
			InputStream is = getAssets().open(fileName);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			lrcText = new String(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lrcText;
	}


	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		lrcBig.onDrag(0);
	}

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
//			if (mediaPlayer.isPlaying()) {
			long time = mediaPlayer.getCurrentPosition();
			lrcBig.updateTime(currentPosition);
//			}
			Log.i("msgInfo1", String.valueOf(currentPosition));

			handler.postDelayed(this, 100);
		}
	};

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(runnable);
		mediaPlayer.reset();
		mediaPlayer.release();
		mediaPlayer = null;
		super.onDestroy();
	}

	@Override
	public void getMsg(String str) {
		currentPosition = Integer.parseInt(str);
	}
}
