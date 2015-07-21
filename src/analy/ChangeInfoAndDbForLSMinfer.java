package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class ChangeInfoAndDbForLSMinfer {
	
	public void ReadAndChangeDb(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			String buffer=null;
			PrintStream ps=new PrintStream(outputfile);
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] sss=buffer.split("[\\s]+");
				String[] ss=sss[0].split("[()]+");
				String rel=ss[0].replace('_', 'r');
				String entities=ss[1].replace('_', 'E');
				ps.println(rel+"("+entities+")\t"+sss[1]+"\t"+sss[2]+"\t"+sss[3]);
			}
			ps.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void ReadAndChangeInfo(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			String buffer=null;
			PrintStream ps=new PrintStream(outputfile);
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				String rel=ss[0].replace('_', 'r');
				String entities=ss[1].replace('_', 'E');
				ps.println(rel+"("+entities+")");
			}
			ps.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public static void main(String[] args)
	{
		ChangeInfoAndDbForLSMinfer ciadfl=new ChangeInfoAndDbForLSMinfer();
		ciadfl.ReadAndChangeDb("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet_no\\test.query",
				"C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet_no\\test.query.1");
	}

}
