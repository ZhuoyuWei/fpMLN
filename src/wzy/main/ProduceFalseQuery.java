package wzy.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import wzy.func.InferenceTestTool;
import wzy.func.InputData;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.model.HyperEdge;
import wzy.stat.GlobalStaticData;

public class ProduceFalseQuery {

	
	public static void PrintFalseQueryForBoostr(String falsequeryfile,String truequeryfile)
	{
		try {
			PrintStream ps=new PrintStream(falsequeryfile);
			PrintStream ps1=new PrintStream(truequeryfile);
			for(int i=0;i<GlobalStaticData.entitativeTestQueryList.size();i++)
			{
				HyperEdge queryHe=GlobalStaticData.entitativeTestQueryList.get(i);
				if(queryHe.value<0.5)
				{
					OutputData.PrintOneEntityHyperEdge(ps, queryHe);
					ps.println(".");
				}
				else
				{
					OutputData.PrintOneEntityHyperEdge(ps1, queryHe);
					ps1.println(".");
				}
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args)
	{
		GlobalStaticData.IntiDataStructure();
		
		InputData.ReadInfo("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb_boostr.info",
				GlobalStaticData.relMapItoS
				, GlobalStaticData.relMapStoI, GlobalStaticData.typeMapItoS, GlobalStaticData.typeMapStoI
				, GlobalStaticData.conceptRelList);
		//System.out.println(GlobalStaticData.relMapStoI);
		GlobalStaticData.query=GlobalStaticData.relMapStoI.get("courseprof");
		InputData.ReadDBgetentityInListSameTime("E:\\Workspaces2014\\Boostr\\Boostr\\data\\webkb_busl\\test1_faculty\\test1_faculty_facts.txt", 
				GlobalStaticData.entityMapStoI
				, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode, GlobalStaticData.relMapStoI
				, GlobalStaticData.conceptRelList, GlobalStaticData.seed_train_list
				, "courseprof");
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);
		
		MLNProcess.KeepTypeIndexToEntitySet();
		
		
		InferenceTestTool.ExtractTrueQueryFromNodes();
System.out.println(GlobalStaticData.entitativeTestQueryList.size());
		InferenceTestTool.ProduceFalseQuery(2.);
		
		PrintFalseQueryForBoostr("E:\\Workspaces2014\\Boostr\\Boostr\\data\\webkb_busl2\\test1_faculty\\test_neg.txt",
				"E:\\Workspaces2014\\Boostr\\Boostr\\data\\webkb_busl2\\test1_faculty\\test_pos.txt");
	}
	
}
