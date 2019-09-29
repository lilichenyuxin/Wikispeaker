package ass3.app.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javafx.concurrent.Task;

public class CreateCreationTask extends Task<String> {
		
	private String _filePathToSaveTo;
	private List<String> _audioFilePaths;
	private String _wikiTerm;
	private int _numImages;
	
	public CreateCreationTask(String filePathToSaveTo, List<String> audioFilePaths, String wikiTerm, int numImages) {
		
		super();
		_filePathToSaveTo = filePathToSaveTo;
		_audioFilePaths = audioFilePaths;
		_wikiTerm = wikiTerm;
		_numImages = numImages;
		
	}

	@Override
	protected String call() throws Exception {
		
		String command;
		
		// overwrites by default, so checking for preexisting creations
		// needs to be done elsewhere
		
		updateProgress(0, 5);
		updateMessage("Performing house-cleaning...");
		
		// delete preexisting temp and creation files
		command = "rm " + _filePathToSaveTo + " temp/*";
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
		System.out.println(pb.start().waitFor());

		// create text file to store paths of audio files
		String textFileContents = "";
		for (String filePath : _audioFilePaths) {
			textFileContents += "file '" + filePath + "'\n";
		}
		pb.command("bash", "-c", "echo \"" + textFileContents + "\" > temp/temp.txt");
		System.out.println(pb.start().waitFor());
		
		// copy audio files to temp directory
		command = "cp audio/*.wav temp/";
		pb.command("bash", "-c", command);
		pb.start().waitFor();
		
		updateProgress(1, 5);
		updateMessage("Concatenating audio files");
		
		// concatenate audio files and save
		command = "ffmpeg -f concat -i temp/temp.txt -c copy temp/audio.wav";
		pb.command("bash", "-c", command);
		System.out.println(pb.start().waitFor());
		
		updateProgress(2, 5);
		updateMessage("Retrieving images...");
		
		// get images from flickr
		List<String> filePaths = FlickrUtils.getImages(_numImages, _wikiTerm, "image", "temp");
				
		updateProgress(3, 5);
		updateMessage("Creating slideshow...");
		
		// get length of audio to help determine required slideshow framerate (image duration)
		File file = new File("temp", "audio.wav");
		AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
		AudioFormat format = audioInputStream.getFormat();
		long frames = audioInputStream.getFrameLength();
		double durationInSeconds = (frames+0.0) / format.getFrameRate();
		
		// create slideshow
		String frameRate = Double.toString( 1 / (durationInSeconds / _numImages) );
		command = "cat temp/image*.jpg | ffmpeg -framerate " + frameRate + " -f image2pipe -i - -vf scale=-2:400 -r 25 temp/slideshow.mp4";
		pb.command("bash", "-c", command);
		System.out.println(pb.start().waitFor());
		
		// overlay text on slideshow
		command = "ffmpeg -i temp/slideshow.mp4 -vf \"drawtext=fontfile=comicsans.ttf:fontsize=30:" +
				  "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text='" + _wikiTerm + "'\" temp/textslideshow.mp4";
		pb.command("bash", "-c", command);
		System.out.println(pb.start().waitFor());
		
		updateProgress(4, 5);
		updateMessage("Merging slideshow and audio...");
		
		// merge video and audio
		command = "ffmpeg -i temp/textslideshow.mp4 -i temp/audio.wav -c:v copy -c:a aac -strict experimental " + _filePathToSaveTo;
		pb.command("bash", "-c", command);
		System.out.println(pb.start().waitFor());
		
		updateProgress(5, 5);
		updateMessage("Finished!");
		
		return "temp/video.mp4";  // path to video file
	
	}
	
	

}
