package sim;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import sim.util.Distribution;
import sim.util.Exponential;

public class Scheduler{
	private static int counterGenerator = 0;
	private Distribution interarrvialDist;
	
	private Vector<PeerProcessManager> pool = new Vector<PeerProcessManager>();
	
	private List<PeerProcessManager> active = Collections.synchronizedList(new ArrayList<PeerProcessManager>());
	
	private int simulationPoints;

	private Thread statisticsCollector;
	
	private void createPeers(String className, int count, double availablityMean, double availablitySD, double reliablity, int uploadRate, int downloadRate){
		for(int i=0; i<count; i++)
			pool.add(new PeerProcessManager(className + "[" + (counterGenerator++) + "]", availablityMean, availablitySD, reliablity, uploadRate, downloadRate));
	}
	
	public Scheduler(int simulationPoints) {
		this.simulationPoints = simulationPoints;
		this.interarrvialDist = new Exponential(Constants.INTER_ARRIVAL_LMDA);
		new Thread(){
			@Override
			public void run() {
				new JOptionPane("Scheduler Started...\nPress the button to terminate!").createDialog(new JFrame(), "Scheduler").show();
				terminate();
			}
		}.start();
	}
	
	private void log(String message){
		System.out.println("Scheduler>>" + message);
	}
	
	private void createPool(String configPath) throws NumberFormatException, IOException {
		BufferedReader conf = new BufferedReader(new FileReader(configPath));
		String line;
		while((line=conf.readLine())!=null){
			String[] tokens = line.split(",");
			createPeers(
						tokens[0],						// class name
						Integer.parseInt(tokens[1]),	// count 
						Double.parseDouble(tokens[2]),Double.parseDouble(tokens[3]), 	// availablity mean and standard deviation
						Double.parseDouble(tokens[4]), 	// reliability
						Integer.parseInt(tokens[5]), Integer.parseInt(tokens[6])	// transfer rates
					);
		}
		log("Pool created at size:" + pool.size());
	}
	
	private List<Long> timeWritter = new LinkedList<Long>();
	private List<Integer> allWritter = new LinkedList<Integer>();
	private List<Integer> allSeedersWritter = new LinkedList<Integer>();
	private List<Integer> activeWritter = new LinkedList<Integer>();
	private List<Integer> activeSeedersWritter = new LinkedList<Integer>();

	private void createStatisticsCollector(){
		statisticsCollector = new Thread(){
			public void run() {
				try {
					while(simulationPoints>0){
						simulationPoints--;
						try {
							sleep(Constants.COLLECTING_TIME);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						int activePeers = 1;
						int activeSeeders = 1;
						int allSeeders = 1;
						for(Iterator<PeerProcessManager> itr=active.iterator(); itr.hasNext();){
							PeerProcessManager manager = itr.next();
							if(manager.isAlive())
								activePeers++;
							if(manager.isSeeder())
								allSeeders++;
							if(manager.isAlive()&&manager.isSeeder())
								activeSeeders++;
							
						}
						String status = new Date() + ",All=" + active.size() + ",Active=" + activePeers + ",ActiveSeeders=" + activeSeeders + ",ActiveLeechers" + (activePeers-activeSeeders);
						log(status);

						timeWritter.add(new Date().getTime());
						allWritter.add(active.size());
						allSeedersWritter.add(allSeeders);
						activeWritter.add(activePeers);
						activeSeedersWritter.add(activeSeeders);
						
						dumpData();
					}
				} finally{
					terminate();
				}
			}
		};
	}
	
	private int getInterArrivalTime(){
		return (int) (interarrvialDist.getSample() * Constants.MILLI_IN_MINUTE);
	}

	public void start() throws IOException, InterruptedException {
		statisticsCollector.start();
		while(true){
			for(int i=0; i<Constants.ARRIVAL_GROUP_SIZE && pool.size()>0; i++){
				PeerProcessManager selected = pool.remove((int)(Math.random() * (pool.size()-1)));
				active.add(selected); 		// start invoking peers processes
				selected.start();
			}
			Thread.sleep(getInterArrivalTime());
		}
	}
	
	private void alert(){
		for(int i=0; i<20; i++){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Toolkit.getDefaultToolkit().beep();
		}	
	}
	
	private void dumpData(){
		PrintWriter dataWriter = null;
		try {
			dataWriter = new PrintWriter(new File(Constants.PEERS_FOLDER + new SimpleDateFormat("MM-dd hh-mm-ss").format(new Date()) + ".csv"));
			dataWriter.println("Experement " + new Date());
			
			dataWriter.println();
			dataWriter.print("Time");
			for(Iterator<Long> itr=timeWritter.iterator(); itr.hasNext(); dataWriter.print("," + itr.next()));
			
			dataWriter.println();
			dataWriter.print("Steps");
			Long start = timeWritter.get(0);
			for(Iterator<Long> itr=timeWritter.iterator(); itr.hasNext(); dataWriter.print("," + (itr.next()-start)));
			
			dataWriter.println();
			dataWriter.print("All");
			for(Iterator<Integer> itr=allWritter.iterator(); itr.hasNext(); dataWriter.print("," + itr.next()));
			
			dataWriter.println();
			dataWriter.print("All Seeders");
			for(Iterator<Integer> itr=allSeedersWritter.iterator(); itr.hasNext(); dataWriter.print("," + itr.next()));

			dataWriter.println();
			dataWriter.print("Active Peers");
			for(Iterator<Integer> itr=activeWritter.iterator(); itr.hasNext(); dataWriter.print("," + itr.next()));

			dataWriter.println();
			dataWriter.print("Active Seeders");
			for(Iterator<Integer> itr=activeSeedersWritter.iterator(); itr.hasNext(); dataWriter.print("," + itr.next()));
			
			dataWriter.println();
			dataWriter.print("Active Leechers");
			for(Iterator<Integer> itr=activeSeedersWritter.iterator(), itrAll=activeWritter.iterator(); itr.hasNext(); dataWriter.print("," + (itrAll.next() - itr.next())));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			if(dataWriter!=null)
				dataWriter.close();
		}
	}
	
	private void terminate(){
		for(Iterator<PeerProcessManager> itr=active.iterator(); itr.hasNext(); itr.next().disconnect());
		dumpData();
		alert();
	    System.exit(0);
	}
	public static void main(String[] args) throws IOException, InterruptedException {
		Scheduler scheduler = new Scheduler(Constants.SIMULATION_POINTS);
		scheduler.createPool("conf");
		scheduler.createStatisticsCollector();
		scheduler.start();
	}
}
