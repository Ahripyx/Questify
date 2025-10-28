package com.questify.ui;

import com.questify.util.ConfigStore;

import javax.swing.*;
import java.awt.*;

public class PrivacyDialog {
	private final ConfigStore cfg;
	private boolean accepted = false;
	
	public PrivacyDialog(ConfigStore cfg) {
		this.cfg = cfg;
	}
	
	public boolean showModal(Component parent) {
		JTextArea ta = new JTextArea(getPolicyText());
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setCaretPosition(0);
		ta.setFont(ta.getFont().deriveFont(14f));
		
		JScrollPane sp = new JScrollPane(ta);
		sp.setPreferredSize(new Dimension(520,320));
		
		JButton accept = new JButton("Accept");
		JButton decline = new JButton("Decline");
		
		final JDialog dialog = new JDialog((Frame)null, "Privacy Policy", true);
		JPanel bp = new JPanel();
		bp.add(accept);
		bp.add(decline);
		dialog.getContentPane().add(sp, BorderLayout.CENTER);
		dialog.getContentPane().add(bp, BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		
		accept.addActionListener(e -> {
			cfg.setPrivacyAccepted(true);
			accepted = true;
			dialog.dispose();
		});
		decline.addActionListener(e ->{
			accepted = false;
			dialog.dispose();
		});
		
		dialog.setVisible(true);
		return accepted;
		}
	
	//AI generated privacy policy for time being
	private String getPolicyText() {
        return "Privacy Policy (example):\n\n" +
               "Questify stores your tasks locally in a text file on your computer. " +
               "No data is sent off-device. The app uses a simple local file to persist tasks " +
               "and a small properties file to record that you accepted this policy.\n\n" +
               "If you decline, the app will exit.";
    }
}
