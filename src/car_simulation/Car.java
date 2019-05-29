package car_simulation;

import java.util.Random;

public class Car implements Cloneable{
	public static Random r=new Random();
	public int number;//车子的编号
	public int x,y;//车子所在位置,注意:合法的坐标值要求x,y至少有一个能整除以10
	public int speed;
	public boolean inQueue;//在等待队列里面,在队列里面就说明一定在红绿灯路口
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
	 * 判断能否沿着当前方向走
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
	
	//要根据路口状态决定是否调用该函数
	public void car_go() {
		//行驶到角落了,调整其行驶方向
		//左上角
		if(x==0&&y==0) {
			if(direction==Direction.LEFT||direction==Direction.UP) {
				if(r.nextBoolean()) direction=Direction.DOWN;
				else direction=Direction.RIGHT;
			}
		}
		//左下角
		if(x==0&&y==Constant.height) {//行驶到角落了,重新设置其方向
			if(direction==Direction.LEFT||direction==Direction.DOWN) {
				if(r.nextBoolean()) direction=Direction.UP;
				else direction=Direction.RIGHT;
			}
		}
		//右上角
		if(x==Constant.width&&y==0) {//行驶到角落了,重新设置其方向
			if(direction==Direction.UP||direction==Direction.RIGHT) {
				if(r.nextBoolean()) direction=Direction.DOWN;
				else direction=Direction.LEFT;
			}
		}
		//右下角
		if(x==Constant.width&&y==Constant.height) {//行驶到角落了,重新设置其方向
			if(direction==Direction.DOWN||direction==Direction.RIGHT) {
				if(r.nextBoolean()) direction=Direction.UP;
				else direction=Direction.LEFT;
			}
		}
		//左边
		if(x==0&&(y>0&&y<Constant.height&&y%10==0)) {
			if(direction==Direction.LEFT) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.UP;
				else if(tmp==1) direction=Direction.DOWN;
				else direction=Direction.RIGHT;
			}
		}
		//上面
		if((x>0&&x<Constant.width&&x%10==0)&&y==0) {
			if(direction==Direction.UP) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.LEFT;
				else if(tmp==1) direction=Direction.DOWN;
				else direction=Direction.RIGHT;
			}
		}
		//右面
		if(x==Constant.width&&(y>0&&y<Constant.height&&y%10==0)) {
			if(direction==Direction.RIGHT) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.LEFT;
				else if(tmp==1) direction=Direction.DOWN;
				else direction=Direction.UP;
			}
		}
		//下面
		if((x>0&&x<Constant.width&&x%10==0)&&y==Constant.height) {
			if(direction==Direction.DOWN) {
				int tmp=r.nextInt(3);
				if(tmp==0) direction=Direction.LEFT;
				else if(tmp==1) direction=Direction.RIGHT;
				else direction=Direction.UP;
			}
		}
		
		//add function:
		//根据当前所处的街道设置速度档次
		if(x>=0&&y>=0)
			speed=Constant.speed_matrix[x/Constant.cell_length][y/Constant.cell_length];
		//go!!!
		if(direction==Direction.UP) y-=speed;
		else if(direction==Direction.DOWN) y+=speed;
		else if(direction==Direction.LEFT) x-=speed;
		else if(direction==Direction.RIGHT) x+=speed;
	}

	/*
	 * 因为状态更新还与路口状态相关,所以状态更新不在这儿写
	 */
//	public void update_state() {
//
//	}
	
	public boolean is_crossroad() {
		if(x>0&&x<Constant.width&&y>0&&y<Constant.height) {//确保不再四边
			if(x%Constant.cell_length==0&&y%Constant.cell_length==0) {//在路口位置
				return true;
			}
		}
		return false;
	}
	
	

}
