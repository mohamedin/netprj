package sim.util;



public class LogNormal implements Distribution{

	private double mean; 
	private double sd;
	public LogNormal(double mean, double sd) {
		this.mean = mean;
		this.sd = sd;
	}
	
	public double getSample(){
		double x = Math.random();
		return Math.exp(-1* Math.pow((Math.log(x)-mean), 2) / (2 * sd*sd) ) / (x * sd * Math.pow(2*Math.PI, .5));
	}
	
	public static void main(String[] args) {
		LogNormal exponential = new LogNormal(0, 1);
		for(int i=0; i<100; i++)
			System.out.println(exponential.getSample());
	}
}
