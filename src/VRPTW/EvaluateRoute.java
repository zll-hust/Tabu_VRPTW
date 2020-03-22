package VRPTW;

import static VRPTW.Parameter.*;

public class EvaluateRoute {
	
	public static boolean Check ( RouteType R[] ) {//检验解R是否满足所有约束
	    double Q = 0;
	    double T = 0;

	    //检查是否满足容量约束
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        if ( R[i].V.size() > 2 && R[i].Load > Capacity )//对有客户且超过容量约束的路径，记录超过部分
	            Q = Q + R[i].Load - Capacity;

	    //检查是否满足时间窗约束
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        T += R[i].SubT;

	    //分别根据约束满足的情况和控制系数Sita更新Alpha和Beta值
	    //新路径满足条件，惩罚系数减小，
	    //新路径违反条件，惩罚系数加大。
	    if ( Q == 0 && Alpha >= 0.001 )
	        Alpha /= ( 1 + Sita );
	    else if ( Q != 0 && Alpha <= 2000 )
	        Alpha *= ( 1 + Sita );

	    if ( T == 0 && Beta >= 0.001 )
	        Beta /= ( 1 + Sita );
	    else if ( T != 0 && Beta <= 2000 )
	        Beta *= ( 1 + Sita );

	    if ( T == 0 && Q == 0 )
	        return true;
	    else
	        return false;
	}


	public static void UpdateSubT(RouteType r) {//更新路径r对时间窗的违反量
		double ArriveTime =0;
        for ( int j = 1; j < r.V.size(); ++j ) {//对每一个节点分别计算超出时间窗的部分
            ArriveTime = ArriveTime 
            		+ r.V.get(j-1).Service //服务时间
            		+ Graph[r.V.get(j-1).Number][r.V.get(j).Number];//路途经过时间
            if ( ArriveTime > r.V.get(j).End )//超过，记录
                r.SubT = r.SubT + ArriveTime - r.V.get(j).End;
            else if ( ArriveTime < r.V.get(j).Begin )//未达到，等待
                ArriveTime = r.V.get(j).Begin;
        }
	}
	
	
	//计算路径规划R的目标函数值，通过该目标函数判断解是否较优
	public static double Calculation ( RouteType R[], int Cus, int NewR ) {
	    //目标函数主要由三个部分组成：D路径总长度（优化目标），Q超出容量约束总量，T超出时间窗约束总量
	    //目标函数结构为 f(R) = D + Alpha * Q + Beta * T, 第一项为问题最小化目标，后两项为惩罚部分
		//其中Alpha与Beta为变量，分别根据当前解是否满足两个约束进行变化，根据每轮迭代得到的解在Check函数中更新
	    
	    double Q = 0;
	    double T = 0;
	    double D = 0;

	    //计算单条路径超出容量约束的总量
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        if ( R[i].V.size() > 2 && R[i].Load > Capacity )
	            Q = Q + R[i].Load - Capacity;

	    //计算总超出时间
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        T += R[i].SubT;

	    //计算路径总长度
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        D += R[i].Dis;

	    return (D + Alpha * Q + Beta * T);//返回目标函数值
	}
}