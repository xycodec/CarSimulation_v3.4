package car_simulation;

import java.util.LinkedList;
import java.util.Queue;

import utils.Logger;

public class CrossRoad implements Cloneable{
	/*
	 * Ĭ�������·�λ�ĺ��̵�״̬Ϊ׼(���ҵĻ�����������)
	 */
	public int redlight_time;//�����ʣʱ��
	public int greenlight_time;//�̵���ʣʱ��
	/*
	 * up,down,left,right,��������,���ÿɲ���
	 */
	public boolean up,down,left,right;//�������ҷ����Ƿ�����ͨ��,true:����
	/*
	 * go_through_car_num,crossroads_time����������,�����Ȳ����뿼�Ƿ�Χ,������һ�����׵İ汾,Ȼ������汾�������뿼�Ƿ�Χ֮��
	 */
	public int go_through_car_num;//ʮ��·�ڵ�λʱ������ͨ�еĳ������������Ҷ������ֵ��,��������Ȳ�����
	public int crossroads_time;//����ͨ��·�ڵ�ʱ���,0->1->2....,Ϊ2ʱ���뿪·����,��������Ȳ�����,����������1(����λʱ��)
	
	public Queue<Car> up_queue,down_queue,left_queue,right_queue;
	public int x,y;//���̵Ƶ�����
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
		//��¡�ĸ�����
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
	
	public void set_go_through(boolean flag) {//flag==true:�������¿�ͨ�У�������Ϊ�̵�ʱ
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
		//���º��̵�״̬
		if(greenlight_time>0) {
			--greenlight_time;
			if(greenlight_time==0) {
				redlight_time=Constant.red_light_time;//���±�Ϊ�����
				set_go_through(false);//����Ϊ���ҿ�ͨ��
			}
		}else if(redlight_time>0) {
			--redlight_time;
			if(redlight_time==0) {
				greenlight_time=Constant.green_light_time;//���±�Ϊ�̵���
				set_go_through(true);//����Ϊ���¿�ͨ��
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
//		//������˵��clone()����ȷʵ��Ч
//		logger.crossroad_log(c1);
//		logger.crossroad_log(c2);
//		
//	}
	
}
