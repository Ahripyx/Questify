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
	
	public MainView(TaskStore store) {
        super("Questify");
        this.store = store;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640,420);
        setLocationRelativeTo(null);
        initUI();
        loadTasks();
    }
	
	private void initUI() {
		listModel = new DefaultListModel<>();
		taskList = new JList<>(listModel);
		taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskList.setFont(taskList.getFont().deriveFont(16f));
		JScrollPane sp = new JScrollPane(taskList);
		
		JButton addBtn = new JButton("Add (Alt+N)");
		addBtn.setMnemonic(KeyEvent.VK_N);
		JButton delBtn = new JButton("Delete");
		JButton toggleBtn = new JButton("Toggle Done");
		
		xpLabel = new JLabel("XP: 0");
		
		JPanel bp = new JPanel();
		
		bp.add(addBtn); 
		bp.add(toggleBtn); 
		bp.add(delBtn); 
		bp.add(Box.createHorizontalStrut(12)); 
		bp.add(xpLabel);
		
		add(sp, BorderLayout.CENTER);
        add(bp, BorderLayout.SOUTH);
        
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
}
