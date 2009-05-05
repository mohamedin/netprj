package sim;

public interface Constants {
	// Time constants
	public static final int DAY = 24;
	public static final long MILLI_IN_MINUTE = 100 * 60;
	public static final long TIMEOUT = Constants.MILLI_IN_MINUTE / 2;	// 1 minute to cause connection timeout
	// Environment constants
	public static final String WOKING_DIR = "E:\\Mohamed.Workspace\\";
	public static final String PEERS_FOLDER = WOKING_DIR + "JBitTorrent\\example\\";
	public static final String TORRENT_FILE = PEERS_FOLDER + "seeder\\SkypeSetup.exe.torrents";
	public static final String DATA_FILE = "SkypeSetup.exe";
	// Network constants
	public static final String SERVER = "127.0.0.1";
	public static final long PIECE_SIZE = 100;
	// Statistics constants
	public static final int ARRIVAL_GROUP_SIZE = 2;
	public static final float INTER_ARRIVAL_LMDA = 0.7f;
	// Peer comparison method
	public static boolean COMAPRE_BY_V = false;	
}