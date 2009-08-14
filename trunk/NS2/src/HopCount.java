import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HopCount {

	static final String FILE_NAME = "C:/cygwin/home/Mohamed/NS2/logs/wirelessGrid_Distance=40_Seeder=0_File=1MB_Peers=81_C=12500.0Bps_RandomSeed=457091259/out.tr";
	static final double stopTime = 10000.0;
	
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(FILE_NAME)));
		
		Pattern pattern = Pattern.compile(".+ (\\d+\\.\\d+)\\s*_(\\d+)_(ARP|RTR|AGT|LL|IFQ|MAC|PHY|TOUT|NRTE|TTL|CBK|\\s|-)*(\\d*).*\\[.*");
		String line = "";
		Map<String, List<String>> hops = new HashMap<String, List<String>>();
		while((line = reader.readLine())!= null){
			if(line.indexOf("DSR")>=0)
				continue;
			Matcher matcher =  pattern.matcher(line);
			if(matcher.find()){
				Double time = Double.parseDouble(matcher.group(1));
				if(time > stopTime)
					break;
				String node = matcher.group(2);
				String packet = matcher.group(4);
				if(packet.length()!=0){
					List<String> set = hops.get(packet);
					if(set==null)
						set = new LinkedList<String>();
					if(!set.contains(node))
						set.add(node);
					hops.put(packet, set);
					continue;
				}
			}
			System.out.println(line);
		}
		
		int max = 0;
		int count = 0;
		int ignored = 0;
		Map<Integer, Integer> clusters = new HashMap<Integer, Integer>();
		for(Iterator<Entry<String, List<String>>> itr=hops.entrySet().iterator(); itr.hasNext(); ){
			Entry<String, List<String>> entry = itr.next();
			List<String> path = entry.getValue();
			int hopCount = path.size() - 1;
			if(hopCount==0)
				ignored++;
			Integer c = clusters.get(hopCount);
			clusters.put(hopCount, (c==null ? 1 : c+1));
			count += hopCount;
			if(hopCount>max){
				max = hopCount;
				System.err.println(entry.getKey() + ":" + path.toString());
			}
		}
	
		System.err.println("Max Hop Count = " + max);
		for(Iterator<Entry<Integer, Integer>> itr=clusters.entrySet().iterator(); itr.hasNext(); ){
			Entry<Integer, Integer> entry = itr.next();
			System.err.println("Cluster " + entry.getKey() + " - Count:" + entry.getValue() + " \t(" + ((float)entry.getValue()/(hops.size()-ignored)*100) + "%)");
		}
		System.err.println("All hops = " + count);
		System.err.println("All Packets = " + (hops.size()-ignored));
		System.err.println("Avg Hop Count = " + ((double)count/(hops.size()-ignored)));
	}
}
