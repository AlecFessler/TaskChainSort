package taskChainPlanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;
    private DirectedAcyclicGraph<Task> graph;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.graph = new DirectedAcyclicGraph<>();

        initializeRootLayout();
        switchToGraphEditingView();
    }

    private void initializeRootLayout() {
        rootLayout = new BorderPane();

        HBox topMenu = new HBox();
        Button editGraphButton = new Button("Graph Editing View");
        Button taskManagementButton = new Button("Task Management View");

        editGraphButton.setOnAction(e -> switchToGraphEditingView());
        taskManagementButton.setOnAction(e -> switchToTaskManagementView());

        topMenu.getChildren().addAll(editGraphButton, taskManagementButton);
        rootLayout.setTop(topMenu);

        Scene scene = new Scene(rootLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Task Chain Planner");
        primaryStage.show();
    }

    public void switchToGraphEditingView() {
        Pane graphPane = new Pane();
        GraphEditingController graphController = new GraphEditingController(graph, graphPane);
        rootLayout.setCenter(graphPane);
    }

    public void switchToTaskManagementView() {
        Pane taskManagementPane = new Pane();
        TaskManagementController taskController = new TaskManagementController(graph, taskManagementPane);
        rootLayout.setCenter(taskManagementPane);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
