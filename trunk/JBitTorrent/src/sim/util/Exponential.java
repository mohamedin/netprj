package sim.util;



public class Exponential implements Distribution{

	private double lamda; 
	public Exponential(double lamda) {
		this.lamda = lamda;
	}
	
	public double getSample(){
		return lamda / Math.exp(lamda * Math.random());
	}
	
	public static void main(String[] args) {
		Exponential exponential = new Exponential(0.7);
		for(int i=0; i<100; i++)
			System.out.println(exponential.getSample());
	}
}
