package org.rpanic;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.NetworkVisualizer;

public class Mian {

	public static void main(String[] args) throws Exception {
		
		System.out.println("Start");
		TCPNeighbor n = new TCPNeighbor(InetAddress.getByName("127.0.0.1"));
		n.setPort(1337);
		
		NeighborPool pool = new NeighborPool(null, n, 1337);
		
		ListenerThread.startListeningThreadTcp(1337, new NeighborRequestReponse(pool));
		
//		n.startListening(new NeighborRequestReponse(pool));

		pool.init();
		
		List<TCPNeighbor> list = new ArrayList<>();
		List<NeighborPool> pools = new ArrayList<>();
		
		pools.add(pool);
		list.add(n);
		
//		TCPNeighbor n3 = new TCPNeighbor(InetAddress.getByName("127.0.0.1"));
//		n3.setPort(1338);
//		
//		NeighborPool pool3 = new NeighborPool(list.get(new Random().nextInt(list.size())), n3, 1338);
//		
//		ListenerThread.startListeningThreadTcp(1338, new NeighborRequestReponse(pool3));
//		
//		pool3.init();
//		
//		pool.list.add(n3);
//		
//		list.add(n3);
//		pools.add(pool3);
		 
		for(int i = 1 ; i < 40 ; i++) {
			
			System.out.println("New Pool: " + (1337+i));
			
			TCPNeighbor n2 = new TCPNeighbor(InetAddress.getByName("127.0.0.1"));
			n2.setPort(1337 + i);
			
			NeighborPool pool2 = new NeighborPool(list.get(new Random().nextInt(list.size())), n2, 1337 + i);
			
			ListenerThread.startListeningThreadTcp(1337 + i, new NeighborRequestReponse(pool2));
			
			pool2.init();
			
			list.add(n2);
			pools.add(pool2);
			
			Thread.sleep(500l);
		}
		
		pool.broadcast("br testBr");
		
		Thread.sleep(50000L);
		
		Collections.sort(NeighborRequestReponse.ports,(o1, o2) -> o1.compareTo(o2));
		
		System.out.println("Test broadcast reciever by: " + Arrays.toString(NeighborRequestReponse.ports.toArray()));
		
		System.err.println("Current Neighbors on pool 1: " + Arrays.toString(pool.list.toArray()));
		
		System.out.println("Read iterations: " + TCPNeighbor.iter);
		
		NetworkVisualizer.visualize(pools.toArray(new NeighborPool[pools.size()]));
		
//		if(args.length > 0 && args[0].startsWith("s")){
//			server();
//		}else{
//			client();
//		}
		
		
		
	}
	
	
}
