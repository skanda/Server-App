package com.quidditch.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;



public class Server {

	Timer timer;
	Timer timer1;
	public static String dataFromApp;
	public static int score = 0;

	public static int goaltry_flag = 0;

	public Server(){
		timer = new Timer();
		timer.schedule(new TimeoutTask(), 0, 1*1000);
	}

	public Server(int i){
	}

	class TimeoutTask extends TimerTask{

		int timeoutval = 0;
		public void run()
		{
			System.out.println("Executing.."+timeoutval);
			if(dataFromApp.equalsIgnoreCase("scoreGoal") || dataFromApp.equalsIgnoreCase("Quaffle") || dataFromApp.equalsIgnoreCase("Bludger"))
			{
				//Do nothing
			}
			timeoutval++;
			if(timeoutval == 15)
				timer.cancel();
		}
	}

	public void SenderThread (String msg, String ipaddr){

		int senderPort = 4242;

		String sendmessage = msg;

		try{
			System.out.println("Sender Thread Running: "+ipaddr+","+senderPort+"===="+msg);
			Socket client = new Socket(ipaddr, senderPort);

			System.out.println("Entered while loop");
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			System.out.println("Sending message :"+sendmessage);
			out.writeUTF(sendmessage);
			client.close();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	


	static class ReceiverThread implements Runnable{

		public int recieverPort = 4343;
		public boolean flag_run = true;
		public boolean newConnections = true;
		InetSocketAddress sockAddr;
		
		DataInputStream in;
		int helloCounter = 0;
		int score1 = 0;
		int score2 = 0;
		public boolean haveQuaffle = true;
		ServerSocket serverSocket;
		Socket server;
		HashMap<Integer, String> dontHave = new HashMap<Integer, String>();
		HashMap<Integer, String> haveQBall = new HashMap<Integer, String>();
		String sendIpAddress;
		int hashValue;
		
		Iterator iter;
		String ipaddr;
		String x;


		Server send = new Server(recieverPort);

		public void run(){
			try
			{
				System.out.println("Receiver Thread Running: "+recieverPort);

				serverSocket = new ServerSocket(recieverPort);

				System.out.println("Waiting to accept..");

				while(newConnections)
				{

					if(helloCounter == 2)
					{
						System.out.println("Entering hellocounter loop");
						
						for (Map.Entry<Integer, String> entry : dontHave.entrySet())
						{
						    System.out.println(entry.getKey() + "," + entry.getValue());
							ipaddr = entry.getValue().substring(0, entry.getValue().indexOf(","));
							System.out.println(ipaddr);
							System.out.println("Before sending the scores");
							send.SenderThread("start-"+String.valueOf((helloCounter%2)+1),ipaddr);
							helloCounter --;
						}
						server.close();
						break;
					}
					
					System.out.println("Before Accepting...");
					server = serverSocket.accept();
					System.out.println("After Accepting...");

					
					in = new DataInputStream(server.getInputStream());
					dataFromApp = in.readUTF().toString();

					if(dataFromApp.equalsIgnoreCase("hello"))
					{
						
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						
						//sockAddr = (InetSocketAddress)server.getRemoteSocketAddress();
						System.out.println(x.substring(1, x.length()));
						
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println(hashValue);
						
						dontHave.put(hashValue,x.substring(1, x.length())+","+String.valueOf((helloCounter%2)+1));
						
						System.out.println("Before sending the scores");
						send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),x.substring(1, x.length()));
					}

					helloCounter++;
					System.out.println(helloCounter);
				}

				while(flag_run)
				{				
					System.out.println("Inside the main while loop");
					if(haveQuaffle)
					{
						System.out.println("Inside have qauffle"+ haveQuaffle);
						Random generator = new Random();
						Object[] values = dontHave.values().toArray();
						sendIpAddress = (values[generator.nextInt(values.length)]).toString();
					//	hashValue = Integer.parseInt(dontHave.get(keysAsArray.get(r.nextInt(keysAsArray.size()))));
						System.out.println(sendIpAddress);
					//	sendIpAddress = dontHave.get(hashValue);
						System.out.println("Sending the Quaffle....");
						send.SenderThread("Quaffle",sendIpAddress.substring(0, sendIpAddress.indexOf(",")));
					}

					System.out.println("Quaffle sent....waiting.."+sendIpAddress.substring(0, sendIpAddress.indexOf(",")));
					
					
					server = serverSocket.accept();
					
					
					
					in = new DataInputStream(server.getInputStream());
					System.out.println("After data in");
					dataFromApp = in.readUTF().toString();
					
					System.out.println("Receieved Data "+dataFromApp);
					
					if(dataFromApp.equalsIgnoreCase("caughtQuaffle"))
					{
						haveQuaffle = false;	
						System.out.println("caught");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println(hashValue+":"+dontHave.get(hashValue).toString());
						
						haveQBall.put(hashValue,dontHave.get(hashValue).toString());
						dontHave.remove(hashValue);
						
					}
					else if(dataFromApp.equalsIgnoreCase("missedQuaffle"))
					{
						haveQuaffle = true;
					}
					else if(dataFromApp.equalsIgnoreCase("goalkeep"))
					{
						new Server();
					}
					else if(dataFromApp.equalsIgnoreCase("trygoal"))
					{
						System.out.println("Entered trygoal");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						System.out.println(x);
						hashValue = x.substring(1, x.length()).hashCode();
						
						System.out.println(hashValue);
						String ipstart = haveQBall.get(hashValue).toString();
						
						System.out.println(ipstart.substring(ipstart.indexOf(",")+1, ipstart.length()));
						if(ipstart.substring(ipstart.indexOf(",")+1, ipstart.length()).equalsIgnoreCase("1"))
						{
							score1+=10;
						}
						else
						{
							score2+=10;
						}	
						
						send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), x.substring(1, x.length()));
						dontHave.put(hashValue,haveQBall.get(hashValue).toString());
						haveQBall.remove(hashValue);
						haveQuaffle = true;
					}
					else if(dataFromApp.equalsIgnoreCase("knockout"))
					{
						System.out.println(dataFromApp);
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


		ReceiverThread t2 = new ReceiverThread();


		new Thread(t2).start();

	}
}

