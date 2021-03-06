package org.rpanic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.pmw.tinylog.Logger;

public class GroupedNeighborPool {

	//TODO noch nicht fertig, mitschicken der shardId usw. noch nicht implementiert
	
	public volatile List<TCPNeighbor> list = new CopyOnWriteArrayList<>();
	int listeningPort;
	public TCPNeighbor self;
	private long shardId;
	
	public static final int MaxNeighborPoolSize = 3;
	public static final int MaxPoolSizeCheck = 3;
	
	public List<String> recievedBroadcasts = new ArrayList<>(); //TODO Besser machen, besseres Konzept, vllt unique IDs vergeben
	
	//Constructor
	
	public GroupedNeighborPool(TCPNeighbor entryPoint, TCPNeighbor self, int listeningPort, long shardId){
		
		if(entryPoint != null)
			list.add(entryPoint);
		this.listeningPort = listeningPort;
		this.self = self;
		this.shardId = shardId;
	}
	
	public void init() {
		
		list.forEach(n -> subscribeTo(n)); //Subscribe to entry Neighbour
		
		refillPoolIfNeeded();
		
		startRefreshRoutine();
	}
	
	public void subscribeTo(TCPNeighbor n){ //TODO Not tested!!
		
		String res = n.send("addMe " + listeningPort);
		if(res == null || !res.equals("added")){
			new Thread(() -> {
				
				boolean match = false;
				while(!match){
					
					try {
						Thread.sleep(1000L);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("Exiting AddingThread");
						break;
					}
					
					match = list.stream().anyMatch(x -> x.send("addMe " + this.listeningPort).equals("added"));
					
				}
				Logger.debug("Added!");
				
			}, "AddingThread").start();
		}else{
			Logger.debug("Not Added!");
		}
		
	}
	
	//Pool Management
	
	public TCPNeighbor lookForNewNeighbor(){
		
		TCPNeighbor neighbor = null;
		
		while(neighbor == null/* || list.contains(neighbor)*/){
			
			if(list.isEmpty())
				break;
			
			int r = new Random().nextInt(list.size());
			neighbor = list.get(r);
			
			String response = neighbor.send("dnn");
			if(response == null) {
//					if(neighbor.socket.isClosed()){
					return null;
//					}
//					continue;
			}
			
			if(response.startsWith("dnnRes ")) {
				response = response.substring(7);
			
				if(response.equals("noNeighbor")){
					 return null;
				}
				
//				arr = response.split(";");
//				
//				String foreignShardId = arr.length > 1 ? arr[1] : "undefined";
//				
//				if(foreignShardId.equals(shardId)){
				
				String[] arr = response.split(":");
					
					if(arr.length < 2){
						return null;
					}
					
					try {
						TCPNeighbor zmq = new TCPNeighbor(InetAddress.getByName(arr[0]));
		
						zmq.setPort(Integer.parseInt(arr[1]));
						return zmq;
						
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			//}
		}
		
		return null;
		
	}
	
	public void refillPoolIfNeeded(){
		
		for(int i = 0 ; list.size() > 0 && list.size() < MaxNeighborPoolSize && i < (MaxNeighborPoolSize - list.size()) ; i++){
			TCPNeighbor n = lookForNewNeighbor();
			if(n != null && !list.contains(n) && !n.equals(self)) {
				System.out.println("Pool " + listeningPort + " Adding new Neighbor: " + n.getAddress().getHostAddress() + ":" + n.getPort());
				list.add(n);
			}
		}
		
	}
	
	public static final long RefreshLoopSleep = 11000;
	
	/**
	 * starts a Rountine which checks if all nodes are still responding
	 */
	public void startRefreshRoutine(){
		
//			list = Collections.synchronizedList(list);
		
		new Thread(() -> {
			while(true){  //TODO Ausschalten m�glich machen
				
				for(TCPNeighbor n : list){
					
					if(!checkConnection(n)) {
						list.remove(n);
					}
					
				}
//					System.out.println("iter" + iter);
				
				//refillPoolIfNeeded();
				
				try {
					Thread.sleep(RefreshLoopSleep);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});//TODO temp .start();
		
		//Nach 2 Minuten addMe an alle bekannten Nodes aussenden
		
	}
	
	public void switchToNewShardId(long shardId){
		
		this.shardId = shardId;
		
		for(TCPNeighbor n : list){
			
			String res = n.send("shard " + shardId);
			if(!res.equals("shard " + shardId)){
				list.remove(n);
			}
		}
		refillPoolIfNeeded();
	}
	
	public void removeNeighborManually(TCPNeighbor n){
		list.remove(n);
	}
	
	public void addNeighborManually(TCPNeighbor n){
		if(!list.contains(n))
			list.add(n);
	}
	
	public boolean checkConnection(TCPNeighbor n) {
		String res = n.send("check " + listeningPort);
		if(res != null && res.equals("checkAck")){
			return true;
		}
		return false;
	}
	
	//Communication
	
	/**
	 * Broadcasts to the whole network asynchronosly
	 */
	public void broadcast(String message){
		
		recievedBroadcasts.add(message);
		
		Logger.debug("Broadcast: " + message + " from port " + listeningPort);
		
		for(TCPNeighbor n : list){
			new Thread(() -> {
				
				n.sendVoid("br " + message);
				
			}).start();
		}
		
	}

	public TCPNeighbor getRandomNeighbor() {
		if(list.size() > 0){
			return list.get(new Random().nextInt(list.size()));
		}
		return null;
	}
	
	public int getPort(){
		return listeningPort;
	}
	
	public long getShardId(){
		return shardId;
	}
	
}
