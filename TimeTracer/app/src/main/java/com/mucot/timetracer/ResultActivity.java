package com.mucot.timetracer;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.os.*;
import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.text.*;
import com.mucot.timetracer.Define;
import java.sql.*;
import android.content.*;
import com.mucot.timetracer.TimeTraceService.*;

public class ResultActivity extends Activity
{
	Handler mHandler = new Handler();
	
	List<String> resultInfo = new CopyOnWriteArrayList<>();;
	String batterInfo = new String();
	ArrayAdapter<String> adapter;
	String[] caption;
	protected TimeTraceService mService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result);
		
		InitListView();
		UpdateView();
		
		Button btnUpdate = (Button)findViewById(R.id.btnUpdate);
		btnUpdate.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				UpdateView();
			}
		});
		Button btnReset = (Button) findViewById(R.id.btnReset);
		btnReset.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				ResetData();
			}
		});
		
		TextView tvResult1 = (TextView)findViewById(R.id.tvResult1);
		tvResult1.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				mHandler.post(new Runnable(){
					public void run(){
						TextView tv = (TextView)findViewById(R.id.tvResult1);
						
						try{
							FileInputStream in = openFileInput(Define.FileName);
							BufferedReader reader = new BufferedReader(
							new InputStreamReader(in,"UTF-8"));
							String tmp;
							StringBuilder sb = new StringBuilder();
							
							while((tmp=reader.readLine())!=null){
								sb.append(tmp);
								sb.append(Define.BR);
							}
							tv.setText(sb.toString());
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	
	private void InitListView(){
		try{
			FileInputStream file = openFileInput(Define.captionFile);
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(file,"UTF-8"));
			String str = reader.readLine();
			reader.close();
			caption = str.split(",");
		}catch(Exception e){
			caption = new String[6];
			for(int i = 0;i < 6;i++){
				caption[i]=String.valueOf(i+1);
			}
		}
		if(caption.length<6){
			caption = new String[6];
			for(int i = 0;i < 6;i++){
				caption[i]=String.valueOf(i+1);
			}
		}
		ListView lvResult1 = (ListView)findViewById(R.id.lvResult1);
		for(int i = 0; i < 6; i++){
			resultInfo.add(caption[i] + " : ");
		}
		
		adapter = new ArrayAdapter<String>(
			this,
			android.R.layout.simple_expandable_list_item_1, 
			resultInfo);
		lvResult1.setAdapter(adapter);
	}
	
	private void UpdateView(){

		TextView tvResult1 = (TextView) findViewById(R.id.tvResult1);
		
		try{
			FileInputStream in = openFileInput(Define.FileName);
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(in,"UTF-8"));
			String tmp;

			for(int i = 0; i < 6;i++){
				tmp = reader.readLine();
				if(tmp != null){
					long time = Long.parseLong(tmp);
					String str = Define.df.format(time);
					resultInfo.set(i, caption[i] + " : " +str);
				}
			}
			adapter.notifyDataSetChanged();

			batterInfo = reader.readLine();
			tmp = reader.readLine();
			Date modify = new Date (Long.parseLong(tmp));
			if(tmp!=null){
				tvResult1.setText("Battery : "+batterInfo+"  "+modify.toLocaleString());
				
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void ResetData(){
		Intent intent = new Intent(this, TimeTraceService.class);
		bindService(intent, conn, BIND_AUTO_CREATE);
		mService.Reset();
		unbindService(conn);
	}
	
	private ServiceConnection conn = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder)
		{
			mService = ((TimeTraceBinder)binder).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName p1)
		{
			mService = null;
		}
	};
}