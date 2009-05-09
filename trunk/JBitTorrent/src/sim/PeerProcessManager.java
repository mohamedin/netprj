package sim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import sim.util.Exponential;
import sim.util.LogNormal;

public class PeerProcessManager{
	private String id;
	private long availability;
	private long reliablity;
	private int uploadRate, downloadRate;
	
	private boolean alive = false;
	private Process process;
	
	public PeerProcessManager(String id, 
			double availabilityMean, double availabilitySD,
			double reliablityMean, 
			int uploadRate, int downloadRate){
		this.id = id;
		this.availability = (long) (new LogNormal(availabilityMean, availabilitySD).getSample() * Constants.MILLI_IN_MINUTE * 10);
		this.reliablity = (long) (new Exponential(reliablityMean).getSample() * Constants.MILLI_IN_MINUTE * 10);
		this.uploadRate = uploadRate;
		this.downloadRate = downloadRate;
		log("Rel:" + reliablity + "/Avail:" + availability);
	}
	
	protected File getPeerFolder(){
		return new File(Constants.PEERS_FOLDER + id);	
	}
	
	protected void log(String message){
		System.out.println(id + " >>" + message);
	}
	
	public void start() throws IOException{
		new Thread(){
			public void run() {
				long awayPeriod = Constants.DAY * Constants.MILLI_IN_MINUTE - availability;
				while(true){
					log("Started");
					try{
						alive = true;
						long lifePeriod = availability;
						while(lifePeriod > 0){
							log("Connected");
							connect();
							sleep(reliablity);
							log("Disconnected");
							try {
								disconnect();						
							} catch (Exception e) {
								e.printStackTrace();
							}
							sleep(Constants.TIMEOUT);
							lifePeriod -= reliablity;
						}
						log("Died");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally{
						alive = false;
					}
					if(awayPeriod>0)
						try {
							sleep(awayPeriod);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			}
		}.start();
	}
	
	private void connect(){
		new Thread(){
			public void run() {
				List<String> command = new ArrayList<String>();
				command.add("java");
				command.add("-classpath");
				command.add("#JBitTorrent/bin;#JBitTorrent/ext/ant.jar;#JBitTorrent/ext/freemarker.jar;#JBitTorrent/ext/groovy.jar;#JBitTorrent/ext/jaxen-core.jar;#JBitTorrent/ext/jaxen-jdom.jar;#JBitTorrent/ext/jdom.jar;#JBitTorrent/ext/kxml.jar;#JBitTorrent/ext/saxpath.jar;#JBitTorrent/ext/simple-upload-0.3.4.jar;#JBitTorrent/ext/velocity.jar;#JBitTorrent/ext/xalan.jar;#JBitTorrent/ext/xerces.jar;#JBitTorrent/mysql-connector-java-3.0.11-stable-bin.jar;#JBitTorrent/ext/xml-apis.jar;#JBitTorrent/mysql-connector-java-3.0.11-stable-bin.jar".replaceAll("#", Constants.WOKING_DIR.replaceAll("\\\\", "/")));
				command.add("sim.PeerProcess");
				command.add(String.valueOf(id));
				command.add(String.valueOf(availability));
				command.add(String.valueOf(reliablity));
				command.add(String.valueOf(uploadRate));
				command.add(String.valueOf(downloadRate));
				
				try {
				    ProcessBuilder builder = new ProcessBuilder(command);
				    process = builder.start();
				    InputStream is = process.getInputStream();
				    InputStreamReader isr = new InputStreamReader(is);
				    BufferedReader br = new BufferedReader(isr);
				    String line;
				    while ((line = br.readLine()) != null) {
				      log(line);
				    }
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
					log("Program terminated!");					
				}
			}
		}.start();
	}
	
	public void disconnect(){
		try {
			if(process!= null)
				process.destroy();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public boolean isSeeder(){
		return new File(getPeerFolder(), "complete").exists();
	}
}
