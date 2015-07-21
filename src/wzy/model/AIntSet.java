package wzy.model;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class AIntSet {

	public Set<Integer> set=new TreeSet();
	
	public int hashCode()
	{
		int sum=0;
		Iterator it=set.iterator();
		while(it.hasNext())
		{
			Integer index=(Integer)it.next();
			sum*=31;
			sum+=index;
		}
		return sum;
	}
	
	public boolean equals(Object obj)
	{
		AIntSet ais=(AIntSet)obj;
		if(ais.set.size()!=set.size())
			return false;
		Iterator it=ais.set.iterator();
		boolean flag=true;
		while(it.hasNext())
		{
			Integer index=(Integer)it.next();
			if(!set.contains(index))
			{
				flag=false;
				break;
			}
		}
		return flag;
	}
}
