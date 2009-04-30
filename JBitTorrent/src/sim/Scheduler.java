package sim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Scheduler{
	private static int counterGenerator = 0;
	
	private Vector<PeerProcessManager> pool = new Vector<PeerProcessManager>();
	
	private List<PeerProcessManager> active = Collections.synchronizedList(new ArrayList<PeerProcessManager>());
	
	private int simulationTime, collectingUnit;

	private Thread statisticsCollector;
	
	private void createPeers(String className, int count, int duration, int reliablity, int uploadRate, int downloadRate){
		for(int i=0; i<count; i++)
			pool.add(new PeerProcessManager(className + "[" + (counterGenerator++) + "]", duration, reliablity, uploadRate, downloadRate));
	}
	
	public Scheduler(int simulationTime, int schedulingUnit) {
		this.collectingUnit = schedulingUnit;
		this.simulationTime = simulationTime;
		new Thread(){
			@Override
			public void run() {
				new JOptionPane("Scheduler Started...\nPress the button to terminate!").createDialog(new JFrame(), "Scheduler").show();
				for(Iterator<PeerProcessManager> itr=active.iterator(); itr.hasNext(); itr.next().disconnect());
			    System.exit(0);
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
						Integer.parseInt(tokens[2]), 	// duration
						Integer.parseInt(tokens[3]), 	// reliability
						Integer.parseInt(tokens[4]), Integer.parseInt(tokens[5])	// transfer rates
					);
		}
		log("Pool created at size:" + pool.size());
	}
	
	private void createStatisticsCollector(){
		statisticsCollector = new Thread(){
			public void run() {
				long unit = collectingUnit * Constants.MILLI_IN_MINUTE;
				PrintWriter timeWritter = null;
				PrintWriter allWritter = null;
				PrintWriter allSeedersWritter = null;
				PrintWriter activeWritter = null;
				PrintWriter activeSeedersWritter = null;
				try {
					timeWritter = new PrintWriter(new File(Constants.PEERS_FOLDER + "time.txt"));
					allWritter = new PrintWriter(new File(Constants.PEERS_FOLDER + "all.txt"));
					allSeedersWritter = new PrintWriter(new File(Constants.PEERS_FOLDER + "all_seeders.txt"));
					activeWritter = new PrintWriter(new File(Constants.PEERS_FOLDER + "active.txt"));
					activeSeedersWritter = new PrintWriter(new File(Constants.PEERS_FOLDER + "active_seeders.txt"));
					
					timeWritter.print("Vector time =[" + new Date().getTime());
					allWritter.print("Vector all =[1");
					allSeedersWritter.print("Vector allSeeders =[1");
					activeWritter.print("Vector active=[1");
					activeSeedersWritter.print("Vector activeSeeders=[1");

					while(true){
						try {
							sleep(unit);
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
						String status = new Date() + "," + active.size() + "," + activePeers + "," + activeSeeders + "," + (activePeers-activeSeeders);
						log(status);

						timeWritter.print("," + new Date().getTime());
						allWritter.print("," + active.size());
						allSeedersWritter.print("," + allSeeders);
						activeWritter.print("," + activePeers);
						activeSeedersWritter.print("," + activeSeeders);
						
						timeWritter.flush();
						allWritter.flush();
						allSeedersWritter.flush();
						activeWritter.flush();
						activeSeedersWritter.flush();
					}

				} catch (IOException e1) {
					e1.printStackTrace();
				} finally{
					timeWritter.print("]");
					allWritter.print("]");
					allSeedersWritter.print("]");
					activeWritter.print("]");
					activeSeedersWritter.print("]");

					if(timeWritter!=null)
						timeWritter.close();
				}
			}
		};
	}
	
	private int getInterArrivalTime(){
		return (int) (Constants.LMDA / Math.exp(Constants.LMDA * Math.random()) * 10);
	}

	public void start() throws IOException, InterruptedException {
		statisticsCollector.start();
		while(simulationTime > 0){
			for(int i=0; i<Constants.ARRIVAL_GROUP_SIZE && pool.size()>0; i++){
				PeerProcessManager selected = pool.remove((int)(Math.random() * (pool.size()-1)));
				active.add(selected); 		// start invoking peers processes
				selected.start();
			}
			long interArrival = getInterArrivalTime();
			Thread.sleep(interArrival * Constants.MILLI_IN_MINUTE);
			simulationTime -= interArrival;
		}
		statisticsCollector.stop();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		Scheduler scheduler = new Scheduler(100, 1);	// create simulation for 100 minutes, with unit time 1.
		scheduler.createPool("conf");
		scheduler.createStatisticsCollector();
		scheduler.start();
	}
}
