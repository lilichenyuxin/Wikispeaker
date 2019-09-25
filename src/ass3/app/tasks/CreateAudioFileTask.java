package ass3.app.tasks;

import java.io.IOException;

import javafx.concurrent.Task;

/*
 * Task subclass which handles the creation of TTS audio files
 */
public class CreateAudioFileTask extends Task<String> {
	
	String _text, _name;
	
	/*
	 * Returns an instance of CreateAudioFileTask. If name is null then the
	 * file is presumed to be temporary (i.e. a preview).
	 */
	public CreateAudioFileTask(String text, String name) {
		
		super();
		_text = text;
		_name = name;
		
	}

	// returns the path to the created audio file
	// removes file with same name if one exists, so a check for such a file should be done elsewhere
	@Override
	protected String call() throws Exception {
		
		String dir;
		if (_name == null) {
			dir = "temp";
		} else {
			dir = "audio";
		}
		
		String filePath = dir + "/" + ( ( _name == null ) ? "temp" : _name ) + ".wav";

		try {

			// create empty dir with appropriate name
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "rm -r " + dir + "; mkdir " + dir);
			builder.start().waitFor();
			
			// create tts audio

			String command = "rm " + filePath + "; echo \"" + _text + "\" | text2wave -o \"" + filePath + "\"";
			builder.command("bash", "-c", command);
			builder.start().waitFor();
			

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filePath;

	}

}
