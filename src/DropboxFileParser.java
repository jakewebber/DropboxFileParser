// Jacob Webber 5/24/2016
// Parse through Dropbox directory recursively, extract dates from file names, output to CSV File.
// The CSV is formatted for Wordpress Category CSV Plugin.

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.util.ArrayList;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import org.json.JSONObject;

public class DropboxFileParser {
	private static final String ACCESS_TOKEN = "your_access_token"; // <-- Replace with private Dropbox Access Token
	public static DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
	public static DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
	public static ArrayList<String> dateList = new ArrayList<>();

	/* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * Main Method 	 */
	public static void main(String args[]) throws DbxException, IOException {
		FullAccount account = client.users().getCurrentAccount();
		System.out.println("Account: " + account.getName().getDisplayName());

		// Get files and folder metadata from Dropbox root directory
		String parentDirectory = "/start_directory"; // <-- Starting directory to parse
		parseFolders(parentDirectory);

		// Create output CSV file from dates ArrayList.
		String datesFileName = parentDirectory.replace("/", "-").replace(" ", "_"); //FileName is the parent directory.
		Path datesFile = Paths.get(datesFileName + ".txt");
		Files.write(datesFile, dateList, Charset.forName("UTF-8"));
	}


	/* --------------------------------------------------
	 * Recursively parse through all folders, subfolders, and files inside a given parent folder. 
	 * Dates are extracted from any files using getDate call. */
	public static void parseFolders(String currentFolder) throws ListFolderErrorException, DbxException{
		ListFolderResult result = client.files().listFolder(currentFolder);
		for (Metadata metadata : result.getEntries()) {
			JSONObject current = new JSONObject(metadata.toStringMultiline());
			getDate(current.getString("name")); //Extract date in the current filename
			if(current.getString(".tag").equalsIgnoreCase("folder")){
				System.out.println("	Parsing '" + metadata.getPathLower() + "' ...");
				parseFolders(metadata.getPathLower()); //recursive parse subdirectory call
			}
		}
		result = client.files().listFolderContinue(result.getCursor());
	}

	/* --------------------------------------------------
	 * Extract a filename for the day, month, and year, add to DateList.
	 * Returns null if the fileName was in an incorrect format.
	 * All dates are added to a public ArrayList of date strings.
	 * Name format example: 'title_report_1-January2016.doc */
	public static String getDate(String name){
		String day = "";
		String year = "";
		String month = "";
		name = name + "  ";
		System.out.printf("%-60s", name);

		//Find Day
		int i = 0;
		while ( i < name.length() && (!Character.isDigit(name.charAt(i))) ) i++;
		int j = i;
		while (j < name.length() && Character.isDigit(name.charAt(j))) j++;
		day = name.substring(i, j);

		//Find Month
		String[] months = {
				"January", 
				"February", 
				"March", 
				"April", 
				"May", 
				"June", 
				"July", 
				"August", 
				"September", 
				"October", 
				"November", 
				"December"
		};
		for(i = 0; i < months.length; i++ ){
			if(name.toLowerCase().contains(months[i].toLowerCase())){
				month = months[i];
			}
		}

		//Find Year
		name = name.substring(j);
		name = name.replaceAll("\\D+","");
		year = name;

		System.out.println("<-->		" + month + " " + day + " " + year);

		//Catching formatting errors
		if(day.length() > 2 || day.length() < 1){
			return null;
		}
		if(year.length() > 4 || year.length() < 4){
			return null;
		}
		if(month.length() < 3){
			return null;
		}
		
		//Add dates to public dates ArrayList.
		dateList.add(month + " " + day + " " + year + ", " + year);
		return month + " " + day + " " + year;
	}
}