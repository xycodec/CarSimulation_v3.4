package utils;

import java.util.ArrayList;

import car_simulation.Car;
import car_simulation.Constant;
import car_simulation.CrossRoad;

/**
 * 日志生成器
 * 支持car,crossroad
 * @author xycode
 *
 */
public class Logger {

	public Logger() {
		
	}
	
	public static void cars_log(ArrayList<Car> cars) {
		for(int i=0;i<cars.size();++i) {
			car_log(cars.get(i));
		}

	}
	
	public static void car_log(Car car) {
		System.out.printf("Car_%3d reachs (%3d,%3d)\n",car.number,car.x,car.y);
	}
	
	public static void crossroads_log(CrossRoad[][] crossroads) {
		for(int m=1;m<=Constant.N-1;++m) {
			for(int n=1;n<=Constant.N-1;++n) {
				crossroad_log(crossroads[m][n]);
			}
		}
	}
	
	public static void crossroad_log(CrossRoad crossroad) {
		//红绿灯状态
		System.out.printf("Crossroad(%3d,%3d):redlight: %3ds, greenlight: %3ds\n",
				crossroad.x,crossroad.y,crossroad.redlight_time,crossroad.greenlight_time);
		//四个等待队列
		System.out.printf("Up Queue: ");
		if(crossroad.up_queue.isEmpty()) System.out.printf("Empty");
		else {
			for(Car c:crossroad.up_queue) {
				System.out.printf("Car_%3d, ",c.number);
			}
		}
		System.out.printf("\nDown Queue: ");
		if(crossroad.down_queue.isEmpty()) System.out.printf("Empty");
		else {
			for(Car c:crossroad.down_queue) {
				System.out.printf("Car_%3d, ",c.number);
			}
		}
		System.out.printf("\nLeft Queue: ");
		if(crossroad.left_queue.isEmpty()) System.out.printf("Empty");
		else {
			for(Car c:crossroad.left_queue) {
				System.out.printf("Car_%3d, ",c.number);
			}
		}
		System.out.printf("\nRight Queue: ");
		if(crossroad.right_queue.isEmpty()) System.out.printf("Empty");
		else {
			for(Car c:crossroad.right_queue) {
				System.out.printf("Car_%3d, ",c.number);
			}
		}
		System.out.println();
		System.out.println();
	}
	
	
	
}
