package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class temp_calculate {

	public static void CalculateAverage(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		double sum=0.;
		int count=0;
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			sum+=Double.parseDouble(buffer);
			count++;
		}
		System.out.println((sum/count));
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.print("e-3\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal1e_3\\res\\cll.result");
		System.out.print("e-2\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal1e_2\\res\\cll.result");	
		System.out.print("e-1\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal1e_1\\res\\cll.result");
		System.out.print("1\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal1\\res\\cll.result");	
		System.out.print("10\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal10\\res\\cll.result");	
		System.out.print("100\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal100\\res\\cll.result");
		System.out.print("1000\t");
		CalculateAverage("C:\\Users\\Administrator\\Documents\\experiments2014\\Cequal1000\\res\\cll.result");			
	}
	
}
