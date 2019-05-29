package car_simulation;

import java.util.Random;

/**
 * һЩ����������Ϣ
 * @author xycode
 *
 */
public class Constant {
	public static final int speed=1;//������Ĭ���ٶ�
	public static final int cell_length=50;//·��֮��ֵ��ĳ���
	public static final int N=10;//��·����10*10
	public static final int width=cell_length*N,height=cell_length*N;
//	public static final int crossroads_time_limit=2;//ͨ��·�������ʱ��
	public static final int red_light_time=30,green_light_time=20;//����·�ں��̵Ƶĳ���ʱ��(��ʣʱ��)
	public static int cars_number=10000;//��������,������my_car
	
	public static Random r=new Random();
	public static int[][] speed_matrix;
	public static int[] speed_level= {1,1,1,2,2,5};
	static {
		speed_matrix=new int[N+1][N+1];
		for(int i=0;i<N+1;++i) {
			for(int j=0;j<N+1;++j) {
				speed_matrix[i][j]=speed_level[r.nextInt(speed_level.length)];//Ĭ��һ��·�����������������ͬ
			}
		}
	}
	
}
