package com.mucot.timetracer;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.util.concurrent.*;
import com.mucot.timetracer.Define;
import java.io.*;
import android.view.View.*;
import android.view.*;


public class SettingActivity extends Activity
{
	EditText eTxt1,eTxt2,eTxt3,eTxt4,eTxt5,eTxt6;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting);
	}

	@Override
	protected void onResume()
	{
		eTxt1 = (EditText)findViewById(R.id.etSetting1);
		eTxt2 = (EditText)findViewById(R.id.etSetting2);
		eTxt3 = (EditText)findViewById(R.id.etSetting3);
		eTxt4 = (EditText)findViewById(R.id.etSetting4);
		eTxt5 = (EditText)findViewById(R.id.etSetting5);
		eTxt6 = (EditText)findViewById(R.id.etSetting6);

		String[] token;
		try{
			FileInputStream file = openFileInput(Define.captionFile);
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(file,"UTF-8"));
			String str = reader.readLine();
			token = str.split(",");
		}catch(Exception e){
			token = new String[6];
			for(int i = 0; i < 6; i++){
				token[i]="";
			}
		}
		
		eTxt1.setText(token[0]);
		eTxt2.setText(token[1]);
		eTxt3.setText(token[2]);
		eTxt4.setText(token[3]);
		eTxt5.setText(token[4]);
		eTxt6.setText(token[5]);
		
		Button btn = (Button)findViewById(R.id.btnSetting);
		btn.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				
				
				StringBuilder sb= new StringBuilder();
				sb.append(eTxt1.getText().toString());sb.append(",");
				sb.append(eTxt2.getText().toString());sb.append(",");
				sb.append(eTxt3.getText().toString());sb.append(",");
				sb.append(eTxt4.getText().toString());sb.append(",");
				sb.append(eTxt5.getText().toString());sb.append(",");
				sb.append(eTxt6.getText().toString());sb.append(Define.BR);
				
				try{
					FileOutputStream out = openFileOutput(
					Define.captionFile,MODE_PRIVATE);
					out.write(sb.toString().getBytes());
					out.close();
				}catch(Exception e){
					
				}
			}
		});
		
		super.onResume();
	}
	
	
}