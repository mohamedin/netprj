package sim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import trackerBT.Constants;
import trackerBT.Utils;

public class DB {
//    static Connection connection = null;
//    static Connection connection2 = null;
//	static {
//    // load the sqlite-JDBC driver using the current class loader
//    
//    try
//    {
//    	
//    	   String userName = "edt";
//           String password = "edt";
//           String url = "jdbc:mysql://192.168.1.70/test";
//           Class.forName ("com.mysql.jdbc.Driver").newInstance ();
//           connection = DriverManager.getConnection (url, userName, password);
//           connection2 = DriverManager.getConnection (url, userName, password);
//           System.out.println ("Database connection established");
//
//       Statement statement = connection.createStatement();
//       statement.setQueryTimeout(30);  // set timeout to 30 sec.
//      
//       statement.executeUpdate("create table if not exists peer (id varchar(255) primary key, bandwidth double, availability  double, reliability  double)");
//       System.out.println("started DB");
//    }
//    catch(Exception e)
//    {
//      
//      e.printStackTrace();
//    }
//	}
	static public synchronized void update(String id, double bw, double av, double re){
//		try{
//	       PreparedStatement statement = connection.prepareStatement("replace into peer (id , bandwidth , availability  , reliability  ) values (?,"+bw+","+av+","+re+")");
//	       statement.setString(1, id);
//	       statement.executeUpdate();
//	       System.out.println("updated for " + id +" with " + bw +"|"+av+"|"+re);
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		
	}
	static public synchronized double getAvg(String id){
//		
//		try{
//			PreparedStatement statement = connection2.prepareStatement("select * from peer where id=?");
//			statement.setString(1, id);
//		      ResultSet rs = statement.executeQuery();
//		      while(rs.next())
//		      {
//		    	  double avg =  (rs.getDouble("bandwidth")+rs.getDouble("availability")+rs.getDouble("reliability"))/3;
//		    	  System.out.println("avg of "+id + " = " + avg);
//		    	  return avg;
//		      }
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("No avg for "+id );
		return 5;
	}
	static public synchronized void close(){
//		try {
//			connection.close();
//			connection2.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}
