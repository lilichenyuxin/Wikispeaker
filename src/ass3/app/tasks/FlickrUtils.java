package ass3.app.tasks;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

import javafx.concurrent.Task;

public class FlickrUtils {
	
	public static List<String> getImages(int numImages, String searchTerm, String prefix, String directory) {
		
		try {
			String config = System.getProperty("user.dir") 
					+ System.getProperty("file.separator") + "flickr-api-keys.txt"; 

			File file = new File(config); 
			BufferedReader br = new BufferedReader(new FileReader(file)); 
			
			String line;
			String apiKey = "",
				   sharedSecret = "";
			
			boolean shouldClose = false;
			while ( (line = br.readLine()) != null ) {
				if (line.trim().startsWith("apiKey")) {
					apiKey =  line.substring(line.indexOf("=")+1).trim();
					if (shouldClose) {
						br.close();
						break;
					} else {
						shouldClose = true;
					}
				} else if (line.trim().startsWith("sharedSecret")) {
					sharedSecret = line.substring(line.indexOf("=")+1).trim();
					if (shouldClose) {
						br.close();
						break;
					} else {
						shouldClose = true;
					}
				}
			}
			br.close();
			
			Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
			
			int page = 0;
			
			PhotosInterface photos = flickr.getPhotosInterface();
			SearchParameters params = new SearchParameters();
			params.setSort(SearchParameters.RELEVANCE);
			params.setMedia("photos"); 
			params.setText(searchTerm);
			
			PhotoList<Photo> results = photos.search(params, numImages, page);
			
			List<String> filePaths = new ArrayList<String>();
			for (int i = 0; i < results.size(); i++) {
				Photo photo = results.get(i);
				BufferedImage image = photos.getImage(photo,Size.LARGE);
		    	String fileName = prefix + i + ".jpg";
		    	filePaths.add(directory + "/" + fileName);
		    	File outputfile = new File(directory, fileName);
		    	ImageIO.write(image, "jpg", outputfile);
			}
			return filePaths;
								
		} catch (IOException | FlickrException e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
