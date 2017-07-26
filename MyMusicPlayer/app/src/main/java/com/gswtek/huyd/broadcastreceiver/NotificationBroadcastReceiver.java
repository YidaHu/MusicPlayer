package com.gswtek.huyd.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.gswtek.huyd.mymusicplayer.MainActivity;

/**
 * Author: huyd
 * Date: 2017-07-20
 * Time: 14:51
 * Describe:接收MyBroadcastReceiver传过来通知栏各个按钮的事件
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
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

		String action = intent.getAction();
		if (action.equals(ACTION_BUTTON)) {
			int buttonId = intent.getIntExtra(INTENT_BUTTONID_TAG, 0);
			switch (buttonId) {
				case BUTTON_PREV_ID:
					Intent intent1 = new Intent("com.example.broadcasttest.NOTIFICATOIN_SELF");
					intent1.putExtra("bofang", "pre");
					context.sendBroadcast(intent1);
					break;
				case BUTTON_PALY_ID:
					Intent intent2 = new Intent("com.example.broadcasttest.NOTIFICATOIN_SELF");
					intent2.putExtra("bofang", "play");
					context.sendBroadcast(intent2);

					break;
				case BUTTON_NEXT_ID:
					Intent intent3 = new Intent("com.example.broadcasttest.NOTIFICATOIN_SELF");
					intent3.putExtra("bofang", "next");
					context.sendBroadcast(intent3);
					break;
				default:
					break;
			}
		}

	}


}
