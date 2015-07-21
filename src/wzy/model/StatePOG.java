package wzy.model;

import java.util.ArrayList;
import java.util.List;

public class StatePOG {
	public List<Integer> state=new ArrayList<Integer>();
	
	public boolean equals(Object obj)
	{
		if(obj==null)
			return false;
		StatePOG statepog=(StatePOG)obj;
		if(state.size()!=statepog.state.size())
			return false;
		for(int i=0;i<state.size();i++)
		{
			if(!state.get(i).equals(statepog.state.get(i)))
				return false;
		}
		return true;
	}
	
	public int hashCode()
	{
		int sum=0;
		
		for(int i=0;i<state.size();i++)
		{
			sum*=31;
			sum+=state.get(i);
		}
		
		return sum;
	}
	
	public StatePOG copyOne()
	{
		StatePOG newone=new StatePOG();
		newone.state.addAll(this.state);
		return newone;
	}
	
}

