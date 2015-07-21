package analy;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class write_lhl_param {
	
	public static void main(String[] args) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(args[0]);
	
		ps.println("inDir               /home/mlgroup/wzy/mln_data/uw-cse/  //should end with /");
		ps.println("outDir              "+args[1]+"       //output clusters and semantic network");  //out_data
		ps.println("infoFile            uw.simple3.mln");
		ps.println("dbFile              "+args[2]);// db path 
		ps.println("bestClustFile       "+args[3]+".bestClust");
		ps.println("combsFile           "+args[3]+".semnet");
		
		ps.println("betaTotal           10.0");
		ps.println("lambda              -1");
		ps.println("maxIter             100000");
		ps.println("checkSymValid       false");
		ps.println("minSymAppear        1");
		ps.println("maxShared           100000");
		ps.println("threshRPerPair      0.0");
		ps.println("llThreshold         0.0");
		ps.println("printUnit           false ");
		ps.println("saveUnit            false");
		ps.println("defaultFalseProb    0.99999");
		ps.println("pureGreedy          false");
		
		ps.close();
	}
	
}
