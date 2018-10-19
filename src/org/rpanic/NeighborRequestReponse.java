package org.rpanic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import org.pmw.tinylog.Logger;


public class NeighborRequestReponse implements Consumer<Socket>{

	public final GroupedNeighborPool pool;
	
	List<Responser<String, Socket>> responser = new ArrayList<>();
	
	public NeighborRequestReponse(GroupedNeighborPool pool) {
		this.pool = pool;
	}
	
	@Override
	public void accept(Socket s) {
		
		while(s != null) {
		
			try {
				
				if(s.isClosed()) {
					Logger.warn("Recieving Socket is closed!");
					return;
				}

				String request = recieve(s.getInputStream());
				
				acceptResponse(s, request);
				
			}catch(IOException e) {
				//e.printStackTrace();
				return;
			}
		}
		
	}
	
	public synchronized void acceptResponse(Socket s, String request) throws IOException {
		
		Logger.debug("New Request: " + request);
		
		String[] tokenized = request.split(" ");
		
		OutputStreamWriter writer = null;
		
		if(tokenized.length >= 1){
			switch(tokenized[0]){
				
			case "conn":
				writer = new OutputStreamWriter(s.getOutputStream());
				writer.write("connAck ;");
				break;
				
			case "check":
				synchronized(pool.list) {
					int recPort = Integer.parseInt(tokenized[1]);
					
					TCPNeighbor n = new TCPNeighbor(s.getInetAddress());
					n.setPort(recPort);
					if(!pool.list.contains(n) && pool.list.size() < GroupedNeighborPool.MaxPoolSizeCheck) {
						pool.list.add(n);
						Logger.debug("Pool " + pool.listeningPort + " Neighbor added port: " + n.tcpPort);
					}
				}
				writer = new OutputStreamWriter(s.getOutputStream());
				writer.write("checkAck ;");
				break;
				
			case "dnn":{
					TCPNeighbor n = pool.getRandomNeighbor();
					writer = new OutputStreamWriter(s.getOutputStream());
					if(n != null){
						writer.write(String.format("dnnRes %s:%d ;", n.getAddress().getHostAddress(), n.getPort()));
					}else{
						writer.write("dnnRes noNeighbor ;");
					}
					break;
				}
			
			case "br":
				respondToBroadcast(s, request);
				break;
				
			case "addMe":
				Logger.debug("Address.... : " + s.getInetAddress());
				TCPNeighbor n = new TCPNeighbor(s.getInetAddress());
				Logger.debug("Port.... : " + tokenized[1]);
				n.setPort(Integer.parseInt(tokenized[1]));
				pool.addNeighborManually(n);
				Logger.debug("Added Neighbor " + n.toString() + " to pool " + pool.getPort());
				writer = new OutputStreamWriter(s.getOutputStream());
				writer.write("added");
				break;
				
			case "closeConn":
				s.close();
				writer = null;
				break;
				
			case "shard":
				if(tokenized.length > 1){
					long shard = Long.parseLong(tokenized[1]);
					if(shard != pool.getShardId()){
						
					}
				}
				writer = new OutputStreamWriter(s.getOutputStream());
				writer.write("shard " + pool.getShardId()); 
				
			default:{
				List<Responser<String, Socket>> clone;
				synchronized (responser) {
					clone = new ArrayList<>(responser);
				}
				for(Responser<String, Socket> r : clone) {
					if(r.acceptable(tokenized[0])) {
						r.accept(request, s);
					}
				}
			}
			
			}
			if(writer != null) {
				writer.flush();
			}
		}
	}
	
	public String recieve(InputStream input) throws IOException{
		
		Scanner s = new Scanner(input).useDelimiter(";");
		String result = null;
		if(s.hasNext()){
			result = s.next().replace(';', ' ').trim();
		}else{
			throw new IOException("Socket closed");
		}
		return result;
		
//		int read = 0;
//		byte[] arr = null;
//		String s = "";
//		while(read < 1) {
//			TCPNeighbor.iter++;
//			arr = new byte[input.available()];
//			read = input.read(arr);
//			if(new String(arr).endsWith(";")) {
//				s += new String(arr);
//				break;
//			}else {
//				s += new String(arr);
//				read = 0;
//			}
//		}
//		return s.replace(';', ' ').trim();
		
	}
	
	public synchronized void addResponser( Responser<String, Socket> responser) {
//		for(Responser<String, Socket> r : responser) {
			this.responser.add(responser);
//		}
	}
	
	public synchronized boolean removeResponser(Responser<String, Socket> responser) {
		
		for(int i = 0 ; i < this.responser.size() ; i++) {
			Responser<String, Socket> r = this.responser.get(i);
			if(r == responser) { //Check for reference Equality
				this.responser.remove(i);
				return true;
			}
		}
		return false;
		
	}

	private void respondToBroadcast(Socket t, String request) {
		
		if(request.startsWith("br ")){

			request = request.substring(3);
			
			if(pool.recievedBroadcasts.contains(request)){
				return;
			}
			
			pool.broadcast(request);  //TODO I think there is a duplicate Broadcast here - other one is NetworkDAG
			
			try {
				acceptResponse(null, request);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			
		}
	}
	
}
