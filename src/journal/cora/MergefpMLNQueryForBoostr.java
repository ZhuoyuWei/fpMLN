package journal.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MergefpMLNQueryForBoostr {

	public List<String> bufList=new ArrayList<String>();
	
	public void ReadOneFile(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			bufList.add(buffer);
		}
		br.close();
	}
	
	public void PrintList(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<bufList.size();i++)
		{
			ps.println(bufList.get(i));
		}
		ps.close();
		
	}
	
	
	public static void main(String[] args) throws IOException
	{
		String[] filename={"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_sameauthor\\test4_same\\test4_same_pos.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_sameauthor\\test4_same\\test4_same_neg.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samebit\\test4_same\\test4_same_pos.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samebit\\test4_same\\test4_same_neg.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_sametitle\\test4_same\\test4_same_pos.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_sametitle\\test4_same\\test4_same_neg.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test4_same\\test4_same_pos.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test4_same\\test4_same_neg.txt"};
	
		String outfile="D:\\data\\temp_infer\\effect\\cora_boostr\\test4_same_pos.txt";
		
		
		MergefpMLNQueryForBoostr mmfb=new MergefpMLNQueryForBoostr();
		for(int i=0;i<filename.length;i++)
		{
			mmfb.ReadOneFile(filename[i]);
		}
		mmfb.PrintList(outfile);
		
	}
	
}
