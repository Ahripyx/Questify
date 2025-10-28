package com.questify;

import com.questify.ui.*;
import com.questify.store.*;
import com.questify.util.ConfigStore;

import java.nio.file.*;

import javax.swing.SwingUtilities;

public class Main {

	public static void main(String[] args) {
		Path appDir = Paths.get(System.getProperty("user.home"), ".questify");
		Path dataFile = appDir.resolve("tasks.txt");
		Path cfgFile = appDir.resolve("config.properties");
		
		ConfigStore cfg = new ConfigStore(cfgFile);
		TextFileTaskStore store = new TextFileTaskStore(dataFile);
		
		// Show splash, then privacy, then main UI on EDT
		SwingUtilities.invokeLater(() -> {
			SplashScreen splash = new SplashScreen();
			splash.showAndWait();
			
			if (!cfg.isPrivacyAccepted()) {
				PrivacyDialog pd = new PrivacyDialog(cfg);
				boolean ok = pd.showModal(null);
				if (!ok) {
					System.exit(0);
				}
			}
			
			MainView main = new MainView(store);
			main.setVisible(true);
		});

	}

}
