package com.questify.util;

import java.nio.file.*;
import java.util.Properties;
import java.io.*;

// Small properties-backed config store for simple app preferences.
public class ConfigStore {
	private final Path cfgFile;
	
	public ConfigStore(Path cfgFile) {
		this.cfgFile = cfgFile;
	}
	
	// Return whether the privacy policy was accepted.
	public boolean isPrivacyAccepted() {
		Properties p = load();
		return Boolean.parseBoolean(p.getProperty("privacyAccepted", "false"));
	}
	
	// Persist privacy acceptance.
	public void setPrivacyAccepted(boolean v) {
		Properties p = load();
		p.setProperty("privacyAccepted", Boolean.toString(v));
		save(p);
	}
	
	// Read XP value from properties.
	public int getXp() {
		Properties p = load();
		String s = p.getProperty("xp", "0");
		try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
	}
	
	// Save XP value.
	public void setXp(int xp) {
        Properties p = load();
        p.setProperty("xp", Integer.toString(xp));
        save(p);
    }
	
	// Load properties file if present.
	private Properties load() {
		Properties p = new Properties();
		if (Files.exists(cfgFile)) {
			try (InputStream in = Files.newInputStream(cfgFile)){
				p.load(in);
			} catch (IOException e) { /* ignore read errors and use defaults */ }
		}
		return p;
	}
	
	// Save properties to disk.
	private void save(Properties p) {
        try {
            Files.createDirectories(cfgFile.getParent());
            try (OutputStream out = Files.newOutputStream(cfgFile)) {
                p.store(out, "Questify config");
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
