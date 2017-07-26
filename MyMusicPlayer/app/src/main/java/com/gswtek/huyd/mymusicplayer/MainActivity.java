package com.gswtek.huyd.mymusicplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.gswtek.huyd.broadcastreceiver.MusicBroadcastReceiver;
import com.gswtek.huyd.service.PlayerService;

/**
 * Author: huyd
 * Date: 2017-07-18
 * Time: 15:21
 * Describe:主界面
 */
@SuppressLint("HandlerLeak")
public class MainActivity extends Activity implements MusicBroadcastReceiver.Message, ServiceConnection {

	public static MainActivity instance = null;

	private CheckBox playOrpause; //播放或暂停按钮
	private MediaPlayer player = new MediaPlayer();
	private ImageView zhuandong;    //转动的图片
	private int[] bgs = {R.mipmap.main_bg01, R.mipmap.main_bg02,
			R.mipmap.main_bg03, R.mipmap.main_bg05, R.mipmap.main_bg06,
			R.mipmap.mybg}; //背景资源
	int SongTime = 0;    //歌曲时长
	int thisSong = 0;    //集合中当前播放的歌曲的下标
	private ListView lv_music;    //播放列表
	private MusicAdapter adapter;    //歌曲适配器
	String save_music = "";        //将歌曲路径拼接成字符串用于保存到文件中
	private Animation animation;    //跳动的帧动画初始化
	ArrayList<MyFile> mf_list = new ArrayList<MyFile>();    //存放歌曲的集合
	private SeekBar sb;    //歌曲进度条
	private File files;        //files目录下的文件
	int currentPosition = 0;    //当前播放进度
	private TextView tv_start;    //用分:秒表示当前播放进度
	private TextView tv_end; //用分:秒表示歌曲总时长
	private ImageView nextMusic;
	private ImageView preMusic;
	PlayerService musicService;
	String path = "";
	MusicBroadcastReceiver musicBroadcastReceiver;
	Handler h_updata = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			sb.setProgress(currentPosition);
			tv_start.setText(timeParse(currentPosition));
			h_updata.sendEmptyMessageDelayed(100, 200);
			super.handleMessage(msg);
		}
	};
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			h_updata.sendEmptyMessage(100);
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//随机背景
		findViewById(R.id.rl_bg).setBackgroundResource(bgs[new Random().nextInt(6)]);
		initviews();
		initData();

		Intent intent = new Intent(MainActivity.this, PlayerService.class);
		MainActivity.this.bindService(intent, this, Context.BIND_AUTO_CREATE);//绑定播放音乐的服务


		//注册广播接收器
		musicBroadcastReceiver = new MusicBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.example.broadcasttest.NOTIFICATOIN_SELF");
		registerReceiver(musicBroadcastReceiver, intentFilter);

		//因为这里需要注入Message，所以不能在AndroidManifest文件中静态注册广播接收器
		musicBroadcastReceiver.setMessage(this);

		new Thread(runnable).start();

		//音乐播放完成监听
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				thisSong++;//播放列表中的下一首
				//如果是列表中的最后一首播放完成后则播放第一首
				thisSong = thisSong > mf_list.size() - 1 ? 0 : thisSong;
				MyFile myfile = (MyFile) adapter.getItem(thisSong);
				Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");
				intent.putExtra("musicName", myfile.file.getName());
				sendBroadcast(intent);

				playmusic(myfile);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
//		new Thread(runnable).start();
//		onCreate();

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Intent intent = new Intent(MainActivity.this, PlayerService.class);
		MainActivity.this.bindService(intent, this, Context.BIND_AUTO_CREATE);//绑定播放音乐的服务
	}

	private void initData() {
		initmusic();
		getMusic();
		files = new File(getFilesDir(), "musiclist.txt");// 获取files目录下的文件
		if (!files.exists()) {
			try {
				files.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 以上是找到files目录下的文件，以下是通过IO流读取数据操作
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len = 0;
			byte[] buf = new byte[1024];
			FileInputStream fis = new FileInputStream(files);
			while ((len = fis.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}
			fis.close();
			String s = new String(baos.toByteArray());
			String[] strings = s.split("@");
			getMusicPathList(strings);
			adapter.setData(mf_list);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	/**
	 * 将音乐路径加入列表
	 */
	private void getMusic() {
		if (mf_list.size() > 0) {
			return;
		}
		File dir = getFilesDir();
		File file = new File(dir, "丑八怪.mp3");
		MyFile myFile = new MyFile(false, file);
		mf_list.add(0, myFile);
	}

	/**
	 * 初始化加载音乐到date/date/工程目录下
	 */
	private void initmusic() {
		File dir = getFilesDir();
		File file = new File(dir, "丑八怪.mp3");
		if (file.exists()) {
			return;
		}
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = getAssets().open("cbg.mp3");
			fos = new FileOutputStream(file);
			byte[] b = new byte[1024];
			int len = 0;
			while ((len = is.read(b)) > 0) {
				fos.write(b, 0, len);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null && fos != null) {
				try {
					is.close();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initviews() {
		zhuandong = (ImageView) findViewById(R.id.zhuandong);
		animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.tip);
		animation.setInterpolator(new LinearInterpolator()); // 设置插入器
		sb = (SeekBar) findViewById(R.id.sb);
		tv_start = (TextView) findViewById(R.id.tv_start);
		tv_end = (TextView) findViewById(R.id.tv_end);
		lv_music = (ListView) findViewById(R.id.lv_music);
		playOrpause = (CheckBox) findViewById(R.id.playOrpause);
		nextMusic = findViewById(R.id.nextMusic);
		preMusic = findViewById(R.id.preMusic);
		adapter = new MusicAdapter();
		lv_music.setAdapter(adapter);

		zhuandong.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(MainActivity.this, LrcActivity.class);
				startActivity(intent);
			}
		});


		//下一首
		nextMusic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				next();
			}
		});

		preMusic.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				previous();
			}
		});

		playOrpause.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
			                             boolean isChecked) {
				playOrPauseMusic(isChecked);
			}
		});

		lv_music.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
			                        long id) {
				thisSong = position;
				playOrpause.setChecked(true);
				MyFile myFile = mf_list.get(position);
				Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");
				intent.putExtra("musicName", myFile.file.getName());
				sendBroadcast(intent);
				playmusic(myFile);
			}
		});

		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

				tv_start.setText(timeParse(sb.getProgress()));

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

				tv_start.setText(timeParse(sb.getProgress()));

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {

				tv_start.setText(timeParse(sb.getProgress()));
				//用户拖动进度条
				if (fromUser) {
//					player.seekTo(progress);// 根据歌曲百分比设置进度条
					musicService.seekPlay(progress);
				}
			}
		});
	}

	/**
	 * 点击播放列表条目时播放音乐
	 *
	 * @param myFile 封装了歌曲信息的对象
	 */
	protected void playmusic(MyFile myFile) {
//		PlayerService play = new PlayerService();
//		play.setMyFile(myFile);
//		play.setMf_list(mf_list);

//		myFile1 = myFile;

//
//		Intent intent = new Intent(MainActivity.this, PlayerService.class);
//		intent.putExtra("myFile", myFile);
//
//		bindService(intent, this, BIND_AUTO_CREATE);
		path = myFile.file.getAbsolutePath();
		musicService.playMusic(path, "0");

//		//将所有歌去置为初始化状态
//		for (MyFile mf : mf_list) {
//			mf.isthisSong = false;
//		}
//		myFile.isthisSong = true;
//		zhuandong.clearAnimation();
//		player.stop();    //停止
//		player.reset();    //重置
//		try {
//		h_updata.removeMessages(100);
//		try {
//			player.setDataSource(myFile.file.getAbsolutePath());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//			player.prepare();    //准备播放
//			SongTime = player.getDuration();
//		tv_end.setText(timeParse(SongTime));   //设置当前歌曲总时长,用 分:秒 表示
//		sb.setMax(SongTime);// 将歌曲的持续时长设置个进度条
//			player.start();        //开始播放
		if (animation != null) {
			zhuandong.startAnimation(animation);
		}
		adapter.notifyDataSetChanged();    //刷新界面数据
		h_updata.sendEmptyMessage(100);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}


	}

	/**
	 * 将音乐的毫秒值转成分:秒
	 *
	 * @param duration 音乐的毫秒值
	 * @return 分:秒
	 */
	public String timeParse(long duration) {
		String time = "";

		long minute = duration / 60000;
		long seconds = duration % 60000;

		long second = Math.round((float) seconds / 1000);

		if (minute < 10) {
			time += "0";
		}
		time += minute + ":";

		if (second < 10) {
			time += "0";
		}
		time += second;

		return time;
	}

	//播放或暂停
	public void playOrPauseMusic(boolean isChecked) {
		//点击了播放按钮
		if (isChecked) {
			sb.setProgress(sb.getProgress());//如果是暂停后点击,则继续之前进度播放
//			player.start();
//			musicService.playMusic(path, "0");
			musicService.startPlay(sb.getProgress());
			mf_list.get(thisSong).isPusing = false;
			zhuandong.startAnimation(animation);
		} else {
			mf_list.get(thisSong).isPusing = true;
//			currentPosition = player.getCurrentPosition();//暂停时获取当前播放进度
//			player.pause();
			musicService.playMusic(path, "1");

			zhuandong.clearAnimation();
		}

		adapter.notifyDataSetChanged();
	}

	public void previous() {
		thisSong--;
		//如果是集合中的第一首,则播放集合中的最后一首,同时避免集合越界
		thisSong = thisSong < 0 ? mf_list.size() - 1 : thisSong;
		MyFile myfile = mf_list.get(thisSong);
		Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");
		intent.putExtra("musicName", myfile.file.getName());
		sendBroadcast(intent);

		playmusic(myfile);
		playOrpause.setChecked(true);
		adapter.notifyDataSetChanged();

	}

	//下一首
	public void next() {
		thisSong++;
		//如果是集合中的最后一首,则播放集合中的第一首,同时避免集合越界
		thisSong = thisSong > mf_list.size() - 1 ? 0 : thisSong;
		MyFile myfile = mf_list.get(thisSong);
		Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");
		intent.putExtra("musicName", myfile.file.getName());
		sendBroadcast(intent);

		playmusic(myfile);
		playOrpause.setChecked(true);
		adapter.notifyDataSetChanged();
	}

	public void nextOther(int song) {
		song++;
		//如果是集合中的最后一首,则播放集合中的第一首,同时避免集合越界
		song = song > mf_list.size() - 1 ? 0 : song;
		MyFile myfile = mf_list.get(song);
		Intent intent = new Intent("com.example.broadcasttest.MY_BROADCAST");
		intent.putExtra("musicName", myfile.file.getName());
		sendBroadcast(intent);

		playmusic(myfile);
		playOrpause.setChecked(true);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 文件夹(SD卡目录)
	 *
	 * @param v
	 */
	public void localfile(View v) {
		Intent i = new Intent(this, FileActivity.class);
		startActivityForResult(i, 1000);
	}

	/**
	 * 将数组中的歌曲路径添加到集合(播放列表)中
	 *
	 * @param strings 歌曲路径的集合
	 */
	public void getMusicPathList(String[] strings) {
		for (String s : strings) {
			if (s.equals("")) {
				break;
			}
			File file = new File(s);
			mf_list.add(new MyFile(false, file));
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1000 && resultCode == 2000) {
			String string = data.getStringExtra("paths");
			// 切割
			String[] strings = string.split("@");
			//将数组中的歌曲路径添加到集合(播放列表)中
			getMusicPathList(strings);
			adapter.setData(mf_list);    //把集合传给适配器
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	//退出程序之前将音乐路径保存到文件中
	@Override
	protected void onDestroy() {
		mf_list.remove(0);
		for (MyFile mf : mf_list) {// 将音乐文件的路径连成一条“@”连接的字符串
			save_music = save_music + mf.file.getAbsolutePath() + "@";
		}
		// 开始存数据
		try {
			FileOutputStream fos = new FileOutputStream(files);
			fos.write(save_music.getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}


	@Override
	public void getMsg(String str) {
		if (str.equals("next")) {
			next();
		} else {
			previous();
		}

	}


	@Override
	public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

		PlayerService.MusicBinder binder = (PlayerService.MusicBinder) iBinder;

		musicService = binder.getService();
		musicService.playMusic(path, "0");
//		bound = true;
		musicService.setCallback(new PlayerService.Callback() {
			@Override
			public void onDataChange(String data) {
				Message msg = new Message();
				msg.obj = data;
				handler.sendMessage(msg);
				int j = 0;
				Log.i("data", j++ + "++++++++++++");
			}
		});


	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			String[] strings = msg.obj.toString().split("\\@");
			SongTime = Integer.parseInt(strings[0]);
//			tv_end.setText(msg.obj.toString());
			tv_end.setText(timeParse(SongTime));   //设置当前歌曲总时长,用 分:秒 表示
			sb.setMax(SongTime);// 将歌曲的持续时长设置个进度条
			currentPosition = Integer.parseInt(strings[1]);
			sb.setProgress(currentPosition);
			Log.i("sb", String.valueOf(currentPosition) + "++++++++++++");

			tv_start.setText(timeParse(sb.getProgress()));

			new Thread(new Runnable() {
				@Override
				public void run() {
					//发送播放歌曲进度事件给歌词界面
					Intent intentlrc = new Intent("com.example.broadcasttest.NOTIFICATOIN_LRC");
					intentlrc.putExtra("lrctime", String.valueOf(sb.getProgress()));
					MainActivity.this.sendBroadcast(intentlrc);
				}
			}).start();
		}
	};


	public int showProgress() {
		return sb.getProgress();
	}
}

