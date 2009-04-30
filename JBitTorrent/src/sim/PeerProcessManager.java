package sim;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PeerProcessManager{
	private String id;
	private int duration;
	private int reliablity;
	private int uploadRate, downloadRate;
	
	private boolean alive = false;
	private Process process;
	
	private Thread workingThread;
	
	public PeerProcessManager(String id, 
			int duration, int reliablity, 
			int uploadRate, int downloadRate){
		this.id = id;
		this.duration = duration;
		this.reliablity = reliablity;
		this.uploadRate = uploadRate;
		this.downloadRate = downloadRate;
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
				long activePeriod = reliablity * Constants.MILLI_IN_MINUTE;
				long awayPeriod = (Constants.DAY - duration) * Constants.MILLI_IN_MINUTE;
				
				while(true){
					log("Started");
					try{
						alive = true;
						long lifePeriod = duration * Constants.MILLI_IN_MINUTE;
						while(lifePeriod > 0){
							log("Connected");
							connect();
							sleep(activePeriod);
							log("Disconnected");
							try {
								disconnect();						
							} catch (Exception e) {
								e.printStackTrace();
							}
							sleep(Constants.TIMEOUT);
							lifePeriod -= activePeriod;
							lifePeriod -= Constants.TIMEOUT;
						}
						log("Died");
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally{
						alive = false;
					}
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
		workingThread = new Thread(){
			public void run() {
				List<String> command = new ArrayList<String>();
				command.add("java");
				command.add("-classpath");
				command.add("#/JBitTorrent/bin;#/JBitTorrent/ext/ant.jar;#/JBitTorrent/ext/freemarker.jar;#/JBitTorrent/ext/groovy.jar;#/JBitTorrent/ext/jaxen-core.jar;#/JBitTorrent/ext/jaxen-jdom.jar;#/JBitTorrent/ext/jdom.jar;#/JBitTorrent/ext/kxml.jar;#/JBitTorrent/ext/saxpath.jar;#/JBitTorrent/ext/simple-upload-0.3.4.jar;#/JBitTorrent/ext/velocity.jar;#/JBitTorrent/ext/xalan.jar;#/JBitTorrent/ext/xerces.jar;#/JBitTorrent/ext/xml-apis.jar".replaceAll("#", Constants.WOKING_DIR.replaceAll("\\\\", "/")));
				command.add("sim.PeerProcess");
				command.add(String.valueOf(id));
				command.add(String.valueOf(duration));
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
		};
		
		workingThread.start();
	}
	
	public void disconnect(){
		process.destroy();
	}
	
	public boolean isAlive(){
		return alive;
	}
	
	public boolean isSeeder(){
		return new File(getPeerFolder(), "complete").exists();
	}
}
