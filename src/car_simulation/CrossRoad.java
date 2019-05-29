package car_simulation;

import java.util.LinkedList;
import java.util.Queue;

import utils.Logger;

public class CrossRoad implements Cloneable{
	/*
	 * 默认以上下方位的红绿灯状态为准(左右的话反过来即可)
	 */
	public int redlight_time;//红灯所剩时间
	public int greenlight_time;//绿灯所剩时间
	/*
	 * up,down,left,right,辅助变量,可用可不用
	 */
	public boolean up,down,left,right;//上下左右方向是否允许通行,true:允许
	/*
	 * go_through_car_num,crossroads_time这两个变量,可以先不纳入考虑范围,即先做一个简易的版本,然后后续版本可以纳入考虑范围之内
	 */
	public int go_through_car_num;//十字路口单位时间允许通行的车辆（上下左右都是这个值）,这个可以先不考虑
	public int crossroads_time;//车辆通过路口的时间点,0->1->2....,为2时就离开路口了,这个可以先不考虑,即把它当成1(个单位时间)
	
	public Queue<Car> up_queue,down_queue,left_queue,right_queue;
	public int x,y;//红绿灯的坐标
	public CrossRoad(int redlight_time, int greenlight_time, boolean up, boolean down, boolean left, boolean right,
			int go_through_car_num, int crossroads_time) {
		super();
		this.redlight_time = redlight_time;
		this.greenlight_time = greenlight_time;
		this.up = up;
		this.down = down;
		this.left = left;
		this.right = right;
		this.go_through_car_num = go_through_car_num;
		this.crossroads_time = crossroads_time;
		
		this.up_queue = new LinkedList<Car>();
		this.down_queue = new LinkedList<Car>();
		this.left_queue = new LinkedList<Car>();
		this.right_queue = new LinkedList<Car>();
	}
	
	public CrossRoad(int redlight_time, int greenlight_time,int x,int y,
			int go_through_car_num, int crossroads_time) {
		super();
		this.redlight_time = redlight_time;
		this.greenlight_time = greenlight_time;
		
		this.x=x;
		this.y=y;
		
		this.go_through_car_num = go_through_car_num;
		this.crossroads_time = crossroads_time;
		
		this.up_queue = new LinkedList<Car>();
		this.down_queue = new LinkedList<Car>();
		this.left_queue = new LinkedList<Car>();
		this.right_queue = new LinkedList<Car>();
	}

	public CrossRoad(int redlight_time, int greenlight_time,int x,int y) {
		super();
		this.redlight_time = redlight_time;
		this.greenlight_time = greenlight_time;
		
		this.x=x;
		this.y=y;
		
		this.up_queue = new LinkedList<Car>();
		this.down_queue = new LinkedList<Car>();
		this.left_queue = new LinkedList<Car>();
		this.right_queue = new LinkedList<Car>();
	}
	
	public CrossRoad() {
		this.up_queue = new LinkedList<Car>();
		this.down_queue = new LinkedList<Car>();
		this.left_queue = new LinkedList<Car>();
		this.right_queue = new LinkedList<Car>();
	}
	
	@Override
	public CrossRoad clone() {
		CrossRoad crossroad=null;
		try {
			crossroad=(CrossRoad)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		//克隆四个队列
		crossroad.up_queue=new LinkedList<Car>();
		for(Car c:this.up_queue) {
			crossroad.up_queue.add(c.clone());
		}
		crossroad.down_queue=new LinkedList<Car>();
		for(Car c:this.down_queue) {
			crossroad.down_queue.add(c.clone());
		}
		crossroad.left_queue=new LinkedList<Car>();
		for(Car c:this.left_queue) {
			crossroad.left_queue.add(c.clone());
		}
		crossroad.right_queue=new LinkedList<Car>();
		for(Car c:this.right_queue) {
			crossroad.right_queue.add(c.clone());
		}
		return crossroad;
	}
	
	public void set_go_through(boolean flag) {//flag==true:设置上下可通行，即上下为绿灯时
		if(flag) {
			up=true;
			down=true;
			left=false;
			right=false;
		}else {
			up=false;
			down=false;
			left=true;
			right=true;
		}
	}
	
	public void update_state() {
		//更新红绿灯状态
		if(greenlight_time>0) {
			--greenlight_time;
			if(greenlight_time==0) {
				redlight_time=Constant.red_light_time;//上下变为红灯了
				set_go_through(false);//设置为左右可通行
			}
		}else if(redlight_time>0) {
			--redlight_time;
			if(redlight_time==0) {
				greenlight_time=Constant.green_light_time;//上下变为绿灯了
				set_go_through(true);//设置为上下可通行
			}
		}
	}
	
//	public static void main(String[] args) {
//		CrossRoad c1=new CrossRoad(30, 30,20,20);
//		c1.down_queue.add(new Car(20,30,44));
//		c1.left_queue.add(new Car(10, 40, 60));
//		CrossRoad c2=c1.clone();
//		Logger logger=new Logger();
//		logger.crossroad_log(c1);
//		logger.crossroad_log(c2);
//		c1.redlight_time-=10;
//		c1.greenlight_time-=20;
//		c1.down_queue.add(new Car(20,30,4));
//		c1.left_queue.add(new Car(10, 40, 6));
//		c1.up_queue.add(new Car(20,30,33));
//		c1.right_queue.add(new Car(10, 40, 67));
//		//输出结果说明clone()方法确实有效
//		logger.crossroad_log(c1);
//		logger.crossroad_log(c2);
//		
//	}
	
}
