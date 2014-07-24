package queuemonitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.border.Border;

/**
 *
 * @author bshteinfeld
 */
public class QueueFrame extends JFrame{

    // Text Areas
    private JTextArea arrivalsTA;
    private JTextArea delaysTA;
    private JTextArea deptsTA;
    private JTextArea statsTA;
    // Font for Arrivals, Deptartures, and Delays
    private final Font nonStatsFont;
    // Font for Statistics (Exchange Rates)
    private final Font statsFont;
    private final Color green;
    private final Color red;
    
    /**
     * Create a new QueueFrame.
     */
    public QueueFrame() {
        nonStatsFont = new Font("Arial", Font.PLAIN, 50);
        statsFont = new Font("Arial", Font.PLAIN, 40);
        green = new Color(70, 165, 70);
        red = new Color(246, 98, 98);
        initComponents();
    }

    /**
     * Initialize components in the frame.
     * Set up text areas with GridLayout.
     */
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        arrivalsTA = new JTextArea();
        delaysTA = new JTextArea();
        deptsTA = new JTextArea();
        statsTA = new JTextArea();
        
        // 2 by 2 grid
        GridLayout layout = new GridLayout(2, 2);
        setLayout(layout);
        
        // Set textareas to correct font
        
        arrivalsTA.setFont(nonStatsFont);
        arrivalsTA.setText("Arrivals:\n");
        delaysTA.setFont(nonStatsFont);
        delaysTA.setText("Delays:\n");
        deptsTA.setFont(nonStatsFont);
        deptsTA.setText("Departures:\n");
        statsTA.setFont(statsFont);
        statsTA.setText("Statistics:\n");
        
        // Create black border around each text area
        Border border = BorderFactory.createLineBorder(Color.BLACK);
        Border blackBorder = BorderFactory.createCompoundBorder(border, 
            BorderFactory.createEmptyBorder(10, 10, 10, 10));
        arrivalsTA.setBorder(blackBorder);
        delaysTA.setBorder(blackBorder);
        deptsTA.setBorder(blackBorder);
        statsTA.setBorder(blackBorder);
        
        // Add textareas into gridlayout
        add(arrivalsTA);
        add(delaysTA);
        add(deptsTA);
        add(statsTA);
        
        setTitle("IT Helpdesk - Queue Monitor");
        // full screen
        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
    }
    
    /*
    * Converts an ArrayList of tickets to a String
    */
    private String ticksToString(ArrayList<Ticket> ticks) {
        if (ticks.isEmpty()) {
            return "";
        }
        String tickStr = "";
        for (Ticket t : ticks) {
            tickStr += t + "\n";
        }
        return tickStr.substring(0, tickStr.length() - 1);
    }
    
    // ----------------------------------------------------------------
    // The following four methods update their corresponding textareas.
    // ----------------------------------------------------------------
    
    public void updateArrivals(ArrayList<Ticket> ticks) {
        arrivalsTA.setText("Arrivals:\n\n" + ticksToString(ticks));
        if (ticks.isEmpty()) {
            arrivalsTA.setBackground(green);
        } else {
            arrivalsTA.setBackground(red);
        }
        arrivalsTA.setFont(nonStatsFont);
        arrivalsTA.setEditable(false);
    }

    public void updateDeptartures(ArrayList<Ticket> ticks) {
        deptsTA.setText("Departures:\n\n" + ticksToString(ticks));
        if (ticks.isEmpty()) {
            deptsTA.setBackground(green);
        } else {
            deptsTA.setBackground(red);
        }
        deptsTA.setFont(nonStatsFont);
        deptsTA.setEditable(false);
    }

    public void updateDelays(ArrayList<Ticket> ticks) {
        delaysTA.setText("Delays:\n\n" + ticksToString(ticks));
        if (ticks.isEmpty()) {
            delaysTA.setBackground(green);
        } else {
            delaysTA.setBackground(red);
        }
        delaysTA.setFont(nonStatsFont);
        delaysTA.setEditable(false);
    }

    public void updateStats(String string) {
        statsTA.setText("Exchange Rates:\n\n" + string);
        statsTA.setFont(statsFont);
        statsTA.setBackground(green);
        statsTA.setEditable(false);
    }

}
