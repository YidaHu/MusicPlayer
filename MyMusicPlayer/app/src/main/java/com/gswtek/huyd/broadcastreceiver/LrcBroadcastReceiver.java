package com.gswtek.huyd.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Author: huyd
 * Date: 2017-07-26
 * Time: 09:52
 * Describe:歌词广播接收器
 */
public class LrcBroadcastReceiver extends BroadcastReceiver {
	private Message message;
	String flag = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		//接收MainActivity传过来的数据
//		Toast.makeText(context, intent.getStringExtra("hello"), Toast.LENGTH_SHORT).show();
		flag = intent.getStringExtra("lrctime");
		//调用Message接口的方法
		message.getMsg(flag);

	}

	public interface Message {
		public void getMsg(String str);
	}

	public void setMessage(Message message) {
		this.message = message;
	}
}
