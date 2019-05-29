package car_simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import utils.Logger;
/**
 * ����·������
 * @author xycode
 *
 */
public class CarSimulation implements Runnable{
	
//	public static Map<Position,CrossRoads> crossroads_mp=new HashMap<>();//������·�ڵ�ӳ��
	public CrossRoad[][] crossroads;//·������
	public ArrayList<Car> other_cars;//���������ļ���
	public Car my_car;//�Լ��ĳ���,ֻ��һ������
	public int start_x,start_y,end_x,end_y;//my_car��������յ�
	public int timer;
	public ArrayList<Position> car_dst;
	
	public static Logger logger;
	public static final int run_time=3003;//����ʱ��
	public static final Direction[] next_direction= 
		{Direction.DOWN,Direction.LEFT,Direction.RIGHT,Direction.UP};
	public static Random r=new Random();
	public static int[][][] crossroad_queue_size=new int[Constant.N+1][Constant.N+1][4];//�洢˳������������
	public CarSimulation() {
		crossroads=new CrossRoad[Constant.N+1][Constant.N+1];
		other_cars=new ArrayList<>();
		car_dst=new ArrayList<>();
		timer=-1;
	}
	
	public static boolean rand=true;//�Ƿ��������������յ�ı�־,Ĭ������
	public void set_my_car(int start_x,int start_y,int end_x,int end_y) {
		my_car.x=start_x;
		my_car.y=start_y;
		
		this.end_x=end_x;
		this.end_y=end_y;
		
	}

	/**
	  * �������״̬
	 */
	public void clear() {
		//crossroads=newCrossRoad[Constant.N+1][Constant.N+1];//init()��ֱ�Ӹ��ǵ�,���Բ���clean��
		if(!other_cars.isEmpty()) other_cars.clear();
		my_car=null;
		timer=-1;
		wait_time=0;
	}
	
	public void init() {
		//�������cars_number�������ڼ���other_cars���棬������λ���غ�
		for(int i=0;i<Constant.cars_number+1;++i) {
			Car tmp=new Car(r.nextInt(Constant.N)*Constant.cell_length,r.nextInt(Constant.N)*Constant.cell_length,i+1);
			car_dst.add(new Position(r.nextInt(Constant.N)*Constant.cell_length,r.nextInt(Constant.N)*Constant.cell_length));
			//���������ʻ����
			tmp.direction=next_direction[r.nextInt(4)];
			
			other_cars.add(tmp);
		}
		//ע��,������ʵҲ��my_car�ĳ�ʼ�������ú���
		my_car=other_cars.get(Constant.cars_number);//���Ϊcars_number+1�ľ���my_car
		if(rand) {
			my_car.x=r.nextInt(Constant.N)*Constant.cell_length;
			my_car.y=r.nextInt(Constant.N)*Constant.cell_length;
			//������һ��Ŀ�ĵ�
			end_x=r.nextInt(Constant.N)*Constant.cell_length;
			end_y=r.nextInt(Constant.N)*Constant.cell_length;
			System.out.printf("Random generate:(%d,%d)->(%d,%d)\n",my_car.x,my_car.y,end_x,end_y);
		}else{
			set_my_car(0, 0, 9*Constant.cell_length, 8*Constant.cell_length);
			System.out.printf("Appoint generate:(%d,%d)->(%d,%d)\n",my_car.x,my_car.y,end_x,end_y);
		}
		
		//�������·��,��������״̬�������crossroads����
		//������,ÿ��·�ڶ����ú��̵�,(N-1)*(N-1)��
		for(int i=1;i<=Constant.N-1;++i) {
			for(int j=1;j<=Constant.N-1;++j) {
				CrossRoad tmp=new CrossRoad(Constant.red_light_time, Constant.green_light_time,i*Constant.cell_length,j*Constant.cell_length);
				//����������ú��̵Ƶĳ�ʼ״̬,�Լ�����ͨ�е�״̬
				if(r.nextBoolean()) {//��������Ϊ���״̬,�����ҿ�ͨ��
					tmp.redlight_time=r.nextInt(Constant.red_light_time-10)+10;//ʱ��[10,Constant.red_light_time)
					tmp.greenlight_time=0;
					tmp.set_go_through(false);//�������ҿ�ͨ��
				}else {
					tmp.redlight_time=0;
					tmp.greenlight_time=r.nextInt(Constant.red_light_time-10)+10;
					tmp.set_go_through(true);
				}
				//�洢��crossroads��,��Ҫ��ѯ,ֱ��x/Constant.cell_length,y/Constant.cell_length��Ϊ������ѯ����
				crossroads[i][j]=tmp;	
			}
		}

	}
	public int wait_time=0;//my_car����㵽�յ�Ĺ����еĵȴ�ʱ��,������static��,��Ϊ�����߳�ʱ,�ᵼ���ظ���
	public static int total_time=0,success_cnt=0;
	public static int total_wait_time=0;
	@Override
	public void run() {
		rand=false;//���������
		init();
		start_x=my_car.x;
		start_y=my_car.y;
		run(false);
		if(timer!=-1) {
			total_time+=timer;
			total_wait_time+=wait_time;
			++success_cnt;
//			//TODO:��μ�¼wait_time.dat
//			pout2.println(timer+","+wait_time);
		}else {
			System.out.printf("Failure(ThreadId=%d): (%3d,%3d)->(%3d,%3d)\n",
					Thread.currentThread().getId(),start_x,start_y,end_x,end_y);
			
		}
		clear();
	}
	
	/**
	 * ��ʼrun simulation
	 * 1.���ӵ�λ�ø���
	 * 2.·�ں��̵�״̬�ĸ���
	 * 3.·�ڶ��еĸ���,��4������
	 * @param enable_car_log
	 * @param trace_car_number
	 * ����car��־����
	 */
	public void run(boolean enable_car_log,int trace_car_number) {
		--trace_car_number;//������0��ʼ�������ȼ�1
		int prev_x,prev_y;//����my_car����һ��ʱ�������λ��
		for(int i=0;i<run_time;++i) {
			if(enable_car_log) logger.car_log(other_cars.get(trace_car_number));
			if(my_car.x==end_x&&my_car.y==end_y) {
				System.out.printf("OK,used time: %ds\nwait time: %ds,travel time: %ds\n",
						i+1,wait_time,i+1-wait_time);
				timer=i+1;
				break;
			}
			//����·�ڵ�״̬(���̵�)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}

			//���³��ӵ�״̬(��û��go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {//Bug!!!ע��:�Ѿ���ӵĳ��ӾͲ�Ҫ���ظ������
					//1.����·�ڶ���
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				//2.�ڲ�·��,��Ҫ�������ó��ӵ�next_direction,�ر�ע������·��,һ��Ҫ����,��������ӻ��������ı��ǻ�
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			prev_x=my_car.x;
			prev_y=my_car.y;//����my_car����һ��ʱ�������λ��
			//���³��ӵ�״̬(��ʼgo)
			//1.�����ڵȴ�������ĳ���
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//���ڵȴ�������
					tmp.car_go();
				}
			}
			//2.������·�ڶ��еĳ��ӵ�״̬
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).�����϶���,�������ͷ����
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {//�����̵ƣ�����
								c.car_go();
								c.inQueue=false;//���ڶ���������
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {//���º�ƣ��������̵ƣ�����
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).�����¶���
					if(!crossroads[m][n].down_queue.isEmpty()) {
						Car c=crossroads[m][n].down_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}
					}
					//3).���������
					if(!crossroads[m][n].left_queue.isEmpty()) {
						Car c=crossroads[m][n].left_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}
					}
					//4).�����Ҷ���
					if(!crossroads[m][n].right_queue.isEmpty()) {
						Car c=crossroads[m][n].right_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}
					}
				}
			}
			
			if(prev_x==my_car.x&&prev_y==my_car.y) {
				++wait_time;
			}
		}
		
	}
	
//	//��¼����ʱ,���ܿ����߳�,�������ݻ����
//	public static PrintWriter pout=null;//��¼crossroads_queue.dat
//	public static PrintWriter pout2=null;//��¼wait_time.dat
//	static {
//		try {
//			pout2=new PrintWriter(new FileWriter("wait_time.dat"),true);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		pout2.println("Total Time,Total Wait Time");
//	}
	/**
	 * ���ɱ�׼��־��׼��־
	 * @param enable_standard_log
	 */
	public void run(boolean enable_standard_log) {
		if(enable_standard_log) {
			System.out.println("Initial State:");
			logger.cars_log(other_cars);
			logger.crossroads_log(crossroads);
		}
		int prev_x,prev_y;//����my_car����һ��ʱ�������λ��
//		try {
//			pout=new PrintWriter(new FileWriter("crossroads_queue.dat"),true);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		pout.println("Up,Down,Left,Right");

		for(int i=0;i<run_time;++i) {
			if(my_car.x==end_x&&my_car.y==end_y) {
				System.out.printf("ThreadId="+Thread.currentThread().getId()+" ");
				System.out.printf("OK,used time: %ds\nwait time: %ds,travel time: %ds\n",
						i+1,wait_time,i+1-wait_time);
				timer=i+1;
				break;
			}
			//��������λ�õ���Ŀ�ĵؾ�������ʧ,�ٲ�����Ӧ����
			//(��ʵ�޸ĳ��ӵ�λ�ü���,Ҳ���¹滮��Ŀ�ĵ�,��ΪĿ�ĵض���·��,�����������,�ʶ��в���Ҫ�޸�״̬)
			for(Car tmp:other_cars) {
				if(tmp.number!=Constant.cars_number+1) {
					if(tmp.x==car_dst.get(tmp.number).x&&tmp.y==car_dst.get(tmp.number).y) {
						tmp.x=r.nextInt(Constant.N)*Constant.cell_length;
						tmp.y=r.nextInt(Constant.N)*Constant.cell_length;
						car_dst.get(tmp.number).x=r.nextInt(Constant.N)*Constant.cell_length;
						car_dst.get(tmp.number).y=r.nextInt(Constant.N)*Constant.cell_length;
					}
				}
			}
			//����·�ڵ�״̬(���̵�)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
					//TODO:��¼ÿ��·�ڵĶ��г���
//					crossroad_queue_size[m][n][0]=crossroads[m][n].up_queue.size();
//					crossroad_queue_size[m][n][1]=crossroads[m][n].down_queue.size();
//					crossroad_queue_size[m][n][2]=crossroads[m][n].left_queue.size();
//					crossroad_queue_size[m][n][3]=crossroads[m][n].right_queue.size();
//					
//					pout.println(crossroad_queue_size[m][n][0]+","+crossroad_queue_size[m][n][1]
//							+","+crossroad_queue_size[m][n][2]+","+crossroad_queue_size[m][n][3]);
				}
			}
			//pout.println(-(i+1)+","+-(i+1)+","+-(i+1)+","+-(i+1));//������ʱ��(i+1)������β���
			//���³��ӵ�״̬(��û��go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {//ע��:�Ѿ���ӵĳ��ӾͲ�Ҫ���ظ������
					//1.����·�ڶ���
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				//2.�ڲ�·��,��Ҫ�������ó��ӵ�next_direction,�ر�ע������·��,һ��Ҫ����,��������ӻ��������ı��ǻ�
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					//TODO:����һ��״��,���ж������й̶�������յ�ʱ,����ᷢ��ʲô���
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			prev_x=my_car.x;
			prev_y=my_car.y;
			//���³��ӵ�״̬(��ʼgo)
			//1.�����ڵȴ�������ĳ���
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//���ڵȴ�������
					tmp.car_go();
				}
			}
			//2.������·�ڶ��еĳ��ӵ�״̬(�ұ��̵����ҿ���,��ߺ���������)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).�����϶���,�������ͷ����
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {//�����̵ƣ�����
								c.car_go();
								c.inQueue=false;//���ڶ���������
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT) {
							if(crossroads[m][n].redlight_time>0) {//���º�ƣ��������̵ƣ������Ϸ�����������
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.RIGHT) {
							if(crossroads[m][n].greenlight_time>0) {//�����̵ƣ������Һ�ƣ������Ϸ�����������
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).�����¶���
					if(!crossroads[m][n].down_queue.isEmpty()) {
						Car c=crossroads[m][n].down_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.LEFT) {
							if(crossroads[m][n].greenlight_time>0) {//���Һ��(�����̵�)������
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.RIGHT) {
							if(crossroads[m][n].greenlight_time>0) {//�����̵�(���º��)������
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}
					}
					//3).���������
					if(!crossroads[m][n].left_queue.isEmpty()) {
						Car c=crossroads[m][n].left_queue.peek();
						if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}else if(c.direction==Direction.UP) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}else if(c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}
					}
					//4).�����Ҷ���
					if(!crossroads[m][n].right_queue.isEmpty()) {
						Car c=crossroads[m][n].right_queue.peek();
						if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}else if(c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}else if(c.direction==Direction.DOWN) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}
					}
				}
			}
			if(prev_x==my_car.x&&prev_y==my_car.y) {
				++wait_time;
			}
		}
		//pout.close();
		if(enable_standard_log) {
			System.out.println("Final State:");
			logger.cars_log(other_cars);
			logger.crossroads_log(crossroads);
		}
	}
	
	/**
	 * ��ӡ��Ӧ����(trace_crossroad_x,trace_crossroad_y)·�ڵ���־���ĸ��ȴ����У����̵�״̬��
	 * @param enable_crossroad_log
	 * @param trace_crossroad_x
	 * @param trace_crossroad_y
	 */
	
	public void run(boolean enable_crossroad_log,
			int trace_crossroad_x,int trace_crossroad_y) {
		for(int i=0;i<run_time;++i) {
			if(enable_crossroad_log) logger.crossroad_log(crossroads[trace_crossroad_x/Constant.cell_length][trace_crossroad_y/Constant.cell_length]);
			if(my_car.x==end_x&&my_car.y==end_y) {
				System.out.printf("OK,used time: %ds\n",i+1);
				timer=i+1;
				break;
			}
			//����·�ڵ�״̬(���̵�)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}
			
			//���³��ӵ�״̬(��û��go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {
					//1.����·�ڶ���
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					//2.�ڲ�·��,��Ҫ�������ó��ӵ�next_direction,�ر�ע������·��,һ��Ҫ����,��������ӻ��������ı��ǻ�
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			//���³��ӵ�״̬(��ʼgo)
			//2.�ٴ����ڵȴ�������ĳ���
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//���ڵȴ�������
					tmp.car_go();
				}
			}
			//1.�ȴ�����·�ڶ��еĳ��ӵ�״̬
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).�����϶���
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {//�����̵ƣ�����
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {//���º�ƣ��������̵ƣ�����
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).�����¶���
					if(!crossroads[m][n].down_queue.isEmpty()) {
						Car c=crossroads[m][n].down_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}
					}
					//3).���������
					if(!crossroads[m][n].left_queue.isEmpty()) {
						Car c=crossroads[m][n].left_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}
					}
					//4).�����Ҷ���
					if(!crossroads[m][n].right_queue.isEmpty()) {
						Car c=crossroads[m][n].right_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}
					}
				}
			}	
		}	
	}
	/**
	 * my_carѰ·,�������Ӧ�ò��뵽run()�����ѭ��������
	  *  �򵥵���Ŀǰλ����Ŀ��λ�õľ���Ϊ��׼,�����������߼�ľ���,���մﵽĿ�ĵ�
	 * 
	 */

	public void naive_search_direction(Car my_car,int end_x,int end_y) {
		if(my_car.inQueue) return;//�ڵȴ���������Ͳ��õ���������
		//�жϰ���ǰ������ʻ�Ļ��ܷ�ʹ��������,�����ܾ͸ı䷽��
		if(my_car.direction==Direction.UP) {
			if(end_y<my_car.y);//���յ�ǰ������ʻ��ʹ�������̵Ļ��Ͳ�������
			else {//����ʹ��������,��ʵ�������ֻ���ܷ�����·��
				//��ǰ����λ����Ŀ�ĵ���Ϸ�
				if(end_x>my_car.x) {//ȷ����ǰ����λ����Ŀ�ĵ�����Ϸ�
					//���Խ��������»����Ҽ���
					
					//ע��:��ѡ������������,Ӧ��ȥѡ�̵�״̬���Ǹ�
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�(�����ڶ���ͷ)
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}
					
				}else if(end_x<my_car.x) {//ȷ����ǰ����λ����Ŀ�ĵ�����Ϸ�
					//���Խ��������»����󼴿�
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_x==my_car.x){//ȷ����ǰ����λ����Ŀ�ĵ�����Ϸ�
					//���Խ��������¼���
					my_car.direction=Direction.DOWN;
				}
			}
		}
		
		if(my_car.direction==Direction.DOWN) {
			if(end_y>my_car.y);//���յ�ǰ������ʻ��ʹ�������̵Ļ��Ͳ�������
			else {//����ʹ��������,��ʵ�������ֻ���ܷ�����·��
				//��ǰ����λ����Ŀ�ĵ���·�
				if(end_x>my_car.x) {//ȷ����ǰ����λ����Ŀ�ĵ�����·�
					//���Խ��������ϻ����Ҽ���
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}
				}else if(end_x<my_car.x) {//ȷ����ǰ����λ����Ŀ�ĵ�����·�
					//���Խ��������ϻ����󼴿�
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_x==my_car.x){//ȷ����ǰ����λ����Ŀ�ĵ�����·�
					//���Խ��������ϼ���
					my_car.direction=Direction.UP;
				}
			}
		}
		
		if(my_car.direction==Direction.LEFT) {
			if(end_x<my_car.x);//���յ�ǰ������ʻ��ʹ�������̵Ļ��Ͳ�������
			else {//����ʹ��������,��ʵ�������ֻ���ܷ�����·��
				//��ǰ����λ����Ŀ�ĵ����
				if(end_y>my_car.y) {//ȷ����ǰ����λ����Ŀ�ĵ�����Ϸ�
					//���Խ��������»����Ҽ���
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}
				}else if(end_y<my_car.y) {//ȷ����ǰ����λ����Ŀ�ĵ�����·�
					//���Խ��������ϻ����Ҽ���
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}
				}else if(end_y==my_car.y){//ȷ����ǰ����λ����Ŀ�ĵ������
					//���Խ��������Ҽ���
					my_car.direction=Direction.RIGHT;
				}
			}
		}
		
		if(my_car.direction==Direction.RIGHT) {
			if(end_x>my_car.x);//���յ�ǰ������ʻ��ʹ�������̵Ļ��Ͳ�������
			else {//����ʹ��������,��ʵ�������ֻ���ܷ�����·��
				//��ǰ����λ����Ŀ�ĵ���ҷ�
				if(end_y>my_car.y) {//ȷ����ǰ����λ����Ŀ�ĵ�����Ϸ�
					//���Խ��������»����󼴿�
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_y<my_car.y) {//ȷ����ǰ����λ����Ŀ�ĵ�����·�
					//���Խ��������ϻ����󼴿�
					if(my_car.is_crossroad()) {//��ʮ��·�ڲ���Ҫ�жϺ��̵�
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}else {//������·��,�����ѡһ�����򼴿�
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_y==my_car.y){//ȷ����ǰ����λ����Ŀ�ĵ�����ҷ�
					//���Խ��������󼴿�
					my_car.direction=Direction.LEFT;
				}
			}
		}
		
	}
	
	/**
	 * 
	 * ��Ϊ����������·�ڵ�next_direction��������õ�,������dfs�Ļ��ǲ��ܱ�֤����ǰ�ͻ��ݺ����Ϊһ��
	 * ����my_car�߲�ͬ��·���Ļ�Ҳ��Ӱ�쵽����������Ϊ
	 * ����dfs���õĳ�����ʵ��:��������������,ֻ�к��̵ƴ��ڵ����
	 * �����������ʵ���޷�ʹ����dfs�����
	 * @param direction
	 * my_car�ӵ�ǰ·������direction����ﵽ��һ��·��(��������·��)
	 */
	public void next_reaches(Direction direction) {
		int tmp_x=my_car.x,tmp_y=my_car.y;
		for(;;) {
			if(my_car.is_crossroad()&&(my_car.x!=tmp_x||my_car.y!=tmp_y)){
				return;
			}
			//����·�ڵ�״̬(���̵�)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}
			//���³��ӵ�״̬(��û��go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {//ע��:�Ѿ���ӵĳ��ӾͲ�Ҫ���ظ������
					//1.����·�ڶ���
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				//2.�ڲ�·��,��Ҫ�������ó��ӵ�next_direction,�ر�ע������·��,һ��Ҫ����,��������ӻ��������ı��ǻ�
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			//���ǵ�my_car��direction
			my_car.direction=direction;
			//���³��ӵ�״̬(��ʼgo)
			//1.�����ڵȴ�������ĳ���
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//���ڵȴ�������
					tmp.car_go();
				}
			}
			//2.������·�ڶ��еĳ��ӵ�״̬
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).�����϶���,�������ͷ����
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {//�����̵ƣ�����
								c.car_go();
								c.inQueue=false;//���ڶ���������
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {//���º�ƣ��������̵ƣ�����
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).�����¶���
					if(!crossroads[m][n].down_queue.isEmpty()) {
						Car c=crossroads[m][n].down_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}
					}
					//3).���������
					if(!crossroads[m][n].left_queue.isEmpty()) {
						Car c=crossroads[m][n].left_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].left_queue.remove();
							}
						}
					}
					//4).�����Ҷ���
					if(!crossroads[m][n].right_queue.isEmpty()) {
						Car c=crossroads[m][n].right_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].right_queue.remove();
							}
						}
					}
				}
			}
			
		}
		
	}
	
	/**
	 * ����ʵ����������dfs�еĲ�������(��ʵ֤��,dfs�����������ֶ�̬����)
	 * ʹ��ʱ,�뱣֤û����������,����Constant.cars_number=0
	 * @param direction
	 */
	public void next_reaches2(Direction direction) {
		int tmp_x=my_car.x,tmp_y=my_car.y;
		for(;;) {
			if(my_car.is_crossroad()&&(my_car.x!=tmp_x||my_car.y!=tmp_y)){
				return;
			}
			//����·�ڵ�״̬(���̵�)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}
			
			//���ǵ�my_car��direction
			my_car.direction=direction;
			my_car.can_go(direction);
			
		}
		
	}
	
	

}
