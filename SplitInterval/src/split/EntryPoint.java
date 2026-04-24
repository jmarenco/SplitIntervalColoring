package split;

import com.google.ortools.Loader;

public class EntryPoint
{
	public static void main(String[] args)
	{
	    Loader.loadNativeLibraries();

//	    Graph graph = new Graph(3);
//		graph.addEdge(0, 1);
//		graph.addEdge(0, 2);
//		graph.setDemand(0, 2);
//		graph.setDemand(1, 2);
//		graph.setDemand(2, 2);
		
		Model model = new Model(Benchmark.erdosRenyi(20, 0.4, 1, 5, 0), 30);
		model.solve();
	}
}
