package com.mucot.timetracer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Toast;
import android.view.View;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.ActivityManager;

public class MainActivity extends Activity 
{
	List<String> menu = new CopyOnWriteArrayList<>();
	ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		String strConnection;

		if(getServiceAlive())
			strConnection = "Stop";
		else
			strConnection = "Start";

		menu.add(strConnection);
		menu.add("Result");
		menu.add("Setting");

		ListView lv = (ListView)findViewById(R.id.listView1);
		adapter = new ArrayAdapter<String>(
			this,android.R.layout.simple_expandable_list_item_1,menu);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new
			AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent,
										View view, int position, long id){
					if(id==0){
						if(getServiceAlive()){
							stopService(new Intent(getApplicationContext(),TimeTraceService.class));
							if(!getServiceAlive())
								menu.set(0, "Start");
						}else{
							startService(new Intent(getApplicationContext(),TimeTraceService.class));
							if(getServiceAlive()) 
								menu.set(0,"Stop");
						}
						adapter.notifyDataSetChanged();
					}else if(id==1){
						Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
						startActivity(intent);
					}else if(id==2){
						Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
						startActivity(intent);
					}else{
						Toast.makeText(getApplicationContext(),"Not implemented",Toast.LENGTH_SHORT).show();
					}
				}
			});

		UpdateReceiver receiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("SEND_MSG");
		registerReceiver(receiver, filter);
    }
	private boolean getServiceAlive(){
		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		for(ActivityManager.RunningServiceInfo serviceInfo:manager.getRunningServices(Integer.MAX_VALUE)){
			if(TimeTraceService.class.getName().equals(serviceInfo.service.getClassName()))
				return true;
		}
		return false;
	}

	protected class UpdateReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Bundle extras = intent.getExtras();
			String msg = extras.getString("message");
			if(msg!=null){
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}

			int status = extras.getInt("status");
			if(status==Define.SERVICE_STOP){
				StopService();
			}
		}

		public void StopService(){
			stopService(new Intent(getApplicationContext(),TimeTraceService.class));
			if(!getServiceAlive()){
				menu.set(0, "Start");
				adapter.notifyDataSetChanged();
			}

		}
	}
}
