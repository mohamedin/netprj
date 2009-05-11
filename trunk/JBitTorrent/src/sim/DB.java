package sim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
    static Connection connection = null;
    static Connection connection2 = null;
    static Connection connection3 = null;
    static Connection connection4 = null;
    static Connection connection5 = null;
	static {
    // load the sqlite-JDBC driver using the current class loader
    
    try
    {
    	
    	   String userName = "root";
           String password = "";
           String url = "jdbc:mysql://localhost/test";
           Class.forName ("com.mysql.jdbc.Driver").newInstance ();
           connection = DriverManager.getConnection (url, userName, password);
           connection2 = DriverManager.getConnection (url, userName, password);
           connection3 = DriverManager.getConnection (url, userName, password);
           connection4 = DriverManager.getConnection (url, userName, password);
           connection5 = DriverManager.getConnection (url, userName, password);
           System.out.println ("Database connection established");

       Statement statement = connection.createStatement();
       statement.setQueryTimeout(30);  // set timeout to 30 sec.
      
       statement.executeUpdate("create table if not exists peer (id varchar(255) primary key, bandwidth double, availability  double, reliability  double)");
       statement.executeUpdate("create table if not exists peerIdMap (id varchar(255) primary key, ourID varchar(255))");
       System.out.println("started DB");
    }
    catch(Exception e)
    {
      
      e.printStackTrace();
    }
	}
	
	static public synchronized void updateMap(String id, String ourId){
		try{
	       PreparedStatement statement = connection4.prepareStatement("replace into peerIdMap (id , ourID ) values (?,?)");
	       statement.setString(1, id);
	       statement.setString(2, ourId);
	       statement.executeUpdate();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	static public synchronized String getMappedID(String id){
		
		try{
			PreparedStatement statement = connection5.prepareStatement("select ourID from peerIdMap where id=?");
			statement.setString(1, id);
		      ResultSet rs = statement.executeQuery();
		      while(rs.next())
		      {

		    	  return rs.getString("ourID");
		      }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "Unknown!!";
	}
	static public synchronized void update(String id, double bw, double av, double re){
		try{
	       PreparedStatement statement = connection.prepareStatement("replace into peer (id , bandwidth , availability  , reliability  ) values (?,"+bw+","+av+","+re+")");
	       statement.setString(1, id);
	       statement.executeUpdate();
	       System.out.println("updated for " + id +" with " + bw +"|"+av+"|"+re);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	static public synchronized double getAvg(String id){
		
		try{
			PreparedStatement statement = connection2.prepareStatement("select * from peer where id=?");
			statement.setString(1, id);
		      ResultSet rs = statement.executeQuery();
		      while(rs.next())
		      {
		    	  double avg =  (rs.getDouble("bandwidth")+rs.getDouble("availability")+rs.getDouble("reliability"))/3;
		    	  System.out.println("avg of "+id + " = " + avg);
		    	  return avg;
		      }
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("No avg for "+id );
		return 5;
	}
	
	static public synchronized double getRE(String id){
		
		try{
			PreparedStatement statement = connection2.prepareStatement("select reliability from peer where id=?");
			statement.setString(1, id);
		      ResultSet rs = statement.executeQuery();
		      while(rs.next())
		      {
		    	  double re =  rs.getDouble("reliability");
		    	  System.out.println("re of "+id + " = " + re);
		    	  return re;
		      }
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("No re for "+id );
		return 0;
	}
	
	static public synchronized double getAV(String id){
		
		try{
			PreparedStatement statement = connection3.prepareStatement("select availability from peer where id=?");
			statement.setString(1, id);
		      ResultSet rs = statement.executeQuery();
		      while(rs.next())
		      {
		    	  double av =  rs.getDouble("availability");
		    	  System.out.println("AV of "+id + " = " + av);
		    	  return av;
		      }
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("No av for "+id);
		return 0;
	}
	static public synchronized void close(){
		try {
			connection.close();
			connection2.close();
			connection3.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
