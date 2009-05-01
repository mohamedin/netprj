package sim;

import jBittorrentAPI.ExampleCreateTorrent;
import jBittorrentAPI.ExamplePublish;
import jBittorrentAPI.ExampleShareFiles;

import java.io.File;


public class Seeder {
	
	private File name;

	public Seeder(String name, final String tracker){
		// validate dirs
		this.name = new File("example", name);
		this.name.mkdirs();
		share(Constants.DATA_FILE, tracker);
	}
	
	public void share(String file, String server){
		String sharedPath = name.getAbsolutePath() + File.separator + file;
		String torrentPath = name.getAbsolutePath() + File.separator + file + ".torrents";
		String comment = "This is " + file + " for sharing";
		ExampleCreateTorrent.main(new String[] { 
				torrentPath, 
				"http://" + server + ":8081/announce",
				String.valueOf(Constants.PIECE_SIZE),
				sharedPath,
				"..",
				name.getName(),
				"..",
				comment
			} );
		ExamplePublish.main(new String[]{
				torrentPath,
				"http://" + server + ":8081/upload",
				"none",
				"none",
				comment
			});
		ExampleShareFiles.main(new String[] {
				torrentPath, 
				name.getAbsolutePath() + File.separator
			});
	}
	
	public static void main(String[] args) {
		DB.update("dummy", 0, 0, 0);
		new Seeder("Seeder", Constants.SERVER);
	}
}
