package VRPTW;

import static java.lang.Math.*;
import static VRPTW.Parameter.*;
import static VRPTW.EvaluateRoute.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;
import VRPTW.RouteType;

public class InitAndPrint {
	
	//计算图上各节点间的距离
	private static double Distance ( CustomerType C1, CustomerType C2 ) {
	    return sqrt ( ( C1.X - C2.X ) * ( C1.X - C2.X ) + ( C1.Y - C2.Y ) * ( C1.Y - C2.Y ) );
	}
	
	
	//读取数据
	public static void ReadIn(String fileName){
		
		for(int i=0;i<CustomerNumber+10;i++) {
			customers[i]=new CustomerType();
		}

		for(int i=0;i<VehicleNumber+10;i++) {
			customers[i]=new CustomerType();
			routes[i]=new RouteType();
			Route_Ans[i]=new RouteType();
		}
		
		try {	
			Scanner in = new Scanner(new FileReader(fileName + ".txt"));
			
			 for ( int i = 1; i <= CustomerNumber + 1; ++i ) {
				 customers[i].Number=in.nextInt()+1;
				 customers[i].X=in.nextDouble();
				 customers[i].Y=in.nextDouble();
				 customers[i].Demand=in.nextDouble();
				 customers[i].Begin=in.nextDouble();
				 customers[i].End=in.nextDouble();
				 customers[i].Service=in.nextDouble();
			 }
			
			in.close();
		}catch (FileNotFoundException e) {
			// File not found
			System.out.println("File not found!");
			System.exit(-1);
		}
		
		for ( int i = 1; i <= VehicleNumber; ++i ) {
	        if ( routes[i].V.size()!=0 )
	            routes[i].V.clear();
	        
	        routes[i].V.add ( new CustomerType (customers[1]) );//尝试往这里加入一个复制，后面也都要改。
	        routes[i].V.add ( new CustomerType (customers[1]) );
	        routes[i].V.get(0).End=routes[i].V.get(0).Begin;//起点
	        routes[i].V.get(1).Begin=routes[i].V.get(1).End;//终点
	        //算例中给出节点0有起始时间0和终止时间，所以如上赋值。
	        routes[i].Load = 0;
	    }
		
		Ans = INF;

	    for ( int i = 1; i <= CustomerNumber + 1; ++i )
	        for ( int j = 1; j <= CustomerNumber + 1; ++j )
	            Graph[i][j] = Distance ( customers[i], customers[j] );
	   
	}
	
	
	//构造初始解
	public static void Construction() {
	    int[] Customer_Set=new int[CustomerNumber + 10];
	    for ( int i = 1; i <= CustomerNumber; ++i )
	        Customer_Set[i] = i + 1;

	    int Sizeof_Customer_Set = CustomerNumber;
	    int Current_Route = 1;

	    //以满足容量约束为目的的随机初始化
	    //即随机挑选一个节点插入到第m条路径中，若超过容量约束，则插入第m+1条路径
	    //且插入路径的位置由该路径上已存在的各节点的最早时间决定
	    while ( Sizeof_Customer_Set > 0 ) {
			int K = (int) (random() * Sizeof_Customer_Set + 1);
			int C = Customer_Set[K];
			Customer_Set[K] = Customer_Set[Sizeof_Customer_Set];
			Sizeof_Customer_Set--;//将当前服务过的节点赋值为最末节点值,数组容量减1
			//随机提取出一个节点，类似产生乱序随机序列的代码

	        if ( routes[Current_Route].Load + customers[C].Demand > Capacity )
	            Current_Route++;
	        //不满足容量约束，下一条车辆路线
	        
	        for ( int i = 0; i < routes[Current_Route].V.size() - 1; i++ )//对路径中每一对节点查找，看是否能插入新节点
	            if ( ( routes[Current_Route].V.get(i).Begin <= customers[C].Begin ) && ( customers[C].Begin <= routes[Current_Route].V.get(i + 1).Begin ) ) {
	            	routes[Current_Route].V.add ( i + 1, new CustomerType (customers[C]) );
	            	//判断时间窗开始部分，满足，则加入该节点。
	            	routes[Current_Route].Load += customers[C].Demand;
	            	customers[C].R = Current_Route;
	            	//更新路径容量，节点类。
	                break;
	            }
	    }
	    
	    
	    //初始化计算超过时间窗约束的总量
	    for ( int i = 1; i <= VehicleNumber; ++i ) {
	    	routes[i].SubT = 0;
	        routes[i].Dis = 0;
	        
	        for(int j = 1; j < routes[i].V.size(); ++j) {
	        	routes[i].Dis += Graph[routes[i].V.get(j-1).Number][routes[i].V.get(j).Number];
	        }
	        
	        UpdateSubT(routes[i]);
	    }
	    
	}

	
	public static void Output () {//结果输出
	    System.out.println("************************************************************");
	    System.out.println("The Minimum Total Distance = "+ Ans);
	    System.out.println("Concrete Schedule of Each Route as Following : ");

	    int M = 0;
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        if ( Route_Ans[i].V.size() > 2 ) {
	            M++;
	            System.out.print("No." + M + " : ");
	            
	            for ( int j = 0; j < Route_Ans[i].V.size() - 1; ++j )
	            	System.out.print( Route_Ans[i].V.get(j).Number + " -> ");
	            System.out.println( Route_Ans[i].V.get(Route_Ans[i].V.size() - 1).Number);
	        }
	    System.out.println("************************************************************");
	}
	
	public static void CheckAns() {
		//检验距离计算是否正确
	    double Check_Ans = 0;
	    for ( int i = 1; i <= VehicleNumber; ++i )
	        for ( int j = 1; j < Route_Ans[i].V.size(); ++j )
	            Check_Ans += Graph[Route_Ans[i].V.get(j-1).Number][Route_Ans[i].V.get(j).Number];

	    System.out.println("Check_Ans="+Check_Ans );
	    
	    //检验是否满足时间窗约束
	    boolean flag=true;
	    for (int i=1;i<=VehicleNumber;i++){
	    	UpdateSubT(Route_Ans[i]);
	    	if( Route_Ans[i].SubT>0 )
	    		flag=false;
	    }
	    if (flag) 
	    	System.out.println("Solution satisfies time windows construction");
	    else 
	    	System.out.println("Solution not satisfies time windows construction");
	    
	}
}
