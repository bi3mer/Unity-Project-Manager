import java.io.File;
import java.text.SimpleDateFormat;

/*
 * Colan Biemer
 * This class is in charge of holding the path string and the
 * date held by the file.
 */

public class Scene {
	private String path;
	private SimpleDateFormat format;
	private long date;
	public File file;
	
	public Scene(File fileFile)
	{
		this.file     = fileFile;
		this.path     = file.toString();
		this.date     = file.lastModified();
		this.format   = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	}
	
	public String getFormattedDate()
	{
		return this.format.format(this.date);
	}
	
	public String getPath()
	{
		return this.path;
	}

	public long getDate() 
	{
		return date;
	}
	
	public File getFile()
	{
		return file;
	}
}
