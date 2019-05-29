package car_simulation;

import java.util.Random;

public class Car implements Cloneable{
	public static Random r=new Random();
	public int number;//���ӵı��
	public int x,y;//��������λ��,ע��:�Ϸ�������ֵҪ��x,y������һ����������10
	public int speed;
	public boolean inQueue;//�ڵȴ���������,�ڶ��������˵��һ���ں��̵�·��
	public Direction direction;
	
	public Car() {
		
	}
	
	public Car(int x, int y,Direction direction,int number) {
		this.x = x;
		this.y = y;
		this.speed = Constant.speed;
		this.inQueue = false;
		this.direction=direction;
		this.number=number;
	}
	
	public Car(int x, int y,int number) {
		this.x = x;
		this.y = y;
		this.speed = Constant.speed;
		this.inQueue = false;
		this.number=number;
	}
	
	@Override
	public Car clone() {
		Car car=null;
		try {
			car=(Car)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return car;
	}
	
	/**
	 * �ж��ܷ����ŵ�ǰ������
	 * @param direction
	 * @return
	 */
	public boolean can_go(Direction direction) {
		if(direction==Direction.UP) {
			if(y-speed>=0) return true;
		}
		else if(direction==Direction.DOWN) {
			if(y+speed<=Constant.height) return true; 
		}
		else if(direction==Direction.LEFT) {
			if(x-speed>=0) return true;
		}
		else if(direction==Direction.RIGHT) {
			if(x+speed<=Constant.width) return true;
		}
		return false;
	}
	
	//Ҫ����·��״̬�����Ƿ���øú���
	public void car_go() {
		//��ʻ��������,��������ʻ����
		//���Ͻ�
		if(x==0&&y==0) {
			if(direction==Direction.LEFT||direction==Direction.UP) {
				if(r.nextBoolean()) direction=Direction.DOWN;
				else direction=Direction.RIGHT;
			}
		}
		//���½�
		if(x==0&&y==Constant.height) {//��ʻ��������,���������䷽��
			if(direction==Direction.LEFT||direction==Direction.DOWN) {
				if(r.nextBoolean()) direction=Direction.UP;
				else direction=Direction.RIGHT;
			}
		}
		//���Ͻ�
		if(x==Constant.width&&y==0) {//��ʻ��������,���������䷽��
			if(direction==Direction.UP||direction==Direction.RIGHT) {
				if(r.nextBoolean()) direction=Direction.DOWN;
				else direction=Direction.LEFT;
			}
		}
		//���½�
		if(x==Constant.width&&y==Constant.height) {//��ʻ��������,���������䷽��
			if(direction==Direction.DOWN||direction==Direction.RIGHT) {
				if(r.nextBoolean()) direction=Direction.UP;
				else direction=Direction.LEFT;
			}
		}
		//���
		if(x==0&&(y>0&&y<Constant.height&&y%10==0)) {
			if(direction==Direction.LEFT) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.UP;
				else if(tmp==1) direction=Direction.DOWN;
				else direction=Direction.RIGHT;
			}
		}
		//����
		if((x>0&&x<Constant.width&&x%10==0)&&y==0) {
			if(direction==Direction.UP) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.LEFT;
				else if(tmp==1) direction=Direction.DOWN;
				else direction=Direction.RIGHT;
			}
		}
		//����
		if(x==Constant.width&&(y>0&&y<Constant.height&&y%10==0)) {
			if(direction==Direction.RIGHT) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.LEFT;
				else if(tmp==1) direction=Direction.DOWN;
				else direction=Direction.UP;
			}
		}
		//����
		if((x>0&&x<Constant.width&&x%10==0)&&y==Constant.height) {
			if(direction==Direction.DOWN) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.LEFT;
				else if(tmp==1) direction=Direction.RIGHT;
				else direction=Direction.UP;
			}
		}
		
		//add function:
		//���ݵ�ǰ�����Ľֵ������ٶȵ���
		if(x>=0&&y>=0)
			speed=Constant.speed_matrix[x/Constant.cell_length][y/Constant.cell_length];
		//go!!!
		if(direction==Direction.UP) y-=speed;
		else if(direction==Direction.DOWN) y+=speed;
		else if(direction==Direction.LEFT) x-=speed;
		else if(direction==Direction.RIGHT) x+=speed;
	}

	/*
	 * ��Ϊ״̬���»���·��״̬���,����״̬���²������д
	 */
//	public void update_state() {
//
//	}
	
	public boolean is_crossroad() {
		if(x>0&&x<Constant.width&&y>0&&y<Constant.height) {//ȷ�������ı�
			if(x%Constant.cell_length==0&&y%Constant.cell_length==0) {//��·��λ��
				return true;
			}
		}
		return false;
	}
	
	

}
