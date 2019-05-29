package car_simulation;

import java.util.Random;

/**
 * 一些常量配置信息
 * @author xycode
 *
 */
public class Constant {
	public static final int speed=1;//汽车的默认速度
	public static final int cell_length=50;//路口之间街道的长度
	public static final int N=10;//公路网格10*10
	public static final int width=cell_length*N,height=cell_length*N;
//	public static final int crossroads_time_limit=2;//通过路口所需的时间
	public static final int red_light_time=30,green_light_time=20;//上下路口红绿灯的持有时间(所剩时间)
	public static int cars_number=10000;//其他车子,不包括my_car
	
	public static Random r=new Random();
	public static int[][] speed_matrix;
	public static int[] speed_level= {1,1,1,2,2,5};
	static {
		speed_matrix=new int[N+1][N+1];
		for(int i=0;i<N+1;++i) {
			for(int j=0;j<N+1;++j) {
				speed_matrix[i][j]=speed_level[r.nextInt(speed_level.length)];//默认一条路上两个方向的限速相同
			}
		}
	}
	
}
