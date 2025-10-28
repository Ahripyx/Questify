package com.questify.ui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class SplashScreen {
	private final JWindow window;
	private final JProgressBar bar;
	
	public SplashScreen() {
		window = new JWindow();
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
		JLabel label = new JLabel("Questify", JLabel.CENTER);
		label.setFont(label.getFont().deriveFont(26f).deriveFont(Font.BOLD));
		p.add(label, BorderLayout.CENTER);
		bar = new JProgressBar(0,100);
		bar.setStringPainted(true);
		p.add(bar, BorderLayout.SOUTH);
		window.getContentPane().add(p);
		window.setSize(420,160);
		window.setLocationRelativeTo(null);
	}
	
	public void showAndWait() {
        window.setVisible(true);
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(70);
                    publish(i);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                bar.setValue(chunks.get(chunks.size()-1));
            }

            @Override
            protected void done() {
                window.setVisible(false);
                window.dispose();
            }
        };
        worker.execute();
        try { worker.get(); } catch (InterruptedException|ExecutionException e) { /* ignore */ }
    }
}
