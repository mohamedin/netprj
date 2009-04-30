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
	
	private Process process;
	
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
		}.start();

	}
	
	public void kill(){
		process.destroy();
	}
	
	public boolean isAlive(){
		return new File(getPeerFolder(), "lock").exists();
	}
	
	public boolean isSeeder(){
		return new File(getPeerFolder(), "complete").exists();
	}

}
