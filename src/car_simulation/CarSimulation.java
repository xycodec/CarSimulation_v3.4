package car_simulation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import utils.Logger;
/**
 * 进行路况仿真
 * @author xycode
 *
 */
public class CarSimulation implements Runnable{
	
//	public static Map<Position,CrossRoads> crossroads_mp=new HashMap<>();//坐标与路口的映射
	public CrossRoad[][] crossroads;//路口数组
	public ArrayList<Car> other_cars;//其它车辆的集合
	public Car my_car;//自己的车辆,只是一个引用
	public int start_x,start_y,end_x,end_y;//my_car的起点与终点
	public int timer;
	public ArrayList<Position> car_dst;
	
	public static Logger logger;
	public static final int run_time=3003;//仿真时间
	public static final Direction[] next_direction= 
		{Direction.DOWN,Direction.LEFT,Direction.RIGHT,Direction.UP};
	public static Random r=new Random();
	public static int[][][] crossroad_queue_size=new int[Constant.N+1][Constant.N+1][4];//存储顺序是上下左右
	public CarSimulation() {
		crossroads=new CrossRoad[Constant.N+1][Constant.N+1];
		other_cars=new ArrayList<>();
		car_dst=new ArrayList<>();
		timer=-1;
	}
	
	public static boolean rand=true;//是否随机生成起点与终点的标志,默认生成
	public void set_my_car(int start_x,int start_y,int end_x,int end_y) {
		my_car.x=start_x;
		my_car.y=start_y;
		
		this.end_x=end_x;
		this.end_y=end_y;
		
	}

	/**
	  * 清除所有状态
	 */
	public void clear() {
		//crossroads=newCrossRoad[Constant.N+1][Constant.N+1];//init()会直接覆盖掉,所以不用clean了
		if(!other_cars.isEmpty()) other_cars.clear();
		my_car=null;
		timer=-1;
		wait_time=0;
	}
	
	public void init() {
		//随机生成cars_number辆车放在集合other_cars里面，允许车的位置重合
		for(int i=0;i<Constant.cars_number+1;++i) {
			Car tmp=new Car(r.nextInt(Constant.N)*Constant.cell_length,r.nextInt(Constant.N)*Constant.cell_length,i+1);
			car_dst.add(new Position(r.nextInt(Constant.N)*Constant.cell_length,r.nextInt(Constant.N)*Constant.cell_length));
			//随机设置行驶方向
			tmp.direction=next_direction[r.nextInt(4)];
			
			other_cars.add(tmp);
		}
		//注意,这里其实也把my_car的初始坐标设置好了
		my_car=other_cars.get(Constant.cars_number);//编号为cars_number+1的就是my_car
		if(rand) {
			my_car.x=r.nextInt(Constant.N)*Constant.cell_length;
			my_car.y=r.nextInt(Constant.N)*Constant.cell_length;
			//再设置一下目的点
			end_x=r.nextInt(Constant.N)*Constant.cell_length;
			end_y=r.nextInt(Constant.N)*Constant.cell_length;
			System.out.printf("Random generate:(%d,%d)->(%d,%d)\n",my_car.x,my_car.y,end_x,end_y);
		}else{
			set_my_car(0, 0, 9*Constant.cell_length, 8*Constant.cell_length);
			System.out.printf("Appoint generate:(%d,%d)->(%d,%d)\n",my_car.x,my_car.y,end_x,end_y);
		}
		
		//随机生成路口,并设置其状态，存放在crossroads里面
		//简便起见,每个路口都设置红绿灯,(N-1)*(N-1)个
		for(int i=1;i<=Constant.N-1;++i) {
			for(int j=1;j<=Constant.N-1;++j) {
				CrossRoad tmp=new CrossRoad(Constant.red_light_time, Constant.green_light_time,i*Constant.cell_length,j*Constant.cell_length);
				//下面随机设置红绿灯的初始状态,以及允许通行的状态
				if(r.nextBoolean()) {//设置上下为红灯状态,即左右可通行
					tmp.redlight_time=r.nextInt(Constant.red_light_time-10)+10;//时间[10,Constant.red_light_time)
					tmp.greenlight_time=0;
					tmp.set_go_through(false);//设置左右可通行
				}else {
					tmp.redlight_time=0;
					tmp.greenlight_time=r.nextInt(Constant.red_light_time-10)+10;
					tmp.set_go_through(true);
				}
				//存储到crossroads中,若要查询,直接x/Constant.cell_length,y/Constant.cell_length作为索引查询即可
				crossroads[i][j]=tmp;	
			}
		}

	}
	public int wait_time=0;//my_car从起点到终点的过程中的等待时间,不能是static的,因为当多线程时,会导致重复加
	public static int total_time=0,success_cnt=0;
	public static int total_wait_time=0;
	@Override
	public void run() {
		rand=false;//不随机生成
		init();
		start_x=my_car.x;
		start_y=my_car.y;
		run(false);
		if(timer!=-1) {
			total_time+=timer;
			total_wait_time+=wait_time;
			++success_cnt;
//			//TODO:多次记录wait_time.dat
//			pout2.println(timer+","+wait_time);
		}else {
			System.out.printf("Failure(ThreadId=%d): (%3d,%3d)->(%3d,%3d)\n",
					Thread.currentThread().getId(),start_x,start_y,end_x,end_y);
			
		}
		clear();
	}
	
	/**
	 * 开始run simulation
	 * 1.车子的位置更新
	 * 2.路口红绿灯状态的更新
	 * 3.路口队列的更新,共4个队列
	 * @param enable_car_log
	 * @param trace_car_number
	 * 附带car日志功能
	 */
	public void run(boolean enable_car_log,int trace_car_number) {
		--trace_car_number;//索引从0开始，所有先减1
		int prev_x,prev_y;//保存my_car的上一个时间的坐标位置
		for(int i=0;i<run_time;++i) {
			if(enable_car_log) logger.car_log(other_cars.get(trace_car_number));
			if(my_car.x==end_x&&my_car.y==end_y) {
				System.out.printf("OK,used time: %ds\nwait time: %ds,travel time: %ds\n",
						i+1,wait_time,i+1-wait_time);
				timer=i+1;
				break;
			}
			//更新路口的状态(红绿灯)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}

			//更新车子的状态(还没有go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {//Bug!!!注意:已经入队的车子就不要再重复入队了
					//1.加入路口队列
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				//2.在岔路口,需要重新设置车子的next_direction,特别注意三岔路口,一定要更新,否则最后车子会收敛到四边徘徊
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			prev_x=my_car.x;
			prev_y=my_car.y;//保存my_car的上一个时间的坐标位置
			//更新车子的状态(开始go)
			//1.处理不在等待队列里的车子
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//不在等待队列中
					tmp.car_go();
				}
			}
			//2.处理在路口队列的车子的状态
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).处理上队列,允许其掉头返回
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {//上下绿灯，可走
								c.car_go();
								c.inQueue=false;//不在队列里面了
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {//上下红灯，即左右绿灯，可走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).处理下队列
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
					//3).处理左队列
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
					//4).处理右队列
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
	
//	//记录数据时,不能开多线程,否则数据会错乱
//	public static PrintWriter pout=null;//记录crossroads_queue.dat
//	public static PrintWriter pout2=null;//记录wait_time.dat
//	static {
//		try {
//			pout2=new PrintWriter(new FileWriter("wait_time.dat"),true);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		pout2.println("Total Time,Total Wait Time");
//	}
	/**
	 * 生成标准日志标准日志
	 * @param enable_standard_log
	 */
	public void run(boolean enable_standard_log) {
		if(enable_standard_log) {
			System.out.println("Initial State:");
			logger.cars_log(other_cars);
			logger.crossroads_log(crossroads);
		}
		int prev_x,prev_y;//保存my_car的上一个时间的坐标位置
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
			//若有其他位置到达目的地就让其消失,再补充相应车子
			//(其实修改车子的位置即可,也重新规划其目的地,因为目的地都在路口,不可能入队列,故队列不需要修复状态)
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
			//更新路口的状态(红绿灯)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
					//TODO:记录每个路口的队列长度
//					crossroad_queue_size[m][n][0]=crossroads[m][n].up_queue.size();
//					crossroad_queue_size[m][n][1]=crossroads[m][n].down_queue.size();
//					crossroad_queue_size[m][n][2]=crossroads[m][n].left_queue.size();
//					crossroad_queue_size[m][n][3]=crossroads[m][n].right_queue.size();
//					
//					pout.println(crossroad_queue_size[m][n][0]+","+crossroad_queue_size[m][n][1]
//							+","+crossroad_queue_size[m][n][2]+","+crossroad_queue_size[m][n][3]);
				}
			}
			//pout.println(-(i+1)+","+-(i+1)+","+-(i+1)+","+-(i+1));//把运行时间(i+1)当作结尾标记
			//更新车子的状态(还没有go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {//注意:已经入队的车子就不要再重复入队了
					//1.加入路口队列
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				//2.在岔路口,需要重新设置车子的next_direction,特别注意三岔路口,一定要更新,否则最后车子会收敛到四边徘徊
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					//TODO:考虑一个状况,当有多辆车有固定起点与终点时,仿真会发生什么情况
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			prev_x=my_car.x;
			prev_y=my_car.y;
			//更新车子的状态(开始go)
			//1.处理不在等待队列里的车子
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//不在等待队列中
					tmp.car_go();
				}
			}
			//2.处理在路口队列的车子的状态(右边绿灯往右可行,左边红灯往左可行)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).处理上队列,允许其掉头返回
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {//上下绿灯，可走
								c.car_go();
								c.inQueue=false;//不在队列里面了
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT) {
							if(crossroads[m][n].redlight_time>0) {//上下红灯，即左右绿灯，处于上方，可往左走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.RIGHT) {
							if(crossroads[m][n].greenlight_time>0) {//上下绿灯，即左右红灯，处于上方，可往右走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).处理下队列
					if(!crossroads[m][n].down_queue.isEmpty()) {
						Car c=crossroads[m][n].down_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.LEFT) {
							if(crossroads[m][n].greenlight_time>0) {//左右红灯(上下绿灯)才能走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}else if(c.direction==Direction.RIGHT) {
							if(crossroads[m][n].greenlight_time>0) {//左右绿灯(上下红灯)才能走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].down_queue.remove();
							}
						}
					}
					//3).处理左队列
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
					//4).处理右队列
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
	 * 打印对应坐标(trace_crossroad_x,trace_crossroad_y)路口的日志（四个等待队列，红绿灯状态）
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
			//更新路口的状态(红绿灯)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}
			
			//更新车子的状态(还没有go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {
					//1.加入路口队列
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					//2.在岔路口,需要重新设置车子的next_direction,特别注意三岔路口,一定要更新,否则最后车子会收敛到四边徘徊
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			//更新车子的状态(开始go)
			//2.再处理不在等待队列里的车子
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//不在等待队列中
					tmp.car_go();
				}
			}
			//1.先处理在路口队列的车子的状态
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).处理上队列
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.UP||c.direction==Direction.DOWN) {
							if(crossroads[m][n].greenlight_time>0) {//上下绿灯，可走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {//上下红灯，即左右绿灯，可走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).处理下队列
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
					//3).处理左队列
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
					//4).处理右队列
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
	 * my_car寻路,这个函数应该插入到run()里面的循环中运行
	  *  简单地以目前位置与目标位置的距离为基准,不断缩短两者间的距离,最终达到目的地
	 * 
	 */

	public void naive_search_direction(Car my_car,int end_x,int end_y) {
		if(my_car.inQueue) return;//在等待队列里面就不用调整方向了
		//判断按当前方向行驶的话能否使距离缩短,若不能就改变方向
		if(my_car.direction==Direction.UP) {
			if(end_y<my_car.y);//按照当前方向行驶会使距离缩短的话就不管它了
			else {//不能使距离缩短,其实这种情况只可能发生在路口
				//当前车子位置在目的点的上方
				if(end_x>my_car.x) {//确定当前车子位置在目的点的左上方
					//所以接下来往下或往右即可
					
					//注意:可选的这两个方向,应该去选绿灯状态的那个
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯(且是在队列头)
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}
					
				}else if(end_x<my_car.x) {//确定当前车子位置在目的点的右上方
					//所以接下来往下或往左即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_x==my_car.x){//确定当前车子位置在目的点的正上方
					//所以接下来往下即可
					my_car.direction=Direction.DOWN;
				}
			}
		}
		
		if(my_car.direction==Direction.DOWN) {
			if(end_y>my_car.y);//按照当前方向行驶会使距离缩短的话就不管它了
			else {//不能使距离缩短,其实这种情况只可能发生在路口
				//当前车子位置在目的点的下方
				if(end_x>my_car.x) {//确定当前车子位置在目的点的左下方
					//所以接下来往上或往右即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}
				}else if(end_x<my_car.x) {//确定当前车子位置在目的点的右下方
					//所以接下来往上或往左即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_x==my_car.x){//确定当前车子位置在目的点的正下方
					//所以接下来往上即可
					my_car.direction=Direction.UP;
				}
			}
		}
		
		if(my_car.direction==Direction.LEFT) {
			if(end_x<my_car.x);//按照当前方向行驶会使距离缩短的话就不管它了
			else {//不能使距离缩短,其实这种情况只可能发生在路口
				//当前车子位置在目的点的左方
				if(end_y>my_car.y) {//确定当前车子位置在目的点的左上方
					//所以接下来往下或往右即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.RIGHT;
					}
				}else if(end_y<my_car.y) {//确定当前车子位置在目的点的左下方
					//所以接下来往上或往右即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.RIGHT;
					}
				}else if(end_y==my_car.y){//确定当前车子位置在目的点的正左方
					//所以接下来往右即可
					my_car.direction=Direction.RIGHT;
				}
			}
		}
		
		if(my_car.direction==Direction.RIGHT) {
			if(end_x>my_car.x);//按照当前方向行驶会使距离缩短的话就不管它了
			else {//不能使距离缩短,其实这种情况只可能发生在路口
				//当前车子位置在目的点的右方
				if(end_y>my_car.y) {//确定当前车子位置在目的点的右上方
					//所以接下来往下或往左即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.DOWN;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_y<my_car.y) {//确定当前车子位置在目的点的右下方
					//所以接下来往上或往左即可
					if(my_car.is_crossroad()) {//在十字路口才需要判断红绿灯
						if(crossroads[my_car.x/Constant.cell_length][my_car.y/Constant.cell_length].greenlight_time>0) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}else {//在三叉路口,就随便选一个方向即可
						if(r.nextBoolean()) my_car.direction=Direction.UP;
						else my_car.direction=Direction.LEFT;
					}
				}else if(end_y==my_car.y){//确定当前车子位置在目的点的正右方
					//所以接下来往左即可
					my_car.direction=Direction.LEFT;
				}
			}
		}
		
	}
	
	/**
	 * 
	 * 因为其他车子在路口的next_direction是随机设置的,所以用dfs的话是不能保证回溯前和回溯后的行为一致
	 * 而且my_car走不同的路径的话也会影响到其它车的行为
	 * 所以dfs适用的场景其实是:不存在其他车子,只有红绿灯存在的情况
	 * 故这个函数其实是无法使用在dfs里面的
	 * @param direction
	 * my_car从当前路口沿着direction方向达到另一个路口(包括三岔路口)
	 */
	public void next_reaches(Direction direction) {
		int tmp_x=my_car.x,tmp_y=my_car.y;
		for(;;) {
			if(my_car.is_crossroad()&&(my_car.x!=tmp_x||my_car.y!=tmp_y)){
				return;
			}
			//更新路口的状态(红绿灯)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}
			//更新车子的状态(还没有go)
			for(Car tmp:other_cars) {
				if(tmp.is_crossroad()&&!tmp.inQueue) {//注意:已经入队的车子就不要再重复入队了
					//1.加入路口队列
					if(tmp.direction==Direction.UP) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].down_queue.add(tmp);
					if(tmp.direction==Direction.DOWN) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].up_queue.add(tmp);
					if(tmp.direction==Direction.LEFT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].right_queue.add(tmp);
					if(tmp.direction==Direction.RIGHT) crossroads[tmp.x/Constant.cell_length][tmp.y/Constant.cell_length].left_queue.add(tmp);
					tmp.inQueue=true;
				}
				//2.在岔路口,需要重新设置车子的next_direction,特别注意三岔路口,一定要更新,否则最后车子会收敛到四边徘徊
				if(tmp.x%Constant.cell_length==0&&tmp.y%Constant.cell_length==0) {
					if(tmp.number==Constant.cars_number+1) naive_search_direction(my_car,end_x,end_y);
					else naive_search_direction(other_cars.get(tmp.number-1),car_dst.get(tmp.number-1).x,car_dst.get(tmp.number-1).y);
				}
			}
			
			//覆盖掉my_car的direction
			my_car.direction=direction;
			//更新车子的状态(开始go)
			//1.处理不在等待队列里的车子
			for(Car tmp:other_cars) {
				if(!tmp.inQueue) {//不在等待队列中
					tmp.car_go();
				}
			}
			//2.处理在路口队列的车子的状态
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					//1).处理上队列,允许其掉头返回
					if(!crossroads[m][n].up_queue.isEmpty()) {
						Car c=crossroads[m][n].up_queue.peek();
						if(c.direction==Direction.DOWN||c.direction==Direction.UP) {
							if(crossroads[m][n].greenlight_time>0) {//上下绿灯，可走
								c.car_go();
								c.inQueue=false;//不在队列里面了
								crossroads[m][n].up_queue.remove();
							}
						}else if(c.direction==Direction.LEFT||c.direction==Direction.RIGHT) {
							if(crossroads[m][n].redlight_time>0) {//上下红灯，即左右绿灯，可走
								c.car_go();
								c.inQueue=false;
								crossroads[m][n].up_queue.remove();
							}
						}
					}
					//2).处理下队列
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
					//3).处理左队列
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
					//4).处理右队列
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
	 * 这其实才是能用在dfs中的操作函数(事实证明,dfs不适用于这种动态问题)
	 * 使用时,须保证没有其他车子,配置Constant.cars_number=0
	 * @param direction
	 */
	public void next_reaches2(Direction direction) {
		int tmp_x=my_car.x,tmp_y=my_car.y;
		for(;;) {
			if(my_car.is_crossroad()&&(my_car.x!=tmp_x||my_car.y!=tmp_y)){
				return;
			}
			//更新路口的状态(红绿灯)
			for(int m=1;m<=Constant.N-1;++m) {
				for(int n=1;n<=Constant.N-1;++n) {
					crossroads[m][n].update_state();
				}
			}
			
			//覆盖掉my_car的direction
			my_car.direction=direction;
			my_car.can_go(direction);
			
		}
		
	}
	
	

}
