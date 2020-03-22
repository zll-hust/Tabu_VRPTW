package VRPTW;

public class CustomerType {
	int Number;//节点自身编号
    int R;//节点所属车辆路径编号
    double X, Y;//节点横纵坐标
    double Begin, End, Service;//节点被访问的最早时间，最晚时间以及服务时长
    double Demand;//节点的需求容量
    
    public CustomerType() {
    	this.Number=0;
    	this.R=0;
    	this.Begin =0;
    	this.End=0;
    	this.Service=0;
    	this.X=0;
    	this.Y=0;
    	this.Demand=0;
    }
    
    public CustomerType(CustomerType c1) {
    	this.Number=c1.Number;
    	this.R=c1.R;
    	this.Begin =c1.Begin;
    	this.End=c1.End;
    	this.Service=c1.Service;
    	this.X=c1.X;
    	this.Y=c1.Y;
    	this.Demand=c1.Demand;
    }
}
