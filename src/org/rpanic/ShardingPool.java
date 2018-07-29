package org.rpanic;

public class ShardingPool extends NeighborPool {

	String shard;
	
	public ShardingPool(TCPNeighbor entryPoint, TCPNeighbor self, int listeningPort, String shardId) {
		super(entryPoint, self, listeningPort);
		this.shard = shardId;
	}

	@Override
	public void init() {
		
		lookForEntry();

		startRefreshRoutine();
	}

	private void lookForEntry() {
		
		
		
	}
	
	

}
