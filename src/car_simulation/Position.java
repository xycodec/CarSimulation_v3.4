package car_simulation;


public class Position implements Cloneable{
	public int x,y;

	public Position() {
		
	}
	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	@Override
	public int hashCode() {
		return (x+11)*(y+13)+super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		Position p=(Position)obj;
		if(p.x==x&&p.y==y) return true;
		else return false;
	}
	
	@Override
	public Position clone() {
		Position pos=null;
		try {
			pos=(Position)super.clone();
		}catch(CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return pos;
	}
	
}
