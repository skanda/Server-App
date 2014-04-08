package com.quidditch.server;




import java.io.DataOutputStream;
import java.io.IOException;

import java.io.OutputStream;


import java.net.Socket;
import java.net.UnknownHostException;


public class Server {

	public static void main(String[] args) {
		try{
			
			int port = 4242;
			System.out.println("Server sending message from port 4242");

			
			String sendmessage;
			
			Socket client = new Socket("192.168.1.9", port);
			
			OutputStream outToServer = client.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			
				sendmessage = "Quaffle";
				out.writeUTF(sendmessage);
				client.close();
			}
			catch (UnknownHostException e) {
				e.printStackTrace();
				// TODO: handle exception
			}
			catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			} 	
			//Server is running always. This is done using this while(true) loop
		}
	}

