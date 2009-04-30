package sim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB {
    static Connection connection = null;
	static {
    // load the sqlite-JDBC driver using the current class loader
    
    try
    {
    	
    	   String userName = "edt";
           String password = "edt";
           String url = "jdbc:mysql://192.168.1.70/EasyDialogWebSiteTest";
           Class.forName ("com.mysql.jdbc.Driver").newInstance ();
           connection = DriverManager.getConnection (url, userName, password);
           System.out.println ("Database connection established");

       Statement statement = connection.createStatement();
       statement.setQueryTimeout(30);  // set timeout to 30 sec.
      
       statement.executeUpdate("create table if not exists peer (id varchar(255) primary key, bandwidth double, availability  double, reliability  double)");
       System.out.println("started DB");
    }
    catch(Exception e)
    {
      
      e.printStackTrace();
    }
	}
	static public synchronized void update(String id, double bw, double av, double re){
		try{
	       Statement statement = connection.createStatement();
	       statement.executeUpdate("replace into peer (id , bandwidth , availability  , reliability  ) values ('"+id+"',"+bw+","+av+","+re+")");
	       System.out.println("updated for " + id +" with " + bw +"|"+av+"|"+re);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	static public synchronized double getAvg(String id){
		try{
			Statement statement = connection.createStatement();
		      ResultSet rs = statement.executeQuery("select * from peer where id='"+id+"'");
		      while(rs.next())
		      {
		    	  double avg =  (rs.getDouble("bandwidth")+rs.getDouble("availability")+rs.getDouble("reliability"))/3;
		    	  System.out.println("avg of "+id + " = " + avg);
		    	  return avg;
		      }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return 5;
	}
	static public synchronized void close(){
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}