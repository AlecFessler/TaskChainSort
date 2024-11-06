package taskChainPlanner;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import java.util.ArrayList;
import java.util.List;

public class TaskListRenderer {
    private Pane taskGroup;
    private static final double TASK_WIDTH = 200;
    private static final double TASK_HEIGHT = 50;
    private static final double PADDING = 10;

    /**
     * Create a task list renderer with the given task group
     *
     * @param taskGroup Pane to render tasks on
     */
    public TaskListRenderer(Pane taskGroup) {
        this.taskGroup = taskGroup;
    }

    /**
     * Returns the valid state transitions given the current task state
     *
     * @param task Task to check state for
     * @return List<String> representing the valid state transitions
     */
    private List<String> getAvailableStates(Task task) {
        List<String> states = new ArrayList<>();
        if (task.isComplete()) {
            states.add("Assigned");
            states.add("Ready");
        } else if (task.isAssigned()) {
            states.add("Complete");
            states.add("Ready");
        } else if (task.isReady()) {
            states.add("Assigned");
        }
        return states;
    }

    /**
     * Render a task on the task group
     *
     * @param task Task to be rendered
     * @param yPos Y position of the task
     * @param isAlternateColor Whether the task should have an alternate color
     * @return Rectangle representing the task
     */
    private Rectangle renderTask(Task task, double yPos, boolean isAlternateColor) {
        Rectangle taskRectangle = new Rectangle(TASK_WIDTH, TASK_HEIGHT);
        taskRectangle.setFill(isAlternateColor ? Color.LIGHTBLUE : Color.DODGERBLUE);
        taskRectangle.setArcWidth(10);
        taskRectangle.setArcHeight(10);

        Text taskNameText = new Text(task.name());
        taskNameText.setFont(new Font(14));
        taskNameText.setWrappingWidth(TASK_WIDTH);
        taskNameText.setTextAlignment(TextAlignment.CENTER);

        ComboBox<String> stateDropdown = new ComboBox<>();
        stateDropdown.getItems().addAll(getAvailableStates(task));
        stateDropdown.setValue(task.taskState());

        taskRectangle.setLayoutY(yPos);
        taskNameText.setLayoutY(yPos + 20);
        stateDropdown.setLayoutY(yPos + 25);

        stateDropdown.setOnAction(event -> {
            String selectedState = stateDropdown.getValue();
            task.clearBitflags();
            switch (selectedState) {
                case "Assigned":
                    task.setAssigned();
                    break;
                case "Complete":
                    task.setComplete();
                    break;
                case "Ready":
                    task.setReady();
                    break;
            }
        });

        taskGroup.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double centerX = (newWidth.doubleValue() - TASK_WIDTH) / 2;
            taskRectangle.setLayoutX(centerX);
            taskNameText.setLayoutX(centerX);
            stateDropdown.setLayoutX(centerX);
        });

        taskGroup.getChildren().addAll(taskRectangle, taskNameText, stateDropdown);
        return taskRectangle;
    }

    /**
     * Render a list of tasks on the task group
     *
     * @param tasks PriorityQueue of tasks to be rendered
     */
    public void renderTasks(PriorityQueue<Task> tasks) {
        taskGroup.getChildren().clear();

        double yPos = PADDING;
        boolean isAlternateColor = false;

        while (!tasks.isEmpty()) {
            Task task = tasks.poll();
            renderTask(task, yPos, isAlternateColor);
            yPos += TASK_HEIGHT + PADDING;
            isAlternateColor = !isAlternateColor;
        }
    }
}
