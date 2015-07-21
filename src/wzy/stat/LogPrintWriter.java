package wzy.stat;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogPrintWriter {

	/**
	 * Open a static PrintWriter with filename
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param ps static PrintWriter
	 * @param filename
	 * @deprecated It is a error.
	 */
	public static void OpenPS(PrintWriter ps,String filename)
	{
		try {
			ps=new PrintWriter(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			ps=null;
			e.printStackTrace();
		}
	}
	
	/**
	 * Close a opened PrintWriter.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param ps
	 */
	public static void ClosePS(PrintWriter ps)
	{
		if(ps!=null)
			ps.close();
	}
	
	/**
	 * It is a static function to initialize all PrintWriter for log or middle output data.<br>
	 * If you want to add new PrintWriters, you need set the PrintWriter static and add the code of initialing it<br>
	 * in this function, and close it in the CloseAllPrintWriter() function<br>
	 * In addtion, you need add annotation information for your additive PrintWriter in this class.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void InitAllPrintWriter()
	{
		// need developer add PrintWriter manually
		//OpenPS(ps_InputData_ReadDB,);
		try {
			ps_InputData_ReadDB=new PrintWriter(GlobalStaticData.baseDir+"/log/inputdata_readdb.log");
			ps_RandomWalkThread_RandomWalk=new PrintWriter(GlobalStaticData.baseDir+"/log/randomwalkthread_randomwalk.log");
			
			ps_thread_info=new PrintWriter(GlobalStaticData.baseDir+"/log/thread_info.log");
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:ms");
			ps_thread_info.println("The program is start in "+df.format(new Date())+"\n");
			
			ps_middleresult_nodepath_train=new PrintWriter(GlobalStaticData.baseDir+"/mid/nodepath_train.db");
			ps_middleresult_nodepath_test=new PrintWriter(GlobalStaticData.baseDir+"/mid/nodepath_test.db");			
			ps_middleresult_train_datapoint=new PrintWriter(GlobalStaticData.baseDir+"/mid/train_datapoint.db");
			ps_middleresult_test_datapoint=new PrintWriter(GlobalStaticData.baseDir+"/mid/test_datapoint.db");			
			
			ps_alwaysdebug=new PrintWriter(GlobalStaticData.baseDir+"/log/debug_anytime.log");
			
			ps_foruser_conceptualFormula=new PrintWriter(GlobalStaticData.baseDir+"/mid/conceptualFormula.user");
			
			ps_MLN_ReadPathsAndProduceTrainningData=new PrintWriter(GlobalStaticData.baseDir+"/log/mln_produce.log");
			
			ps_middleresult_weights=new PrintWriter(GlobalStaticData.baseDir+"/mid/weights.db");
			
			ps_finalresult_cll=new PrintWriter(GlobalStaticData.baseDir+"/res/cll.result");
			ps_finalresult_probability=new PrintWriter(GlobalStaticData.baseDir+"/res/probability.result");
			ps_finalresult_view=new PrintWriter(GlobalStaticData.baseDir+"/res/userview.result");
			ps_finalresult_expcll=new PrintWriter(GlobalStaticData.baseDir+"/res/expcll.result");
			
			ps_system_out=new PrintWriter(GlobalStaticData.baseDir+"/log/system_out.log");
			
			try {
				ps_debug_testcount=new PrintWriter(new OutputStreamWriter(new FileOutputStream(
						"ps_debug_test.count"),"utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Close all the print streams initialized in the InitAllPrintWriter() function.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void CloseAllPrintWriter()
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
	
	


	public static PrintWriter getPs_InputData_ReadDB() {
		return ps_InputData_ReadDB;
	}

	public static PrintWriter getPs_RandomWalkThread_RandomWalk() {
		return ps_RandomWalkThread_RandomWalk;
	}

	public static PrintWriter getPs_thread_info() {
		return ps_thread_info;
	}

	public static PrintWriter getPs_MLN_ReadPathsAndProduceTrainningData() {
		return ps_MLN_ReadPathsAndProduceTrainningData;
	}

	public static PrintWriter getPs_middleresult_nodepath_train() {
		return ps_middleresult_nodepath_train;
	}

	public static PrintWriter getPs_middleresult_nodepath_test() {
		return ps_middleresult_nodepath_test;
	}

	public static PrintWriter getPs_middleresult_train_datapoint() {
		return ps_middleresult_train_datapoint;
	}

	public static PrintWriter getPs_middleresult_test_datapoint() {
		return ps_middleresult_test_datapoint;
	}

	public static PrintWriter getPs_middleresult_weights() {
		return ps_middleresult_weights;
	}

	public static PrintWriter getPs_alwaysdebug() {
		return ps_alwaysdebug;
	}

	public static PrintWriter getPs_foruser_conceptualFormula() {
		return ps_foruser_conceptualFormula;
	}

	public static PrintWriter getPs_finalresult_cll() {
		return ps_finalresult_cll;
	}

	public static PrintWriter getPs_finalresult_probability() {
		return ps_finalresult_probability;
	}

	public static PrintWriter getPs_finalresult_view() {
		return ps_finalresult_view;
	}
	public static PrintWriter getPs_finalresult_expcll() {
		return ps_finalresult_expcll;
	}
	public static PrintWriter getPs_system_out() {
		return ps_system_out;
	}









	private static PrintWriter ps_InputData_ReadDB;
	private static PrintWriter ps_RandomWalkThread_RandomWalk;
	private static PrintWriter ps_thread_info;
	private static PrintWriter ps_MLN_ReadPathsAndProduceTrainningData;
	
	private static PrintWriter ps_middleresult_nodepath_train;
	private static PrintWriter ps_middleresult_nodepath_test;	
	private static PrintWriter ps_middleresult_train_datapoint;
	private static PrintWriter ps_middleresult_test_datapoint;	
	
	private static PrintWriter ps_middleresult_weights;
	
	private static PrintWriter ps_alwaysdebug;
	
	private static PrintWriter ps_foruser_conceptualFormula;
	
	private static PrintWriter ps_finalresult_cll;
	private static PrintWriter ps_finalresult_probability;
	private static PrintWriter ps_finalresult_view;
	private static PrintWriter ps_finalresult_expcll;
	
	private static PrintWriter ps_system_out; //replace the System.out
	
	
	public static PrintWriter ps_debug_testcount;
}
