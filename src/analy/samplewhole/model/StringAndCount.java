package analy.samplewhole.model;

import java.util.Comparator;

public class StringAndCount implements Comparator{

	public String path;
	public Integer count;
	
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		StringAndCount s1=(StringAndCount)o1;
		StringAndCount s2=(StringAndCount)o2;
		
		if(s1.count>s2.count)
			return -1;
		else if(s1.count<s2.count)
			return 1;
		else
			return 0;

	}
	
	
	
}
