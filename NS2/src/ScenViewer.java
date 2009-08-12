import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScenViewer extends JFrame {
	
	public ScenViewer(final List<Point> path) {
		setSize(550, 550);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(new JPanel(){
		    public boolean isOpaque() {
		        return true;
		    }

		    protected void paintComponent(Graphics g) {
		        int w = getWidth();
		        int h = getHeight();

		        // Paint the top left and bottom right in red.
		        g.setColor(Color.WHITE);
		        g.fillRect(0, 0, w, h);
		        
		        Iterator <Point> itr = path.iterator();
		        Point point = itr.next();
		        g.setColor(Color.BLACK);
		        g.drawString("Start", point.x, point.y);
		        g.setColor(Color.RED);
		        while(itr.hasNext()){
		        	Point newPoint = itr.next();
		        	g.drawLine(point.x, point.y, newPoint.x, newPoint.y);
		        	point = newPoint;
		        }
		        g.setColor(Color.BLACK);
		        g.drawString("end", point.x, point.y);
		    }
		});
	}

	public static void main(String[] args) throws IOException {
		List<Point> path = new LinkedList<Point>();

		BufferedReader reader = new BufferedReader(new FileReader(new File("wireless/mobile/scen.tcl")));
		String line = "";
		while((line = reader.readLine())!= null){
			int i = line.indexOf("set X_");
			if(i>0){
				double x = Double.parseDouble(line.substring(i+ 7).trim());
				double y = Double.parseDouble(reader.readLine().substring(i + 7).trim());
				path.add(new Point((int)x, (int)y));
			}
			i = line.indexOf("setdest");
			if(i>0){
				String[] strings = line.substring(i + 7).replaceAll("\"", "").trim().split(" ");
				double x = Double.parseDouble(strings[0]);
				double y = Double.parseDouble(strings[1]);
				path.add(new Point((int)x, (int)y));
			}
		}
		
		new ScenViewer(path).setVisible(true);
	}
}
