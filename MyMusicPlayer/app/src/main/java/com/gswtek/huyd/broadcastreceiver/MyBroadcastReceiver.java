package com.gswtek.huyd.broadcastreceiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.gswtek.huyd.mymusicplayer.MainActivity;
import com.gswtek.huyd.mymusicplayer.R;

/**
 * Author: huyd
 * Date: 2017-07-20
 * Time: 11:55
 * Describe:广播接收器.接收前面传来的歌曲信息,显示到标题栏中
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
	private Context context;
	private String musicName = "";
	private String musicPerson = "";
	public static final int NOTIFICATION_ID = 10001;
	public final static String ACTION_BUTTON = "com.notifications.intent.action.ButtonClick";
	public final static int BUTTON_PREV_ID = 1;
	/**
	 * ²¥·Å/ÔÝÍ£ °´Å¥µã»÷ ID
	 */
	public final static int BUTTON_PALY_ID = 2;
	/**
	 * ÏÂÒ»Ê× °´Å¥µã»÷ ID
	 */
	public final static int BUTTON_NEXT_ID = 3;

	public final static String INTENT_BUTTONID_TAG = "ButtonId";


	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		musicName = intent.getStringExtra("musicName").replace(".mp3", "");


//		Toast.makeText(context, intent.getStringExtra("musicName"), Toast.LENGTH_SHORT).show();
		showNofitication();
	}

	//歌曲通知显示
	public void showNofitication() {
		int icon = R.mipmap.ic_launcher;
		CharSequence tickerText = "Notification01";
		long when = System.currentTimeMillis();
		NotificationManager manager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification builder = new Notification(icon, tickerText, when);
		RemoteViews contentview = new RemoteViews(context.getPackageName(), R.layout.notification_layout);

		contentview.setTextViewText(R.id.notification_music_title, musicName);
		contentview.setTextViewText(R.id.notification_music_Artist, "");
		contentview.setImageViewResource(R.id.notification_artist_image, R.mipmap.skin_kg_playing_bar_default_avatar);
		builder.contentView = contentview;

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);


		builder.contentIntent = pendingIntent;


		Intent buttonIntent = new Intent(ACTION_BUTTON);
		//上一首
		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PREV_ID);
		PendingIntent intent_prev = PendingIntent.getBroadcast(context, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		contentview.setOnClickPendingIntent(R.id.notification_previous_song_button, intent_prev);
		//暂停/播放
		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_PALY_ID);
		PendingIntent intent_paly = PendingIntent.getBroadcast(context, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		contentview.setOnClickPendingIntent(R.id.notification_play_button, intent_paly);
		//下一首
		buttonIntent.putExtra(INTENT_BUTTONID_TAG, BUTTON_NEXT_ID);
		PendingIntent intent_next = PendingIntent.getBroadcast(context, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		contentview.setOnClickPendingIntent(R.id.notification_next_song_button, intent_next);

		builder.flags = builder.FLAG_NO_CLEAR;//设置通知点击或滑动时不被清除
		manager.notify(1, builder);//开启通知

	}


}