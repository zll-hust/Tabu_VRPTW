package VRPTW;
 
import static VRPTW.EvaluateRoute.*;
import static java.lang.Math.*;
import static VRPTW.Parameter.*;

public class TS {
	
	public static void TabuSearch() {
		//禁忌搜索
	    //采取插入算子，即从一条路径中选择一点插入到另一条路径中
	    //在该操作下形成的邻域中选取使目标函数最小化的解
		
	    double Temp1;
	    double Temp2;

	    //初始化禁忌表
	    for ( int i = 2; i <= CustomerNumber + 1; ++i ) {
	        for ( int j = 1; j <= VehicleNumber; ++j )
	            Tabu[i][j] = 0;
	        TabuCreate[i] = 0;
	    }

	    int Iteration = 0;
	    while ( Iteration < IterMax ) {
	        int BestC = 0;
	        int BestR = 0;
	        int BestP = 0;
	        int P=0;
	        double BestV = INF;

	        for ( int i = 2; i <= CustomerNumber + 1; ++i ) {//对每一个客户节点
	            for ( int j = 1; j < routes[customers[i].R].V.size(); ++j ) {//对其所在路径中的每一个节点
	                if ( routes[customers[i].R].V.get(j).Number == i ) {//找到节点i在其路径中所处的位置j
	                    P = j;//标记位置
	                    break;
	                }
	            }
	          
	            removenode(customers[i].R,P,i);//将客户i从原路径的第P个位置中移除
	            
	            //找到一条路径插入删去的节点
	            for ( int j = 1; j <= VehicleNumber; ++j ) 
	            	 for ( int l = 1; l < routes[j].V.size(); ++l )//分别枚举每一个节点所在位置
	                        if ( customers[i].R != j ) {
	                        	
	                        	addnode(j,l,i);//将客户l插入路径j的第i个位置
	                        	
	                            Temp1 = routes[customers[i].R].SubT;  //记录原先所在路径的时间窗违反总和
	                            Temp2 = routes[j].SubT;               //记录插入的路径时间窗违反总和
	                            
	                            //更新i节点移出的路径：
	                    	    routes[customers[i].R].SubT = 0;
	                    	    UpdateSubT(routes[customers[i].R]);
	                    	    //更新i节点移入的路径j：
	                    	    routes[j].SubT = 0;
	                    	    UpdateSubT(routes[j]);
	                            double TempV = Calculation ( routes, i, j );//计算目标函数值
	                            
	                            if((TempV < Ans)|| //藐视准则，如果优于全局最优解
	                            		(TempV < BestV &&   //或者为局部最优解，且未被禁忌
	                            		   ( routes[j].V.size() > 2 && Tabu[i][j] <= Iteration ) || ( routes[j].V.size() == 2 && TabuCreate[i] <= Iteration )))
	                            	//禁忌插入操作，前者为常规禁忌表，禁忌插入算子；后者为特殊禁忌表，禁忌使用新的车辆
	            	            	//路径中节点数超过2，判断是否禁忌插入算子；路径中只有起点、终点，判断是否禁忌使用新车辆。
	                            if ( TempV < BestV ) { //记录局部最优情况
	                                BestV = TempV; //best vehicle 所属车辆
	                                BestC = i;     //best customer客户
	                                BestR = j;     //best route   所属路径
	                                BestP = l;     //best position所在位置
	                            }
	                            
	                            //节点新路径复原
	                            routes[customers[i].R].SubT = Temp1;
	                            routes[j].SubT = Temp2;
	                            removenode(j,l,i);
	                        }
	            //节点原路径复原
	            addnode(customers[i].R,P,i);
	        }

	        //更新车辆禁忌表
	        if ( routes[BestR].V.size() == 2 )
	            TabuCreate[BestC] = Iteration + 2 * TabuTenure + (int)(random() * 10);
	        //更新禁忌表
	        Tabu[BestC][customers[BestC].R] = Iteration + TabuTenure + (int)(random() * 10);
	        //如果全局最优的节点正好属于当前路径，过
	        for ( int i = 1; i < routes[customers[BestC].R].V.size(); ++i )
	            if ( routes[customers[BestC].R].V.get(i).Number == BestC ) {
	                P = i;
	                break;
	            }

	        //依据上述循环中挑选的结果，生成新的总体路径规划
	        //依次更新改变过的路径的：载重，距离长度，超出时间窗重量
	        
	        //更新原路径
	        removenode(customers[BestC].R,P,BestC);
	        //更新新路径
	        addnode(BestR,BestP,BestC);
	        //更新超出时间
	        routes[BestR].SubT = 0;
	        UpdateSubT(routes[BestR]);
	        routes[customers[BestC].R].SubT = 0;
	        UpdateSubT(routes[customers[BestC].R]);

	        //更新被操作的节点所属路径编号
	        customers[BestC].R = BestR;
	        
	        //如果当前解合法且较优则更新存储结果
	        if ( ( Check ( routes ) == true ) && ( Ans > BestV ) ) {
	        	 for ( int i = 1; i <= VehicleNumber; ++i ) {
	        	        Route_Ans[i].Load = routes[i].Load;
	        	        Route_Ans[i].V.clear();
	        	        for ( int j = 0; j < routes[i].V.size(); ++j )
	        	            Route_Ans[i].V.add ( routes[i].V.get(j) );
	        	    }
	            Ans = BestV;
	        }
	        
	        Iteration++;
	    }
	}
	
	private static void addnode(int r,int pos,int Cus) {//节点加入的路径routes[r],节点customer[Cus],节点加入路径的位置pos
		//更新在路径r中加上节点Cus的需求
        routes[r].Load += customers[Cus].Demand;
        //更新在路径r中插入节点Cus后所组成的路径距离
        routes[r].Dis = routes[r].Dis 
        		- Graph[routes[r].V.get(pos-1).Number][routes[r].V.get(pos).Number]
                + Graph[routes[r].V.get(pos-1).Number][customers[Cus].Number] 
                + Graph[routes[r].V.get(pos).Number][customers[Cus].Number];
        //在路径r中插入节点Cus
        routes[r].V.add (pos ,new CustomerType (customers[Cus]) );//插入i到下标为l处
	}
	
	
	private static void removenode(int r,int pos,int Cus) {//节点去除的路径routes[r],节点customer[cus],节点所在路径的位置pos 
        //更新在路径r中去除节点Cus的需求
        routes[r].Load -= customers[Cus].Demand;
        //更新在路径r中去除节点Cus后所组成的路径的距离
        routes[r].Dis = routes[r].Dis 
        		- Graph[routes[r].V.get(pos-1).Number][routes[r].V.get(pos).Number]
	            - Graph[routes[r].V.get(pos).Number][routes[r].V.get(pos+1).Number] 
	            + Graph[routes[r].V.get(pos-1).Number][routes[r].V.get(pos+1).Number];
        //从路径r中去除节点Cus
        routes[r].V.remove ( pos );
	}
}