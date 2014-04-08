package com.example.client;

import java.io.DataInputStream;

import java.net.ServerSocket;
import java.net.Socket;



//import myapp.test.nfctest.R;


import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	String advice;
	TextView tv;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) findViewById(R.id.tv); 

		new fileReceive().execute();
	}

	
	
	private class fileReceive extends AsyncTask<String, String, String>
	{
		int port = 4242;

		String values;
		


	//	@Override
		protected String doInBackground(String... arg0) {
		 
			try{
				ServerSocket serverSocket = new ServerSocket(port);
				
				//Server is running always. This is done using this while(true) loop
				
					//Reading the message from the client
				System.out.println("App listening...");
					Socket server = serverSocket.accept();
					
					DataInputStream in = new DataInputStream(server.getInputStream());

					values = in.readUTF().toString();
					Toast.makeText(getApplicationContext(), values, Toast.LENGTH_LONG).show();
					System.out.println("Rx value:"+values);
				
					server.close();
				//	flag = false;

				
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
		  return values;
		    }

		protected void onPostExecute(String values) {
			   // execution of result of Long time consuming operation
			   tv.setText(values);
			  }

		 protected void onProgressUpdate(String... text) {
			   tv.setText("Waiting");
			   // Things to be done while execution of long running operation is in
			   // progress. For example updating ProgessDialog
			  }
	}

}
