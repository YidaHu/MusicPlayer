package com.gswtek.huyd.broadcastreceiver;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Author: huyd
 * Date: 2017-07-21
 * Time: 13:52
 * Describe:音乐播放方式广播接收器.接收字符串功能为上一首/下一首
 */
public class MusicBroadcastReceiver extends BroadcastReceiver {
	private Message message;
	String flag = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		//接收MainActivity传过来的数据
//		Toast.makeText(context, intent.getStringExtra("hello"), Toast.LENGTH_SHORT).show();
		flag = intent.getStringExtra("bofang");
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
