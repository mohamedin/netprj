package sim.util;

import sim.Constants;

public class Test {

	private static String millToMinute(long l){
		int minutes = (int)(l / Constants.MILLI_IN_MINUTE);
		l -= minutes * Constants.MILLI_IN_MINUTE;
		int seconds = (int)(l / 1000);
		l -= seconds * 1000;
		return minutes + ":" + seconds + ":" + l;
	}
	
	public static void main(String[] args) {
		LogNormal logNormal = new LogNormal(1.5, 1);	// availablity
		Exponential exponential = new Exponential(.05);	// relibality
		for(int i=0; i<100; i++)
			System.out.println(millToMinute((long)(logNormal.getSample() * Constants.MILLI_IN_MINUTE * 10)) + "   " + millToMinute((long)(exponential.getSample() * Constants.MILLI_IN_MINUTE * 10)));
	}
}
