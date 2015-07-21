package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class WordNetAnalyze {

	public Set<String> constanceSet=new HashSet<String>();
	int linecount=0;
	
	public Set<String> proSet=new HashSet<String>();
	public Set<String> perSet=new HashSet<String>();
	
	
	public void ReadAndCount(String filename)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				String[] tokens=ss[1].split(",");
				//if(!ss[0].contains("project"))
				for(int i=0;i<tokens.length;i++)
				{
					constanceSet.add(tokens[i]);
					//perSet.add(tokens[i]);
				}
			//	else
				//{
					//proSet.add(tokens[0]);
					//perSet.add(tokens[1]);
					
				//}
				linecount++;
			}
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
		WordNetAnalyze wna=new WordNetAnalyze();
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet\\train.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet\\test.db");		
		
		
	/*	wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\ai.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\graphics.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\language.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\systems.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\theory.db");		*/
		
	/*	wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.1.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.2.db");		
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.3.db");
		wna.ReadAndCount("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.4.db");	*/
		
		System.out.println(wna.constanceSet.size()+"\t"+wna.linecount);
		//System.out.println(wna.proSet.size()+"\t"+wna.perSet.size());
	}
	
}
