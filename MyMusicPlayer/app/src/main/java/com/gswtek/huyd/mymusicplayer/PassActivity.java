package com.gswtek.huyd.mymusicplayer;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class PassActivity extends Activity {
	private int[] bgs = {R.mipmap.main_bg01,
			R.mipmap.main_bg02, R.mipmap.main_bg03,
			R.mipmap.main_bg04, R.mipmap.main_bg05,
			R.mipmap.main_bg06, R.mipmap.mybg};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pass);
		findViewById(R.id.ll_bg).setBackgroundResource(bgs[new Random().nextInt(7)]);
		gotoMain();
	}

	/**
	 * 睡眠2s后跳转主页面
	 */
	private void gotoMain() {
		new Thread() {
			public void run() {
				SystemClock.sleep(2000);
				startActivity(new Intent(PassActivity.this, MainActivity.class));
				finish();
			}

			;
		}.start();
	}
}
