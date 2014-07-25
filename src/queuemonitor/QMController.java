package queuemonitor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the controller for the Queue Monitor. It serves as the
 * bridge between the model (DBConnection) and the view (QueueFrame).
 *
 * @author bshteinfeld
 */
public class QMController {

    // Timer serves to repeatedly re-query the KACE server (with delay)
    private Timer timer;
    private DBConnection connection = null;
    // ArrayList of the tickets
    private ArrayList<Ticket> arrivalTickets;
    private ArrayList<Ticket> deptTickets;
    private ArrayList<Ticket> delayedTickets;
    // View of the QMFrame
    private QueueFrame frame;

    /**
     * Creates a QMController. Initialize the connection to the KACE server, the
     * view, and the Timer.
     */
    public QMController() {
        // Establish connection to database
        connection = new DBConnection();
        connection.setUpDBConnection();

        // Initialize the tickets ArrayList
        arrivalTickets = new ArrayList<Ticket>();
        deptTickets = new ArrayList<Ticket>();
        delayedTickets = new ArrayList<Ticket>();

        // Initialize the view
        frame = new QueueFrame();
        frame.setVisible(true);

        // Set up the Timer to re-query the database every 5000 milliseconds
        ScheduledExecutorService executor =
                Executors.newScheduledThreadPool(1);
        executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                requeryDB();
            }
        },
         1000, 5000, TimeUnit.MILLISECONDS);
    }

    /**
     * This method is called by the timer. It re-queries the KACE database.
     */
    public void requeryDB() {
        try {
            // ------------------------------------------
            // Initialize all resultsets from the queries. 
            // ------------------------------------------
            
            ResultSet arrivalTicks = connection.executeQuery(
                    // only extract id, title, and time created
                    "select HD_TICKET.TITLE, HD_TICKET.ID, HD_TICKET.CREATED\n"
                    + "from HD_TICKET	"
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID "
                    // restrict to IT Helpdesk
                    + "where HD_TICKET.HD_QUEUE_ID = 1 "
                    // restrict to unassigned (open) tickets
                    + "and HD_STATUS.STATE = 'opened'	"
                    // restrict to 'New Starter' tickets
                    + "and HD_TICKET.TITLE like '[NEW STARTER]%' "
                    + "and HD_TICKET.HD_STATUS_ID = 4 "
                    // sort in order oldest -> newest
                    + "order by HD_TICKET.ID ASC");

            ResultSet deptTicks = connection.executeQuery(
                    "select HD_TICKET.ID, HD_TICKET.TITLE, HD_TICKET.CREATED\n"
                    + "from HD_TICKET "
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID "
                    + "where HD_TICKET.HD_QUEUE_ID = 1    "
                    + "and HD_STATUS.STATE = 'opened' "
                    // restrict to termination tickets
                    + "and (HD_TICKET.TITLE like '[TERMINATION]%' \n"
                    + "or HD_TICKET.TITLE like 'New Term Notice%')\n"
                    + "and HD_TICKET.HD_STATUS_ID = 4 "
                    + "order by HD_TICKET.CREATED asc");

            // Reteive current time
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.DATE, -1);
            // Calculate current time minus one day, which represents
            // the cutoff for classifying a ticket as 'delayed'
            Date delayCutoffDate = cal.getTime();

            // Format cutOffDate in order to be able to compare it to dates
            // in the resultSet
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String delayCutoffDateString = dateFormat.format(delayCutoffDate);

            ResultSet delayedTicks = connection.executeQuery(
                    "select HD_TICKET.ID, HD_TICKET.CREATED, HD_TICKET.TITLE \n"
                    + "from HD_TICKET\n"
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID\n"
                    + "where HD_TICKET.HD_QUEUE_ID = 1 	\n"
                    + "and HD_CATEGORY_ID  not in (43,42,53)\n"
                    + "and HD_STATUS.STATE = 'opened'\n"
                    + "and HD_TICKET.OWNER_ID = 0 \n"
                    // exclude starters and terminations
                    + "and HD_TICKET.TITLE not like '[NEW STARTER]%' \n"
                    + "and HD_TICKET.TITLE not like '[TERMINATION]%' \n"
                    + "and HD_TICKET.TITLE not like 'New Term Notice%' \n"
                    // include only tickets older than one day
                    + "and (HD_TICKET.CREATED < " + delayCutoffDateString + ") \n"
                    + "order by HD_TICKET.CREATED asc");

            ResultSet averageClosingTime7Days = connection.executeQuery(
                    // Caclulate average closing time
                    "select TIME_FORMAT(SEC_TO_TIME(AVG(UNIX_TIMESTAMP(HD_TICKET.TIME_CLOSED)"
                    + " - UNIX_TIMESTAMP(HD_TICKET.CREATED))),'%Hh %im %ss') AS AVERAGE_TIME\n"
                    + "from ORG1.HD_TICKET\n"
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID \n"
                    + "where HD_TICKET.HD_QUEUE_ID = 1  \n"
                    // Exlcude tickets made and closed at the same time
                    + "and HD_TICKET.TIME_CLOSED != '0000-00-00 00:00:00'\n"
                    + "and HD_TICKET.TIME_OPENED != '0000-00-00 00:00:00'\n"
                    // Only include past 7 days worth of tickets
                    + "and  HD_TICKET.CREATED > DATE_SUB(NOW(), INTERVAL 7 DAY)");

            ResultSet averageClosingTime30Days = connection.executeQuery(
                    "select TIME_FORMAT(SEC_TO_TIME(AVG(UNIX_TIMESTAMP(HD_TICKET.TIME_CLOSED)"
                    + " - UNIX_TIMESTAMP(HD_TICKET.CREATED))),'%Hh %im %ss') AS AVERAGE_TIME\n"
                    + "from ORG1.HD_TICKET\n"
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID \n"
                    + "where HD_TICKET.HD_QUEUE_ID = 1  \n"
                    + "and HD_TICKET.TIME_CLOSED != '0000-00-00 00:00:00'\n"
                    + "and HD_TICKET.TIME_OPENED != '0000-00-00 00:00:00'\n"
                    // Only include past 30 days worth of tickets
                    + "and  HD_TICKET.CREATED > DATE_SUB(NOW(), INTERVAL 30 DAY)");

            ResultSet numUnassignedTicks = connection.executeQuery(
                    // Count number of tickets
                    "select count(HD_TICKET.ID) as count\n"
                    + "from HD_TICKET\n"
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID\n"
                    + "where HD_TICKET.HD_QUEUE_ID = 1\n"
                    // restrict to unassigned
                    + "and HD_TICKET.OWNER_ID = 0");

            ResultSet numOpenTicks = connection.executeQuery(
                    "select count(HD_TICKET.TITLE) as count\n"
                    + "from HD_TICKET\n"
                    + "left join HD_STATUS on HD_TICKET.HD_STATUS_ID = HD_STATUS.ID\n"
                    + "where HD_TICKET.HD_QUEUE_ID = 1\n"
                    // restrict to unclosed but assigned tickets
                    + "and (HD_STATUS.STATE = 'opened' or HD_STATUS.STATE = 'stalled')");

            // Print result set
            /*
             ResultSetMetaData rsmd = delayedTicks.getMetaData();
             int columnsNumber = rsmd.getColumnCount();
             while (delayedTicks.next()) {
             for (int i = 1; i <= columnsNumber; i++) {
             if (i > 1) {
             System.out.print(",  ");
             }
             String columnValue = delayedTicks.getString(i);
             System.out.print(columnValue + " " + rsmd.getColumnName(i));
             }
             System.out.println("");
             }*/
            
            
            // -----------------------------------------------------------------
            // Convert all resultsets into their respective ArrayList of tickets
            // -----------------------------------------------------------------
            
            while (arrivalTicks.next()) {
                Ticket at = new Ticket(arrivalTicks);
                arrivalTickets.add(at);
            }

            while (deptTicks.next()) {
                Ticket dt = new Ticket(deptTicks);
                if (isSameDay(dt.extractDateFromTitle(), currentDate)) {
                    deptTickets.add(dt);
                }
            }

            while (delayedTicks.next()) {
                Ticket dt = new Ticket(delayedTicks);
                delayedTickets.add(dt);
            }

            // Construct statistics string consisting of results of queries
            String stats;

            averageClosingTime7Days.next();

            stats = "Average closing time (last 7 days): "
                    + averageClosingTime7Days.getString("AVERAGE_TIME");

            averageClosingTime30Days.next();

            stats += "\nAverage closing time (last 30 days): "
                    + averageClosingTime30Days.getString("AVERAGE_TIME");

            numUnassignedTicks.next();

            stats += "\nNumber of unassigned tickets in queue: " 
                    + numUnassignedTicks.getString("count");

            numOpenTicks.next();

            stats += "\nNumber of open tickets in queue: " 
                    + numOpenTicks.getString("count");

            // Update the textArea with the new tickets
            frame.updateArrivals(arrivalTickets);
            frame.updateDeptartures(deptTickets);
            frame.updateDelays(delayedTickets);
            frame.updateStats(stats);
            
            // Re-initialize arraylists of tickets
            arrivalTickets = new ArrayList<Ticket>();
            deptTickets = new ArrayList<Ticket>();
            delayedTickets = new ArrayList<Ticket>();
            
            // Close all resultSets
            arrivalTicks.close();
            deptTicks.close();
            delayedTicks.close();
            averageClosingTime30Days.close();
            averageClosingTime7Days.close();
            numUnassignedTicks.close();
            numOpenTicks.close();
        } catch (SQLException ex) {
            System.err.println("Error executing query");
        }
    }

    /*
     * Returns true if two Date objects represent the same day. False otherwise.
     */
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Close the connection to the database.
     */
    public void closeConnection() {
        connection.closeDBConnection();
    }
}
