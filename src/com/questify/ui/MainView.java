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

// Main application window with two lists and a simple button bar.
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
	
	private JList<? extends Task> lastListFocused;
	
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
	
	// Build UI components and wire interactions.
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
        int cols = 3; // number of main buttons
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
        
        // Delete key binding for lists.
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
        
        // Accessibility info.
        activeList.getAccessibleContext().setAccessibleName("Active Tasks List");
        activeList.getAccessibleContext().setAccessibleDescription("List of active tasks. Use arrow keys to navigate, Enter to edit, Delete to remove, Space to toggle completion.");
        
        completedList.getAccessibleContext().setAccessibleName("Completed Tasks List");
        completedList.getAccessibleContext().setAccessibleDescription("List of completed tasks. Use arrow keys to navigate, Enter to edit, Delete to remove, Space to toggle completion.");
        
        activeScroll.getAccessibleContext().setAccessibleName("Active Tasks");
        activeScroll.getAccessibleContext().setAccessibleDescription("Contains the active tasks list");
        completedScroll.getAccessibleContext().setAccessibleName("Completed Tasks");
        completedScroll.getAccessibleContext().setAccessibleDescription("Contains the completed tasks list");
        
        addBtn.setToolTipText("Add a new task (Alt+N)");
        addBtn.getAccessibleContext().setAccessibleName("Add task");
        addBtn.getAccessibleContext().setAccessibleDescription("Create a new task. Shortcut: Alt+N");
        
        delBtn.setToolTipText("Delete selected task");
        delBtn.getAccessibleContext().setAccessibleName("Delete task");
        delBtn.getAccessibleContext().setAccessibleDescription("Delete the selected task");
        
        toggleBtn.setToolTipText("Toggle selected task between active and completed");
        toggleBtn.getAccessibleContext().setAccessibleName("Toggle task");
        toggleBtn.getAccessibleContext().setAccessibleDescription("Mark selected task complete or incomplete");
        
        xpLabel.getAccessibleContext().setAccessibleName("Experience points");
        xpLabel.getAccessibleContext().setAccessibleDescription("Your earned experience points");
        xpLabel.setFocusable(false);
        
        // Track last-focused list for navigation.
        lastListFocused = activeList;
        activeList.addFocusListener(new FocusAdapter() { @Override public void focusGained(FocusEvent e) { lastListFocused = activeList; }});
        completedList.addFocusListener(new FocusAdapter() { @Override public void focusGained(FocusEvent e) { lastListFocused = completedList; }});

        // Common list key bindings.
        addCommonListBindings(activeList);
        addCommonListBindings(completedList);

        // Common list key bindings.
        addListFocusTraversalBehavior(activeList, completedList, addBtn);
        addListFocusTraversalBehavior(completedList, activeList, addBtn);

        // Custom focus traversal behavior.
        addButtonArrowNavigation(addBtn, toggleBtn);
        addButtonArrowNavigation(toggleBtn, delBtn);
        addButtonArrowNavigation(delBtn, xpLabel); // xpLabel is non-focusable but used as fallback.

        // Left from the first button returns to the last list.
        addBtn.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "leftFromFirstButton");
        addBtn.getActionMap().put("leftFromFirstButton", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (lastListFocused != null) transferFocusToList(lastListFocused);
            }
        });
        
        loadTasks();
	}
	
	// Enter to edit, Space to toggle for a list.
    private void addCommonListBindings(JList<Task> list) {
        InputMap im = list.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = list.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");
        am.put("edit", new AbstractAction() { public void actionPerformed(ActionEvent e) { onEdit(); }});

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggle");
        am.put("toggle", new AbstractAction() { public void actionPerformed(ActionEvent e) { onToggle(); }});
    }
    
    // Configure up/down/left/right behavior for list navigation.
    private void addListFocusTraversalBehavior(JList<Task> list, JList<Task> otherList, JButton firstButton) {
        InputMap im = list.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = list.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "downOrMove");
        am.put("downOrMove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                int size = list.getModel().getSize();
                if (size == 0) {
                    if (otherList.getModel().getSize() > 0) {
                        otherList.requestFocusInWindow();
                        otherList.setSelectedIndex(0);
                        otherList.ensureIndexIsVisible(0);
                    } else {
                        firstButton.requestFocusInWindow();
                    }
                    return;
                }
                if (idx < 0) {
                    list.setSelectedIndex(0);
                    list.ensureIndexIsVisible(0);
                    return;
                }
                if (idx >= size - 1) {
                    if (otherList.getModel().getSize() > 0) {
                        otherList.requestFocusInWindow();
                        otherList.setSelectedIndex(0);
                        otherList.ensureIndexIsVisible(0);
                    } else {
                        firstButton.requestFocusInWindow();
                    }
                } else {
                    list.setSelectedIndex(idx + 1);
                    list.ensureIndexIsVisible(idx + 1);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "upOrMove");
        am.put("upOrMove", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                int size = list.getModel().getSize();
                if (size == 0) {
                    if (otherList.getModel().getSize() > 0) {
                        otherList.requestFocusInWindow();
                        int li = otherList.getModel().getSize() - 1;
                        otherList.setSelectedIndex(li);
                        otherList.ensureIndexIsVisible(li);
                    }
                    return;
                }
                if (idx < 0) {
                    int li = size - 1;
                    list.setSelectedIndex(li);
                    list.ensureIndexIsVisible(li);
                    return;
                }
                if (idx == 0) {
                    if (otherList.getModel().getSize() > 0) {
                        otherList.requestFocusInWindow();
                        int li = otherList.getModel().getSize() - 1;
                        otherList.setSelectedIndex(li);
                        otherList.ensureIndexIsVisible(li);
                    }
                } else {
                    list.setSelectedIndex(idx - 1);
                    list.ensureIndexIsVisible(idx - 1);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveToButtons");
        am.put("moveToButtons", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                firstButton.requestFocusInWindow();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveToOtherList");
        am.put("moveToOtherList", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (otherList.getModel().getSize() > 0) {
                    otherList.requestFocusInWindow();
                    otherList.setSelectedIndex(0);
                    otherList.ensureIndexIsVisible(0);
                } else {
                    firstButton.requestFocusInWindow();
                }
            }
        });
    }
    
    // Left/right arrow navigation for buttons.
    private void addButtonArrowNavigation(final JComponent button, final Component toRight) {
        InputMap im = button.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = button.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "moveRight");
        am.put("moveRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (toRight != null) {
                    toRight.requestFocusInWindow();
                } else {
                    button.transferFocus();
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "moveLeft");
        am.put("moveLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // go back to the last focused list
                if (lastListFocused != null) {
                    transferFocusToList(lastListFocused);
                } else {
                    button.transferFocusBackward();
                }
            }
        });
    }
    
    // Move keyboard focus to given list and ensure selection is valid.
    private void transferFocusToList(JList<? extends Task> list) {
        if (list == null) return;
        list.requestFocusInWindow();
        int size = list.getModel().getSize();
        if (size > 0) {
            int sel = list.getSelectedIndex();
            if (sel < 0) sel = 0;
            if (sel >= size) sel = size - 1;
            list.setSelectedIndex(sel);
            list.ensureIndexIsVisible(sel);
        }
    }
	
    // Add a new task from user input.
    private void onAdd() {
		String title = JOptionPane.showInputDialog(this, "Task title:");
        if (title != null && !title.trim().isEmpty()) {
            String formatted = formatTitle(title);
            Task t = new Task(UUID.randomUUID().toString(), formatted, false);
            activeModel.addElement(t);
            saveTasksAsync();

            // Select the newly added task and return focus to the active list.
            int newIndex = activeModel.getSize() - 1;
            if (newIndex >= 0) {
                activeList.setSelectedIndex(newIndex);
                activeList.ensureIndexIsVisible(newIndex);
                activeList.requestFocusInWindow();
                lastListFocused = activeList;
            }
        } else {
            // If the user cancelled the Add dialog, restore focus to the last-focused list.
            if (lastListFocused != null) transferFocusToList(lastListFocused);
        }
    }
    
    // Delete selected task.
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
	
	// Toggle task completion and adjust XP.
	private void onToggle() {
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
	
	// Edit selected task title.
	private void onEdit() {
        if (activeList.getSelectedIndex() >= 0) {
            int idx = activeList.getSelectedIndex();
            Task t = activeModel.get(idx);
            String s = JOptionPane.showInputDialog(this, "Edit task title:", t.getTitle());
            if (s != null && !s.trim().isEmpty()) {
                String formatted = formatTitle(s);
                t.setTitle(formatted);
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
                String formatted = formatTitle(s);
                t.setTitle(formatted);
                completedModel.set(idx, t);
                saveTasksAsync();
            }
        }
    }

	// Increase XP and persist.
	private void addXp(int amount) {
        xp += amount;
        xpLabel.setText("XP: " + xp);
        try {
            cfg.setXp(xp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	// Decrease XP and persist.
	private void removeXp(int amount) {
		xp -= amount;
		xpLabel.setText("XP: " + xp);
		try {
            cfg.setXp(xp);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	// Load tasks from the TaskStore on a background thread.
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
	
	// Merge active then completed into a single list for saving.
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
	
	// Normalize a title: trim, lowercase, then capitalize first char.
	private String formatTitle(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return trimmed;
        String lower = trimmed.toLowerCase(Locale.getDefault());
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
	
	// Renderer for tasks with simple styling and safe HTML escaping.
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

        private String escapeHtml(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
