import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;

/*
 * Colan Biemer
 * This class will build a hashtable of Scenes with the project
 * name as the access point to the array of Scenes. It will read
 * from the computer and all of its files to build this table.
 * 
 * This will also act as a hashtable.
 */
public class SceneHash  {
	Hashtable<String, ArrayList<Scene>> projects;
	public String txtFile = "files.txt";
	
	public SceneHash()
	{
		projects = new Hashtable<String,ArrayList<Scene>>();
	}
	
	public void readFileAndBuild() // called at opening of application
	{
		//this.run();
		File file = new File(txtFile);
		// check if file exists txtFile is not a directory
		// make sure it hasn't been tampered with
		if(file.exists() && !file.isDirectory()) 
		{ 
			Collection<File> files = new ArrayList<File>(); // place read files into this
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(file));
				// read from files with this
				String line; // line that will be given text file line
				while ((line = br.readLine()) != null)  // read line of txt into string line
				{
					File f = new File(line);
					if(f.exists() && !f.isDirectory()) // ensure that this is a file and exists
					{
						files.add(f); // add file
					}
					// if it isn't valid then we move forward, this is to prevent tampering with the file
					// which could cause fatal errors down the line if we don't catch them early.
				}
				br.close(); // close reader, because we're done reading the file
			} 
			catch (FileNotFoundException e) 
			{ 
				// file not found
				System.out.println("line 58: file not found scenehsash.java");
				e.printStackTrace();
			} 
			catch (IOException e) 
			{ 
				// error closing file
				System.out.println("line 65: Error closing file scenehsash.java");
				e.printStackTrace();
			} 
			// we now have the collection of files
			this.buildHash(files);
		}
		else
		{
			// file doesn't exists so find stuff and build the table
			// then save the table
			findAndBuild();
		}
	}
	
	// this will be used everytime refresh button is used, first load, or
	// when the "files.txt" has been tampered with and does not exist.
	// this will also create a progress bar, and destroy it when done
	public void findAndBuild() 
	{	
		// create and start progress bar
		final ProgressBarThreaded progressBar = new ProgressBarThreaded();
		progressBar.run();
		
		// Set up and search through computer for projects
		Thread thread = new Thread()
		{
			/*
			 * (non-Javadoc)
			 * @see java.lang.Thread#run()
			 * 
			 * Rather than running the thread and updating the GUI with a listener like we talked about, I simplified it
			 * and have the thread close the progress bar. The other option was a lot more effort.
			 */
			public void run()
			{
				buildHashInThread();
				
				// done loading so end progress bar
				progressBar.doneRunning();
				GUI.getInstance().populate();
			}
			
		};
		// start search
		thread.start();
	}
	
	public void buildHash(Collection<File> files)
	{
		// Build hash from the collection
		for(File file : files)
		{
			// get project id;
			String path    = file.toString();
			String[] parts = path.split("/"); // this could be an issue for windows
			
			// look through file for the folder assets.
			// if it exists add it to the hash.
			// if it doesn't move on to the next because it is not a unity project.
			for(int i = 0; i < parts.length; i++)
			{
				if(parts[i].equals("Assets")) // add error checking for i
				{
					Scene scene = this.createScene(file);
					this.add(parts[i-1],scene); // add to hash
				}
			}
			
		}
	}
	
	private void buildHashInThread()
	{
		File root = FileSystemView.getFileSystemView().getHomeDirectory();
		String[] extensions = new String[] {"unity"}; // search for files with this extension
		Collection<File> files = FileUtils.listFiles(root,extensions,true); // change sample to root
		
		// write the files to txtFile, for future use.
		try 
		{
			// this will write to the file, it will overwrite it if it already exists
			PrintWriter writer = new PrintWriter(txtFile);
			
			for(File file : files)
			{
				writer.println(file.getPath()); // write file path to file
			}
			writer.close(); // close file
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File exception error");
		} 
		
		buildHash(files); // give files to hash to create hash table
		return;
	}
	
	public ArrayList<Scene> getProject(String project)
	{
		return this.projects.get(project);
	}
	
	public void add(String id, Scene scene)
	{
		if(!this.projects.containsKey(id))
		{
			// if the array list hasn't yet been instantiated, instantiate it
			// and then go below to add the scene to the array list.
			this.projects.put(id, new ArrayList<Scene>()); 
		}
		this.projects.get(id).add(scene);
	}
	
	public Hashtable<String, ArrayList<Scene>> getHash()
	{
		return projects;
	}
	
	private Scene createScene(File file)
	{	 
		return new Scene(file);
	}
}
