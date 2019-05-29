package utils;

import java.util.ArrayList;

import car_simulation.Car;
import car_simulation.CarSimulation;
import car_simulation.Constant;
import car_simulation.CrossRoad;

/**
 * ���ڱ���carsimulation������״̬,Ҳ�����������״̬���ָ�֮ǰ������״̬
 * @author xycode
 *
 */
public class SimulationState {
	public CrossRoad[][] crossroads;//·������
	public ArrayList<Car> other_cars;//���������ļ���
	public Car my_car;//�Լ��ĳ���
	public int start_x,start_y,end_x,end_y;
	
	public SimulationState() {
		crossroads=new CrossRoad[Constant.N+1][Constant.N+1];
		other_cars=new ArrayList<>();
	}
	
	/**
	 * 	״̬�ָ�����
	 *  ��carsimulation��״̬�ָ�Ϊ�����״̬
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
		carsimulation.my_car=carsimulation.other_cars.get(Constant.cars_number);//���ﲻ��ֱ�ӿ�¡,������ܻᵼ��my_car״̬��һ��
		
		carsimulation.start_x=this.start_x;
		carsimulation.start_y=this.start_y;
		carsimulation.end_x=this.end_x;
		carsimulation.end_x=this.end_y;
	}
	
	/**
	 * ״̬�洢����
	 * �洢carsimulation��״̬
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
		this.my_car=this.other_cars.get(Constant.cars_number);//���ﲻ��ֱ�ӿ�¡,������ܻᵼ��my_car״̬��һ��
		
		this.start_x=carsimulation.start_x;
		this.start_y=carsimulation.start_y;
		this.end_x=carsimulation.end_x;
		this.end_x=carsimulation.end_y;

	}
	
//	//����
//	public static void main(String[] args) {
//		CarSimulation carsimulation=new CarSimulation();
//		carsimulation.init();
//		SimulationState state_1=new SimulationState();
//		state_1.store_state(carsimulation);//�洢״̬
//		carsimulation.logger.crossroads_log(carsimulation.crossroads);
//		System.out.println("-----------------------------����Ϊ��ʼ״̬------------------------------");
//		carsimulation.run(false);
//		carsimulation.logger.crossroads_log(carsimulation.crossroads);
//		System.out.println("-----------------------------����Ϊ���н������м�״̬------------------------------");
//		state_1.recover_state(carsimulation);//�ָ�״̬
//		carsimulation.logger.crossroads_log(carsimulation.crossroads);
//		System.out.println("-----------------------------����Ϊ���ջָ��õ�״̬------------------------------");
//		//������������֪,ǰ��״̬һ��,����������
//	}
	
	
}
