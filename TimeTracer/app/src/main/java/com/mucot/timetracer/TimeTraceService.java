package com.mucot.timetracer;
import android.app.*;
import android.content.*;
import android.os.*;
import java.util.*;
import java.io.*;
import android.util.*;
import android.widget.*;
import java.text.*;
import android.hardware.usb.*;
import com.mucot.timetracer.Define;

public class TimeTraceService extends Service
{
	Handler mHandler = new Handler();
	FileOutputStream out;
	
	StringBuilder outStr;
	
	FTDriver mSerial;
	boolean mStop;
	
	Status current = new Status();
	long start, end; // Date
	long[] time = new long[6];
	
	private static final String ACTION_USB_PERMISSION =
	"com.mucot.timetracer.USB_PERMISSION";
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		// [FTDriver] Create Instance
		mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));

		// [FTDriver] setPermissionIntent() before begin()
		PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
																		ACTION_USB_PERMISSION), 0);
		mSerial.setPermissionIntent(permissionIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{

// [FTDriver] Open USB Serial
		if(mSerial.begin(FTDriver.BAUD115200)) {

			ResetTime();
			
			mainloop();
			mStop = false;
			
			String wbuf = ":788001000F0000000000000000F8\r\n";
			mSerial.write(wbuf.getBytes());

			start = System.currentTimeMillis();
			Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
			
		} else {
			//Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
			SendMassage("Failed");
			SendStatus(Define.SERVICE_STOP);
		}
		return START_STICKY; 
	}

	@Override
	public void onDestroy()
	{
		mStop = true;
		mSerial.end();

		Toast.makeText(getApplicationContext()
		, "Disconnected", Toast.LENGTH_SHORT).show();
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO: Implement this method
		return new TimeTraceBinder();
	}
	
	public void Reset(){
		ResetTime();
	}
	
	private void mainloop(){
		new Thread(mLoop).start();
		return;
	}
	
	private Runnable mLoop = new Runnable(){
		@Override
		public void run()
		{

			// [FTDriver] Create Read Buffer
			byte[] rbuf = new byte[4096]; // 1byte <--slow-- [Transfer Speed] --fast--> 4096 byte

			for(;;){
				boolean isUpdate = false;
				int index = current.mSelect - 1;
				end  = System.currentTimeMillis();
				if(end - start >10000) isUpdate=true;
				
				// [FTDriver] Read from USB Serial
				mSerial.read(rbuf);
				String rStr = new String(rbuf);
				String[] token = rStr.split(":");
				
				if(token.length >= 11){
					Status st = TokenAnalysis(token);
					if(st!=null){
						if(st.mSelect != current.mSelect ||
						current.mSelect!=0){
							current.Copy(st);
							isUpdate = true;
							}
					}
				}
				
				if(isUpdate){
					if(index>=0)
					time[index] = time[index] + (end - start);
					start = end;
					
					outStr = new StringBuilder();
					outStr.append(time[0]);outStr.append(Define.BR);
					outStr.append(time[1]);outStr.append(Define.BR);
					outStr.append(time[2]);outStr.append(Define.BR);
					outStr.append(time[3]);outStr.append(Define.BR);
					outStr.append(time[4]);outStr.append(Define.BR);
					outStr.append(time[5]);outStr.append(Define.BR);
					outStr.append(current.mLife);outStr.append(Define.BR);
					outStr.append(end);

					try{
						out = openFileOutput(Define.FileName,MODE_PRIVATE);
						out.write(String.valueOf(outStr.toString()).getBytes());
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(mStop) {
					return;
				}
			}
		}
	};
			
	private Status TokenAnalysis(String[] token){
		if(token.length<11)return null;
		Status status = new Status();
		
		for(String tmp:token){
			String[] paraValue = tmp.split("=");
			if(paraValue.length<2)continue;
			
			switch(paraValue[0]){
				case "lq":
					status.mLq = Integer.parseInt(paraValue[1]);
					break;
				case "ct":
					status.mCount = Integer.decode("0x" + paraValue[1]);
					
					break;
				case "ba":
					status.mLife = Integer.parseInt(paraValue[1]);
					break;
				case "x":
					status.mSelect = Integer.parseInt(paraValue[1]);
					break;
			}
		}
		
		return status;
	}
	
	private void SendMassage(long value){
		SendMassage(String.valueOf(value));
	}
	
	private void SendMassage(String msg){
		Intent broadcast = new Intent();
        broadcast.putExtra("message", msg);
        broadcast.setAction("SEND_MSG");
        getBaseContext().sendBroadcast(broadcast);
	}
	
	private void SendStatus(int status){
		Intent broadcast = new Intent();
		broadcast.putExtra("status", status);
		broadcast.setAction("SEND_MSG");
		getBaseContext().sendBroadcast(broadcast);
	}
	
	private void ResetTime(){
		Date date = new Date(0);
		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		calender.add(Calendar.HOUR, -9);
		for(int i = 0;i < time.length;i++){
			time[i] = calender.getTimeInMillis();
		}
		start = System.currentTimeMillis();
	}
	
	private class Status{
		public int mLq;
		public int mCount;
		public int mLife;
		public int mSelect;
		
		public Status(){}
		public Status(int lq, int count, int life, int select){
			mLq = lq;
			mCount = count;
			mLife = life;
			mSelect = select;
		}
		
		private void Copy(Status org){
			if(org == null)return;
			
			this.mLq = org.mLq;
			this.mCount = org.mCount;
			this.mLife = org.mLife;
			this.mSelect = org.mSelect;
		}
	}
	
	public class TimeTraceBinder extends Binder{
		public TimeTraceService getService(){
			return TimeTraceService.this;
		}
	}
}