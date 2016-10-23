package jfx8ibe;

import com.leapmotion.leap.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

/**
 * Chapter 12. JavaFX and Leap Motion
 * @author jpereda
 */
public class StarLeapFX extends Application {
    
    private final SimpleLeapListener listener = new SimpleLeapListener();
    private final Controller leapController = new Controller();
    
    @Override
    public void start(Stage primaryStage) {
        
        leapController.addListener(listener);        
        SVGPath sv=new SVGPath();
        sv.setContent("M 0,44.9 H 123.5 L 23.6,117.5 61.8,0 100,117.5 z");
        sv.setEffect(new DropShadow());
        sv.setStyle("-fx-fill:radial-gradient(center 50% 20%, radius 90%, "
                + "black 0%, black 30%, red 70%, yellow 85%, red 93%, yellow 100%);");
        sv.setTranslateY(302.5);           
        final AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 0% 100%, "
                       + "#99fff3 0%, #00ccb4 70%, #10cc00 71%, #0c9900 100%);");
        root.getChildren().add(sv);
        Scene scene = new Scene(root, 800, 600);
        
        listener.pointProperty().addListener((ov, t, t1) -> {
            Platform.runLater(() -> {
                Point2D d=root.sceneToLocal(t1.getX()-scene.getX()-scene.getWindow().getX(),
                                            t1.getY()-scene.getY()-scene.getWindow().getY());
                double dx=d.getX(), dy=d.getY();
                if(dx>=0d && dx<=root.getWidth()-sv.getBoundsInParent().getWidth() &&
                   dy>=0d && dy<=root.getHeight()-sv.getBoundsInParent().getHeight()){
                    double ix=(dx-sv.getTranslateX());
                    double iy=(dy-sv.getTranslateY());
                    if(ix*ix+iy*iy>50){
                        sv.setRotate(Math.toDegrees(Math.PI/2+Math.atan2(iy, ix)));
                    }
                    sv.setTranslateX(dx);
                    sv.setTranslateY(dy);
                }
            });
        });
        primaryStage.setTitle("My flying Shooting Star!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop(){
        leapController.removeListener(listener);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}