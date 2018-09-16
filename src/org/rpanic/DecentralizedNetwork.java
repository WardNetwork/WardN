package org.rpanic;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DecentralizedNetwork {

	private static GroupedNeighborPool pool;
	
	public static void init(String address, int port) {
		
		TCPNeighbor entry = null;
		try {
			entry = new TCPNeighbor(InetAddress.getByName(address));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		entry.setPort(port);
		pool = new GroupedNeighborPool(entry,null, 1337, 1);
		
		pool.refillPoolIfNeeded();
		
	}
	
	public static GroupedNeighborPool getPool() {
		return pool;
	}
	
}
