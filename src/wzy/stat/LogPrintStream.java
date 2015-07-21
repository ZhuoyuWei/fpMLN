package wzy.stat;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a collection of PrintStream of PrintWriter, and they are all static and thread safe. It is for Debug.
 * @author Zhuoyu Wei
 * @version 1.0
 * 
 * Filed:
 * ps_InputData_ReadDB, PrintStream, Class:InputData, Function:ReadDB
 */

public class LogPrintStream {

	/**
	 * Open a static PrintStream with filename
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param ps static printStream
	 * @param filename
	 * @deprecated It is a error.
	 */
	public static void OpenPS(PrintStream ps,String filename)
	{
		try {
			ps=new PrintStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			ps=null;
			e.printStackTrace();
		}
	}
	
	/**
	 * Close a opened printstream.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param ps
	 */
	public static void ClosePS(PrintStream ps)
	{
		if(ps!=null)
			ps.close();
	}
	
	/**
	 * It is a static function to initialize all PrintStream for log or middle output data.<br>
	 * If you want to add new printstreams, you need set the printstream static and add the code of initialing it<br>
	 * in this function, and close it in the CloseAllPrintStream() function<br>
	 * In addtion, you need add annotation information for your additive printstream in this class.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void InitAllPrintStream()
	{
		// need developer add PrintStream manually
		//OpenPS(ps_InputData_ReadDB,);
		try {
			ps_InputData_ReadDB=new PrintStream(GlobalStaticData.baseDir+"/log/inputdata_readdb.log");
			ps_RandomWalkThread_RandomWalk=new PrintStream(GlobalStaticData.baseDir+"/log/randomwalkthread_randomwalk.log");
			
			ps_thread_info=new PrintStream(GlobalStaticData.baseDir+"/log/thread_info.log");
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:ms");
			ps_thread_info.println("The program is start in "+df.format(new Date())+"\n");
			
			ps_middleresult_nodepath_train=new PrintStream(GlobalStaticData.baseDir+"/mid/nodepath_train.db");
			ps_middleresult_nodepath_test=new PrintStream(GlobalStaticData.baseDir+"/mid/nodepath_test.db");			
			ps_middleresult_train_datapoint=new PrintStream(GlobalStaticData.baseDir+"/mid/train_datapoint.db");
			ps_middleresult_test_datapoint=new PrintStream(GlobalStaticData.baseDir+"/mid/test_datapoint.db");			
			
			ps_alwaysdebug=new PrintStream(GlobalStaticData.baseDir+"/log/debug_anytime.log");
			
			ps_foruser_conceptualFormula=new PrintStream(GlobalStaticData.baseDir+"/mid/conceptualFormula.user");
			
			ps_MLN_ReadPathsAndProduceTrainningData=new PrintStream(GlobalStaticData.baseDir+"/log/mln_produce.log");
			
			ps_middleresult_weights=new PrintStream(GlobalStaticData.baseDir+"/mid/weights.db");
			
			ps_finalresult_cll=new PrintStream(GlobalStaticData.baseDir+"/res/cll.result");
			ps_finalresult_probability=new PrintStream(GlobalStaticData.baseDir+"/res/probability.result");
			ps_finalresult_view=new PrintStream(GlobalStaticData.baseDir+"/res/userview.result");
			ps_finalresult_expcll=new PrintStream(GlobalStaticData.baseDir+"/res/expcll.result");
			
			ps_system_out=new PrintStream(GlobalStaticData.baseDir+"/log/system_out.log");
			
			ps_debug_testcount=new PrintStream("ps_debug_test.count");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Close all the print streams initialized in the InitAllPrintStream() function.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void CloseAllPrintStream()
	{
		if(ps_InputData_ReadDB!=null)
			ps_InputData_ReadDB.close();
		if(ps_RandomWalkThread_RandomWalk!=null)
			ps_RandomWalkThread_RandomWalk.close();
		if(ps_thread_info!=null)
			ps_thread_info.close();
		if(ps_middleresult_nodepath_train!=null)
			ps_middleresult_nodepath_train.close();
		if(ps_middleresult_nodepath_test!=null)
			ps_middleresult_nodepath_test.close();		
		if(ps_middleresult_train_datapoint!=null)
			ps_middleresult_train_datapoint.close();
		if(ps_middleresult_test_datapoint!=null)
			ps_middleresult_test_datapoint.close();		
		if(ps_middleresult_weights!=null)
			ps_middleresult_weights.close();
		
		if(ps_alwaysdebug!=null)
			ps_alwaysdebug.close();
		
		if(ps_foruser_conceptualFormula!=null)
			ps_foruser_conceptualFormula.close();
		
		if(ps_MLN_ReadPathsAndProduceTrainningData!=null)
			ps_MLN_ReadPathsAndProduceTrainningData.close();
		
		if(ps_finalresult_cll!=null)
			ps_finalresult_cll.close();
		if(ps_finalresult_probability!=null)
			ps_finalresult_probability.close();
		if(ps_finalresult_view!=null)
			ps_finalresult_view.close();
		if(ps_finalresult_expcll!=null)
			ps_finalresult_expcll.close();
		
		if(ps_system_out!=null)
			ps_system_out.close();
	}
	
	


	public static PrintStream getPs_InputData_ReadDB() {
		return ps_InputData_ReadDB;
	}

	public static PrintStream getPs_RandomWalkThread_RandomWalk() {
		return ps_RandomWalkThread_RandomWalk;
	}

	public static PrintStream getPs_thread_info() {
		return ps_thread_info;
	}

	public static PrintStream getPs_MLN_ReadPathsAndProduceTrainningData() {
		return ps_MLN_ReadPathsAndProduceTrainningData;
	}

	public static PrintStream getPs_middleresult_nodepath_train() {
		return ps_middleresult_nodepath_train;
	}

	public static PrintStream getPs_middleresult_nodepath_test() {
		return ps_middleresult_nodepath_test;
	}

	public static PrintStream getPs_middleresult_train_datapoint() {
		return ps_middleresult_train_datapoint;
	}

	public static PrintStream getPs_middleresult_test_datapoint() {
		return ps_middleresult_test_datapoint;
	}

	public static PrintStream getPs_middleresult_weights() {
		return ps_middleresult_weights;
	}

	public static PrintStream getPs_alwaysdebug() {
		return ps_alwaysdebug;
	}

	public static PrintStream getPs_foruser_conceptualFormula() {
		return ps_foruser_conceptualFormula;
	}

	public static PrintStream getPs_finalresult_cll() {
		return ps_finalresult_cll;
	}

	public static PrintStream getPs_finalresult_probability() {
		return ps_finalresult_probability;
	}

	public static PrintStream getPs_finalresult_view() {
		return ps_finalresult_view;
	}
	public static PrintStream getPs_finalresult_expcll() {
		return ps_finalresult_expcll;
	}
	public static PrintStream getPs_system_out() {
		return ps_system_out;
	}









	private static PrintStream ps_InputData_ReadDB;
	private static PrintStream ps_RandomWalkThread_RandomWalk;
	private static PrintStream ps_thread_info;
	private static PrintStream ps_MLN_ReadPathsAndProduceTrainningData;
	
	private static PrintStream ps_middleresult_nodepath_train;
	private static PrintStream ps_middleresult_nodepath_test;	
	private static PrintStream ps_middleresult_train_datapoint;
	private static PrintStream ps_middleresult_test_datapoint;	
	
	private static PrintStream ps_middleresult_weights;
	
	private static PrintStream ps_alwaysdebug;
	
	private static PrintStream ps_foruser_conceptualFormula;
	
	private static PrintStream ps_finalresult_cll;
	private static PrintStream ps_finalresult_probability;
	private static PrintStream ps_finalresult_view;
	private static PrintStream ps_finalresult_expcll;
	
	private static PrintStream ps_system_out; //replace the System.out
	
	
	public static PrintStream ps_debug_testcount;
	
}
