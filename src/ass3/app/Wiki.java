package ass3.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javafx.application.Platform;
import javafx.concurrent.Task;

//This class is using javafx test to generate threading.
class Wiki extends Task<String>{
	private String _key;


	public Wiki(String keyword) {
		_key = keyword;	
	}

	@Override
	protected String call() throws Exception {
		Thread.sleep(100);
		//read input word from user
		String command = "wikit " + _key;
		ProcessBuilder pk = new ProcessBuilder("bash", "-c", command);
		Process process = pk.start();
		BufferedReader text = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String wikiText = "";
		String line;

		//read output text from wiki.s
		while ((line = text.readLine()) != null){
			if(line.contains(" not found :^(")) {
				Invalidkeyword i = new Invalidkeyword();
				Platform.runLater(i);
			}
			else {
				wikiText += line;
			}
		}
		return wikiText.trim();
	}
}

