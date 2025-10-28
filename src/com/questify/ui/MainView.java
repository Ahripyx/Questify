package com.questify.ui;

import com.questify.model.Task;
import com.questify.store.TaskStore;
import com.questify.util.ConfigStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class MainView extends JFrame {
	private DefaultListModel<Task> activeModel;
	private DefaultListModel<Task> completedModel;
    private JList<Task> activeList;
    private JList<Task> completedList;
	private final TaskStore store;
	private final ConfigStore cfg;
	private JLabel xpLabel;
	private int xp = 0;
	private final Dimension phoneSize;
	
	public MainView(TaskStore store, Dimension phoneSize, ConfigStore cfg) {
        super("Questify");
        this.store = store;
        this.phoneSize = phoneSize;
        this.cfg = cfg;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        initUI();
        
        xp = cfg.getXp();
        if (xpLabel != null) xpLabel.setText("XP: " + xp);
        
        getContentPane().setPreferredSize(phoneSize);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }
	
	private void initUI() {
		activeModel = new DefaultListModel<>();
		completedModel = new DefaultListModel<>();
		
		activeList = new JList<>(activeModel);
        completedList = new JList<>(completedModel);
        
        activeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        completedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        activeList.setFont(activeList.getFont().deriveFont(16f));
        completedList.setFont(completedList.getFont().deriveFont(16f));
        
        TaskCellRenderer renderer = new TaskCellRenderer();
        activeList.setCellRenderer(renderer);
        completedList.setCellRenderer(renderer);
        
        JScrollPane activeScroll = new JScrollPane(activeList);
        JScrollPane completedScroll = new JScrollPane(completedList);
        activeScroll.setBorder(BorderFactory.createTitledBorder("Active Tasks"));
        completedScroll.setBorder(BorderFactory.createTitledBorder("Completed Tasks"));
		
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, activeScroll, completedScroll);
        split.setResizeWeight(0.5);
        split.setOneTouchExpandable(true);
        
		JButton addBtn = new JButton("Add");
		addBtn.setMnemonic(KeyEvent.VK_N);
		JButton delBtn = new JButton("Delete");
		JButton toggleBtn = new JButton("Toggle");
		
		int gap = 8;
        int cols = 3; // 3 main buttons
        int padding = 32; // left+right padding inside buttonBar
        int avail = Math.max(240, phoneSize.width - padding - (gap * (cols - 1)));
        int btnW = Math.max(120, avail / cols);
		
        addBtn.setPreferredSize(new Dimension(btnW, 48));
        toggleBtn.setPreferredSize(new Dimension(btnW, 48));
        delBtn.setPreferredSize(new Dimension(btnW, 48));
        xpLabel = new JLabel("XP: 0");
        xpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        xpLabel.setPreferredSize(new Dimension(100, 48));
		
		xpLabel = new JLabel("XP: 0");
		xpLabel.setHorizontalAlignment(SwingConstants.CENTER);
		xpLabel.setPreferredSize(new Dimension(100, 48));
		
		JPanel buttonBar = new JPanel(new GridLayout(1, 4, 6, 0));
        buttonBar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buttonBar.add(addBtn);
        buttonBar.add(toggleBtn);
        buttonBar.add(delBtn);
        buttonBar.add(xpLabel);
		
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(split, BorderLayout.CENTER);
        getContentPane().add(buttonBar, BorderLayout.SOUTH);
        
        addBtn.addActionListener(e -> onAdd());
        delBtn.addActionListener(e -> onDelete());
        toggleBtn.addActionListener(e -> onToggle());
        
        // keyboard delete
        activeList.getInputMap(JComponent.WHEN_FOCUSED)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteActive");
	    activeList.getActionMap().put("deleteActive", new AbstractAction() {
	        public void actionPerformed(ActionEvent e) { onDelete(); }
	    });
	    
	    completedList.getInputMap(JComponent.WHEN_FOCUSED)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteCompleted");
	    completedList.getActionMap().put("deleteCompleted", new AbstractAction() {
	        public void actionPerformed(ActionEvent e) { onDelete(); }
	    });
	    
	    MouseAdapter editOnDouble = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEdit();
            }
        };
        activeList.addMouseListener(editOnDouble);
        completedList.addMouseListener(editOnDouble);
        
        //WCAG related stuff
        activeList.getAccessibleContext().setAccessibleName("Active Tasks List");
        activeList.getAccessibleContext().setAccessibleDescription("List of active tasks. Use arrow keys to navigate, Enter to edit, Delete to remove, Space to toggle completion.");
        
        completedList.getAccessibleContext().setAccessibleName("Completed Tasks List");
        completedList.getAccessibleContext().setAccessibleDescription("List of completed tasks. Use arrow keys to navigate, Enter to edit, Delete to remove, Space to toggle completion.");
        
        // Expose scroll panes to accessibility
        activeScroll.getAccessibleContext().setAccessibleName("Active Tasks");
        activeScroll.getAccessibleContext().setAccessibleDescription("Contains the active tasks list");
        completedScroll.getAccessibleContext().setAccessibleName("Completed Tasks");
        completedScroll.getAccessibleContext().setAccessibleDescription("Contains the completed tasks list");
        
        // Buttons: accessible name, description and tooltip
        addBtn.setToolTipText("Add a new task (Alt+N)");
        addBtn.getAccessibleContext().setAccessibleName("Add task");
        addBtn.getAccessibleContext().setAccessibleDescription("Create a new task. Shortcut: Alt+N");
        
        delBtn.setToolTipText("Delete selected task");
        delBtn.getAccessibleContext().setAccessibleName("Delete task");
        delBtn.getAccessibleContext().setAccessibleDescription("Delete the selected task");
        
        toggleBtn.setToolTipText("Toggle selected task between active and completed");
        toggleBtn.getAccessibleContext().setAccessibleName("Toggle task");
        toggleBtn.getAccessibleContext().setAccessibleDescription("Mark selected task complete or incomplete");
        
        // XP label accessible
        xpLabel.getAccessibleContext().setAccessibleName("Experience points");
        xpLabel.getAccessibleContext().setAccessibleDescription("Your earned experience points");
        
        // Keyboard: Enter to edit, Space to toggle (for focused list)
        InputMap activeIM = activeList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap activeAM = activeList.getActionMap();
        activeIM.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");
        activeAM.put("edit", new AbstractAction() { public void actionPerformed(ActionEvent e) { onEdit(); }});
        activeIM.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggle");
        activeAM.put("toggle", new AbstractAction() { public void actionPerformed(ActionEvent e) { onToggle(); }});
        
        
        InputMap compIM = completedList.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap compAM = completedList.getActionMap();
        compIM.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");
        compAM.put("edit", new AbstractAction() { public void actionPerformed(ActionEvent e) { onEdit(); }});
        compIM.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggle");
        compAM.put("toggle", new AbstractAction() { public void actionPerformed(ActionEvent e) { onToggle(); }});
        
        loadTasks();
	}
	
	private void onAdd() {
        String title = JOptionPane.showInputDialog(this, "Task title:");
        if (title != null && !title.trim().isEmpty()) {
            Task t = new Task(UUID.randomUUID().toString(), title.trim(), false);
            activeModel.addElement(t);
            saveTasksAsync();
        }
    }
	
	private void onDelete() {
        if (activeList.getSelectedIndex() >= 0) {
            activeModel.remove(activeList.getSelectedIndex());
            saveTasksAsync();
            return;
        }
        if (completedList.getSelectedIndex() >= 0) {
            completedModel.remove(completedList.getSelectedIndex());
            saveTasksAsync();
        }
    }
	
	private void onToggle() {
        // If an active task selected -> mark done, move to completed and award XP
        int aidx = activeList.getSelectedIndex();
        if (aidx >= 0) {
            Task t = activeModel.get(aidx);
            t.setDone(true);
            activeModel.remove(aidx);
            completedModel.addElement(t);
            addXp(10);
            saveTasksAsync();
            return;
        }
        // If a completed task selected -> mark undone, move to active
        int cidx = completedList.getSelectedIndex();
        if (cidx >= 0) {
            Task t = completedModel.get(cidx);
            t.setDone(false);
            completedModel.remove(cidx);
            activeModel.addElement(t);
            removeXp(10);
            saveTasksAsync();
        }
    }
	
	private void onEdit() {
        if (activeList.getSelectedIndex() >= 0) {
            int idx = activeList.getSelectedIndex();
            Task t = activeModel.get(idx);
            String s = JOptionPane.showInputDialog(this, "Edit task title:", t.getTitle());
            if (s != null && !s.trim().isEmpty()) {
                t.setTitle(s.trim());
                activeModel.set(idx, t);
                saveTasksAsync();
            }
            return;
        }
        if (completedList.getSelectedIndex() >= 0) {
            int idx = completedList.getSelectedIndex();
            Task t = completedModel.get(idx);
            String s = JOptionPane.showInputDialog(this, "Edit task title:", t.getTitle());
            if (s != null && !s.trim().isEmpty()) {
                t.setTitle(s.trim());
                completedModel.set(idx, t);
                saveTasksAsync();
            }
        }
    }

	
	private void addXp(int amount) {
        xp += amount;
        xpLabel.setText("XP: " + xp);
        try {
            cfg.setXp(xp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	private void removeXp(int amount) {
		xp -= amount;
		xpLabel.setText("XP: " + xp);
		try {
            cfg.setXp(xp);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	private void loadTasks() {
        SwingWorker<List<Task>,Void> w = new SwingWorker<>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                return store.loadTasks();
            }
            @Override
            protected void done() {
                try {
                    List<Task> tasks = get();
                    activeModel.clear();
                    completedModel.clear();
                    // preserve order: add active first then completed
                    for (Task t : tasks) {
                        if (t.isDone()) completedModel.addElement(t);
                        else activeModel.addElement(t);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        w.execute();
    }
	
	private List<Task> getAllTasksFromModel() {
        List<Task> out = new ArrayList<>();
        for (int i = 0; i < activeModel.getSize(); i++) out.add(activeModel.get(i));
        for (int i = 0; i < completedModel.getSize(); i++) out.add(completedModel.get(i));
        return out;
    }
	
	private void saveTasksAsync() {
        List<Task> tasks = getAllTasksFromModel();
        SwingWorker<Void,Void> w = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                store.saveTasks(tasks);
                return null;
            }
        };
        w.execute();
    }
	
	private class TaskCellRenderer implements ListCellRenderer<Task> {
        private final JPanel panel = new JPanel(new BorderLayout());
        private final JLabel label = new JLabel();

        TaskCellRenderer() {
            label.setOpaque(false);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 16f));
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            panel.add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index, boolean isSelected, boolean cellHasFocus) {
            String title = value.getTitle();
            if (value.isDone()) {
                label.setText("<html><span style='color:gray;'><s>" + escapeHtml(title) + "</s></span></html>");
            } else {
                label.setText("<html>" + escapeHtml(title) + "</html>");
            }

            if (isSelected) {
                panel.setBackground(new Color(0xDDEEFF));
            } else {
                panel.setBackground(Color.WHITE);
            }
            return panel;
        }

        // minimal HTML-escape for safety
        private String escapeHtml(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
