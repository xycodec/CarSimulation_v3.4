package car_simulation;

import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void demo_1(CarSimulation carsimulation) {
		carsimulation.init();
		carsimulation.run(true);
		carsimulation.clear();
	}
	
	public static void demo_2(CarSimulation carsimulation) {
		carsimulation.init();
		carsimulation.run(true,Constant.cars_number+1);
		carsimulation.clear();
	}
	
	//1000 simulation
	public static void demo_3(CarSimulation carsimulation) {
		ArrayList<Integer> success_runtime=new ArrayList<>();
		int total_time=0,success_cnt=0;
		for(int i=0;i<1000;++i) {
			carsimulation.init();
			carsimulation.run(false);
			if(carsimulation.timer!=-1) {
				total_time+=carsimulation.timer;
				++success_cnt;
				success_runtime.add(carsimulation.timer);
			}else {
				System.out.printf("Failure: (%3d,%3d)->(%3d,%3d)\n",
						carsimulation.start_x,carsimulation.start_y,carsimulation.end_x,carsimulation.end_y);
			}
			carsimulation.clear();
		}
		System.out.printf("Success Times: %d\nAverage Time: %.2f unit time\n",success_cnt,total_time/(double)success_cnt);
		int cnt=0;
		for(int n:success_runtime) {
			if(cnt==10) {
				cnt=0;
				System.out.println();
			}
			++cnt;
			System.out.printf("%d ",n);
		}
	}
	
	public static void main(String[] args) {
//		CarSimulation carsimulation=new CarSimulation();
//		demo_1(carsimulation);
//		demo_2(carsimulation);
		
		
		CarSimulation.total_time=0;
		CarSimulation.total_wait_time=0;
		CarSimulation.success_cnt=0;
		List<Thread> thread_list=new ArrayList<>();
		for(int i=0;i<10;++i) {
			Thread t=new Thread(new CarSimulation());
			t.start();
			thread_list.add(t);
		}
		//不能边start边join,不然就无法并发,会导致这样的执行顺序:t1->t2->t3->....(实际上的顺序执行)
		try {
			for(Thread t:thread_list) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(CarSimulation.success_cnt>0)//success_cnt可能为0
			System.out.printf("Success Times: %d\nAverage Time: %.2f unit time\nAverage Wait Time: %.2f unit time\n",
					CarSimulation.success_cnt,CarSimulation.total_time/(double)CarSimulation.success_cnt,
					CarSimulation.total_wait_time/(double)CarSimulation.success_cnt);
		else System.out.println("Warning: Success Times is zero!");
		
		
//		//记录Total Time,Wait Time
//		for(int k=0;k<500;++k) {
//			CarSimulation.total_time=0;
//			CarSimulation.success_cnt=0;
//			CarSimulation.total_wait_time=0;
//			List<Thread> thread_list=new ArrayList<>();
//			for(int i=0;i<20;++i) {
//				Thread t=new Thread(new CarSimulation());
//				t.start();
//				thread_list.add(t);
//			}
//			//不能边start边join,不然就无法并发,会导致这样的执行顺序:t1->t2->t3->....(实际上的顺序执行)
//			try {
//				for(Thread t:thread_list) {
//					t.join();
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			if(CarSimulation.success_cnt>0)//success_cnt可能为0
//				System.out.printf("%.2f,%.2f\n",
//					CarSimulation.total_time/(double)CarSimulation.success_cnt,
//					CarSimulation.total_wait_time/(double)CarSimulation.success_cnt);
//			else System.out.println("Warning: Success Times is zero!");
//			
//			Constant.cars_number+=100;
//		}

	}

}
