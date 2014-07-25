package queuemonitor;

import javax.swing.JApplet;

/**
 *
 * @author bshteinfeld
 */
public class QueueMonitor extends JApplet{
    
    @Override
    public void init() {
        QMController controller = new QMController();
    }
    /*
    public static void main(String[] args) {      
        QMController controller = new QMController();
    }
    */
}