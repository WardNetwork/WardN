package org.rpanic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

import org.pmw.tinylog.Logger;


public class NeighborRequestReponse implements Consumer<Socket>{

	GroupedNeighborPool pool;
	
	List<Responser<String, Socket>> responser = new ArrayList<>();
	
	public NeighborRequestReponse(GroupedNeighborPool pool) {
		this.pool = pool;
	}
	
	@Override
	public void accept(Socket s) {
		
		while(s != null) {
		
			try {
				
				if(s.isClosed()) {
					System.out.println("Recieving Socket is closed!");
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
	
	static List<Integer> ports = new ArrayList<>(); //TODO DEBUG
	
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
				
			//DEBUG
			case "testBr":
				ports.add(pool.listeningPort);
				//DEBUG
				break;
				
			case "addMe":
				Logger.debug("Address.... : " + s.getInetAddress());
				TCPNeighbor n = new TCPNeighbor(s.getInetAddress());
				Logger.debug("Port.... : " + tokenized[1]);
				n.setPort(s.getLocalPort());
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
				writer = new OutputStreamWriter(s.getOutputStream());
				writer.write("shard " + pool.getShardId()); 
				
			default:{
				for(Responser<String, Socket> r : responser) {
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
	
	public void addResponser( Responser<String, Socket> responser) {
//		for(Responser<String, Socket> r : responser) {
			this.responser.add(responser);
//		}
	}

	private void respondToBroadcast(Socket t, String request) {
		
		if(request.startsWith("br ")){
			
//			if(pool.recievedBroadcasts.size() > 0) { //TODO Check ich nicht warum ich das geschrieben hab
//				return;
//			}

			request = request.substring(3);
			
			if(pool.recievedBroadcasts.contains(request)){
				return;
			}
			
			pool.broadcast(request); 
			
			try {
				acceptResponse(null, request);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			
			
		}
	}
	
}
