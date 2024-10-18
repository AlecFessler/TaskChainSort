package taskChainPlanner;

import javafx.scene.layout.Pane;

public class TaskManagementController {
    private PriorityQueue<Task> sortedTasks;
    private TaskListRenderer taskListRenderer;

    /**
     * Constructor for TaskManagementController
     *
     * @param graph DirectedAcyclicGraph<Task> graph
     * @param taskManagementPane Pane taskManagementPane
     */
    public TaskManagementController(DirectedAcyclicGraph<Task> graph, Pane taskManagementPane) {
        this.sortedTasks = Task.sortTasks(graph);
        this.taskListRenderer = new TaskListRenderer(taskManagementPane);
        renderTasks();
    }

    /**
     * Renders the tasks in the task list
     */
    private void renderTasks() {
        taskListRenderer.renderTasks(sortedTasks);
    }
}
