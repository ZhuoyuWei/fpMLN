package debug;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wzy.func.InputData;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class InputOutputTest {

	public static List<HyperEdge> hyperEdgeList=new ArrayList<HyperEdge>();
	
	public static void PrintTestInputEntity(List<HyperEdge> heList,String filename)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<heList.size();i++)
			{
				HyperEdge he=heList.get(i);
				ps.print(GlobalStaticData.relMapItoS.get(he.rel)+"\t");
				for(int j=0;j<he.nodeList.size();j++)
				{
					HyperNode hn=GlobalStaticData.entityToNode.get(he.nodeList.get(j));
					ps.print(GlobalStaticData.entityMapItoS.get(he.nodeList.get(j))+":"
							+GlobalStaticData.typeMapItoS.get(hn.type)+"\t");
				}
				ps.println();
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void PrintTestInputConcept(List<HyperEdge> heList,String filename)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<heList.size();i++)
			{
				HyperEdge he=heList.get(i);
				ps.print(GlobalStaticData.relMapItoS.get(he.rel)+"\t");
				for(int j=0;j<he.nodeList.size();j++)
				{
					ps.print(GlobalStaticData.typeMapItoS.get(he.nodeList.get(j))+"\t");
				}
				ps.println();
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args)
	{
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter("parameters.txt");
		GlobalStaticData.IntiDataStructure();
		LogPrintStream.InitAllPrintStream();
		
		
		InputData.ReadInfo(parameterCollection.getParameter("inputInfoFile"), GlobalStaticData.relMapItoS
				, GlobalStaticData.relMapStoI, GlobalStaticData.typeMapItoS, GlobalStaticData.typeMapStoI
				, GlobalStaticData.conceptRelList);
		PrintTestInputConcept(GlobalStaticData.conceptRelList,"log/concept.test");
		InputData.ReadDB(parameterCollection.getParameter("inputDbFile"), GlobalStaticData.entityMapStoI
				, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode, GlobalStaticData.relMapStoI
				, GlobalStaticData.conceptRelList);
		PrintTestInputEntity(hyperEdgeList,"log/hyperedge.text");
	}
	
}
