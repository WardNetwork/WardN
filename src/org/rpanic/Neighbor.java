package org.rpanic;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class Neighbor {

	InetAddress address;
	
	int port;
	
	Socket socket;
	
	public Neighbor(InetAddress address){
		this.address = address;
	}
	
	public boolean openConnection(){
		
		if(port == 0){
			throw new IllegalArgumentException("port not set");
		}
		
		try {
			socket = new Socket(address, port);
			
			OutputStream out = socket.getOutputStream();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.write("conn\n".getBytes());
			out.flush();
			byte[] arr = new byte[in.available()];
			in.readFully(arr);
			String response = new String(arr);
			if(response.equals("connAck")){
				return true;
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	public void startListening(Consumer<Socket> consumer){
		
		new Thread(() -> {
			try (ServerSocket ss = new ServerSocket(port)){
				
				socket = ss.accept();
				new Thread(() -> {
					consumer.accept(socket);
				}).start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	public void setPort(int port){
		this.port = port;
	}

	public int getPort(){
		return port;
	}
	
	public String send(String req){
		
		if(socket == null)
			openConnection();
		
		try{
		
			OutputStream out = socket.getOutputStream();
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.write(req.getBytes());
			byte[] arr = new byte[in.available()];
			in.readFully(arr);
			return new String(arr);
		
		}catch(EOFException e){
			return null;
		}catch(IOException e){
			e.printStackTrace();
		}
		return null;
	}
	
}
