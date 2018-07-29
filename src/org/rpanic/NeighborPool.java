package org.rpanic;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class NeighborPool {

	public volatile List<TCPNeighbor> list = new CopyOnWriteArrayList<>();
	int listeningPort;
	public TCPNeighbor self;
	
	public static final int MaxNeighborPoolSize = 10;
	public static final int MaxPoolSizeCheck = 10;
	
	public List<String> recievedBroadcasts = new ArrayList<>(); //TODO Besser machen, besseres Konzept, vllt unique IDs vergeben
	
	//Constructor
	
	public NeighborPool(TCPNeighbor entryPoint, TCPNeighbor self, int listeningPort){
		
		if(entryPoint != null)
			list.add(entryPoint);
		this.listeningPort = listeningPort;
		this.self = self;
	}
	
	public void init() {
		
		refillPoolIfNeeded();
		
		startRefreshRoutine();
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
//				if(neighbor.socket.isClosed()){
					return null;
//				}
//				continue;
			}
			
			if(response.startsWith("dnnRes ")) {
				response = response.substring(7);
			
				if(response.equals("noNeighbor")){
					 return null;
				}
				
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
	
	public static final long RefreshLoopSleep = 10000;
	
	/**
	 * starts a Rountine which checks if all nodes are still responding
	 */
	public void startRefreshRoutine(){
		
//		list = Collections.synchronizedList(list);
		
		new Thread(() -> {
			while(true){  //TODO Ausschalten möglich machen
				
				for(TCPNeighbor n : list){
					
					if(!checkConnection(n)) {
						list.remove(n);
					}
					
				}
//				System.out.println("iter" + iter);
				
				refillPoolIfNeeded();
				
				try {
					Thread.sleep(RefreshLoopSleep);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}).start();
		
		//Nach 2 Minuten addMe an alle bekannten Nodes aussenden
		
		
	}
	
	public void addNeighborManually(TCPNeighbor n){
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
		
		System.out.println("Broadcast: " + message + " from port " + listeningPort);
		
		for(TCPNeighbor n : list){
			new Thread(() -> {
				
				n.send("br " + message);
				
			}).start();
		}
		
	}

	public TCPNeighbor getRandomNeighbor() {
		if(list.size() > 0){
			return list.get(new Random().nextInt(list.size()));
		}
		return null;
	}
	
}
