package org.rpanic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.pmw.tinylog.Logger;

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
	
	public void sendVoid(String message) {
		
		message += " ;";
		
		if(socket == null || socket.isClosed())
			openConnection();
				
		try {
			
			if(!socket.isConnected() || socket.isClosed()) {
				Logger.error("Socket is not connected!");
				Thread.dumpStack();
			}
			
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			out.write(message);
			out.flush();
			
			Logger.debug(Calendar.getInstance().get(Calendar.MILLISECOND) + "| SendingVoid: " + message);
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String send(String message) {
		
		message += " ;";
		
		if(socket == null || socket.isClosed())
			openConnection();
				
		try {
			
			if(!socket.isConnected() || socket.isClosed()) {
				Logger.error("Socket is not connected!");
				Thread.dumpStack();
				return null;
			}
			
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			out.write(message);
			out.flush();
			
			Logger.debug(Calendar.getInstance().get(Calendar.MILLISECOND) + "| Sending: " + message);
			
//			System.out.print("Sent: " + message);
			
			InputStream in = socket.getInputStream();
			String reply = recieve(in);
			Logger.debug(Calendar.getInstance().get(Calendar.MILLISECOND) + "| Recieved: " + reply);
			
			
//			System.out.println(" Reply: " + reply);
			
//			socket.close();
//			socket = null;
			
			if(reply != null){
				if(reply.endsWith(";")){
					reply = reply.substring(0, reply.length()-1).trim();
				}else{
					//TODO System.err.println("Reply ends not with semicolon, something is weird here! " + reply);
				}
			}
			
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
