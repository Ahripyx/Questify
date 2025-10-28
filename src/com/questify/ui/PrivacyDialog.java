package com.questify.ui;

import com.questify.util.ConfigStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;

// Modal privacy policy dialog that persists acceptance state.
public class PrivacyDialog {
	private final ConfigStore cfg;
	private boolean accepted = false;
	private final Dimension size;
	
	public PrivacyDialog(ConfigStore cfg, Dimension size) {
		this.cfg = cfg;
		this.size = size;
	}
	
	// Show modal dialog and return true if user accepted.
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
		
		dialog.getRootPane().setDefaultButton(accept);
		
		title.getAccessibleContext().setAccessibleName("Privacy Policy Title");
		ta.getAccessibleContext().setAccessibleName("Privacy policy text");
		ta.getAccessibleContext().setAccessibleDescription("Detailed description of how Questify stores data on this device");
		
		accept.getAccessibleContext().setAccessibleName("Accept privacy policy");
		accept.getAccessibleContext().setAccessibleDescription("Accept the privacy policy and continue to the app");
		decline.getAccessibleContext().setAccessibleName("Decline privacy policy");
		decline.getAccessibleContext().setAccessibleDescription("Decline the privacy policy and exit the app");
		
		// ESC acts like decline.
		dialog.getRootPane().registerKeyboardAction(e -> {
		    accepted = false;
		    dialog.dispose();
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		// Ensure Accept gets initial focus when shown.
		accept.addHierarchyListener(e -> {
		    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && accept.isShowing()) {
		        accept.requestFocusInWindow();
		    }
		});
		
		
		dialog.setVisible(true);
		return accepted;
		}
	
	 // Privacy policy text shown to users.
	private String getPolicyText() {
		return
				  "Questify Privacy Policy — Version 1.1 (Last updated: 2025-10-28)\n\n"
				+ "Summary:\n"
				+ "Questify is an offline, local-only application. It stores your tasks and app settings only on the device where the app runs and does not transmit any data to external servers. If you decline this policy the app will quit.\n\n"
				+ "1. What we collect\n"
				+ "- Task data: each task's title, completion state, and an internal identifier. These are stored in a plain-text file (e.g., ~/.questify/tasks.txt).\n"
				+ "- App settings: simple application preferences such as whether you accepted this policy and your XP value (e.g., ~/.questify/config.properties).\n\n"
				+ "2. Purpose of collection\n"
				+ "Data is collected and stored only to provide the app's core functionality: saving and restoring your tasks and user preferences so the app behaves as expected between launches.\n\n"
				+ "3. Where data is stored and format\n"
				+ "All data is written locally to the user's profile in a folder named .questify (for example: ~/.questify on Unix-like systems). Files are stored in plain text unless you apply OS-level encryption. Questify does not encrypt files itself.\n\n"
				+ "4. Sharing and third parties\n"
				+ "Questify does not send data to remote servers, nor does it share or sell your information to third parties. If you explicitly export or share a file from your device (for example, by attaching it to an email), that action is initiated by you and outside the app's control.\n\n"
				+ "5. Retention and deletion\n"
				+ "Data is retained until you delete it. To remove all Questify data, delete the .questify directory in your user home folder (or the equivalent on your operating system). There is no automatic or remote deletion.\n\n"
				+ "6. Security\n"
				+ "Files are stored locally in plain text. Protect access to your device using your operating system account controls and disk-encryption options if you require stronger security. Avoid storing sensitive personal information in task text.\n\n"
				+ "7. Children\n"
				+ "Questify is not intended for children under 13. The app does not knowingly collect information from children.\n\n"
				+ "8. Changes to this policy\n"
				+ "If Questify's data practices change (for example, if syncing, telemetry, or external backups are added), we will update this policy and show the updated text in the app so users can review and re-accept it.\n\n"
				+ "9. Contact\n"
				+ "Questions about this policy or requests related to data stored by Questify can be sent to: privacy@questify.ca .\n\n"
				+ "How to remove data now:\n"
				+ "1) Close Questify. 2) Delete the .questify folder in your home directory (for example: rm -rf ~/.questify on Unix-like systems). This removes both tasks and app settings.\n\n"
				+ "By accepting below you confirm that you understand and agree that Questify will store data locally on your device as described above.\n";

    }
}
