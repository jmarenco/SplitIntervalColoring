package split;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import com.google.ortools.linearsolver.MPSolver.ResultStatus;

public class Model
{
	private Graph _graph;
	private int _colors;
	private MPSolver _solver;
	private MPVariable[] l;
	private MPVariable[] r;
	private MPVariable[][] x;
	private MPVariable z;

	private ResultStatus _status;
	private double _primal;
	private double _dual;
	
	private boolean _solverOutput = true;
	private boolean _verbose = true;
	private boolean _checkSolution = true;

	public Model(Graph graph, int colors)
	{
		_graph = graph;
		_colors = colors;
	}
	
	public void solve()
	{
		createSolver();
		createVariables();
		createDemandConstraints();
		createAntiparallelityConstraints();
		createSymmetryConstraints();
		createOrderingConstraints();
		createObjective();
		solveModel(3600);
		checkSolution();
		closeSolver();
	}
	
	private void createSolver()
	{
	    _solver = MPSolver.createSolver("SCIP");

	    if( _solver == null )
	    	throw new RuntimeException("Solver is null!");
	    
	    if( _solverOutput == true )
			_solver.enableOutput();
	}
	
	private void createVariables()
	{
		l = new MPVariable[_graph.size()];
		r = new MPVariable[_graph.size()];
		x = new MPVariable[_graph.size()][_graph.size()];
		z = _solver.makeNumVar(0, _colors, "z");
		
		for(int i=0; i<_graph.size(); ++i)
		{
			l[i] = _solver.makeNumVar(0, _colors, "l" + i);
			r[i] = _solver.makeNumVar(0, _colors, "r" + i);
		}

		for(int i=0; i<_graph.size(); ++i)
		for(int j=0; j<_graph.size(); ++j)
			x[i][j] = _solver.makeBoolVar("x(" + i + "," + j + ")");
	}
	
	private void createDemandConstraints()
	{
		for(int i=0; i<_graph.size(); ++i) if( _graph.isOriginal(i) )
		{
			int ip = _graph.twinIndex(i);
			
			MPConstraint constr = _solver.makeConstraint(_graph.getDemand(i), _graph.getDemand(i));

			constr.setCoefficient(r[i], 1);
			constr.setCoefficient(l[i], -1);
			constr.setCoefficient(r[ip], 1);
			constr.setCoefficient(l[ip], -1);
		}
	}
	
	private void createAntiparallelityConstraints()
	{
		for(int i=0; i<_graph.size(); ++i)
		for(int j=0; j<_graph.size(); ++j) if( _graph.isEdge(i, j) )
		{
			MPConstraint constr = _solver.makeConstraint(-1000, _colors);
			
			constr.setCoefficient(r[i], 1);
			constr.setCoefficient(l[j], -1);
			constr.setCoefficient(x[i][j], _colors);
		}
	}
	
	private void createSymmetryConstraints()
	{
		for(int i=0; i<_graph.size(); ++i)
		for(int j=i+1; j<_graph.size(); ++j) if( _graph.isEdge(i, j) )
		{
			MPConstraint constr = _solver.makeConstraint(1, 1);
			
			constr.setCoefficient(x[i][j], 1);
			constr.setCoefficient(x[j][i], 1);
		}
	}
	
	private void createOrderingConstraints()
	{
		for(int i=0; i<_graph.size(); ++i)
		{
			MPConstraint constr = _solver.makeConstraint(-1000, 0);
			
			constr.setCoefficient(l[i], 1);
			constr.setCoefficient(r[i], -1);
		}
	}

	private void createObjective()
	{
		MPObjective obj = _solver.objective();
		obj.setCoefficient(z, 1);
	}
	
	private void solveModel(double timeLimit)
	{
		_solver.setTimeLimit((int)(1000 * timeLimit));
		_status = _solver.solve();
		
		if( _status == ResultStatus.OPTIMAL || _status == ResultStatus.FEASIBLE )
		{
			_primal = z.solutionValue();
			_dual = _status == ResultStatus.OPTIMAL ? z.solutionValue() : _solver.objective().bestBound();
		}

		if( _verbose == true )
		{
			System.out.println("Status: " + _status);
			
			if( _status == ResultStatus.OPTIMAL || _status == ResultStatus.FEASIBLE )
			{
				System.out.println("Makespan: " + z.solutionValue());
				System.out.println();
		
				for(int i=0; i<_graph.size(); ++i) if( _graph.isOriginal(i) )
				{
					int ip = _graph.twinIndex(i);
					
					System.out.println(" l(" + i + ")  = " + String.format("%.1f", l[i].solutionValue()) + ", r(" + i + ")  = " + String.format("%.1f", r[i].solutionValue()));
					System.out.println(" l(" + i + "') = " + String.format("%.1f", l[ip].solutionValue()) + ", r(" + i + "') = " + String.format("%.1f", r[ip].solutionValue()));
				}
			}
	
			System.out.println();
		}
	}
	
	private void checkSolution()
	{
		if( _checkSolution == false )
			return;
		
		for(int i=0; i<_graph.size(); ++i) if( _graph.isOriginal(i) )
		{
			int ip = _graph.twinIndex(i);
			double asignado = r[i].solutionValue() - l[i].solutionValue() + r[ip].solutionValue() - l[ip].solutionValue();

			if( Math.abs(asignado - _graph.getDemand(i)) > 0.001)
				throw new RuntimeException("Assigned demand for " + i + " = " + asignado + ", but d(" + i + ") = " + _graph.getDemand(i));
		}
		
		for(int i=0; i<_graph.size(); ++i)
		for(int j=0; j<_graph.size(); ++j) if( _graph.isEdge(i, j) )
		{
			if( r[i].solutionValue() > l[j].solutionValue() + 0.001 && r[j].solutionValue() > l[i].solutionValue() + 0.001 )
				throw new RuntimeException("Intervals of " + i + " and " + j + " do not overlap");
		}
	}
	
	private void closeSolver()
	{
		_solver.clear();
	}
	
	public ResultStatus getStatus()
	{
		return _status;
	}
	
	public double getUB()
	{
		return _primal;
	}
	
	public double getLB()
	{
		return _dual;
	}
}
