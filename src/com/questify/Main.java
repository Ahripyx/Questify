package com.questify;

import com.questify.ui.*;
import com.questify.store.*;
import com.questify.util.ConfigStore;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Enumeration;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.nio.file.*;

public class Main {

	
	public static final Dimension PHONE_SIZE_SMALL = new Dimension(480, 900);
    public static final Dimension PHONE_SIZE_HIRES = new Dimension(1080, 1920);
    
    public static final boolean ALWAYS_SHOW_PRIVACY_FOR_TESTING = true;
	
	public static void main(String[] args) {
		
		setGlobalFont(new Font("SansSerif", Font.PLAIN, 16));
		
		Path appDir = Paths.get(System.getProperty("user.home"), ".questify");
		Path dataFile = appDir.resolve("tasks.txt");
		Path cfgFile = appDir.resolve("config.properties");
		
		ConfigStore cfg = new ConfigStore(cfgFile);
		TextFileTaskStore store = new TextFileTaskStore(dataFile);
		
		// Show splash, then privacy, then main UI on EDT
		SwingUtilities.invokeLater(() -> {
			SplashScreen splash = new SplashScreen(3000, Main.PHONE_SIZE_SMALL);
			splash.showAndWait();
			
			if (ALWAYS_SHOW_PRIVACY_FOR_TESTING || !cfg.isPrivacyAccepted()) {
				PrivacyDialog pd = new PrivacyDialog(cfg, Main.PHONE_SIZE_SMALL);
				boolean ok = pd.showModal(null);
				if (!ok) {
					System.exit(0);
				}
			}
			
			MainView main = new MainView(store, PHONE_SIZE_SMALL);
			main.setVisible(true);
		});

	}
	
	private static void setGlobalFont(Font font) {
        FontUIResource fr = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fr);
            }
        }
    }

}
