package sim;

import jBittorrentAPI.DownloadManager;
import jBittorrentAPI.TorrentFile;
import jBittorrentAPI.TorrentProcessor;
import jBittorrentAPI.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PeerProcess implements FileCompleteListener{
	private String id;
	private DownloadManager dm;
	
	public PeerProcess(String id, 
			int duration, int reliablity, 
			int uploadRate, int downloadRate){
		this.id = id;
		getPeerFolder().mkdirs();

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
					e.printStackTrace();
					System.exit(1);
				}
			} else
				try {
					Thread.sleep(Constants.MILLI_IN_MINUTE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		
	}
	
	protected File getPeerFolder(){
		return new File(Constants.PEERS_FOLDER + id);	
	}
	
	public void fileCompleted() {
		File file = new File(getPeerFolder(), "complete");
		if(!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		DB.update("dummy", 0, 0, 0);
		if(args==null || args.length<5)
			System.out.println("Invalid usage...");
		else
			new PeerProcess(args[0], 
						Integer.parseInt(args[1]), 
						Integer.parseInt(args[2]), 
						Integer.parseInt(args[3]), Integer.parseInt(args[4]));
	}
}
