package journal.exp;

import analy.ChangeClauseToMLN_learned;

public class ChangeFormulaToMCSAT {

	
	public static String[] onequery=new String[4];
	
	public static void main(String[] args)
	{
		//webkb
		ChangeClauseToMLN_learned.onequery[0]="faculty";
		ChangeClauseToMLN_learned.onequery[1]="student";
		ChangeClauseToMLN_learned.onequery[2]="faculty";
		ChangeClauseToMLN_learned.onequery[3]="student";		
		
		//uw
/*		ChangeClauseToMLN_learned.onequery[0]="professor";
		ChangeClauseToMLN_learned.onequery[1]="student";
		ChangeClauseToMLN_learned.onequery[2]="introCourse";
		ChangeClauseToMLN_learned.onequery[3]="faculty";		*/	
		
		
		
		ChangeClauseToMLN_learned.ReadClauseAndWeightsToPrint("D:\\data\\Experiments_6\\new_learn_for_long\\webkb_l2_learning_result\\result_l2_learnweight_webkb_filted\\courseProf\\test_4\\mid\\conceptualFormula.user",
				"D:\\data\\Experiments_6\\new_learn_for_long\\webkb_l2_learning_result\\result_l2_learnweight_webkb_filted\\courseProf\\test_4\\mid\\weights.db",
				"C:\\Users\\Administrator\\Desktop\\fpMLN实验另一部分\\mln_data\\webkb-data\\forinfer.mln",
				"D:\\data\\Experiments_6\\new_learn_for_long\\webkb_l2_mcsat_data\\weights.mln");
		
		
/*		ChangeClauseToMLN_learned.ReadClauseAndWeightsToPrint("D:\\data\\Experiments_6\\learn\\uw_fpMLN_results\\result_uw_fpmln_2014.10.18\\advisedBy\\ai\\mid\\conceptualFormula.user",
				"D:\\data\\Experiments_6\\learn\\uw_fpMLN_results\\result_uw_fpmln_2014.10.18\\advisedBy\\ai\\mid\\weights.db",
				"C:\\Users\\Administrator\\Desktop\\fpMLN实验另一部分\\fpMLN实验另一部分\\mln_data\\uw-cse\\forinfer.mln",
				"D:\\data\\Experiments_6\\learn\\uw_fpMLN_mcsat\\weights.mln");		
		*/
		
	}
	
	
	
	
}
