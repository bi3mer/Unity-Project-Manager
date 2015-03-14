import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;


/*
 * Colan Biemer
 * CS338
 * 
 * this class is in charge of the entire GUI, both building it and
 * running it.
 * 
 * This is a singelton pattern class. it does this so the GUI can be update from the scene hash
 * or anywhere else, which simplifies the code drastically (specifically around the progress bar.
 */
public class GUI {
	// this holds all of the necessary data to run the project
	public SceneHash hash;
	
	// used for double click listening 
	private boolean isAlreadyOneClick = false;  // project listener
	private boolean isOneClickCombo   = false;  // comboBox listener
	
	// this is in charge of the comboboxes in column 1
	private List<TableCellEditor> editors = new ArrayList<TableCellEditor>();
	
	// table and model
	private DefaultTableModel model;
	private JTable table;
	
	// refresh button
	private JButton refreshButton;
	
	// search bar
	private JTextField searchBar;
	
	
	protected GUI()
	{
		// building the scene hash will occur later when the GUI is
		// in and we can display a loading screen and such
		hash = new SceneHash(); 
	}
	
	 private static GUI instance = null;

   public static GUI getInstance() {
      if(instance == null) {
         instance = new GUI();
      }
      return instance;
   }
	
	// This method will create the entire gui and return it to the main 
	// that will create place the components on the screen
	@SuppressWarnings("serial")
	public Component createComponent()
	{
		// we aren't going to populate the tabel here
		// instead populate it later.
		this.model  = new DefaultTableModel(){
			// http://stackoverflow.com/questions/4051659/identifying-double-click-in-java
			/*
			 * (non-Javadoc)
			 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
			 * 
			 * This method stops the user from editing the scenes name, but allows for them
			 * to change the values in the combo box.
			 */
			public boolean isCellEditable(int row, int column) 
			{
				if(column == 1)
				{
					return true;
				}
			    return false;
			}
		};
		// Add column titles
		String columns[] = {"Project", "Scenes"};
		for(int i = 0 ; i < columns.length;i++){
			this.model.addColumn(columns[i]);
		}
		this.table = new JTable(model)
		{
			/*
			 * (non-Javadoc)
			 * @see javax.swing.JTable#getCellEditor(int, int)
			 * this method is added so we can properly use combo boxes in the table
			 */
			public TableCellEditor getCellEditor(int row, int column)
            {
                int modelColumn = convertColumnIndexToModel( column );

                if (modelColumn == 1)
                    return editors.get(row);
                else
                    return super.getCellEditor(row, column);
            }
			
			/*
			 * (non-Javadoc)
			 * @see javax.swing.JTable#getToolTipText(java.awt.event.MouseEvent)
			 * 
			 * override tool tip for each column
			 * http://stackoverflow.com/questions/8332159/how-to-add-tooltips-to-jtables-rows
			 */
			 public String getToolTipText(MouseEvent e) {
	                String tip = null;
	                java.awt.Point p = e.getPoint();
	                int colIndex = columnAtPoint(p);

	                try {
	                	// specify which tip to give by column index
	                    if(colIndex == 0){
	                      tip = "Double click to open, right click to reveal in finder";
	                    }
	                    else
	                    {
	                    	tip = "Click to specify scene that is opened";
	                    }
	                } catch (RuntimeException e1) {
	                    System.out.println("null pointer error");
	                }

	                return tip;
	            }
		};
		
		
		//click and double click listener
		this.table.addMouseListener(new MouseAdapter()
		{
			// http://stackoverflow.com/questions/4051659/identifying-double-click-in-java
			public void mouseClicked(MouseEvent e)
			{
				// right click: opens the finder with project start folder
				// Mac right click is with control, hence isControlDown()
				 if (e.getButton() == MouseEvent.BUTTON3 || e.isControlDown())
				 {
					 String hashKey = table.getValueAt( table.getSelectedRow(),0).toString();
					 ArrayList<Scene> scenes = hash.getHash().get(hashKey);
					 String[] pathSplit = scenes.get(0).getPath().split("/");
					 String path = "";
					 
					 // build path to project folder
					 for(int i = 0; i < pathSplit.length; i++)
					 {
						 if(pathSplit[i].equals(hashKey))
						 {
							 // once the hash key has been found add it, and be done
							 path += "/" + hashKey;
							 break;
						 }
						 path += "/" + pathSplit[i];
					 }
					 
					 //open in finder
					 ProcessBuilder finder = new ProcessBuilder("open", path);
					 try 
					 {
						finder.start();
					} 
					 catch (IOException e1) 
					 {
						System.out.println("error with open finder");
					}
					 return;
				 }
				if(table.getSelectedColumn() != 0)
				{
					return;
				}
				if(isAlreadyOneClick)
				{
					if(table.getValueAt(table.getSelectedRow(), 1) == null || table.getValueAt(table.getSelectedRow(), 1).toString().equals(""))
					{
						openRecent(table.getValueAt( table.getSelectedRow(),0).toString());
					}
					else // else open item in combobox
					{
						openGivenScene(table.getValueAt(table.getSelectedRow(),1).toString());
					}
				}
				else 
				{
					isAlreadyOneClick = true; // set it true so, if a quick click happens it will register.
					Timer t = new Timer("doubleClick",false);
					t.schedule(new TimerTask()
					{
						@Override
						public void run() 
						{
							isAlreadyOneClick = false;
						}
						
					},500);
				}
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(this.table); // add scrolling to table
		/*
		 * ADD JTABLE ACTIONLISTENER for double click, single click.
		 * need button generator with code to generate it as well.
		 */
		
		// initialize refresh button
		this.refreshButton = new JButton("refresh");
		this.refreshButton.setMnemonic(KeyEvent.VK_I);
		
		// add listener to refresh button to rebuild the hash
        this.refreshButton.addActionListener(new ActionListener() 
        {
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				/*
				 * give progress bar
				 * create listener for thread notification
				 * then do stuff with it
				 */
				// refer to function loadingScreen() comments to see what this does
				loadingScreen();
			}
	    });
		
        // initialize open scene button
 		JButton openScene = new JButton("Open Scene");
 		openScene.setMnemonic(KeyEvent.VK_I);
 		
 		
 		openScene.addActionListener(new ActionListener()
 		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				String scene;
				try
				{
					scene = table.getValueAt(table.getSelectedRow(), 1).toString();
				}
				catch (NullPointerException e)
				{
					// open recent scene, since combobox is empty
					openRecent(table.getValueAt( table.getSelectedRow(),0).toString());
					return;
				}
				if(scene == null || scene == "")
				{
					System.out.println("invalid");
					return;
				}
				// open scene in combo box
				openGivenScene(table.getValueAt(table.getSelectedRow(),1).toString());
			}
 			
 		});
        
		// give text field initial value of "Search..." with spaces to increase jtextfield size
		this.searchBar = new JTextField("Search...               "); 
		
		// add tooltip to search bar
		this.searchBar.setToolTipText("Search for specific unity project");
		
		// if the search bar is clicked, remove default text
		this.searchBar.addMouseListener(new MouseAdapter()
		{
			 @Override
			public void mouseClicked(MouseEvent e)
			{
				 // If searchBar contains default text remove it when clicked
				 if(searchBar.getText().equals("Search...               "))
				 {
					 searchBar.setText("");
				 }
			}
		});
		this.searchBar.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent arg0) 
			{
				updateSearchAdd(searchBar.getText());
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) 
			{
				if(searchBar.getText().equals("") && model.getRowCount() == hash.getHash().size())
				{
					return; // not interested in updating for "", or if the table is already full
				}
				updateSearchRemove(searchBar.getText());
			}
			
			@Override
			public void changedUpdate(DocumentEvent arg0) 
			{
				// this should never be called
				System.out.println("Changed update in search bars houdl not have been called.");
			}
			
		});
		
		JPanel buttonPane = new JPanel();
		JPanel mainPane   = new JPanel();
		JPanel openPane = new JPanel();
		
		/*
		 * If there aren't a lot of projects the project manager page will look dumb with the button being
		 * way below it. To compensate, this preferable size will allow it to look more natural. Without 
		 * adding in unnecessary rows
		 */
		table.setPreferredScrollableViewportSize(new Dimension(500, 100)); 
		openScene.setSize(new Dimension(50,20));
		buttonPane.setLayout(new BorderLayout());
		
		// mainPane.setLayout(new GridLayout(0,1));
		mainPane.setLayout(new BorderLayout());
		
		buttonPane.add(this.searchBar, BorderLayout.WEST); // add search bar to pane
		buttonPane.add(this.refreshButton, BorderLayout.EAST); // add refresh button
		
		// add openScene button to openPane, along with setting its layout
		openPane.setLayout(new FlowLayout());
		openPane.add(openScene);
		
		mainPane.add(buttonPane,BorderLayout.NORTH);
		mainPane.add(scrollPane,BorderLayout.CENTER);
		mainPane.add(openPane,BorderLayout.SOUTH);
		
		return mainPane;
	}
	
	// build hash on load via reading file, and if that fails it will generate
	public void onLoad()
	{
		this.hash.readFileAndBuild();
		populate();
	}
	
	//http://stackoverflow.com/questions/9962426/progress-bar-to-run-simultaneously-with-a-functionin-another-class
	// was used to develop this funciton
	public void loadingScreen()
	{
		// if I have time make this an asynchronous call. Which will vastly improve it
//		JProgressBar progressBar = new JProgressBar();
//		progressBar.setIndeterminate(true);
		hash.findAndBuild();
		populate();
	}
	
	// look through hash and update  the table model accordingly for removals.
	public void updateSearchRemove(String search)
	{
		/*
		 * efficiency wise this is the best, because this can be done by adding only bits
		 * and pieces rather than removing all of them, adding all of them and then
		 * going back to remove the bits and pieces. In terms of me getting this project 
		 * done on time, though, this is ideal. If i have time I'll get back to this, after
		 * doing things like making the loading asynchronous
		 */
		populate();
		if(search.equals(""))
		{
			return;
		}
		updateSearchAdd(search);
	}
	
	// look through hash and update  the table model accordingly for additions.
	public void updateSearchAdd(String search)
	{
		for (int i = this.model.getRowCount() - 1; i > -1; i--) {
	        if(!this.model.getValueAt(i, 0).toString().contains(search))
	        {
	        	// Key not found, so remove
	        	this.model.removeRow(i);
	        }
	    }
	}

	// populate table
	public void populate()
	{
		// empty table and editors to re-populate
		for (int i = this.model.getRowCount() - 1; i > -1; i--) {
	        this.model.removeRow(i);
	    }
		
		editors.clear();
		
		/*
		 * We'll populate compareTable with the string key and the scene that is the most up to date
		 * then we'll use the completed compareTable to find the order in which we'll display unity
		 * projects.
		 */
		Enumeration<String> enumKey = this.hash.getHash().keys();

		
		// Now we can populate the list Model
		//http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TableRenderDemoProject/src/components/TableRenderDemo.java
		//prep table for combo boxes
		
		//used to create unique combo box for each row entry
		enumKey = this.hash.getHash().keys();
		ArrayList<String> keys = new ArrayList<String>();
		
		// add keys to ArrayList to be sorted
		while(enumKey.hasMoreElements())
		{
			keys.add(enumKey.nextElement());
		}
		
		// sort keys by name
		// sorting by modification has proved to be more difficult than expected
		//http://stackoverflow.com/questions/5815423/sorting-arraylist-in-alphabetical-order-case-insensitive
		Collections.sort(keys,new Comparator<String>() 
		{
	        @Override
	        public int compare(String s1, String s2) {
	            return s1.compareToIgnoreCase(s2);
	        }
		});

		// iterate through the hash table, via the keys.
		for(int j = 0 ; j < keys.size(); j++)
		{
			// get initial data
			String key= keys.get(j);
			ArrayList<Scene> scenes = this.hash.getHash().get(key);
			String[] scenePath = new String[scenes.size()];
			
			// place scene name into the path
			for(int i = 0; i < scenePath.length; i++)
			{
				String[] sceneName = scenes.get(i).getPath().split("/");
				scenePath[i] = sceneName[sceneName.length-1];
			}
			
			// sort scene paths for combo box, regardless of upper or lower case for each character
			// http://www.java2s.com/Tutorial/Java/0140__Collections/Sortanarraycaseinsensitive.htm
			Arrays.sort(scenePath, Collator.getInstance());
			
			JComboBox box = new JComboBox(scenePath);
			
			/*
			 *  mouse listener will detect double clicked
			 *  if not it will just leave it selected which can then be used
			 *  by the button to open.
			 */
			box.addMouseListener(new MouseAdapter()
			{

				@Override
				public void mouseClicked(MouseEvent arg0) 
				{
					// get combo box
					JComboBox box = (JComboBox)arg0.getSource();
					
					// if the combo box isn't selected, we don't care
					if(table.getSelectedColumn() != 1 || box.getSelectedItem() == null) 
					{
						return;
					}
					
					// if combobox is null, return
					if(box.getSelectedItem() == null)
					{
						return;
					}
					
					// get scene name
					String sceneName = box.getSelectedItem().toString();
					
					// if scene name doesn't exist in either no text, or null, just return
					if(sceneName == "" || sceneName == null) // if the scene isn't selected or something like that, we don't care
					{
						return;
					}
					
					// combobox has already been clicked once so open the scene associated with it
					if(isOneClickCombo)
					{
						openComboBox(box);
					}
					else 
					{
						// set up double clicking with a timer schedule
						isOneClickCombo = true; // set it true so, if a quick click happens it will register.
						Timer t = new Timer("doubleClickCombo",false);
						t.schedule(new TimerTask()
						{
							@Override
							public void run() 
							{
								isOneClickCombo = false;
							}
							
						},500);
					}
				}
				
			});
			
			// set editor for combob box, in the JTable
			DefaultCellEditor dce = new DefaultCellEditor(box);
			
			// add edtior
			editors.add(dce);
			
			/*
			 *  create a new row and place the project name inside of it.
			 *  Note: when we set the ditor before hand, it is assocated with the
			 *  proper row and column.
			 */
			Object data[] = {key};
			model.addRow(data);
		}
	}
	/*
	 * Given a key, open the most recently modified unity scene
	 */
	public void openRecent(String key)
	{
		ArrayList<Scene> scenes = this.hash.getHash().get(key); // get array list to loop through
		Scene recentScene = scenes.get(0); // this will hold the most recent sceen to open the unity project in
		for(int i = 1; i < scenes.size(); i++)
		{
			if(scenes.get(i).getDate() > recentScene.getDate())
			{
				recentScene = scenes.get(i);
			}
		}
		openScene(recentScene);
	}
	
	/*
	 * open unity scene given a combo box
	 */
	public void openComboBox(JComboBox box)
	{
		// get name of scene
		String sceneName = box.getSelectedItem().toString();
		openGivenScene(sceneName);
	}
	
	/*
	 * Open unity scene given a scene name
	 */
	public void openGivenScene(String sceneName)
	{
		// Open scene corresponding to the selected entry in the combo box
		String key = table.getValueAt( table.getSelectedRow(),0).toString();
		ArrayList<Scene> scenes = hash.getHash().get(key);
		
		// find the whole path from the scene
		for(Scene scene : scenes)
		{
			String[] sceneNameVal = scene.getPath().split("/"); // split to check scenenames
			if(sceneNameVal[sceneNameVal.length-1].equals(sceneName)) //use split to see if scene names are the same
			{
				openScene(scene); //if the same open this and return
				return;
			}
		}
	}
	
	/*
	 * will open unity scene given a Scene
	 */
	public void openScene(Scene scene)
	{
		try 
		{
			Desktop.getDesktop().open(scene.getFile());
		} 
		catch (IOException e) 
		{
			System.out.println("Error" + e);
		}
	}
}
