package com.jack.mobilesafe.activity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jack.mobilesafe.R;
import com.jack.mobilesafe.utils.StreamUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

public class SplashActivity extends Activity {

    private TextView tvVersion;
    private TextView tvProgress;
    
    private String mVersionName;
	private int mVersionCode;
	private String mDescription;
	private String mDownloadUrl;
	
	private static final int CODE_SUCCESS =1;
	private static final int CODE_UPDATE =2;
	private static final int CODE_URL_EXPECTION =3;
	private static final int CODE_NET_EXPECTION =4;
	private static final int CODE_JSON_EXPECTION =5;
	
	
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case CODE_SUCCESS:
				enterHome();
				break;
			case CODE_UPDATE:
				updateDialog();
				break;
			case CODE_URL_EXPECTION:
				Toast.makeText(getApplicationContext(), "网址错误", Toast.LENGTH_SHORT).show();
				enterHome();
				break;
			case CODE_NET_EXPECTION:
				Toast.makeText(getApplicationContext(), "网络连接错误", Toast.LENGTH_SHORT).show();
				enterHome();
				break;
			case CODE_JSON_EXPECTION:
				Toast.makeText(getApplicationContext(), "网络数据错误", Toast.LENGTH_SHORT).show();
				enterHome();
				break;

			}
		};
	};

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvProgress = (TextView) findViewById(R.id.tv_progress);
        checkVersion();
    }
	
	protected void updateDialog() {
		AlertDialog.Builder builder= new AlertDialog.Builder(this);
		builder.setTitle("最新版本"+mVersionCode);
		builder.setMessage(mDescription);
		builder.setPositiveButton("立即更新", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				download();
			}
		});
		
		builder.setNegativeButton("下次再说", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				enterHome();
				
			}
		});
		
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				enterHome();
			}
		});
		builder.show();
	}

	protected void download() {
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String target = Environment.getExternalStorageDirectory()+"/"+"update.apk";
			tvProgress.setVisibility(View.VISIBLE);
			HttpUtils httpUtls = new HttpUtils();
			httpUtls.download(mDownloadUrl, target, new RequestCallBack<File>() {
				
				@Override
				public void onLoading(long total, long current,
						boolean isUploading) {
					tvProgress.setText("下载进度："+current*100/total+"%");
					super.onLoading(total, current, isUploading);
				}
				@Override
				public void onSuccess(ResponseInfo<File> arg0) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.addCategory(Intent.CATEGORY_DEFAULT);
					intent.setDataAndType(Uri.fromFile(arg0.result), "application/vnd.android.package-archive");
					
					startActivityForResult(intent, 0);
				}
				
				@Override
				public void onFailure(HttpException arg0, String arg1) {
					Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_SHORT).show();
					enterHome();
				}
			});
		}else{
			Toast.makeText(this, "未找到SD卡", Toast.LENGTH_SHORT).show();
		}
		
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		enterHome();
		super.onActivityResult(requestCode, resultCode, data);
	}
	public int  getVersion(){
		PackageManager manager = getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			int versionCode = info.versionCode;
			return versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public void checkVersion(){
		final long startTime = System.currentTimeMillis();
		new Thread(){
			
			@Override
			public void run() {
				Message msg = Message.obtain();
				HttpURLConnection conn =null;
				try {
					String path="http://10.0.2.2:8080/update.json";
					URL url = new URL(path);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
					conn.connect();
					int code = conn.getResponseCode();
					if(code == 200){
						InputStream in = conn.getInputStream();
						String result = StreamUtils.toString(in);
						
						JSONObject object = new JSONObject(result);
						mVersionName = object.getString("versionName");
						mVersionCode = object.getInt("versionCode");
						mDescription = object.getString("description");
						mDownloadUrl = object.getString("downloadUrl");
						
						}
						if(mVersionCode > getVersion()){
							msg.what = CODE_UPDATE; 
							
						}else{
							msg.what = CODE_SUCCESS; 
						}
					
				} catch (MalformedURLException e) {
					msg.what = CODE_URL_EXPECTION;
					e.printStackTrace();
				} catch (ProtocolException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					msg.what = CODE_NET_EXPECTION;
					e.printStackTrace();
				} catch (JSONException e) {
					msg.what = CODE_JSON_EXPECTION;
					e.printStackTrace();
				}finally{
					
					long endTime = System.currentTimeMillis();
					if((endTime - startTime) <2000){
						try {
							sleep(2000 -(endTime - startTime));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
	
					handler.sendMessage(msg);
					if(conn != null){
						conn.disconnect();
					}
				}
			}
		}.start();
		
	}

	protected void enterHome() {
		Intent intent = new Intent(this, HomeActivity.class);
		startActivity(intent);
		finish();
	}
	
	
}
