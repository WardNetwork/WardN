package org.rpanic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

import org.pmw.tinylog.Logger;

public class ListenerThread {
	
	public static void startListeningThreadTcp(int port, Consumer<Socket> consumer){
		
		new Thread(() -> {
		
			ServerSocket ss ;
			try {
				ss = new ServerSocket(port);
			} catch (IOException e) {
				Logger.error("Serversocket couldn´t be opened. Port: " + port);
				e.printStackTrace();
				return;
			}
			
			Logger.info("ListenerThread listening on port " + port + " and waiting for connection");
			
			while(!ss.isClosed()) {
				
				try {
					
					Socket s = ss.accept();
					
					new Thread(() -> {
						
						consumer.accept(s);
						
					}).start();
				
				}catch(IOException e) {
					e.printStackTrace();
				}
				
			}
			
		}).start();
	}
	
}
