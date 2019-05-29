package utils;

import java.util.ArrayList;

import car_simulation.Car;
import car_simulation.CarSimulation;
import car_simulation.Constant;
import car_simulation.CrossRoad;

/**
 * 用于保存carsimulation的运行状态,也可用所保存的状态来恢复之前的运行状态
 * @author xycode
 *
 */
public class SimulationState {
	public CrossRoad[][] crossroads;//路口数组
	public ArrayList<Car> other_cars;//其它车辆的集合
	public Car my_car;//自己的车辆
	public int start_x,start_y,end_x,end_y;
	
	public SimulationState() {
		crossroads=new CrossRoad[Constant.N+1][Constant.N+1];
		other_cars=new ArrayList<>();
	}
	
	/**
	 * 	状态恢复函数
	 *  将carsimulation的状态恢复为该类的状态
	 *  this_state-->carsimulation_state
	 * @param carsimulation
	 */

	public void recover_state(CarSimulation carsimulation) {
		carsimulation.crossroads=new CrossRoad[Constant.N+1][Constant.N+1];
		for(int m=1;m<=Constant.N-1;++m) {
			for(int n=1;n<=Constant.N-1;++n) {
				carsimulation.crossroads[m][n]=this.crossroads[m][n].clone();
			}
		}
		
		carsimulation.other_cars=new ArrayList<>();
		for(Car car:this.other_cars) {
			carsimulation.other_cars.add(car.clone());
		}
		carsimulation.my_car=carsimulation.other_cars.get(Constant.cars_number);//这里不能直接克隆,否则可能会导致my_car状态不一致
		
		carsimulation.start_x=this.start_x;
		carsimulation.start_y=this.start_y;
		carsimulation.end_x=this.end_x;
		carsimulation.end_x=this.end_y;
	}
	
	/**
	 * 状态存储函数
	 * 存储carsimulation的状态
	 * carsimulation_state-->this_state
	 * @param carsimulation
	 */
	public void store_state(CarSimulation carsimulation) {
		this.crossroads=new CrossRoad[Constant.N+1][Constant.N+1];
		for(int m=1;m<=Constant.N-1;++m) {
			for(int n=1;n<=Constant.N-1;++n) {
				this.crossroads[m][n]=carsimulation.crossroads[m][n].clone();
			}
		}
		
		this.other_cars=new ArrayList<>();
		for(Car car:carsimulation.other_cars) {
			this.other_cars.add(car.clone());
		}
		this.my_car=this.other_cars.get(Constant.cars_number);//这里不能直接克隆,否则可能会导致my_car状态不一致
		
		this.start_x=carsimulation.start_x;
		this.start_y=carsimulation.start_y;
		this.end_x=carsimulation.end_x;
		this.end_x=carsimulation.end_y;

	}
	
//	//测试
//	public static void main(String[] args) {
//		CarSimulation carsimulation=new CarSimulation();
//		carsimulation.init();
//		SimulationState state_1=new SimulationState();
//		state_1.store_state(carsimulation);//存储状态
//		carsimulation.logger.crossroads_log(carsimulation.crossroads);
//		System.out.println("-----------------------------以上为初始状态------------------------------");
//		carsimulation.run(false);
//		carsimulation.logger.crossroads_log(carsimulation.crossroads);
//		System.out.println("-----------------------------以上为运行结束的中间状态------------------------------");
//		state_1.recover_state(carsimulation);//恢复状态
//		carsimulation.logger.crossroads_log(carsimulation.crossroads);
//		System.out.println("-----------------------------以上为最终恢复好的状态------------------------------");
//		//根据输出结果可知,前后状态一致,即功能正常
//	}
	
	
}
