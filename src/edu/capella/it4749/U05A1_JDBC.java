package edu.capella.it4749;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author Deb
 */
public class U05A1_JDBC extends Application {
    //creates a logger
    private static final Logger log = Logger.getLogger(U05A1_JDBC.class.getName());
    
    GridPane grid = new GridPane();
    Label selectPromptLabel = new Label("Please select a course for which you want to register");
    ComboBox<Course> coursesComboBox = new ComboBox<>();   
    Label confirmPromptLabel = new Label("");
    Label registeredCoursesPromptLabel = new Label("You are currently registered for");
    Label creditHourPromptLabel = new Label("Current total Credit Hours");            
    Label registeredCoursesLabel = new Label("");
    Label creditHoursLabel = new Label("0");
    Label dataFilePathLabel = new Label("");    
    
    Course choice;
    final int MAX_CREDIT_LOAD = 9;
    int totalCredit = 0;     
    
    @Override
    public void start(Stage primaryStage) {
        //connection to the database and sql query
        String sqlQuery = "select course_code, credit_hours from course_offerings " + 
                        "order by course_code";
        String username = "registrar";
        String password = "P@ssword";
        String dbURL = "jdbc:mariadb://localhost:3306/registration";
        
        //calls to getCourses and populates the arrayList with the correct course objects needed
        ArrayList<Course> courses = getCourses(username, password, dbURL, sqlQuery);
        log.log(Level.INFO, courses.size() + " courses found.");
        
        //for every course c in courses
        for(Course c : courses) {
            //drag c out and put it in the comboBox
            coursesComboBox.getItems().add(c);
        }
        
        log.log(Level.INFO, "Setting up the User Interface...");//Tells the logger that it is setting up the user interface
        RowConstraints row0 = new RowConstraints();
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        RowConstraints row3 = new RowConstraints();
        RowConstraints row4 = new RowConstraints();
        RowConstraints row5 = new RowConstraints();
        
        // Configure row heights
        row0.setPercentHeight(10);
        row1.setPercentHeight(5);
        row2.setPercentHeight(10);
        row3.setPercentHeight(30);
        row4.setPercentHeight(10);
        row5.setPercentHeight(35);
         
        grid.getRowConstraints().addAll(row0, row1,row2, row3, row4, row5);

        grid.setAlignment(Pos.CENTER);

        grid.setHgap(5);
        grid.setVgap(5);
         
        // add lable to display path to course data file
        grid.add(dataFilePathLabel, 0, 1);
        GridPane.setHalignment(dataFilePathLabel, HPos.LEFT);
        grid.add(selectPromptLabel, 0, 2);
        GridPane.setHalignment(selectPromptLabel, HPos.LEFT);
        // hide select course prompt until combobox is populated & displayed
        selectPromptLabel.setVisible(false);
        
        // configure & add combobox for available courses
        coursesComboBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(coursesComboBox, 0, 3);
        GridPane.setValignment(coursesComboBox, VPos.TOP);
        // hide combobox until user has selected data file to populate it
        coursesComboBox.setVisible(false);

        grid.add(confirmPromptLabel, 1, 3);  
        GridPane.setHalignment(confirmPromptLabel, HPos.LEFT);
        GridPane.setValignment(confirmPromptLabel, VPos.TOP);
        
        grid.add(registeredCoursesPromptLabel, 0, 4);  
        GridPane.setHalignment(registeredCoursesPromptLabel, HPos.LEFT);
        GridPane.setValignment(registeredCoursesPromptLabel, VPos.TOP);
        
        grid.add(creditHourPromptLabel, 1, 4);  
        GridPane.setHalignment(creditHourPromptLabel, HPos.LEFT);   
        GridPane.setValignment(creditHourPromptLabel, VPos.TOP);
         
        grid.add(registeredCoursesLabel, 0, 5);
        GridPane.setHalignment(registeredCoursesLabel, HPos.LEFT);   
        GridPane.setValignment(registeredCoursesLabel, VPos.TOP);
        registeredCoursesLabel.setStyle("-fx-background-color: #fff600;");
  
        grid.add(creditHoursLabel, 1, 5); 
        GridPane.setHalignment(creditHoursLabel, HPos.LEFT);   
        GridPane.setValignment(creditHoursLabel, VPos.TOP);
        creditHoursLabel.setStyle("-fx-background-color: #fff600;");
        
        //sets both the coursesComboBox and the selectPromptLabel to visible
        //with a true setting the setVisible to true
        coursesComboBox.setVisible(true);
        selectPromptLabel.setVisible(true);
         
        Scene scene = new Scene(grid, 500, 500, Color.RED);
        
        primaryStage.setTitle("JavaFX Register for Courses");
        primaryStage.setScene(scene);
        primaryStage.show();
        log.log(Level.INFO, "Displaying the stage...");
        // ****************************************************
        // Course combobox event handler
        // ****************************************************
        coursesComboBox.setOnAction(e -> {
             if(totalCredit < MAX_CREDIT_LOAD) {     
                choice = coursesComboBox.getValue();
                if(!choice.getIsRegisteredFor()) {
                    choice.setIsRegisteredFor(true);
                    String registeredCourses = registeredCoursesLabel.getText();
                    registeredCourses += choice + "\n";
                    registeredCoursesLabel.setText(registeredCourses);
                    totalCredit += choice.getCredits();
                    creditHoursLabel.setText(Integer.toString(totalCredit));
                    confirmPromptLabel.setText("Registration confirmed for\n course " + choice.getCode());
                }
                else {
                    confirmPromptLabel.setText("**Invalid**\nYou have already \nregistered for " + choice.getCode());
                }
             }
             else {
                    confirmPromptLabel.setText("**Invalid**\nYou cannot register for \nmore than 9 credits.");
             }
        });
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //created the fileHandler and simpleformatter to create logs into the proper log file
        try {
            FileHandler fileHandler = new FileHandler("jdbc.log");
            SimpleFormatter simple = new SimpleFormatter();
            //set the simpleformatter to the filehandler
            fileHandler.setFormatter(simple);
            log.setLevel(Level.ALL);
            log.addHandler(fileHandler);
            log.getLogger("").getHandlers()[0].setLevel(Level.WARNING);
            //caught any exceptions that needed to be caught 
        } catch(IOException ex) {
            System.err.println("Error opening the Log File...");
            System.exit(1);
        }
        Logger.getLogger("").getHandlers()[0].setLevel(Level.WARNING);
        log.log(Level.INFO, "Starting the JavaFXRegistration Application...");
        launch(args);
    }
    
    // Asynchronous method. Makes call to synchronous method on new thread
    private static Future<ArrayList<Course>> getCoursesAsync(String user, 
                   String password, String dbURL, String sql) {
        
        CompletableFuture<ArrayList<Course>> result = new CompletableFuture<>();
        new Thread( ( ) -> {
            // Call to synchronous method on new thread
            result.complete(getCourses(user, password, dbURL, sql));
        }).start(); //needs a start at the end of any completeable future
        return result; 
    }
    
    //new method called getCourses that has the parameters username, password, dbURL, and sql; all strings
    public static ArrayList<Course> getCourses(String username, String password,
            String dbURL, String sql) {
        //create ArrayList called courses
        ArrayList<Course> courses = new ArrayList<>();
        
        //variables used later in the program
        Statement stmt = null;
        ResultSet result = null;
        
        //try with resources, in order to get the connection we need a DriverManager
        //so that we
        try(Connection connection = DriverManager.getConnection(dbURL, username, password)) {
            log.log(Level.INFO, "Connection Status: " + connection.isValid(0));
            //creates a connection statement then puts it in stmt 
            stmt = connection.createStatement();
            log.log(Level.CONFIG, "SQL query: " + connection.nativeSQL(sql));
 
            //if the stmt is notnull the following code executes, else statement is not required
            //because if it is null the program will not run
            if(Objects.nonNull(stmt)) {
                //the query sql will be run and the following results will be placed in result
                result = stmt.executeQuery(sql);
                log.log(Level.INFO, "SQL result ready: " + result.isBeforeFirst());
                //while rusult has a next varchar or character
                while(result.next()) {
                    //pulls out the course code
                    String code = result.getString("course_code");
                    //pulls out the credit hours 
                    int credits = result.getInt("credit_hours");
                    //adds them line by line to the courses variable
                    courses.add(new Course(code, credits));
                    
                }
            }
        }
        catch(SQLInvalidAuthorizationSpecException ex) {
            log.log(Level.WARNING, "Cannot login to database..." + ex.getMessage());
        }
        catch(SQLNonTransientConnectionException ex) {
            log.log(Level.SEVERE, "Cannot connect to server..." + ex.getMessage());
        }
        catch(SQLSyntaxErrorException ex) {
            log.log(Level.SEVERE, "SQL Error..." + ex.getMessage());
        }
        catch(SQLException ex) {
            log.log(Level.SEVERE, "Error in DB connection..." + ex.getMessage());
        }
        //return courses
        return courses;
    }
    
}
