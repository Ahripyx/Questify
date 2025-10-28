package com.questify.ui;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class SplashScreen {
	private final JDialog dialog;
    private final JProgressBar bar;
	
	private final int minDisplayMs;
	
	
	public SplashScreen(int minDisplayMs, Dimension size) {
		this.minDisplayMs = minDisplayMs;
		
		dialog = new JDialog((Frame) null, true);
        dialog.setUndecorated(true); 
        
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		p.setBackground(Color.WHITE);
		
		JLabel label = new JLabel("Questify", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(28f).deriveFont(Font.BOLD));
		label.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
		p.add(label, BorderLayout.CENTER);
		
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
}
