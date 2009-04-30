package sim;

public interface Constants {
	public static final long MILLI_IN_MINUTE = 100 * 60;
	public static final long TIMEOUT = 1 * Constants.MILLI_IN_MINUTE;	// 1 minute to cause connection timeout

	public static final String WOKING_DIR = "D:\\Mohamedin.Workspace\\";
	public static final String PEERS_FOLDER = WOKING_DIR + "JBitTorrent\\example\\";
	public static final String TORRENT_FILE = PEERS_FOLDER + "seeder\\1.rar.torrents";
	public static final String DATA_FILE = "1.rar";
	
	public static final String SERVER = "127.0.0.1";
	
	public static final long PIECE_SIZE = 100;
	
	public static final int ARRIVAL_GROUP_SIZE = 2;
	public static final float LMDA = 0.7f;
	
	public static final int DAY = 24;
	
}