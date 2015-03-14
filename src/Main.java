import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;

/*
 * Colan Biemer
 * CS 338 - GUI Final Project
 * Unity Project Manger
 * 
 * The only thing I didn't get in that i wanted to get in was the ordering of the table by recently
 * modified to least recently. I sacrificed that time to instead build the progress bar which
 * seemed much more important to the GUI design. 
 * 
 * using the singleton pattern for my gui, for the progress bar to function a wee bit better
 */

public class Main {
	public static void main(String[] args) 
	{
		// Start application
		try 
		{
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } 
		catch (Exception e) 
		{
			System.out.println("UI manager issue");
		}
		
//		ProgressBarThreaded progressBar = new ProgressBarThreaded();
//		progressBar.run();
        //Create the top-level container and add contents to it.
        JFrame frame = new JFrame("Unity Project Manager");
        
        // get window information from GUI and run it
        //GUI app = new GUI();
        Component contents = GUI.getInstance().createComponent();
        frame.getContentPane().add(contents);
        
        // run application
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        GUI.getInstance().onLoad();
	}

}
