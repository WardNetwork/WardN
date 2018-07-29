package util;

import java.awt.Dimension;

import javax.swing.JFrame;

import org.rpanic.NeighborPool;
import org.rpanic.TCPNeighbor;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

public class NetworkVisualizer {

	public static void visualize(NeighborPool... pools) {
		Layout<String, String> layout;
		BasicVisualizationServer<String,String> vv;
		DirectedSparseMultigraph<String, String> g = new DirectedSparseMultigraph<>();
		
		System.out.println("Started parsing the edges...");
		
		for(NeighborPool pool : pools) {
			
			for(TCPNeighbor n : pool.list) {

				g.addEdge(pool.self.getPort()+""+n.getPort(), pool.self.getPort()+"",n.getPort()+"");
			}
		}
		
		System.out.println("Edges finished, starting rendering");
		
//		Layout<String, String> layout = new DAGLayout<String, String>(g);
//		layout = new ISOMLayout<String, String>(g);
		layout = new CircleLayout<String, String>(g);
		 layout.setSize(new Dimension(1200,800)); // sets the initial size of the spacec
		 // The BasicVisualizationServer<V,E> is parameterized by the edge types
		 vv = new BasicVisualizationServer<String,String>(layout);
		 vv.setPreferredSize(new Dimension(1200,800)); //Sets the viewing area size
		 
		 vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		 
		 JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.getContentPane().add(vv);
		 frame.pack();
		 frame.setVisible(true); 
		
	}
	
}
