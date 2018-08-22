package org.rpanic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class TCPNeighbor {
	
	public static final int timeOut = 5000;
	
	int tcpPort;
	InetAddress address;
	
	Socket socket = null;

	public TCPNeighbor(InetAddress address) {
		super();
		this.address = address;
	}

	public int getPort() {
		return tcpPort;
	}
	
	public void setPort(int port) {
		this.tcpPort = port;
	}

	public InetAddress getAddress() {
		return address;
	}
	
	public boolean openConnection() {
		
		try {
			socket = new Socket(address, tcpPort);
			
			socket.setSoTimeout(timeOut);
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	public void close() {
		
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace(); //TODO Noch untersuchen ob vllt etwas zu tun ist, ansonsten printStackTrace weg
		}
		
	}
	
	public String getShardId(){
		
		String res = send("shard");
		if(res.startsWith("shard ") && res.length() > 6 /* tests if after the whitespace there is something */){
			return res.split(" ")[1];
		}
		return null;
		
	}
	
	public String send(String message) {
		
		message += " ;";
		
		if(socket == null)
			openConnection();
				
		try {
			
			if(!socket.isConnected() || socket.isClosed()) {
				System.out.println("Socket is not connected!");
				return null;
			}
			
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			out.write(message);
			out.flush();
			
//			System.out.print("Sent: " + message);
			
			InputStream in = socket.getInputStream();
			String reply = recieve(in);
			
//			System.out.println(" Reply: " + reply);
			
//			socket.close();
//			socket = null;
			
			return reply;
			
		} catch (IOException e) {
			System.err.println("Error while sending and recieving a message: " + message + " ToPort: " + tcpPort + " | " + e.getMessage());
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return null;
	}
	
	public static volatile int iter = 0;
	
	public String recieve(InputStream input) throws IOException{
		
		Scanner s = new Scanner(input).useDelimiter(";");
		
		String result = null;
		while(s.hasNext()) {
			String retrieved = s.next();
			if(retrieved != null && !retrieved.equals("")){
				result = retrieved.replace(';', ' ').trim();
				break;
			}else{
				throw new IOException("Socket closed");
			}
		}
		return result;
		
	}
	
	public boolean equals(Object obj) {
		
		if(obj instanceof TCPNeighbor){
			TCPNeighbor n = (TCPNeighbor)obj;
			return n.address.getHostAddress().equalsIgnoreCase(this.address.getHostAddress()) && n.tcpPort == this.tcpPort;
		}
		
		return super.equals(obj);
		
	}
	
	@Override
	public String toString() {
		return address.getHostAddress() + ":" + getPort();
	}
	
	//*******************   Sending queue ***************

	volatile Queue<String> queue = new LinkedList<>();
	
	public String sendQueue(String s) {
		
		queue.add(s);
		
		return "";
		
	}
	
	
}
