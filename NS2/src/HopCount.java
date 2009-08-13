import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HopCount {

	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("C:/trace files/out2.tr")));
		
		Pattern pattern = Pattern.compile(".*(\\d|\\.)* _(\\d*)_(ARP|RTR|AGT|LL|IFQ|MAC|PHY|\\s|-)*(\\d*).*\\[.*");
		String line = "";
		Map<String, Set<String>> hops = new HashMap<String, Set<String>>();
		while((line = reader.readLine())!= null){
			Matcher matcher =  pattern.matcher(line);
			if(matcher.find()){
				String node = matcher.group(2);
				String packet = matcher.group(4);
				if(packet.length()!=0){
					Set<String> set = hops.get(packet);
					if(set==null)
						set = new HashSet<String>();
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
		for(Iterator<Entry<String, Set<String>>> itr=hops.entrySet().iterator(); itr.hasNext(); ){
			Entry<String, Set<String>> entry = itr.next();
			Set<String> path = entry.getValue();
			int hopCount = path.size() - 1;
			if(hopCount==0)
				ignored++;
			count += hopCount;
			if(hopCount>max){
				max = hopCount;
				System.err.println(entry.getKey() + ":" + path.toString());
			}
		}
	
		System.err.println("Max Hop Count = " + max);
		System.err.println("All hops = " + count);
		System.err.println("All Packets = " + (hops.size()-ignored));
		System.err.println("Avg Hop Count = " + ((double)count/(hops.size()-ignored)));
	}
}
