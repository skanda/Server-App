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


	public static String dataFromApp;
	public static String[] recievedData;
	public static boolean goalkeepFlag = false;

	public static int score = 0;

	public static int goaltry_flag = 0;

	public Server(){

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

		Timer timer1;
		Timer timer2;

		public int recieverPort = 4343;
		public boolean flag_run = true;
		public boolean newConnections = true;
		InetSocketAddress sockAddr;

		DataInputStream in;
		int helloCounter = 0;
		int score1 = 0;
		int score2 = 0;
		int count = 0;
		public static boolean haveQuaffle = true;
		public static boolean haveBludger = true;
		public static boolean knockoutOpponent = false;
		public static boolean goalattempt = false;


		ServerSocket serverSocket;
		Socket server;
		static HashMap<Integer, String> dontHave = new HashMap<Integer, String>();
		static HashMap<Integer, String> haveQBall = new HashMap<Integer, String>();
		static HashMap<Integer, String> haveBBall = new HashMap<Integer, String>();
		static HashMap<String, Integer> ipRssi = new HashMap<String, Integer>();
		static ArrayList<String> knockoutList = new ArrayList<String>();
		static HashMap<Integer, String> offlineList = new HashMap<Integer, String>();
		static HashMap<Integer, String> goalKeep = new HashMap<Integer, String>();

		String sendIpAddress;
		int hashValue;

		Iterator iter;
		String ipaddr;
		String x;
		int rssiVal;
		public static String tryknockoutIP; 
		public static String trygoalIP;
		public static String trygoalTeam;
		public static int hashVal;

		Server send = new Server();

		public ReceiverThread(){

		}

		class TimeoutTask1 extends TimerTask{

			int timeoutval1 = 0;
			int hash;

			public TimeoutTask1(int hashVal)
			{
				this.hash = hashVal;
			}
			public void run()
			{
				System.out.println("Executing.."+timeoutval1);
				timeoutval1++;
				if(timeoutval1 == 15)
				{
					System.out.println("15 sec over before"+hash);

					for (Map.Entry<Integer, String> entry : goalKeep.entrySet())
					{
						System.out.println("Entry Key:"+entry.getKey()+","+"Entry Value"+entry.getValue());
					}

					if(goalKeep.containsKey(hash))
					{
						System.out.println("15 sec over");
						dontHave.put(hash, goalKeep.get(hash).toString());
						goalKeep.remove(hash);
						timer1.cancel();
					}
				}

			}
		}

		/*	class TimeoutTask2 extends TimerTask{

			int timeoutval2 = 0;
			public void run()
			{
				System.out.println("Executing.."+timeoutval2);
				timeoutval2++;
				if(timeoutval2 == 15)
					timer2.cancel();
			}
		}*/

		public void startTimer(int hashVal){
			System.out.println("In Server, starting goalkeeping");
			System.out.println("Inside starttimer"+hashVal);
			timer1 = new Timer();
			timer1.schedule(new TimeoutTask1(hashVal), 0, 1*1000);
			//		if(i==1)
			//		{

			/*		}
			else
			{
				timer2 = new Timer();
				timer2.schedule(new TimeoutTask2(), 0, 1*1000);
			}*/
		}

		public void stopTimer(){
			System.out.println("Stopping Timer...");
			timer1.cancel();
		}



		public void run(){
			try
			{
				ReceiverThread rthread = new ReceiverThread();	
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

					recievedData=dataFromApp.split(";");

					if(recievedData[0].equalsIgnoreCase("hello"))
					{

						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();

						//sockAddr = (InetSocketAddress)server.getRemoteSocketAddress();
						System.out.println(x.substring(1, x.length()));

						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println(hashValue);

						dontHave.put(hashValue,x.substring(1, x.length())+","+String.valueOf((helloCounter%2)+1));

						System.out.println("Before sending the scores");
						send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),x.substring(1, x.length()));
						ipRssi.put(x.substring(1, x.length())+","+String.valueOf((helloCounter%2)+1),Integer.parseInt(recievedData[1]));
					}

					helloCounter++;
					System.out.println(helloCounter);
				}

				while(flag_run)
				{				
					System.out.println("Inside the main while loop");
					haveBludger = false;

					if(haveQuaffle)
					{
						System.out.println("Inside have qauffle"+ haveQuaffle);

						int i=0;
						Random generator = new Random();
						Object[] values = new Object[dontHave.size()-goalKeep.size()];
						for (Map.Entry<Integer, String> entry : dontHave.entrySet())
						{
							if(!(goalKeep.containsKey(entry.getKey())))
							{
								values[i]=entry.getValue();
								i++;
							}

						}
						sendIpAddress = (values[generator.nextInt(values.length)]).toString();
						//	hashValue = Integer.parseInt(dontHave.get(keysAsArray.get(r.nextInt(keysAsArray.size()))));
						System.out.println(sendIpAddress);
						//	sendIpAddress = dontHave.get(hashValue);
						System.out.println("Sending the Quaffle....");
						send.SenderThread("Quaffle",sendIpAddress.substring(0, sendIpAddress.indexOf(",")));

					}
					if(haveBludger && !haveQuaffle)
					{	
						Random generator = new Random();
						Object[] values = dontHave.values().toArray();
						sendIpAddress = (values[generator.nextInt(values.length)]).toString();
						send.SenderThread("Bludger",sendIpAddress.substring(0, sendIpAddress.indexOf(",")));
						haveBludger = false;
					}

					System.out.println("Quaffle sent....waiting.."+sendIpAddress.substring(0, sendIpAddress.indexOf(",")));


					server = serverSocket.accept();



					in = new DataInputStream(server.getInputStream());
					System.out.println("After data in");
					dataFromApp = in.readUTF().toString();
					recievedData = dataFromApp.split(";");

					System.out.println("Receieved Data "+dataFromApp);
					System.out.println("Receieved from IP "+((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString());

					if(recievedData[0].equalsIgnoreCase("caughtQuaffle"))
					{

						haveQuaffle = false;	
						System.out.println("caught Quaffle");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println(hashValue+":"+dontHave.get(hashValue).toString());

						if(goalattempt==true)
						{
							System.out.println("Inside the caught quaffle..goal attempted");
							send.SenderThread("missedgoal", trygoalIP);
							goalattempt = false;
							goalKeep.remove(hashValue);
							if(goalKeep.isEmpty())
							{
								System.out.println("Stopped Goal keeping...inside caught quaffle");
								goalkeepFlag = false;
								rthread.stopTimer();
							}
						}

						haveQBall.put(hashValue,dontHave.get(hashValue).toString());
						dontHave.remove(hashValue);

					}
					else if(recievedData[0].equalsIgnoreCase("missedQuaffle"))
					{
						if(goalattempt)
						{

							if(trygoalTeam.equalsIgnoreCase("1"))
							{
								score1+=10;
							}
							else
							{
								score2+=10;
							}	

							send.SenderThread("goal", trygoalIP);
							for (Map.Entry<Integer, String> entry1 : dontHave.entrySet())
							{
								ipaddr = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr);
							}

							for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
							{
								ipaddr = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr);
							}
							haveQuaffle = true;
						}
						else
							haveQuaffle = true;
					}
					else if(recievedData[0].equalsIgnoreCase("caughtBludger"))
					{
						haveBludger = false;
						System.out.println("caught Bludger");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println(hashValue+":"+dontHave.get(hashValue).toString());

						haveBBall.put(hashValue, dontHave.get(hashValue).toString());
						dontHave.remove(hashValue);
					}
					else if(recievedData[0].equalsIgnoreCase("missedBludger"))
					{
						haveBludger = true;
					}
					else if(recievedData[0].equalsIgnoreCase("goalkeep"))
					{

						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();

						System.out.println("ENtered GOal Keep function");
						for (Map.Entry<Integer, String> entry : dontHave.entrySet())
						{
							String temp = entry.getValue().toString();
							System.out.println(" Entry value"+ temp );
							String teamNum = temp.substring(temp.indexOf(",")+1, temp.length());
							System.out.println("teamNum"+ teamNum);

							if(!goalKeep.containsKey(hashValue))
							{
								if(dontHave.containsKey(hashValue) && (teamNum.equalsIgnoreCase("1")))
								{
									System.out.println("team 1 trying to goal keep");
									goalKeep.put(hashValue, entry.getValue().toString());
									goalkeepFlag = true;
									System.out.println("Before starting the timer"+hashValue);
									rthread.startTimer(hashValue);
								}
								else if(dontHave.containsKey(hashValue) && (teamNum.equalsIgnoreCase("2")))
								{
									System.out.println("team 1 trying to goal keep");
									goalKeep.put(hashValue, entry.getValue().toString());
									goalkeepFlag = true;
									System.out.println("Before starting the timer"+hashValue);
									rthread.startTimer(hashValue);
								}
							}	
						}



					}
					else if(recievedData[0].equalsIgnoreCase("trygoal"))
					{
						System.out.println("Entered trygoal");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						System.out.println(x);
						String teamVal;
						String temp;
						hashValue = x.substring(1, x.length()).hashCode();
						trygoalIP = x.substring(1, x.length());

						System.out.println(hashValue);
						String ipstart = haveQBall.get(hashValue).toString();
						teamVal = ipstart.substring(ipstart.indexOf(",")+1, ipstart.length()).toString();

						trygoalTeam = teamVal;

						if(!goalKeep.isEmpty() && goalkeepFlag)
						{
							for(Map.Entry<Integer, String> entry : goalKeep.entrySet())
							{
								String entryvalue = entry.getValue().toString();
								temp = entryvalue.substring(entryvalue.indexOf(",")+1, entryvalue.length());
								String ipaddr = entryvalue.substring(0, entryvalue.indexOf(","));

								if(goalkeepFlag == true && temp != teamVal)
								{
									goalattempt = true;
									send.SenderThread("Quaffle",ipaddr);

								}
								else if(goalkeepFlag == true && temp == teamVal)
								{
									System.out.println(ipstart.substring(ipstart.indexOf(",")+1, ipstart.length()));
									if(ipstart.substring(ipstart.indexOf(",")+1, ipstart.length()).equalsIgnoreCase("1"))
									{
										score1+=10;
									}
									else
									{
										score2+=10;
									}	

									for (Map.Entry<Integer, String> entry1 : dontHave.entrySet())
									{
										ipaddr = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
										send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr);
									}

									for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
									{
										ipaddr = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
										send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr);
									}
									haveQuaffle = true;
								}
								dontHave.put(hashValue,haveQBall.get(hashValue).toString());
								haveQBall.remove(hashValue);

							}
						}
						else
						{
							if(ipstart.substring(ipstart.indexOf(",")+1, ipstart.length()).equalsIgnoreCase("1"))
							{
								score1+=10;
							}
							else
							{
								score2+=10;
							}	

							for (Map.Entry<Integer, String> entry1 : dontHave.entrySet())
							{
								ipaddr = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr);
							}

							for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
							{
								ipaddr = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr);
							}
							haveQuaffle = true;

							dontHave.put(hashValue,haveQBall.get(hashValue).toString());
							haveQBall.remove(hashValue);
						}
					}
					else if(recievedData[0].equalsIgnoreCase("transQuaffle"))
					{
						System.out.println("Inside transquaffle block");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						dontHave.put(hashValue,haveQBall.get(hashValue).toString());
						haveQBall.remove(hashValue);
						System.out.println("End of transquaffle block");
					}
					else if(recievedData[0].equalsIgnoreCase("transBludger"))
					{
						System.out.println("Inside transbludger");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println("trans bludger hashaval:"+hashValue);
						System.out.println("trans bludger value:"+haveBBall.get(hashValue).toString());
						dontHave.put(hashValue,haveBBall.get(hashValue).toString());
						haveBBall.remove(hashValue);
						System.out.println("End of transbludger block");
					}
					else if(recievedData[0].equalsIgnoreCase("haveQuaffle"))
					{
						System.out.println("Inside haveQuaffle block");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						haveQBall.put(hashValue,dontHave.get(hashValue).toString());
						dontHave.remove(hashValue);	
						System.out.println("End of  haveQuaffle block");
					}
					else if(recievedData[0].equalsIgnoreCase("haveBludger"))
					{
						System.out.println("Inside havebludger block");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println("have bludger hashaval:"+hashValue);
						System.out.println("have bludger value:"+dontHave.get(hashValue).toString());
						haveBBall.put(hashValue,dontHave.get(hashValue).toString());
						System.out.println("Inside havebludger block: Added to haveBBall map");
						dontHave.remove(hashValue);	
						System.out.println("End of havebludger block");
						haveBludger = false;
					}
					else if(recievedData[0].equalsIgnoreCase("tryknockout"))
					{
						int hashvalTeam;
						String teamNum;
						String opponentTeam;
						String temp;
						boolean candidateFound = false;


						System.out.println("Inside Tryknockout");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();

						tryknockoutIP = x.substring(1, x.length());
						hashValue = x.substring(1, x.length()).hashCode();
						temp = haveBBall.get(hashValue).toString();
						teamNum = temp.substring(temp.indexOf(",")+1, temp.length());

						if(!candidateFound)
						{
							for (Map.Entry<Integer, String> entry : haveQBall.entrySet())
							{
								String entryValue = entry.getValue().toString();
								opponentTeam = entryValue.substring(entryValue.indexOf(",")+1,entryValue.length());

								System.out.println("Iterating QBall");
								if(!(teamNum.equalsIgnoreCase(opponentTeam)))
								{
									if(ipRssi.containsKey(entryValue))
										rssiVal = ipRssi.get(entryValue);

									//		if((rssiVal < (Integer.parseInt(recievedData[1])+5)) && (rssiVal > (Integer.parseInt(recievedData[1])-5)))
									//		{
									knockoutList.add(entryValue);
									candidateFound = true;
									System.out.println("Candidate found in QBall");
									//		}
									//		else
									//			candidateFound = false;

								}
							}
						}

						if(!candidateFound)
						{
							for (Map.Entry<Integer, String> entry : haveBBall.entrySet())
							{
								String entryValue = entry.getValue().toString();
								opponentTeam = entryValue.substring(entryValue.indexOf(",")+1,entryValue.length());

								System.out.println("Iterating BBall");
								if(!(teamNum.equalsIgnoreCase(opponentTeam)))
								{
									if(ipRssi.containsKey(entryValue))
										rssiVal = ipRssi.get(entryValue);

									//		if((rssiVal < (Integer.parseInt(recievedData[1])+5)) && (rssiVal > (Integer.parseInt(recievedData[1])-5)))
									//		{
									knockoutList.add(entryValue);
									candidateFound = true;
									System.out.println("Candidate  Found in BBall");
									//		}
									//		else
									//			candidateFound = false;

								}
							}
						}

						if(!candidateFound)
						{
							for (Map.Entry<Integer, String> entry : dontHave.entrySet())
							{
								String entryValue = entry.getValue().toString();
								opponentTeam = entryValue.substring(entryValue.indexOf(",")+1,entryValue.length());

								System.out.println("Iterating dontHave");
								if(!(teamNum.equalsIgnoreCase(opponentTeam)))
								{
									if(ipRssi.containsKey(entryValue))
										rssiVal = ipRssi.get(entryValue);

									//		if((rssiVal < (Integer.parseInt(recievedData[1])+5)) && (rssiVal > (Integer.parseInt(recievedData[1])-5)))
									//		{
									knockoutList.add(entryValue);
									candidateFound = true;
									System.out.println("Candidate Found in donthave");
									//		}
									//		else
									//			candidateFound = false;

								}

							}
						}

						if(candidateFound)
						{
							Random randomgen = new Random();
							int index = randomgen.nextInt(knockoutList.size());
							String item = knockoutList.get(index);

							System.out.println("Sending knockout message.."+item.substring(0, item.indexOf(",")));
							send.SenderThread("knockout", item.substring(0, item.indexOf(",")));
						}
						//Drop off the ball
						else
						{
							System.out.println("Candidate not found...");
							haveBludger = true;
						}

						System.out.println("Dropping the ball..");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = x.substring(1, x.length()).hashCode();
						dontHave.put(hashValue, haveBBall.get(hashValue).toString());
						haveBBall.remove(hashValue);
					}
					else if(recievedData[0].equalsIgnoreCase("knockedout"))
					{
						boolean playerKnockedout = false;
						String xip;
						String teamnum_goalkeep;

						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						xip = x.substring(1,x.length());
						hashValue = x.substring(1, x.length()).hashCode();

						System.out.println("Entered knockedout block...");
						if(!playerKnockedout)
						{
							for (Map.Entry<Integer, String> entry : haveQBall.entrySet())
							{
								String entryValue = entry.getValue().toString();
								ipaddr = entry.getValue().substring(0, entry.getValue().indexOf(",")).toString();

								System.out.println("Iterating QBall to put to offline list");
								if(xip.equalsIgnoreCase(ipaddr)){
									System.out.println("Putting to knockoutlist from QBall");
									haveQuaffle = true;
									offlineList.put(hashValue,entryValue);
									haveQBall.remove(hashValue);
									knockoutList.clear();
									playerKnockedout = true;
									break;
								}
							}
						}

						if(!playerKnockedout)
						{
							for (Map.Entry<Integer, String> entry : haveBBall.entrySet())
							{
								String entryValue = entry.getValue().toString();
								ipaddr = entry.getValue().substring(0, entry.getValue().indexOf(",")).toString();

								System.out.println("Iterating BBall to put to offline list");
								if(xip.equalsIgnoreCase(ipaddr)){

									System.out.println("Putting to knockoutlist from BBall");
									haveBludger = true;
									offlineList.put(hashValue,entryValue);
									haveBBall.remove(hashValue);
									knockoutList.clear();
									playerKnockedout = true;
									break;
								}
							}
						}

						if(!playerKnockedout)
						{
							for (Map.Entry<Integer, String> entry : dontHave.entrySet())
							{
								String entryValue = entry.getValue().toString();
								ipaddr = entry.getValue().substring(0, entry.getValue().indexOf(",")).toString();
								String teamNum = entry.getValue().substring(entry.getValue().indexOf(",")+1,entry.getValue().length()).toString();

								if(goalKeep.containsKey(hashValue))
								{
									System.out.println("Goal Keep has the entry");
									goalKeep.remove(entry.getKey());
									if(goalKeep.isEmpty())
									{
										System.out.println("Stop goalkeeping...inside knockedout");
										goalkeepFlag = false;
										rthread.stopTimer();
									}

								}

								System.out.println("IP1:"+xip);
								System.out.println("IP2:"+ipaddr);
								System.out.println("Iterating dontHave to put to offline list");
								if(xip.equalsIgnoreCase(ipaddr))
								{
									System.out.println("Putting to knockoutlist from donthave");
									offlineList.put(hashValue,entryValue);
									System.out.println("Value of offlinelist entry"+offlineList.get(hashValue));
									dontHave.remove(hashValue);
									knockoutList.clear();
									playerKnockedout = true;
									break;
								}
							}

						}
						haveBludger = true;
						send.SenderThread("knockoutsuccess", tryknockoutIP);

					}
					else if(recievedData[0].equalsIgnoreCase("reconnect"))
					{

						//Multiple players can be reconnecting...add to the donthave list after some flag is set
						System.out.println("Inside reconnect...");
						x = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						System.out.println(x);
						hashValue = x.substring(1, x.length()).hashCode();
						System.out.println("hashval reconenct:"+hashValue);
						System.out.println(offlineList.get(hashValue));
						if(!dontHave.containsKey(hashValue))
						{
							dontHave.put(hashValue, offlineList.get(hashValue).toString());
							offlineList.remove(hashValue);
						}

						System.out.println("Reconnecting the knockedout player...");
						send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),x.substring(1, x.length()));

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

