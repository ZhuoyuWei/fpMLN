package journal.pre;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class FilterNoForCora {

	
	
	public void FilterCora(String inputfile,String outfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream ps=new PrintStream(outfile);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				if(buffer.charAt(0)=='!')
					continue;
				ps.println(buffer);
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
	
	
	
	public void FilterTestDB(String inputfile,String factfile,String queryfile,String query)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream psfact=new PrintStream(factfile);
			PrintStream psquery=new PrintStream(queryfile);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				String rel=ss[0].replaceAll("!", "");
				if(rel.equals(query))
				{
					psquery.println(buffer);
				}
				else
				{
					psfact.println(buffer);
				}
			}
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
		FilterNoForCora fc=new FilterNoForCora();
		//fc.FilterCora("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\cora_data\\cora.er_new\\miss-5.db"
				//, "C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\miss-5.db");
		fc.FilterTestDB("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\cora_data\\cora.er_new\\coraSepFixedhwng.5.db", 
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\cora_data\\SameVenue\\cora_no_query_test\\coraSepFixedhwng.5.db",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\cora_data\\SameVenue\\cora_query\\cora_5.query", "SameVenue");
		
	}
	
}
