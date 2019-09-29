package ass3.app.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;

/*
 * Task subclass which handles the creation of TTS audio files
 */
public class CreateAudioFileTask extends Task<String> {
	
	public enum Synthesiser {
		
		eSpeak("British Male", "default",
			   "American Male", "en-us",
			   "Scottish Male", "other/en-sc"),
		Festival("English Male", "male.scm",
				 "English Female", "female.scm");
		
		private Map<String, String> _voices;
		
		private Synthesiser(String... namesAndArguments) {
			
			
			_voices = new HashMap<String, String>();
			if (namesAndArguments.length % 2 != 0) {
				throw new RuntimeException("invalid number of synthesiser arguments");
			} else {
				
				for (int i = 0; i < namesAndArguments.length; i+= 2) {
					
					String name = namesAndArguments[i];
					String argument = namesAndArguments[i+1];
					
					_voices.put(name,  argument);
					
				}
				
			}
			
		}
		
		public List<String> getVoiceNames() {
			
			// sort to ensure voices are always returned in same order
			
			List<String> list = new ArrayList<String>(_voices.keySet());
			Collections.sort(list);
			return list;
			
		}
		
		public String getArgument(String voiceName) {
			
			return _voices.get(voiceName);
			
		}
		
	}

	private Synthesiser _synth;
	private String _voice;
	private String _text;
	private String _name;
	
	/*
	 * Returns an instance of CreateAudioFileTask. If name is null then the
	 * file is presumed to be temporary (i.e. a preview).
	 */
	public CreateAudioFileTask(String synthName, String voice, String text, String name) {
		
		super();
		
		_synth = Synthesiser.valueOf(synthName);
		_voice = voice;
		
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
			
			updateProgress(0, 4);
			
			// create dir if it doesn't exist
			ProcessBuilder builder = new ProcessBuilder("mkdir", dir);
			builder.start().waitFor();
			
			// delete file with name specified if one exists
			builder.command("rm", filePath);
			builder.start().waitFor();
			
			updateProgress(1, 4);
			
			// create txt file containing text to be spoken
			String command = "echo \"" + _text + "\" > temp/temp.txt";
			builder.command("bash", "-c", command);
			builder.start().waitFor();
			
			updateProgress(2, 4);
			
			// create tts audio
			
			// use desired synthesiser
			if (_synth == Synthesiser.Festival) {
				command = "text2wave -o \"" + filePath + "\" \"temp/temp.txt\" -eval \"tts/festival_voices/" + _synth.getArgument(_voice) + "\"";
				System.out.println(command);
			} else {
				command = "echo \"" + _text + "\" | espeak --stdin -v" + _synth.getArgument(_voice) + " -w \"" + filePath + "\"";
			}
			builder.command("bash", "-c", command);
			builder.start().waitFor();
			
			updateProgress(4, 4);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return filePath;

	}

}
