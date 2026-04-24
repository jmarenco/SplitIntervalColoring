package split;

import java.util.Random;

public class Benchmark
{
	public static Graph erdosRenyi(int vertices, double density, int minDemand, int maxDemand, int seed)
	{
		Graph ret = new Graph(vertices);
		Random random = new Random(seed);
		
		for(int i=0; i<vertices; ++i)
			ret.setDemand(i, minDemand + random.nextInt(maxDemand - minDemand + 1));
		
		int edges = (vertices * (vertices-1)) / 2;
		
		for(int k=0; k<edges; ++k)
		{
			int i = random.nextInt(vertices-1);
			int j = i + random.nextInt(vertices-i-1) + 1;
			
			ret.addEdge(i, j);
		}
		
		return ret;
	}
}
