# DropboxFileParser
*Recursively search through a given Dropbox directory and all subdirectories, extract dates from file names, output to CSV.*

The date extraction works on documents in precisely this format: 
`titlereport_1-January2016.doc`
and adds the results to an ArrayList for output to a file. 

The CSV file is formatted for a mass CSV-to-category generator PHP plugin for Wordpress with the year as the parent category and all dates as subcategories for the relevant year. 

###Basic Recursive Folder Parsing Template

```java
	/* --------------------------------------------------
	 * Recursively parse through all folders, subfolders, and files inside a given parent folder. */
	public static void parseFolders(String currentFolder) throws ListFolderErrorException, DbxException{
		ListFolderResult result = client.files().listFolder(currentFolder);
		for (Metadata metadata : result.getEntries()) {
			JSONObject current = new JSONObject(metadata.toStringMultiline());
			
			// Add code to use current object.
			
			if(current.getString(".tag").equalsIgnoreCase("folder")){ //Current object is a subdirectory
				System.out.println("	Parsing '" + metadata.getPathLower() + "' ...");
				parseFolders(metadata.getPathLower()); //recursive parse subdirectory call
			}
		}
		result = client.files().listFolderContinue(result.getCursor());
	}
```
