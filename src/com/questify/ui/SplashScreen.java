package com.questify.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class SplashScreen {
	private final JDialog dialog;
    private final JProgressBar bar;
	
	private final int minDisplayMs;
	
	private static final int ICON_SIZE = 80;
	
	
	public SplashScreen(int minDisplayMs, Dimension size) {
		this.minDisplayMs = minDisplayMs;
		
		dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true); 
        
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		p.setBackground(Color.WHITE);
		
		JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        topRow.setOpaque(false);
        
        topRow.setBorder(BorderFactory.createEmptyBorder(350, 0, 8, 0));
        
        JLabel iconLabel = new JLabel();
        ImageIcon icon = loadAndScaleIcon("/resources/questify.png", ICON_SIZE, ICON_SIZE);
        if (icon == null) {
            // try alternate classpath location
            icon = loadAndScaleIcon("/com/questify/ui/resources/questify.png", ICON_SIZE, ICON_SIZE);
        }
        if (icon == null) {
            // try filesystem fallback (project-root/resources/questify.png)
            icon = loadAndScaleIconFromFile("resources/questify.png", ICON_SIZE, ICON_SIZE);
        }
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
        }
        
		JLabel title = new JLabel("Questify", JLabel.CENTER);
		title.setFont(title.getFont().deriveFont(60f).deriveFont(Font.BOLD));
		title.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        title.setOpaque(false);
        
        topRow.add(iconLabel);
        topRow.add(title);
        
        p.add(topRow, BorderLayout.CENTER);
		
		bar = new JProgressBar(0,100);
		bar.setStringPainted(true);
		bar.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
		p.add(bar, BorderLayout.SOUTH);
		
		dialog.getContentPane().add(p);
		 if (size == null) {
	            dialog.setSize(420, 160);
	        } else {
	 
	            dialog.setSize(size);
	        }
	        dialog.setLocationRelativeTo(null);
	}
	
	public void showAndWait() {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
            	long start = System.currentTimeMillis();
            	
            	final int steps = 60;
            	final long sleep = Math.max(1, minDisplayMs / steps);
            	for (int i = 0; i <= steps; i++) {
                    long elapsed = System.currentTimeMillis() - start;
                    int progress = (int) Math.min(100, (elapsed * 100.0) / Math.max(1, minDisplayMs));
                    publish(progress);
                    if (elapsed >= minDisplayMs) break;
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleep);
                    } catch (InterruptedException ex) {
                        // restore interrupted status and break
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            	publish(100);
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int last = chunks.get(chunks.size() - 1);
                bar.setValue(last);
            }

            @Override
            protected void done() {
                // dispose the dialog
                dialog.setVisible(false);
                dialog.dispose();
            }
        };
        worker.execute();
        dialog.setVisible(true);
    }
	
    // Try to load an icon from the classpath resource (resourcePath). Return null if not found.
    private ImageIcon loadAndScaleIcon(String resourcePath, int w, int h) {
        try {
            URL u = getClass().getResource(resourcePath);
            if (u == null) return null;
            ImageIcon raw = new ImageIcon(u);
            Image img = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ex) {
            return null;
        }
    }

    // Try to load an icon from the filesystem (relative to working dir). Return null if not found.
    private ImageIcon loadAndScaleIconFromFile(String filePath, int w, int h) {
        try {
            File f = new File(filePath);
            if (!f.exists()) return null;
            URL u = f.toURI().toURL();
            ImageIcon raw = new ImageIcon(u);
            Image img = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ex) {
            return null;
        }
    }
}
