package jfx8ibe;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Screen;
import com.leapmotion.leap.Vector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;

/**
 * Chapter 12. JavaFX and Leap Motion
 * @author jpereda
 */
public class SimpleLeapListener extends Listener {

    private final ObjectProperty<Point2D> point=new SimpleObjectProperty<>();
    public ObservableValue<Point2D> pointProperty(){ return point; }
    
    @Override
    public void onFrame(Controller controller) {
        Frame frame = controller.frame();
        if (!frame.hands().isEmpty()) {
            Screen screen = controller.locatedScreens().get(0);
            if (screen != null && screen.isValid()){
                Hand hand = frame.hands().get(0);
                if(hand.isValid()){
                    Vector intersect = screen.intersect(hand.palmPosition(),
                                       hand.direction(), true);
                    point.setValue(new Point2D(
                        screen.widthPixels()*Math.min(1d,Math.max(0d,intersect.getX())),
                        screen.heightPixels()*Math.min(1d,Math.max(0d,(1d-intersect.getY())))
                    ));
                }
            }
        }
    }
}