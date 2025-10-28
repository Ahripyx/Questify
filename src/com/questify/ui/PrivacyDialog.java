package com.questify.ui;

import com.questify.util.ConfigStore;

import javax.swing.*;
import java.awt.*;

public class PrivacyDialog {
	private final ConfigStore cfg;
	private boolean accepted = false;
	private final Dimension size;
	
	public PrivacyDialog(ConfigStore cfg, Dimension size) {
		this.cfg = cfg;
		this.size = size;
	}
	
	public boolean showModal(Component parent) {
		JLabel title = new JLabel("Privacy Policy", JLabel.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
		title.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));
		
		JTextArea ta = new JTextArea(getPolicyText());
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setCaretPosition(0);
		ta.setFont(ta.getFont().deriveFont(14f));
		
		JScrollPane sp = new JScrollPane(ta);
		sp.setPreferredSize(new Dimension(size));
		
		JButton accept = new JButton("Accept");
		JButton decline = new JButton("Decline");
		
		final JDialog dialog = new JDialog((Frame)null, "Privacy Policy", true);
		
		dialog.getContentPane().add(title, BorderLayout.NORTH);
		
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
	
	//AI generated but still related to app
	private String getPolicyText() {
		return 	"Questify stores your tasks and app settings only on the device where the app runs. " +
				"No data is transmitted to any server. The app writes a simple plain-text tasks file " +
		        "and a properties file to your user home folder (e.g., ~/.questify/tasks.txt and ~/.questify/config.properties). " +
		        "The privacy dialog presented at first launch records your explicit acceptance in the local config file " +
		        "so you are not asked again. Questify does not collect, share, or sell any personal information. " +
		        "If you decline the privacy policy, the app will quit and no files will be written.";

    }
}
