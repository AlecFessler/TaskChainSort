package taskChainPlanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.control.TextField;
import javafx.geometry.VPos;

public class GraphRenderer {
    /**
     * The GraphRenderer is responsible for rendering the visual representation
     * of the graph based on the current state, as well as providing visual feedback
     * for certain user actions.
     *
     * The graph is rerendered in whole each time the renderGraph method is called,
     * which isn't the most efficient way to do it, but it's far more simple and
     * isn't a problem at this scale.
     *
     * The graph is rendered as a collection of circles representing tasks, and lines
     * representing the dependencies between tasks. The tasks are stored in a map
     * that maps each task to its corresponding group, which contains the visual
     * representation of the task. The edges are stored in a similar map, mapping
     * each edge to its corresponding line.
     *
     * The renderTempEdge and renderInvalidEdge methods do not respond to the graph
     * state, but are used to provide visual feedback for the user when they are
     * creating a new edge. The renderTempEdge method is used to render a temporary
     * edge that follows the mouse cursor, while the renderInvalidEdge method is used
     * to render a red edge when the user tries to create an edge that would create
     * a cycle in the graph.
     */
    private DirectedAcyclicGraph<Task> graph;
    private Group graphGroup;
    private int taskRadius = 60;
    private int arrowSize = 20;
    private Map<Task, Group> taskGroups = new HashMap<>();
    private Map<DirectedAcyclicGraph.Edge<Task>, Group> edgeLines = new HashMap<>();
    private Group tempEdge = null;
    private int invalidEdgeTimeout = 500;

    /**
     * Constructor for the GraphRenderer class.
     *
     * @param graph The graph to render.
     * @param graphGroup The group to render the graph in.
     */
    public GraphRenderer(DirectedAcyclicGraph<Task> graph, Group graphGroup) {
        this.graph = graph;
        this.graphGroup = graphGroup;
    }

    /**
     * Renders the graph in the graphGroup.
     *
     * @param reset Whether to reset the graph before rendering.
     */
    public void renderGraph(boolean reset) {
        if (reset) {
            taskGroups.clear();
            edgeLines.clear();
            graphGroup.getChildren().clear();
        }
        for (Task node : graph.nodes()) {
            Group taskGroup = renderTask(node);
            taskGroups.put(node, taskGroup);
        }
        for (DirectedAcyclicGraph.Edge<Task> edge : graph.edges()) {
            Group edgeLine = renderEdge(edge);
            edgeLines.put(edge, edgeLine);
        }
    }

    /**
     * Makes a task group for a task.
     *
     * @param task The task to make a group for.
     * @return The group representing the task.
     */
    private Group makeTask(Task task) {
        Group taskGroup = new Group();

        Circle circle = new Circle(task.x(), task.y(), taskRadius);
        circle.setFill(Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);

        Text text = new Text(task.name());
        text.setTextOrigin(VPos.CENTER);
        text.setTextAlignment(TextAlignment.CENTER);

        text.setLayoutX(task.x() - taskRadius);
        text.setLayoutY(task.y());
        text.setWrappingWidth(2 * taskRadius);

        Text stateText = new Text(task.taskState());
        stateText.setTextOrigin(VPos.CENTER);
        stateText.setTextAlignment(TextAlignment.CENTER);

        stateText.setLayoutX(task.x() - taskRadius);
        stateText.setLayoutY(task.y() + taskRadius / 2);
        stateText.setWrappingWidth(2 * taskRadius);

        taskGroup.getChildren().addAll(circle, text, stateText);

        return taskGroup;
    }

    /**
     * Renders a task in the graphGroup.
     *
     * @param task The task to render.
     * @return The group representing the task.
     */
    private Group renderTask(Task task) {
        Group taskGroup = makeTask(task);
        graphGroup.getChildren().add(taskGroup);
        return taskGroup;
    }

    /**
     * Makes an edge line between two points.
     *
     * @param x1 The x-coordinate of the first point.
     * @param y1 The y-coordinate of the first point.
     * @param x2 The x-coordinate of the second point.
     * @param y2 The y-coordinate of the second point.
     * @return The line representing the edge.
     */
    public Group makeEdge(double x1, double y1, double x2, double y2, Color color) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(color);
        line.setStrokeWidth(5);

        double angle = Math.atan2(y2 - y1, x2 - x1);

        double arrowLength = taskRadius;
        double arrowX = x2 - arrowLength * Math.cos(angle);
        double arrowY = y2 - arrowLength * Math.sin(angle);

        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
            arrowX, arrowY,
            arrowX - arrowSize * Math.cos(angle - Math.PI / 6),
            arrowY - arrowSize * Math.sin(angle - Math.PI / 6),
            arrowX - arrowSize * Math.cos(angle + Math.PI / 6),
            arrowY - arrowSize * Math.sin(angle + Math.PI / 6)
        );
        arrowHead.setFill(color);

        Group edgeGroup = new Group();
        edgeGroup.getChildren().addAll(line, arrowHead);
        return edgeGroup;
    }

    /**
     * Renders an edge in the graphGroup.
     *
     * Edges are always rendered below the tasks, so they are added at index 0.
     *
     * @param edge The edge to render.
     * @return The line representing the edge.
     */
    private Group renderEdge(DirectedAcyclicGraph.Edge<Task> edge) {
        Task source = edge.from;
        Task target = edge.to;
        Group edgeLine = makeEdge(source.x(), source.y(), target.x(), target.y(), Color.GRAY);
        graphGroup.getChildren().add(0, edgeLine);
        return edgeLine;
    }

    /**
     * Renders a temporary edge in the graphGroup.
     *
     * @param startTask The task the edge starts from.
     * @param x The x-coordinate of the end of the edge.
     * @param y The y-coordinate of the end of the edge.
     */
    public void renderTempEdge(Task startTask, double x, double y) {
        if (tempEdge != null) {
            graphGroup.getChildren().remove(tempEdge);
        }
        tempEdge = makeEdge(startTask.x(), startTask.y(), x, y, Color.GRAY);
        graphGroup.getChildren().add(0, tempEdge);
    }

    /**
     * Renders an invalid edge in the graphGroup.
     * The edge is rendered in red and removed after a timeout.
     *
     * @param startTask The task the edge starts from.
     * @param endTask The task the edge ends at.
     */
    public void renderInvalidEdge(Task startTask, Task endTask) {
        Group invalidEdge = makeEdge(startTask.x(), startTask.y(), endTask.x(), endTask.y(), Color.RED);
        graphGroup.getChildren().add(0, invalidEdge);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    graphGroup.getChildren().remove(invalidEdge);
                });
            }
        }, invalidEdgeTimeout);
    }

    /**
     * Clears the temporary edge from the graphGroup.
     */
    public void clearTempEdge() {
        if (tempEdge != null) {
            graphGroup.getChildren().remove(tempEdge);
            tempEdge = null;
        }
    }

    /**
     * Renders an editable task name in the graphGroup.
     *
     * @param task The task to render the editable name for.
     * @return The text field representing the editable name.
     */
    public TextField renderEditableTaskName(Task task) {
        Group taskGroup = taskGroups.get(task);
        taskGroup.getChildren().remove(1);

        TextField textField = new TextField(task.name());
        textField.setPrefWidth(2 * taskRadius);
        textField.setLayoutX(task.x() - taskRadius);
        textField.setLayoutY(task.y() - taskRadius / 4);

        textField.setStyle("-fx-border-color: transparent; -fx-background-color: lightblue;");
        taskGroup.getChildren().add(1, textField);
        return textField;
    }

    /**
     * @return The task group radius constant.
     */
    public int taskRadius() {
        return taskRadius;
    }

    /**
     * @return The task groups map.
     */
    public Map<Task, Group> taskGroups() {
        return taskGroups;
    }

    /**
     * @return The edge lines map.
     */
    public Map<DirectedAcyclicGraph.Edge<Task>, Group> edgeLines() {
        return edgeLines;
    }
}
