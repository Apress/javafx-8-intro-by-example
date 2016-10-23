package jfx8ibe;

import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author cdea
 */
public class TouchEventsWithPaths extends Application {

    Path onePath = new Path();
    Point2D anchorPt;
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chapter 12 Touch Events with Paths");
        
        Group root = new Group();        
        // add path
        root.getChildren().add(onePath);
        
        Scene scene = new Scene(root, 600, 800, Color.WHITE);
                
        RadialGradient gradient1 = new RadialGradient(0,
                .1,
                100,
                100,
                20,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED),
                new Stop(1, Color.BLACK));
        
        // create a sphere
        Circle sphere = new Circle(100, 100, 20, gradient1);
                
        // add sphere
        root.getChildren().add(sphere);
       
        // animate sphere by following the path.
        PathTransition pathTransition = 
              new PathTransition(Duration.millis(4000), onePath, sphere);
        pathTransition.setCycleCount(1);
        pathTransition.setOrientation(
              PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            
        // once finished clear path
        pathTransition.setOnFinished((actionEvent) -> 
              onePath.getElements().clear());

        // starting initial path
        scene.setOnMousePressed((mouseEvent) ->   
           startPath(mouseEvent.getX(), mouseEvent.getY())
        );
        
        scene.setOnTouchPressed((touchEvent) ->
              startPath(touchEvent.getTouchPoint()
                                  .getX(), 
                        touchEvent.getTouchPoint()
                                  .getY()));
        
        // dragging creates lineTos added to the path
        scene.setOnMouseDragged((mouseEvent) -> 
                   drawPath(mouseEvent.getX(), mouseEvent.getY()));
        scene.setOnTouchMoved((touchEvent) -> 
                   drawPath(touchEvent.getTouchPoint()
                                  .getX(), 
                        touchEvent.getTouchPoint()
                                  .getY()));
        
        
        // end the path when mouse released event
        scene.setOnMouseReleased((mouseEvent) -> endPath(pathTransition));
        scene.setOnTouchReleased((touchEvent) -> endPath(pathTransition));
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void startPath(double x, double y) {
      onePath.getElements().clear();
      // start point in path
      anchorPt = new Point2D(x, y);
      onePath.setStrokeWidth(3);
      onePath.setStroke(Color.BLACK);
      onePath.getElements()
             .add(new MoveTo(anchorPt.getX(), anchorPt.getY()));
    }
    
    private void drawPath(double x, double y) {
      onePath.getElements().add(new LineTo(x, y));
    }
    
    private void endPath(PathTransition pathTransition) {
       onePath.setStrokeWidth(0);
           if (onePath.getElements().size() > 1) {
              pathTransition.stop();
              pathTransition.playFromStart();
           }
    }
}

