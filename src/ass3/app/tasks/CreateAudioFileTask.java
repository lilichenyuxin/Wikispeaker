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
			builder.start();

			// create tts audio

			String command = "echo \"" + _text + "\" | text2wave -o \"" + filePath + "\"";
			builder.command("bash", "-c", command);
			builder.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filePath;

	}

}
