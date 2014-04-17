package com.quidditch.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class Server {

	Timer timer;

	public Server(){
		timer = new Timer();
		timer.schedule(new TimeoutTask(), 0, 1*1000);
	}

	class TimeoutTask extends TimerTask{

		int timeoutval = 0;
		public void run()
		{
			System.out.println("Executing.."+timeoutval);
				timeoutval++;
				if(timeoutval == 15)
					timer.cancel();
		}
	}

	static class SenderThread implements Runnable{

		public int senderPort = 4242;
		public String sendmessage;
		public boolean flag = true;

		public void run(){
			try{
				System.out.println("Sender Thread Running: "+senderPort);
				Socket client = new Socket("192.168.1.9", senderPort);

				while(flag)
				{
					OutputStream outToServer = client.getOutputStream();
					DataOutputStream out = new DataOutputStream(outToServer);
					sendmessage = "Quaffle";
					out.writeUTF(sendmessage);
				}
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	static class ReceiverThread implements Runnable{

		public int recieverPort = 4343;
		public String flag = "open";

		public void run(){
			try
			{
				System.out.println("Receiver Thread Running: "+recieverPort);
				ServerSocket serverSocket = new ServerSocket(recieverPort);

				while(flag.equalsIgnoreCase("open"))
				{
					System.out.println("Waiting to accept..");
					Socket server = serverSocket.accept();
					System.out.println("After accept call");

					DataInputStream in = new DataInputStream(server.getInputStream());

					String dataFromApp = in.readUTF().toString();

					System.out.println("Receieved Data "+dataFromApp);
					if(dataFromApp.equalsIgnoreCase("goalkeep"))
					{
						new Server();
					}

					System.out.println("Timeout over");

					server.close();
				}
				serverSocket.close();
			}
			catch (Exception e) {
				// TODO: handle exception
			}
		}

	}


	public static void main(String[] args) {

		SenderThread t1 = new SenderThread();
		ReceiverThread t2 = new ReceiverThread();

		new Thread(t1).start();
		new Thread(t2).start();

	}
}

