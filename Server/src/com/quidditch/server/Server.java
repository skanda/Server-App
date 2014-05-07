package com.quidditch.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;



public class Server {

	public static String dataFromApp;
	public static String[] recievedData;

	public static String qValue = "";
	public static String bValue = "";

	public Server(){
	}

	public void SenderThread (String msg, String ipaddr){

		int senderPort = 4242;

		String sendmessage = msg;

		try{
			System.out.println("Sender Thread Running: "+ipaddr+","+senderPort+"===="+msg);
			Socket client = new Socket(ipaddr, senderPort);

			System.out.println("Preparing to send..");
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

		DataInputStream in;
		int helloCounter = 0;
		int score1 = 0;
		int score2 = 0;
		public static boolean haveQuaffle = true;
		public static boolean haveBludger = true;
		public static boolean knockoutOpponent = false;
		public static boolean goalattempt = false;
		public static boolean addGoalKeep = false;


		ServerSocket serverSocket;
		Socket server;
		static HashMap<Integer, String> dontHave = new HashMap<Integer, String>();
		static HashMap<Integer, String> haveQBall = new HashMap<Integer, String>();
		static HashMap<Integer, String> haveBBall = new HashMap<Integer, String>();
		static HashMap<String, Integer> ipRssi = new HashMap<String, Integer>();
		static ArrayList<String> knockoutList = new ArrayList<String>();
		static HashMap<Integer, String> offlineList = new HashMap<Integer, String>();
		static HashMap<Integer, String> goalKeep = new HashMap<Integer, String>();

		int hashValue;
		Object[] values_dontHave_Q;
		Object[] values_dontHave_B;
		Object[] values_dontHave;

		String ipaddr_MapEntry;
		String ipAddress;
		int rssiVal;
		public static String tryknockoutIP; 
		public static String trygoalIP;
		public static String trygoalTeam;
		public static String knockoutTarget="";

		int hashValueB;
		int hashValueQ;
		Server send = new Server();

		public ReceiverThread(){

		}

		class TimeoutTask1 extends TimerTask{

			int timeoutval1 = 0;
			int hashEntry;
			String teamnumber;

			public TimeoutTask1(int hashVal, String teamnum)
			{
				this.hashEntry = hashVal;
				this.teamnumber = teamnum;
			}
			public void run()
			{
				System.out.println("Executing.."+timeoutval1+"for team"+teamnumber);
				timeoutval1++;
				if(timeoutval1 == 15)
				{
					System.out.println("15 sec over before"+hashEntry);

					for (Map.Entry<Integer, String> entry : goalKeep.entrySet())
					{
						System.out.println("Entry Key:"+entry.getKey()+","+"Entry Value"+entry.getValue());
					}

					if(goalKeep.containsKey(hashEntry))
					{
						System.out.println("15 sec over");
						goalKeep.remove(hashEntry);
						if(teamnumber.equalsIgnoreCase("1"))
							timer1.cancel();
						else
							timer2.cancel();
					}
				}

			}
		}

		//Test hashVal or hashEntry ****
		public void startTimer(int hashEntry, String teamNumber){
			System.out.println("In Server, starting goalkeeping");
			System.out.println("Inside starttimer"+hashEntry);

			if(teamNumber.equalsIgnoreCase("1"))
			{
				timer1 = new Timer();
				timer1.schedule(new TimeoutTask1(hashEntry, teamNumber), 0, 1*1000);
			}
			else
			{
				timer2 = new Timer();
				timer2.schedule(new TimeoutTask1(hashEntry, teamNumber), 0, 1*1000);
			}
		}

		public void stopTimer(String teamnum){
			System.out.println("Stopping Timer...");
			if(teamnum.equalsIgnoreCase("1")){
				System.out.println("Stopping timer for team1");
				timer1.cancel();
			}
			else{
				System.out.println("Stopping timer for team2");
				timer2.cancel();
			}
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
							ipaddr_MapEntry = entry.getValue().substring(0, entry.getValue().indexOf(","));
							String initteamNum = entry.getValue().substring(entry.getValue().indexOf(",")+1,entry.getValue().length());
							System.out.println(ipaddr_MapEntry);
							System.out.println("Before sending the scores");
							send.SenderThread("start-"+initteamNum,ipaddr_MapEntry);
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

						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();

						System.out.println(ipAddress.substring(1, ipAddress.length()));

						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						System.out.println(hashValue);

						dontHave.put(hashValue,ipAddress.substring(1, ipAddress.length())+","+String.valueOf((helloCounter%2)+1));

						System.out.println("Before sending the scores");
						send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipAddress.substring(1, ipAddress.length()));
						ipRssi.put(ipAddress.substring(1, ipAddress.length())+","+String.valueOf((helloCounter%2)+1),Integer.parseInt(recievedData[1]));
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

						if(!dontHave.isEmpty()){
							int i=0;
							Random generator = new Random();
							values_dontHave_Q = new Object[dontHave.size()-goalKeep.size()];

							System.out.println("values size"+values_dontHave_Q.length);
							for (Map.Entry<Integer, String> entry : dontHave.entrySet())
							{
								if(!(goalKeep.containsKey(entry.getKey())) && !(haveBBall.containsKey(entry.getKey())))
								{
									System.out.println("Found a value, adding to array");
									values_dontHave_Q[i]=entry.getValue();
									i++;
								}

							}

							System.out.println("after computing values size"+dontHave.size());
							if((dontHave.size()-goalKeep.size()) != 0)
							{
								while(true){
									String sendIpAddressQuaffle;
									sendIpAddressQuaffle = (values_dontHave_Q[generator.nextInt(values_dontHave_Q.length)]).toString();
									System.out.println(sendIpAddressQuaffle);
									if(!knockoutTarget.equalsIgnoreCase(sendIpAddressQuaffle.substring(0, sendIpAddressQuaffle.indexOf(",")))){
										System.out.println("Sending the Quaffle....");
										send.SenderThread("Quaffle",sendIpAddressQuaffle.substring(0, sendIpAddressQuaffle.indexOf(",")));
										hashValueQ = sendIpAddressQuaffle.substring(0, sendIpAddressQuaffle.indexOf(",")).toString().hashCode();
										haveQuaffle = false;
										System.out.println("Q hashvalue:"+hashValueQ);
										qValue = dontHave.get(hashValueQ).toString();
										haveQBall.put(hashValueQ, qValue);
										dontHave.remove(hashValueQ);
										break;
									}
									else
									{
										sendIpAddressQuaffle = (values_dontHave_Q[generator.nextInt(values_dontHave_Q.length)]).toString();
										send.SenderThread("Quaffle",sendIpAddressQuaffle.substring(0, sendIpAddressQuaffle.indexOf(",")));
										hashValueQ = sendIpAddressQuaffle.substring(0, sendIpAddressQuaffle.indexOf(",")).toString().hashCode();
										haveQuaffle = false;
										System.out.println("Q hashvalue:"+hashValueQ);
										qValue = dontHave.get(hashValueQ).toString();
										haveQBall.put(hashValueQ, qValue);
										dontHave.remove(hashValueQ);
										break;
									}
								}
							}
						}
					}
					if(haveBludger)
					{	
						int i=0;
						Random generator = new Random();
						values_dontHave_B = new Object[dontHave.size()-goalKeep.size()];


						for (Map.Entry<Integer, String> entry : dontHave.entrySet())
						{
							if(!(goalKeep.containsKey(entry.getKey())) && !(haveQBall.containsKey(entry.getKey())))
							{
								values_dontHave_B[i]=entry.getValue();
								i++;
							}

						}

						System.out.println("after computing values size in haveBludger"+dontHave.size());
						if((dontHave.size()-goalKeep.size()) != 0)
						{
							while(true){
								String sendIpAddressBludger;
								sendIpAddressBludger = (values_dontHave_B[generator.nextInt(values_dontHave_B.length)]).toString();
								System.out.println(sendIpAddressBludger);
								if(!knockoutTarget.equalsIgnoreCase(sendIpAddressBludger.substring(0, sendIpAddressBludger.indexOf(",")))){
									System.out.println("Sending the Bludger....");
									send.SenderThread("Bludger",sendIpAddressBludger.substring(0, sendIpAddressBludger.indexOf(",")));
									hashValueB = sendIpAddressBludger.substring(0, sendIpAddressBludger.indexOf(",")).toString().hashCode();
									haveBludger = false;
									System.out.println("B hashvalue:"+hashValueB);
									bValue = dontHave.get(hashValueB).toString();
									haveBBall.put(hashValueB, bValue);
									dontHave.remove(hashValueB);
									break;
								}
								else
								{
									sendIpAddressBludger = (values_dontHave_B[generator.nextInt(values_dontHave_B.length)]).toString();
									System.out.println("Sending the Bludger....");
									send.SenderThread("Bludger",sendIpAddressBludger.substring(0, sendIpAddressBludger.indexOf(",")));
									hashValueB = sendIpAddressBludger.substring(0, sendIpAddressBludger.indexOf(",")).toString().hashCode();
									haveBludger = false;
									System.out.println("B hashvalue:"+hashValueB);
									bValue = dontHave.get(hashValueB).toString();
									haveBBall.put(hashValueB, bValue);
									dontHave.remove(hashValueB);
									break;
								}
							}
						}

					}



					if((score1 > 100) || (score2 > 100))
					{
						double randomVal;
						String sendIpAddressSnitch;
						System.out.println("Inside Snitch distribution");
						if(Math.abs(score1 - score2) <= 60)
						{
							//	randomVal = Math.random();
							//	if(randomVal > 0.5)
							//	{
							int i=0;
							Random generator = new Random();
							values_dontHave = new Object[dontHave.size()-goalKeep.size()];

							System.out.println("Generating randomly...");
							for (Map.Entry<Integer, String> entry : dontHave.entrySet())
							{
								if(!(goalKeep.containsKey(entry.getKey())))
								{
									values_dontHave[i]=entry.getValue();
									i++;
								}

							}

							if((dontHave.size()-goalKeep.size()) != 0)
							{
								sendIpAddressSnitch = (values_dontHave[generator.nextInt(values_dontHave.length)]).toString();
								//	hashValue = Integer.parseInt(dontHave.get(keysAsArray.get(r.nextInt(keysAsArray.size()))));
								System.out.println("Sending Snitch to the IP:"+sendIpAddressSnitch);
								//	sendIpAddress = dontHave.get(hashValue);
								System.out.println("Sending the Snitch....");
								send.SenderThread("snitch",sendIpAddressSnitch.substring(0, sendIpAddressSnitch.indexOf(",")));
							//	haveQuaffle = false;
							}
							//		}
						}
					}

					System.out.println("Data sent....waiting..");


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
						qValue = "";
						String tempValue;
						String teamNumber;
						System.out.println("caught Quaffle");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						//	System.out.println(hashValue+":"+dontHave.get(hashValue).toString());


						if(goalattempt==true)
						{
							System.out.println("Inside the caught quaffle..goal attempted");
							send.SenderThread("missedgoal", trygoalIP);
							goalattempt = false;
							tempValue = goalKeep.get(hashValue).toString();
							teamNumber = tempValue.substring(tempValue.indexOf(",")+1, tempValue.length());
							haveQBall.put(hashValue,goalKeep.get(hashValue).toString());
							goalKeep.remove(hashValue);
							rthread.stopTimer(teamNumber);

							System.out.println("Stopped Goal keeping...inside caught quaffle");
						}

						//	haveQBall.put(hashValue,dontHave.get(hashValue).toString());
						//	System.out.println("Size of dont have list before removing"+dontHave.size());
						//	dontHave.remove(hashValue);
						//	System.out.println("Size of dont have list after removing"+dontHave.size());

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
								ipaddr_MapEntry = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr_MapEntry);
							}

							for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
							{
								ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
							}
							for (Map.Entry<Integer, String> entry2 : haveBBall.entrySet())
							{
								ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
							}
							goalattempt = false;
							haveQuaffle = true;
						}
						else
						{
							dontHave.put(hashValueQ,qValue);
							haveQBall.remove(hashValueQ);
							haveQuaffle = true;
						}
					}
					else if(recievedData[0].equalsIgnoreCase("caughtBludger"))
					{
						haveBludger = false;
						bValue = "";
						System.out.println("caught Bludger");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();

					}
					else if(recievedData[0].equalsIgnoreCase("missedBludger"))
					{
						haveBludger = true;
						dontHave.put(hashValueB,bValue);
						haveBBall.remove(hashValueB);
					}
					else if(recievedData[0].equalsIgnoreCase("goalkeep"))
					{

						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						String tempValue = dontHave.get(hashValue).toString();
						String teamname = tempValue.substring(tempValue.indexOf(",")+1, tempValue.length());

						System.out.println("Team trying to goal keep"+teamname);
						System.out.println("ENtered GOal Keep function");

						if(goalKeep.isEmpty())
						{
							addGoalKeep = true;
						}
						else{
							System.out.println("Iterating goalkeep to check if trying to goal keep from same team");
							for(Map.Entry<Integer, String> entry: goalKeep.entrySet())
							{
								String sameteamNum = (entry.getValue().toString()).substring(entry.getValue().toString().indexOf(",")+1, entry.getValue().toString().length());
								if(teamname.equalsIgnoreCase(sameteamNum)){
									System.out.println("Team no. trying to goalkeep"+sameteamNum);
									addGoalKeep = false;
									System.out.println("Before sending sec goalkeeper");
									send.SenderThread("secondgoalkeeper", ipAddress.substring(1, ipAddress.length()));
									break;
								}
								else{
									System.out.println("Not from same team"+sameteamNum);
									addGoalKeep = true;
								}
							}
						}
						System.out.println("After breaking out of second goal keep test"+addGoalKeep);
						if(addGoalKeep){
							if(!goalKeep.containsKey(hashValue))
							{
								if(dontHave.containsKey(hashValue) && (teamname.equalsIgnoreCase("1")))
								{
									System.out.println("team 1 trying to goal keep");
									goalKeep.put(hashValue, dontHave.get(hashValue));

									System.out.println("Before starting the timer 1"+hashValue);
									rthread.startTimer(hashValue,teamname);
								}
								else if(dontHave.containsKey(hashValue) && (teamname.equalsIgnoreCase("2")))
								{
									System.out.println("team 2 trying to goal keep");
									goalKeep.put(hashValue, dontHave.get(hashValue));

									System.out.println("Before starting the timer 2"+hashValue);
									rthread.startTimer(hashValue,teamname);
								}
							}
						}
					}
					else if(recievedData[0].equalsIgnoreCase("trygoal"))
					{
						System.out.println("Entered trygoal");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						System.out.println(ipAddress);
						String teamVal;
						String tempValue;
						String opponentGoalkeeper="None";
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						trygoalIP = ipAddress.substring(1, ipAddress.length());

						System.out.println(hashValue);
						String ipTeamData = haveQBall.get(hashValue).toString();
						teamVal = ipTeamData.substring(ipTeamData.indexOf(",")+1, ipTeamData.length()).toString();

						trygoalTeam = teamVal;

						if(!goalKeep.isEmpty())
						{
							for(Map.Entry<Integer, String> entry : goalKeep.entrySet())
							{
								String entryvalue = entry.getValue().toString();
								tempValue = entryvalue.substring(entryvalue.indexOf(",")+1, entryvalue.length());
								ipaddr_MapEntry = entryvalue.substring(0, entryvalue.indexOf(","));

								if( tempValue != teamVal)
								{
									opponentGoalkeeper = ipaddr_MapEntry;
									goalattempt = true;


								}

							}
							if(!(opponentGoalkeeper.equalsIgnoreCase("None")))
							{
								send.SenderThread("Quaffle",opponentGoalkeeper);
							}
							else
							{
								System.out.println(ipTeamData.substring(ipTeamData.indexOf(",")+1, ipTeamData.length()));
								if(ipTeamData.substring(ipTeamData.indexOf(",")+1, ipTeamData.length()).equalsIgnoreCase("1"))
								{
									score1+=10;
								}
								else
								{
									score2+=10;
								}	

								for (Map.Entry<Integer, String> entry1 : dontHave.entrySet())
								{
									ipaddr_MapEntry = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
									send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr_MapEntry);
								}

								for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
								{
									ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
									send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
								}
								for (Map.Entry<Integer, String> entry2 : haveBBall.entrySet())
								{
									ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
									send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
								}
								haveQuaffle = true;
							}
							dontHave.put(hashValue,haveQBall.get(hashValue).toString());
							haveQBall.remove(hashValue);

						}
						else
						{
							if(ipTeamData.substring(ipTeamData.indexOf(",")+1, ipTeamData.length()).equalsIgnoreCase("1"))
							{
								score1+=10;
							}
							else
							{
								score2+=10;
							}	

							for (Map.Entry<Integer, String> entry1 : dontHave.entrySet())
							{
								ipaddr_MapEntry = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr_MapEntry);
							}

							for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
							{
								ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
							}
							for (Map.Entry<Integer, String> entry2 : haveBBall.entrySet())
							{
								ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
								send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
							}
							
							haveQuaffle = true;

							dontHave.put(hashValue,haveQBall.get(hashValue).toString());
							haveQBall.remove(hashValue);
						}
					}
					else if(recievedData[0].equalsIgnoreCase("transQuaffle"))
					{
						System.out.println("Inside transquaffle block");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						dontHave.put(hashValue,haveQBall.get(hashValue).toString());
						haveQBall.remove(hashValue);
						System.out.println("End of transquaffle block");
					}
					else if(recievedData[0].equalsIgnoreCase("transBludger"))
					{
						System.out.println("Inside transbludger");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						System.out.println("trans bludger hashaval:"+hashValue);
						System.out.println("trans bludger value:"+haveBBall.get(hashValue).toString());
						dontHave.put(hashValue,haveBBall.get(hashValue).toString());
						haveBBall.remove(hashValue);
						System.out.println("End of transbludger block");
					}
					else if(recievedData[0].equalsIgnoreCase("haveQuaffle"))
					{
						System.out.println("Inside haveQuaffle block");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						haveQBall.put(hashValue,dontHave.get(hashValue).toString());
						dontHave.remove(hashValue);	
						System.out.println("End of  haveQuaffle block");
					}
					else if(recievedData[0].equalsIgnoreCase("haveBludger"))
					{
						System.out.println("Inside havebludger block");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
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

						String teamNum;
						String opponentTeam;
						String tempValue;
						boolean candidateFound = false;


						System.out.println("Inside Tryknockout");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();

						tryknockoutIP = ipAddress.substring(1, ipAddress.length());
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						tempValue = haveBBall.get(hashValue).toString();
						teamNum = tempValue.substring(tempValue.indexOf(",")+1, tempValue.length());

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

									if((rssiVal < (Integer.parseInt(recievedData[1])+15)) && (rssiVal > (Integer.parseInt(recievedData[1])-15)))
									{
										knockoutList.add(entryValue);
										candidateFound = true;
										System.out.println("Candidate found in QBall");
									}
									else
										candidateFound = false;

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

									if((rssiVal < (Integer.parseInt(recievedData[1])+15)) && (rssiVal > (Integer.parseInt(recievedData[1])-15)))
									{
										knockoutList.add(entryValue);
										candidateFound = true;
										System.out.println("Candidate  Found in BBall");
									}
									else
										candidateFound = false;

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

									if((rssiVal < (Integer.parseInt(recievedData[1])+15)) && (rssiVal > (Integer.parseInt(recievedData[1])-15)))
									{
										knockoutList.add(entryValue);
										candidateFound = true;
										System.out.println("Candidate Found in donthave");
									}
									else
										candidateFound = false;

								}

							}
						}

						if(candidateFound)
						{
							Random randomgen = new Random();
							int index = randomgen.nextInt(knockoutList.size());
							String item = knockoutList.get(index);

							System.out.println("Sending knockout message.."+item.substring(0, item.indexOf(",")));
							knockoutTarget = item.substring(0, item.indexOf(","));
							send.SenderThread("knockout", item.substring(0, item.indexOf(",")));
						}
						//Drop off the ball
						else
						{
							System.out.println("Candidate not found...");
							haveBludger = true;
						}

						System.out.println("Dropping the ball..");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						dontHave.put(hashValue, haveBBall.get(hashValue).toString());
						haveBBall.remove(hashValue);
					}
					else if(recievedData[0].equalsIgnoreCase("knockedout"))
					{
						boolean playerKnockedout = false;
						String knockedoutIPaddr;

						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						knockedoutIPaddr = ipAddress.substring(1,ipAddress.length());
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();

						//	knockoutTarget="";
						System.out.println("Entered knockedout block...");
						if(!playerKnockedout && haveQBall.containsKey(hashValue))
						{	
							System.out.println("Checking in QBAll list");
							String tempValue = haveQBall.get(hashValue).toString();
							System.out.println("Got tempValue");
							String ipaddr = tempValue.substring(0, tempValue.indexOf(","));
							System.out.println("Got ipaddr");

							if(knockedoutIPaddr.equalsIgnoreCase(ipaddr)){
								System.out.println("Putting to knockoutlist from QBall");
								haveQuaffle = true;
								offlineList.put(hashValue,tempValue);
								haveQBall.remove(hashValue);
								knockoutList.clear();
								playerKnockedout = true;
							}
						}

						if(!playerKnockedout && haveBBall.containsKey(hashValue))
						{
							System.out.println("Checking in BBAll list");
							String tempValue = haveBBall.get(hashValue).toString();
							String ipaddr = tempValue.substring(0, tempValue.indexOf(","));

							if(knockedoutIPaddr.equalsIgnoreCase(ipaddr)){

								System.out.println("Putting to knockoutlist from BBall");
								haveBludger = true;
								offlineList.put(hashValue,tempValue);
								haveBBall.remove(hashValue);
								knockoutList.clear();
								playerKnockedout = true;
							}
						}

						if(!playerKnockedout && dontHave.containsKey(hashValue))
						{
							System.out.println("Checking in dontHave list");
							String tempValue = dontHave.get(hashValue).toString();
							String ipaddr = tempValue.substring(0, tempValue.indexOf(","));
							String teamNum = tempValue.substring(tempValue.indexOf(",")+1, tempValue.length());

							if(goalKeep.containsKey(hashValue))
							{
								System.out.println("Goal Keep has the entry");
								goalKeep.remove(hashValue);
								rthread.stopTimer(teamNum);

							}

							System.out.println("IP1:"+knockedoutIPaddr);
							System.out.println("IP2:"+ipaddr);
							//	System.out.println("Iterating dontHave to put to offline list");
							if(knockedoutIPaddr.equalsIgnoreCase(ipaddr))
							{
								System.out.println("Putting to knockoutlist from donthave");
								offlineList.put(hashValue,tempValue);
								System.out.println("Value of offlinelist entry"+offlineList.get(hashValue));
								dontHave.remove(hashValue);
								knockoutList.clear();
								playerKnockedout = true;
							}
						}
						haveBludger = true;
						send.SenderThread("knockoutsuccess", tryknockoutIP);

					}
					else if(recievedData[0].equalsIgnoreCase("reconnect"))
					{

						//Multiple players can be reconnecting...add to the donthave list after some flag is set
						System.out.println("Inside reconnect...");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						System.out.println(ipAddress);
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();
						System.out.println("hashval reconenct:"+hashValue);
						System.out.println(offlineList.get(hashValue));
						if(!dontHave.containsKey(hashValue))
						{
							dontHave.put(hashValue, offlineList.get(hashValue).toString());
							offlineList.remove(hashValue);
						}
						knockoutTarget="";

						System.out.println("Reconnecting the knockedout player...");
						send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipAddress.substring(1, ipAddress.length()));

					}
					else if(recievedData[0].equalsIgnoreCase("caughtsnitch"))
					{
						System.out.println("Inside Caught Snitch...");
						ipAddress = ((InetSocketAddress)server.getRemoteSocketAddress()).getAddress().toString();
						hashValue = ipAddress.substring(1, ipAddress.length()).hashCode();

						String teamval = dontHave.get(hashValue);
						String teamnum = teamval.substring(teamval.indexOf(",")+1, teamval.length());

						if(teamnum.equalsIgnoreCase("1"))
						{
							score1+=150;
						}
						else
						{
							score2+=150;
						}	

						for (Map.Entry<Integer, String> entry1 : dontHave.entrySet())
						{
							ipaddr_MapEntry = entry1.getValue().substring(0, entry1.getValue().indexOf(","));
							send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2), ipaddr_MapEntry);
							send.SenderThread("gameover", ipaddr_MapEntry);
						}

						for (Map.Entry<Integer, String> entry2 : haveQBall.entrySet())
						{
							ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
							send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
							send.SenderThread("gameover", ipaddr_MapEntry);
						}
						for (Map.Entry<Integer, String> entry2 : haveBBall.entrySet())
						{
							ipaddr_MapEntry = entry2.getValue().substring(0, entry2.getValue().indexOf(","));
							send.SenderThread(String.valueOf(score1)+":"+String.valueOf(score2),ipaddr_MapEntry);
							send.SenderThread("gameover", ipaddr_MapEntry);
						}
						server.close();
						break;
					}


					System.out.println("Closing one of socket connection");


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

