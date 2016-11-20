package com.jack.mobilesafe.activity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.jack.mobilesafe.R;

public class HomeActivity extends Activity {
	
	private String[] mItems = new String[] { "手机防盗", "通讯卫士", "软件管理", "进程管理",
			"流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心" };

	private int[] mPics = new int[] { R.drawable.home_safe,
			R.drawable.home_callmsgsafe, R.drawable.home_apps,
			R.drawable.home_taskmanager, R.drawable.home_netmanager,
			R.drawable.home_trojan, R.drawable.home_sysoptimize,
			R.drawable.home_tools, R.drawable.home_settings }; 
	
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.activity_home);
	        
	        GridView gv = (GridView) findViewById(R.id.gv);
	        gv.setAdapter(new MyAdapter());
	    }
		
	 class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mItems.length;
		}

		@Override
		public Object getItem(int arg0) {
			return mItems[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View view = View.inflate(getApplicationContext(), R.layout.home_item, null);
			
			ImageView iv = (ImageView) view.findViewById(R.id.imageView1);
			TextView tv = (TextView) view.findViewById(R.id.textView1);
			
			iv.setImageResource(mPics[arg0]);
			tv.setText(mItems[arg0]);
			return view;
		}
		 
	 }
}
