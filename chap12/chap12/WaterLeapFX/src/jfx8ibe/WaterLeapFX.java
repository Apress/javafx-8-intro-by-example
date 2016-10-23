package jfx8ibe;

import com.leapmotion.leap.*;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import javafx.animation.AnimationTimer;
import javafx.application.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Chapter 12. JavaFX and Leap Motion
 * @author jpereda
 */
public class WaterLeapFX extends Application {
    
    private int original[], water[];
    private short waterMap[];
    private int width, height, halfWidth, halfHeight, size;
    private int oldInd, newInd, mapInd;
    
    private AnimationTimer timer;
    private long lastTimerCall;
    
    private final SimpleLeapListener listener = new SimpleLeapListener();
    private final Controller leapController = new Controller();
    
    @Override
    public void start(Stage primaryStage) {
        Image img=screenSnapshot();
        final ImageView imageView = new ImageView(img);
        width = (int)imageView.getImage().getWidth();
        height = (int)imageView.getImage().getHeight();
        halfWidth=width>>1;
        halfHeight=height>>1;
        size = width * (height+2)*2;
        waterMap = new short[size];
        water = new int[width*height];
        original = new int[width*height];
        oldInd = width;
        newInd = width * (height+3);
        PixelReader pixelReader = imageView.getImage().getPixelReader();
        pixelReader.getPixels(0,0,width,height, 
                WritablePixelFormat.getIntArgbInstance(),original, 0,width);
        
        StackPane root = new StackPane();
        root.getChildren().add(imageView);
        Scene scene = new Scene(root, width, height);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, t1 -> {
            if(t1.getCode() == KeyCode.ESCAPE){ Platform.exit(); }
        });
        
        lastTimerCall = System.nanoTime();
        timer = new AnimationTimer() {
            @Override public void handle(long now) {
                if (now > lastTimerCall + 30_000_000l) {
                    imageView.setImage(applyEffect());
                    lastTimerCall = now;
                }
            }
        };
        
        leapController.addListener(listener);  
        listener.isSwipe().addListener((ov, b, b1) -> {
            if(b1){ Platform.runLater(() -> waterMap = new short[size] ); }
        });
        listener.isDrop().addListener((ov, b, b1) -> {
            if(b1){
                Platform.runLater(() -> {
                    for(int i=0; i<listener.points.size(); i++){
                        Point3D t1=listener.points.get(i);
                        Point2D d=root.sceneToLocal(
                                t1.getX()-scene.getX()-scene.getWindow().getX(),
                                t1.getY()-scene.getY()-scene.getWindow().getY());
                        double dx=d.getX(), dy=d.getY(), dz = Math.abs(t1.getZ());
                        int rad=(dz<50)?5:((dz<150)?4:3);
                        if(dx>=0d && dx<=root.getWidth() && dy>=0d && dy<=root.getHeight()){
                            waterDrop((int)dx,(int)dy,rad);
                        }
                    }
                });
            }
        });
       
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        timer.start();
    }
    
    @Override
    public void stop(){
        timer.stop();
        leapController.removeListener(listener);
    }
    
    public void waterDrop(int dx, int dy, int rad) {
        for (int j=dy-rad; j<dy+rad; j++) {
            for (int k=dx-rad; k<dx+rad; k++) {
                if (j>=0 && j<height && k>=0 && k<width) {
                    waterMap[oldInd+(j*width)+k] += 128;            
                } 
            }
        }
    }
    
    private Image applyEffect() {
        int a,b, i=oldInd;
        oldInd=newInd;
        newInd=i;
        i=0;
        mapInd=oldInd;
        for (int y=0;y<height;y++) {
            for (int x=0;x<width;x++) {
                short data = (short)((waterMap[mapInd-width]+waterMap[mapInd+width]+
                                      waterMap[mapInd-1]+waterMap[mapInd+1])>>1);
                data -= waterMap[newInd+i];
                data -= data>>4;
                waterMap[newInd+i]=data;
                data = (short)(1024-data);
                a=((x-halfWidth)*data/1024)+halfWidth;
                if (a>=width){ a=width-1; }
                if (a<0){ a=0; }
                b=((y-halfHeight)*data/1024)+halfHeight;
                if (b>=height){ b=height-1; }
                if (b<0){ b=0; }
                water[i++]=original[a+(b*width)];
                mapInd++;
            }
        }
        WritableImage raster = new WritableImage(width, height);
        PixelWriter pixelWriter = raster.getPixelWriter();
        pixelWriter.setPixels(0,0,width,height, 
                PixelFormat.getIntArgbInstance(),water, 0,width);
        return raster;
    } 
    
    private Image screenSnapshot() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            Robot robot = new Robot(env.getScreenDevices()[0]);
            final BufferedImage bi = robot.createScreenCapture(env.getMaximumWindowBounds());
            return SwingFXUtils.toFXImage(bi, null);
        } catch (AWTException e) {}
        return null;
    }
    
    // We define SimpleLeapListener class, the Leap Listener subclass,
    // as a private inner class of WaterLeapFX
    private class SimpleLeapListener extends Listener {

        private final BooleanProperty drop=new SimpleBooleanProperty(false);
        protected final ObservableList<Point3D> points= FXCollections.observableArrayList();
        private final BooleanProperty swipe=new SimpleBooleanProperty(false);
        
        @Override
        public void onConnect(Controller controller){
            controller.enableGesture(Gesture.Type.TYPE_SWIPE);
        }
        @Override
        public void onFrame(Controller controller) {
            Frame frame = controller.frame();
            Screen screen = controller.locatedScreens().get(0);
            if (screen != null && screen.isValid()){
                drop.set(false);
                points.clear();
                for(Finger finger : frame.fingers()){
                    if(finger.isValid()){
                        Vector inter = screen.intersect(finger.tipPosition(),
                                                        finger.direction(), true);
                        Point3D p = new Point3D(
                            screen.widthPixels()*Math.min(1d,Math.max(0d,inter.getX())),
                            screen.heightPixels()*Math.min(1d,Math.max(0d,(1d-inter.getY()))),
                            finger.tipPosition().getZ());
                        points.add(p);
                    }
                }
                drop.set(!points.isEmpty());                
                swipe.set(false);
                for (Gesture gesture : frame.gestures()) {
                    if(gesture.type()==Gesture.Type.TYPE_SWIPE &&
                       gesture.state().equals(Gesture.State.STATE_STOP)){
                        swipe.set(true);
                        break;
                    }
                }
            }
        }

	public ObservableValue<Boolean> isDrop() { return drop; }
        public ObservableValue<Boolean> isSwipe() { return swipe; }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}