package split;

import java.util.ArrayList;

public class Cliquer
{
	private Graph _graph;
	private ArrayList<ArrayList<Integer>> _cliques;
	private ArrayList<Integer> _current;
	
	private int _maxSize;
	
	public Cliquer(Graph graph, int maxSize)
	{
		_graph = graph;
		_maxSize = maxSize;
	}
	
	public static ArrayList<ArrayList<Integer>> findAll(Graph graph)
	{
		return new Cliquer(graph, graph.size()).findAll();
	}
	
	public static ArrayList<ArrayList<Integer>> findAll(Graph graph, int maxSize)
	{
		return new Cliquer(graph, maxSize).findAll();
	}
	
	public ArrayList<ArrayList<Integer>> findAll()
	{
		_current = new ArrayList<Integer>();
		_cliques = new ArrayList<ArrayList<Integer>>();
		
		recursion(0);
		
		return _cliques;
	}
	
	private void recursion(int from)
	{
		if( from == _graph.size() )
		{
			_cliques.add(new ArrayList<Integer>(_current));
		}
		else if( _current.size() < _maxSize )
		{
			for(int i=from+1; i<_graph.size(); ++i) if( neighborOfAllInCurrent(i) )
			{
				_current.add(i);
				recursion(i+1);
				_current.removeLast();
			}
		}
	}
	
	private boolean neighborOfAllInCurrent(int vertex)
	{
		return _current.stream().allMatch(v -> _graph.isEdge(vertex, v));
	}
}
