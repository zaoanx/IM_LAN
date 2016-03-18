package com.example.imclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "MainActivity";
	private static final String IP = "192.168.1.101";
    private static final int PORT = 50000;
	
	private EditText etUsername;
	private Button btLogin;
	private EditText etTo;
	private EditText etMessage;
    private Button btSend;
    private TextView tvRecord;
    
    private Thread thread;
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private String strMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    
    private void initView () {
    	etUsername = (EditText) findViewById(R.id.et_username);
    	btLogin = (Button) findViewById(R.id.bt_login);
    	btLogin.setOnClickListener(this);
    	etTo = (EditText) findViewById(R.id.et_to);
    	etMessage = (EditText) findViewById(R.id.et_message);
        btSend = (Button) findViewById(R.id.bt_send);
        btSend.setOnClickListener(this);
        btSend.setClickable(false);
        tvRecord = (TextView) findViewById(R.id.tv_record);
        
        thread = new Thread(runnable);
        thread.start();
    }
    
    @Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.bt_login:
			login();
			break;
		case R.id.bt_send:
			send();
			break;
		default:
			break;
		}
	}
    
    private void login () {
    	if (etUsername.getText().toString().equals("")) {
    		Toast.makeText(this, "请输入用户名", Toast.LENGTH_LONG).show();
    		return; 
    	} else {
    		new loginTask().execute();
    	}
    }
    
    private class loginTask extends AsyncTask<Void, Object, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			btLogin.setClickable(false);
			try {
    			socket = new Socket(IP, PORT);
    			printWriter = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())) , true);
    			printWriter.println(etUsername.getText().toString());
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.i(TAG, etUsername.getText().toString());
                return true;
    		} catch (Exception e) {
    			e.printStackTrace();
    		} 
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				btSend.setClickable(true);
			} else {
				btLogin.setClickable(true);
			}
		}
    }
    
    private void send () {
    	if (etTo.getText().toString().equals("")
    			|| etMessage.getText().toString().equals("")) {
    		Toast.makeText(this, "请输入完整信息", Toast.LENGTH_LONG).show();
    		return; 
    	} else {
    		new sendTask().execute();
    	}
    }
    
    private class sendTask extends AsyncTask<Void, Object, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String to = etTo.getText().toString();
                String message = etMessage.getText().toString();
                printWriter.println(etUsername.getText().toString() + "|" + to + "|" + message);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				tvRecord.post(new Runnable() {
					@Override
					public void run() {
						tvRecord.append("\n" + etUsername.getText().toString() + " : " + etMessage.getText().toString());
					}
				});
			}
		}
		
	}
    
    /**
     * 接收消息
     */
    private Runnable runnable = new Runnable() {
        public void run() {
            while (true) {
                try {
                    if (bufferedReader != null && (strMessage = bufferedReader.readLine()) != null) {
                    	String[] info = strMessage.split("\\|");
                        strMessage = info[0] + " : " + info[2];
                        tvRecord.post(new Runnable() {
							@Override
							public void run() {
								tvRecord.append("\n" + strMessage);
							}
						});
                    }                    
                } catch (Exception e) {
                	e.printStackTrace();
                }
            }
        }
    };

}