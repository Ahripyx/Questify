package com.questify.model;

// Simple task data holder.
public class Task {
	private String id;
	private String title;
	private boolean done;
	
	public Task(String id, String title, boolean done) {
		this.id = id;
		this.title = title;
		this.done = done;
	}
	
	public String getId() { return id; }
	public String getTitle() { return title;}
	public boolean isDone() { return done; }
	
	public void setTitle(String title) { this.title = title; }
	public void setDone(boolean done) { this.done = done; }
	
	@Override
	public String toString() {
		return (done ? "[x] " : "[ ] ") + title;
	}

}
