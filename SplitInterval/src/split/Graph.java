package split;

public class Graph
{
	private boolean[][] A;
	private int[] d;
	private int n;
	
	public Graph(int vertices)
	{
		n = vertices;
		A = new boolean[2*n][2*n];
		d = new int[n];
		
		for(int i=0; i<n; ++i)
			A[i][i+n] = A[i+n][i] = true;
	}
	
	public void setDemand(int vertex, int demand)
	{
		d[vertex] = demand;
	}
	
	public int getDemand(int i)
	{
		return d[i];
	}
	
	public void addEdge(int i, int j)
	{
		if( i == j )
			throw new RuntimeException("The loop (" + i + ", " + j + ") is not allowed");
		
		A[i][j] = A[j][i] = true;
		A[i+n][j] = A[j][i+n] = true;
		A[i][j+n] = A[j+n][i] = true;
		A[i+n][j+n] = A[j+n][i+n] = true;
	}
	
	public boolean isEdge(int i, int j)
	{
		return A[i][j];
	}
	
	public boolean isOriginal(int i)
	{
		return i < n;
	}
	
	public boolean isTwin(int i)
	{
		return i >= n;
	}
	
	public int twinIndex(int i)
	{
		return i < n ? i+n : i-n;
	}
	
	public int originalIndex(int i)
	{
		return i < n ? i : i-n;
	}
	
	public int size()
	{
		return A.length;
	}
	
	public Graph neighborsOf(int vertex)
	{
		Graph ret = new Graph(n);
		
		for(int i=0; i<n; ++i)
			ret.setDemand(i, this.getDemand(i));
		
		for(int i=0; i<n; ++i)
		for(int j=0; j<n; ++j) if( this.isEdge(i, j) && this.isEdge(i, vertex) && this.isEdge(j, vertex) )
			ret.addEdge(i, j);
		
		return ret;
	}
	
	public Graph neighborsOf(int vertex1, int vertex2)
	{
		Graph ret = new Graph(n);
		
		for(int i=0; i<n; ++i)
			ret.setDemand(i, this.getDemand(i));
		
		for(int i=0; i<n; ++i)
			for(int j=0; j<n; ++j) if( this.isEdge(i, j) && this.isEdge(i, vertex1) && this.isEdge(j, vertex1)  && this.isEdge(i, vertex2) && this.isEdge(j, vertex2))
			ret.addEdge(i, j);
		
		return ret;
	}
}
