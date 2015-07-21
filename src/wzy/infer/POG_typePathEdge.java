package wzy.infer;




/**
 * About state and type encoding:
 * state: 1, AheadRemove
 *        2, BackRemove
 *        3, AheadPush
 *        4, BaclPush
 * Therefore, 1 corresponds to 3, and 2 correspond 4.
 * types are according to the GlobalStaticData.typeMapStoI.
 * @author Administrator
 */
public class POG_typePathEdge {

	public int state;
	public int type;
	
	public boolean equals(Object o)
	{
		POG_typePathEdge pog=(POG_typePathEdge)o;
		if(state==pog.state&&type==pog.type)
			return true;
		else
			return false;
	}
	
	
	public int hashCode()
	{
		return state*31+type;
	}
	
	//POG_typePathNode objectNode;
	
}
