package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MatrixTrans {

	public void ReadAndWrite(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		List<String[]> tokenList=new ArrayList<String[]>();
		int l=0;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			tokenList.add(ss);
			l=ss.length;
		}
		PrintStream ps=new PrintStream(outputfile);
		for(int i=0;i<l;i++)
		{
		
			for(int j=0;j<tokenList.size();j++)
			{
				ps.print(tokenList.get(j)[i]+" ");
			}
			ps.println();
		}
		ps.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		MatrixTrans mt=new MatrixTrans();
		mt.ReadAndWrite("C:\\Users\\Administrator\\Desktop\\temp_re.txt",
				"C:\\Users\\Administrator\\Desktop\\temp_re_trans.txt");
	}
}
