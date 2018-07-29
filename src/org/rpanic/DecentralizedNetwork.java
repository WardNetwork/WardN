package org.rpanic;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DecentralizedNetwork {

	private static NeighborPool pool;
	
	public static void init(String address, int port) {
		
		TCPNeighbor entry = null;
		try {
			entry = new TCPNeighbor(InetAddress.getByName(address));
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		entry.setPort(port);
		pool = new NeighborPool(entry,null, 1337);
		
		pool.refillPoolIfNeeded();
		
	}
	
	public static NeighborPool getPool() {
		return pool;
	}
	
}
