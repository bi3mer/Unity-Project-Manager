import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

/*
 * this is no longer threaded, the name is misleading-- apologies.
 */

public class ProgressBarThreaded implements Runnable{
	
	public JFrame frame;
	
	public void doneRunning()
	{
		frame.setVisible(false);
		frame.dispose();
	}
	
	public void run() 
	{
		frame = new JFrame("loading and finding files");
		frame.setLayout(new FlowLayout());
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Finding Unity projects!");
		frame.add(progressBar);
		frame.setSize(250,100);
		
		// place in center of the screen
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
		int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
		frame.setLocation(x, y);
		frame.setVisible(true);
	}
}
