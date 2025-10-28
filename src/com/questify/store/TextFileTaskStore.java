package com.questify.store;

import com.questify.model.Task;

import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;

// File-based TaskStore using a simple line format with a custom seperator.
public class TextFileTaskStore implements TaskStore { 
	private final Path file;
	
	public TextFileTaskStore(Path file) {
		this.file = file;
	}
	
	@Override
	public List<Task> loadTasks() throws IOException{
		if (!Files.exists(file)) return new ArrayList<>();
		
		List<String> lines = Files.readAllLines(file);
		List<Task> out = new ArrayList<>();
		
		for (String line : lines) {
			
			// Line format: id||SEP||title||SEP||done
            String[] parts = line.split("\\|\\|SEP\\|\\|", 3);
            if (parts.length >= 3) {
            	String id = parts[0];
            	String title = parts[1];
            	boolean done = Boolean.parseBoolean(parts[2]);
            	out.add(new Task(id, title, done));
            }
		}
		return out;
	}
	
	@Override
	public void saveTasks(List<Task> tasks) throws IOException {
		List<String> lines = tasks.stream()
				.map(t -> String.join("||SEP||", t.getId(), t.getTitle(), Boolean.toString(t.isDone())))
				.collect(Collectors.toList());
		Files.createDirectories(file.getParent());
		Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}
}
