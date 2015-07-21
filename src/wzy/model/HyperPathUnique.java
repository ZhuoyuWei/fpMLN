package wzy.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HyperPathUnique {
	public HyperEdge head;
	public Set<HyperEdge> bodys=new TreeSet<HyperEdge>();
	
	public boolean equals(Object o)
	{
		HyperPathUnique hp=(HyperPathUnique)o;
		if(!this.head.equals(hp.head))
			return false;
		if(this.bodys.size()!=hp.bodys.size())
			return false;
		boolean flag=true;
		Iterator it=this.bodys.iterator();
		while(it.hasNext())
		{
			HyperEdge thishe=(HyperEdge)it.next();
			if(!hp.bodys.contains(thishe))
			{
				flag=false;
				break;
			}
		}
		return flag;
	}
	public int hashcode(Object o)
	{
		HyperPathUnique hpu=(HyperPathUnique)o;
		int sum=hpu.head.hashCode();
		Iterator it=hpu.bodys.iterator();
		while(it.hasNext())
		{
			HyperEdge thishe=(HyperEdge)it.next();
			sum*=31;
			sum+=thishe.hashCode();
		}
		return sum;
	}
	
	public void CopyPathData(HyperPath hp)
	{
		this.head=hp.path.get(hp.path.size()-1);
		this.bodys.clear();
		for(int i=0;i<hp.path.size()-1;i++)
		{
			this.bodys.add(hp.path.get(i));
		}
	}
	
	public HyperPath ChangeToHyperPath()
	{
		HyperPath hp=new HyperPath();
		Iterator it=this.bodys.iterator();
		while(it.hasNext())
		{
			hp.path.add((HyperEdge)it.next());
			hp.path.add(this.head);
		}
		return hp;
	}
	
	
	
}
