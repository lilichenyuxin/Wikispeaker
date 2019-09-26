package ass3.app.tasks;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

import javafx.concurrent.Task;

public class RetrieveImagesFromFlickrTask extends Task<List<String>> {
	
	private final int _numImages;
	private final String _searchTerm;
	
	public RetrieveImagesFromFlickrTask(int numImages, String searchTerm) {
		
		super();
		
		_numImages = numImages;
		_searchTerm = searchTerm;
		
		this.setOnSucceeded((e) -> {
			System.out.println(this.getValue());
		});
	}

	@Override
	protected List<String> call() throws Exception {
		
		String config = System.getProperty("user.dir") 
				+ System.getProperty("file.separator") + "flickr-api-keys.txt"; 

		File file = new File(config); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		
		String line;
		String apiKey = "",
			   sharedSecret = "";
		
		updateProgress(0, 3);
		updateMessage("Retrieving Flickr API key");
		
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
		
		updateProgress(1, 3);
		updateMessage("Retrieving images from Flickr");
		
		Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
		
		int page = 0;
		
        PhotosInterface photos = flickr.getPhotosInterface();
        SearchParameters params = new SearchParameters();
        params.setSort(SearchParameters.RELEVANCE);
        params.setMedia("photos"); 
        params.setText(_searchTerm);
        
        PhotoList<Photo> results = photos.search(params, _numImages, page);
        
        updateProgress(2, 3);
        updateMessage("Saving images to local computer");
        
        List<String> fileNames = new ArrayList<String>();
        for (Photo photo: results) {
        	try {
        		BufferedImage image = photos.getImage(photo,Size.LARGE);
	        	String fileName = _searchTerm.trim().replace(' ', '-')+"-"+System.currentTimeMillis()+"-"+photo.getId()+".jpg";
	        	fileNames.add(fileName);
	        	File outputfile = new File("downloads",fileName);
	        	ImageIO.write(image, "jpg", outputfile);
        	} catch (FlickrException fe) {
        		this.setException(fe);
        		fe.printStackTrace();
        	}
        }
        
        updateProgress(3, 3);
        updateMessage("Finished");
		
		return fileNames;
		
	}
}
