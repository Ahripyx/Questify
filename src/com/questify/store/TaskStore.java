package com.questify.store;

import com.questify.model.Task;
import java.util.List;

//Interface for task persistence backends.
public interface TaskStore {
    List<Task> loadTasks() throws Exception;
    void saveTasks(List<Task> tasks) throws Exception;
}
