package sim;

import jBittorrentAPI.DownloadManager;
import jBittorrentAPI.TorrentFile;
import jBittorrentAPI.TorrentProcessor;
import jBittorrentAPI.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class PeerProcess implements FileCompleteListener{
	private String id;
	private int duration;
	private int reliablity;
	private int uploadRate, downloadRate;
	
	private Thread sharingThread;
	private DownloadManager dm;
	
	private JDialog dialog;
	private JOptionPane messageBox;
	
	public PeerProcess(String id, 
			int duration, int reliablity, 
			int uploadRate, int downloadRate){
		this.id = id;
		this.duration = duration;
		this.reliablity = reliablity;
		this.uploadRate = uploadRate;
		this.downloadRate = downloadRate;
		getPeerFolder().mkdirs();
		messageBox = new JOptionPane("Peer Started...");
		new Thread(){
			@Override
			public void run() {
				dialog = messageBox.createDialog(new JFrame(), PeerProcess.this.id);
				dialog.show();
			    System.exit(0);
			}
		}.start();
	}
	
	protected File getPeerFolder(){
		return new File(Constants.PEERS_FOLDER + id);	
	}
	
	protected void log(String message){
		System.out.println(id + " >>" + message);
		messageBox.setMessage(message);
	}

	private void lock(){
		File file = new File(getPeerFolder(), "lock");
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void release(){
		new File(getPeerFolder(), "lock").delete();
	}
	
	public void fileCompleted() {
		dialog.setTitle(id + "- SEEDER");
		File file = new File(getPeerFolder(), "complete");
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private void sleep(long mislli){
		messageBox.setMessage(messageBox.getMessage() + ": stay for " + (mislli/1000) + " seconds");
		try {
			Thread.sleep(Constants.TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void start(){
		File killer = new File(Constants.PEERS_FOLDER + "kill");
		long activePeriod = reliablity * Constants.MILLI_IN_MINUTE;
		long awayPeriod = (Constants.DAY - duration) * Constants.MILLI_IN_MINUTE;
		
		while(!killer.exists()){
			log("Started");
			try{
				lock();
				long lifePeriod = duration * Constants.MILLI_IN_MINUTE;
				while(lifePeriod > 0 && !killer.exists()){
					System.out.println(lifePeriod + " " + activePeriod);
					log("Connected");
					connect();
					sleep(activePeriod);
					log("Disconnected");
					disconnect();
					sleep(Constants.TIMEOUT);
					lifePeriod -= activePeriod;
					lifePeriod -= Constants.TIMEOUT;
				}
				log("Died");
			} finally{
				release();
			}
			sleep(awayPeriod);
		}
		deleteDirectory(getPeerFolder());
	}
	
	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      for(int i=0; i<files.length; i++) {
	         if(files[i].isDirectory()) {
	           deleteDirectory(files[i]);
	         }
	         else {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	  }

	
	private void connect(){
		sharingThread = createSharingThread();
		sharingThread.start();
	}
	
	private void disconnect(){
		if(dm!=null)
			try {
				dm.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		sharingThread.stop();
	}
	
	protected Thread createSharingThread() {
		return new Thread(id + "-sharing"){
			public void run() {
				File torrent = new File(Constants.TORRENT_FILE);
				while(true)
					if(torrent.exists()){
						File myTorrent = new File(getPeerFolder(), torrent.getName());
						try {
							copyFile(torrent, myTorrent);
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							TorrentProcessor tp = new TorrentProcessor();

							TorrentFile t = tp.getTorrentFile(tp.parseTorrent(myTorrent.getAbsolutePath()));
							jBittorrentAPI.Constants.SAVEPATH = getPeerFolder().getAbsolutePath() + File.separator;
							jBittorrentAPI.Constants.DOWNRATE = downloadRate;
							jBittorrentAPI.Constants.UPRATE = uploadRate;
							jBittorrentAPI.Constants.AV = duration;
							jBittorrentAPI.Constants.RE = reliablity;
							
							if (t != null) {
								byte[] bid = Utils.generateID();
								DB.update(Utils.byteArrayToByteString(bid), 4, duration, reliablity);
								dm = new DownloadManager(t,	bid);
								dm.addFileCompleteListener(PeerProcess.this);
								dm.startListening(6881, 6889);
								dm.startTrackerUpdate();
								dm.blockUntilCompletion();
								dm.stopTrackerUpdate();
								dm.closeTempFiles();
							} else {
								System.err.println("Provided file is not a valid torrent file");
								System.err.flush();
								System.exit(1);
							}
						} catch (Exception e) {
							System.out.println("Error while processing torrent file. Please restart the client");
							// e.printStackTrace();
							System.exit(1);
						}
					} else
						try {
							sleep(Constants.MILLI_IN_MINUTE);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
			}
		};
	}
	
	public static void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
		}
	}
	
	public static void main(String[] args) throws NumberFormatException, InterruptedException {
		if(args==null || args.length<5)
			System.out.println("Invalid usage...");
		else
			new PeerProcess(args[0], 
						Integer.parseInt(args[1]), 
						Integer.parseInt(args[2]), 
						Integer.parseInt(args[3]), Integer.parseInt(args[4])).start();
	}
}
