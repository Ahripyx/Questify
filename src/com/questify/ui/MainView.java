package com.questify.ui;

import com.questify.model.Task;
import com.questify.store.TaskStore;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class MainView extends JFrame {
	private DefaultListModel<Task> listModel;
	private JList<Task> taskList;
	private final TaskStore store;
	private JLabel xpLabel;
	private int xp = 0;
	private final Dimension phoneSize;
	
	public MainView(TaskStore store, Dimension phoneSize) {
        super("Questify");
        this.store = store;
        this.phoneSize = phoneSize;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        initUI();
        getContentPane().setPreferredSize(phoneSize);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }
	
	private void initUI() {
		listModel = new DefaultListModel<>();
		taskList = new JList<>(listModel);
		taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskList.setFont(taskList.getFont().deriveFont(16f));
		taskList.setCellRenderer(new TaskCellRenderer());
		
		
		JScrollPane sp = new JScrollPane(taskList);
		sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        
		JButton addBtn = new JButton("Add");
		addBtn.setMnemonic(KeyEvent.VK_N);
		JButton delBtn = new JButton("Delete");
		JButton toggleBtn = new JButton("Done");
		
		int gap = 8;
        int cols = 3; // 3 main buttons
        int padding = 32; // left+right padding inside buttonBar
        int avail = Math.max(240, phoneSize.width - padding - (gap * (cols - 1)));
        int btnW = Math.max(120, avail / cols);
		
		JPanel buttonBar = new JPanel(new GridLayout(1, 4, 6, 0));
		addBtn.setPreferredSize(new Dimension(btnW, 48));
        toggleBtn.setPreferredSize(new Dimension(btnW, 48));
        delBtn.setPreferredSize(new Dimension(btnW, 48));
		
		xpLabel = new JLabel("XP: 0");
		xpLabel.setHorizontalAlignment(SwingConstants.CENTER);
		xpLabel.setPreferredSize(new Dimension(100, 48));
		
		buttonBar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        buttonBar.add(addBtn);
        buttonBar.add(toggleBtn);
        buttonBar.add(delBtn);
        buttonBar.add(xpLabel);
		
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sp, BorderLayout.CENTER);
        getContentPane().add(buttonBar, BorderLayout.SOUTH);
        
        addBtn.addActionListener(e -> onAdd());
        delBtn.addActionListener(e -> onDelete());
        toggleBtn.addActionListener(e -> onToggle());
        
        // keyboard delete
        taskList.getInputMap(JComponent.WHEN_FOCUSED)
        	.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        taskList.getActionMap().put("delete", new AbstractAction() {
        	public void actionPerformed(ActionEvent e) { onDelete(); }
        });
        
        // double click to edit
        taskList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) onEdit();
            }
        });
        
        
        loadTasks();
	}
	
	private void onAdd() {
        String title = JOptionPane.showInputDialog(this, "Task title:");
        if (title != null && !title.trim().isEmpty()) {
            Task t = new Task(UUID.randomUUID().toString(), title.trim(), false);
            listModel.addElement(t);
            saveTasksAsync();
        }
    }
	
	private void onDelete() {
        int idx = taskList.getSelectedIndex();
        if (idx >= 0) {
            listModel.remove(idx);
            saveTasksAsync();
        }
    }
	
	private void onToggle() {
        int idx = taskList.getSelectedIndex();
        if (idx >= 0) {
            Task t = listModel.get(idx);
            boolean nowDone = !t.isDone();
            t.setDone(nowDone);
            listModel.set(idx, t); // refresh
            if (nowDone) addXp(10);
            saveTasksAsync();
        }
    }
	
	private void onEdit() {
        int idx = taskList.getSelectedIndex();
        if (idx >= 0) {
            Task t = listModel.get(idx);
            String s = JOptionPane.showInputDialog(this, "Edit task title:", t.getTitle());
            if (s != null && !s.trim().isEmpty()) {
                t.setTitle(s.trim());
                listModel.set(idx, t);
                saveTasksAsync();
            }
        }
    }
	
	private void addXp(int amount) {
        xp += amount;
        xpLabel.setText("XP: " + xp);
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
                    listModel.clear();
                    tasks.forEach(listModel::addElement);
                } catch (Exception e) { e.printStackTrace(); }
            }
        };
        w.execute();
    }
	
	private List<Task> getAllTasksFromModel() {
        List<Task> out = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++) out.add(listModel.get(i));
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
            panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // padding like mobile
            panel.add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Task> list, Task value, int index, boolean isSelected, boolean cellHasFocus) {
            String title = value.getTitle();
            if (value.isDone()) {
                // simple strike-through via HTML
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
