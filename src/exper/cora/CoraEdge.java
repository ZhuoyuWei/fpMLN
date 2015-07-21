package exper.cora;

import java.util.ArrayList;
import java.util.List;

public class CoraEdge {

	int index;
	String rel;
	List<String> entityList=new ArrayList<String>();
	
	public int hashCode()
	{
		int sum=rel.hashCode();
		for(int i=0;i<entityList.size();i++)
		{
			sum*=31+entityList.get(i).hashCode();
		}
		return sum;
	}
	public boolean equals(Object o)
	{
		CoraEdge ce=(CoraEdge)o;
		if(!rel.equals(ce.rel))
			return false;
		if(entityList.size()!=ce.entityList.size())
			return false;
		for(int i=0;i<entityList.size();i++)
		{
			if(!entityList.get(i).equals(ce.entityList.get(i)))
				return false;
		}
		return true;
	}
	
}
