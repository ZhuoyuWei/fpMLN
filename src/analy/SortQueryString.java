package analy;

import java.util.Comparator;

public class SortQueryString implements Comparator {

	public String str;
	public Double value;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		SortQueryString sqs1=(SortQueryString)o1;
		SortQueryString sqs2=(SortQueryString)o2;
		if(sqs1.value>sqs2.value)
			return -1;
		else if(sqs1.value<sqs2.value)
			return 1;
		else
			return 0;
	}
	
}
