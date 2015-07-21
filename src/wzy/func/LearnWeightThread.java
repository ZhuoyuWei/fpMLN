package wzy.func;

import java.util.List;
import java.util.concurrent.Callable;

import wzy.model.DataPoint;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class LearnWeightThread implements Callable{

	public Integer relIndex;
	public Integer rel;
	public Learner learner;
	public List<DataPoint> dpList;
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		try{
		long start=System.currentTimeMillis();
		System.out.println("Learner "+relIndex+" start.");
		learner.Learning(dpList);
		//GlobalStaticData.relToLearner.put(rel, learner);
		long end=System.currentTimeMillis();
		System.out.println("Learner "+relIndex+" is over "+(end-start)+"ms");
		
		}catch(Exception e)
		{
			e.printStackTrace(LogPrintStream.getPs_alwaysdebug());
		}
		return null;
	}

}
