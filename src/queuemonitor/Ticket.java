package queuemonitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents one Ticket.
 *
 * @author bshteinfeld
 */
public class Ticket {

    // ID number of ticket
    private int id;
    // Description of ticket
    private String title;
    // Date object representing date created of the ticket
    private Date timeCreated;
    // Used to construct a java Date object from the result set
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Create a new ticket from a result set
     *
     * @param rs -- ResultSet returned from search query whose pointer is at the
     * current ticker
     */
    public Ticket(ResultSet rs) {
        try {
            // Extract info from current ticket in resultset
            id = rs.getInt("ID");
            title = rs.getString("TITLE");
            String time = rs.getString("CREATED");
            Scanner s = new Scanner(time);
            timeCreated = dateFormat.parse(s.next());
        } catch (SQLException ex) {
            System.err.println("Unable to read entry");
        } catch (ParseException ex) {
            System.err.println("Unable to parse date from entry");
        }
    }

    /**
     * Return the date within a title string.
     * Different types of tickets have different date formats.
     */
    public Date extractDateFromTitle() {
        Scanner scan = new Scanner(title);
        String s = scan.next();
        
        if(s.equals("[TERMINATION]:") || s.equals("New"))
            return getDate(title);
        else
            return null;
    }
    
    /*
    * Given a string, extract the date from it using Regex.
    */
    private static Date getDate(String desc) {
        Scanner scan = new Scanner(desc);
        String s = scan.next();
        Matcher m = null;
        DateFormat df = null;
        
        if(s.equals("[TERMINATION]:")) {
            m = Pattern.compile("(0[1-9]|1[012])[- /.](0[1-9]|[12][0-9]|3[01])[- /.](19|20)\\d\\d").matcher(desc);
            df = new SimpleDateFormat("MM/dd/yyyy");
        } else if (s.equals("New")) {
            m = Pattern.compile("(19|20)\\d\\d[- /. /s](0[1-9]|1[012])[- /. /s](0[1-9]|[12][0-9]|3[01])").matcher(desc);
            df = new SimpleDateFormat("yyyy MM dd");
        }

        String match = "";
         
        while (m.find()) {
            match = m.group();
        }
        
        try {
            return df.parse(match);
        } catch (ParseException ex) {
            System.err.println("Error parsing date");
        }

        return null;
    }
    
    /**
     * Return ticket ID. 
     */
    public int getID() {
        return id;
    }

    /**
     * Return string representation of a ticket.
     */
    @Override
    public String toString() {
        return "T#: " + id + " (" + timeCreated.toString().substring(0, 10) + ")";
    }
}
