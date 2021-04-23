package dev.theskidster.jlogger;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author J Hoffman
 * Created: Apr 19, 2021
 */

/**
 * JLogger is a lightweight application logger for Java projects. Comprised of a single static class, it can be used to keep a chronological record of significant 
 * events occurring within an application at runtime.
 * <br><br>
 * Output produced by JLogger can be viewed through the applications console in real-time, or after the application has ceased execution following a call to 
 * {@link logSevere(String, Exception) logSevere()}, in which case a .txt file will be generated containing JLoggers text output.
 * <br><br>
 * JLogger provides the following methods for generating text output:
 * <ul>
 * <li>{@link logInfo(String) logInfo()} - Produces a low-priority message, useful for tracking state changes.</li>
 * <li>{@link logWarning(String, Exception) logWarning()} - Indicates that the application may have encountered a non-fatal error.</li>
 * <li>{@link logSevere(String, Exception) logSevere()} - A fatal error has occurred which will require the application to cease execution immediately.</li>
 * <li>{@link setModule(String) setModule()} - Appends the name of whichever application is using the logger to the log messages it creates.</li>
 * <li>{@link newHorizontalLine() newHorizontalLine()} - Adds a horizontal line to the text output.</li>
 * <li>{@link newLine() newLine()} - Inserts a new line into the text output.</li>
 * </ul>
 */
public final class JLogger {
    
    private static final int MAX_LENGTH = 0x75300; //480,000
    
    private static String module = "";
    
    private static PrintWriter logText;
    private static final StringBuilder output = new StringBuilder();
    
    /**
     * Appends the name of the implementing application to each log message after the priority indicator (INFO, WARNING, etc). This is particularly useful in larger 
     * projects where frequent calls to the logger are common across several decoupled modules.
     * <br><br>
     * For implementations that choose to make use of this feature, remember to set the module value to {@code null} after the desired log methods have been used 
     * otherwise the previously set module name will persist in places it shouldn't. 
     * 
     * @param value the name of the module from which calls to this logger will be made. Or {@code null} to reset it.
     */
    public static void setModule(String value) {
        module = (value != null) ? " (" + value + ")" : "";
    }
    
    /**
     * Inserts a horizontal line into the logger output with no other information. Included here to encourage structure.
     */
    public static void newHorizontalLine() {
        String line = "--------------------------------------------------------------------------------";
        System.out.println(line);
        output.append(line)
              .append(System.lineSeparator());
    }
    
    /**
     * Inserts an empty space into the logger output with no other information. Included here to encourage structure.
     */
    public static void newLine() {
        System.out.println();
        output.append(System.lineSeparator());
    }
    
    /**
     * Writes an informative low-priority message to the applications console. Typically used to indicate significant state changes occurring within the 
     * application.
     * 
     * @param message the text to appear as output in the console/log file 
     */
    public static void logInfo(String message) {
        System.out.println("INFO" + module + ": " + message);
        
        output.append("INFO")
              .append(module)
              .append(": ")
              .append(message)
              .append(System.lineSeparator());
    }
    
    /**
     * Writes a medium-priority message to the applications console. Warning messages should be used to indicate that the application may have entered an 
     * invalid state which hasn't (yet) resulted in a crash- but may produce undefined behavior.
     * 
     * @param message the text to appear as output in the console/log file 
     * @param e       an optional exception used to output a stack trace. If {@code null} is passed, no stack trace information will be displayed.
     */
    public static void logWarning(String message, Exception e) {
        String timestamp = new SimpleDateFormat("MM-dd-yyyy h:mma").format(new Date());
        
        System.out.println(System.lineSeparator() + timestamp);
        System.out.println("WARNING" + module + ": " + message + System.lineSeparator());
        
        output.append(System.lineSeparator())
              .append(timestamp)
              .append(System.lineSeparator())
              .append("WARNING")
              .append(module)
              .append(": ")
              .append(message)
              .append(System.lineSeparator())
              .append(System.lineSeparator());
        
        //Output the stack trace if an exception if it's provided.
        if(e != null) {
            var stackTrace = e.getStackTrace();
            
            System.out.println(e.toString());
            output.append(e.toString())
                  .append(System.lineSeparator());
            
            for(StackTraceElement element : stackTrace) {
                System.out.println("\t" + element.toString());
                
                output.append("\t")
                      .append(element.toString())
                      .append(System.lineSeparator());
            }
            
            System.out.println();
            output.append(System.lineSeparator());
        }
    }
    
    /**
     * Writes a high-priority message to the applications console. Use of this method is reserved only for instances wherein the application 
     * has encountered some fatal error that will require it to cease execution.
     * <br><br>
     * A .txt file containing the recorded output will be generated in the directory from which the application was launched.
     * 
     * @param message the text to appear as output in the console/log file 
     * @param e       an optional exception used to output a stack trace. If {@code null} is passed, JLogger will generate a nondescript
     *                {@link RuntimeException}.
     */
    public static void logSevere(String message, Exception e) {
        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        String time = new SimpleDateFormat("h:mma").format(new Date());
        
        String timestamp = date + " " + time;
                
        System.err.println(System.lineSeparator() + timestamp);
        System.err.println("ERROR" + module + ": " + message + System.lineSeparator());

        output.append(System.lineSeparator())
              .append(timestamp)
              .append(System.lineSeparator())
              .append("ERROR")
              .append(module)
              .append(": ")
              .append(message)
              .append(System.lineSeparator())
              .append(System.lineSeparator());

        //If no exception is provided we'll throw our own.
        if(e == null) e = new RuntimeException();
        var stackTrace = e.getStackTrace();
        
        System.err.println(e.toString());
        output.append(e.toString())
              .append(System.lineSeparator());

        for(StackTraceElement element : stackTrace) {
            System.err.println("\t" + element.toString());

            output.append("\t")
                  .append(element.toString())
                  .append(System.lineSeparator());
        }

        System.err.println();
        output.append(System.lineSeparator());
        
        File file     = new File("log " + date + ".txt");
        int duplicate = 0;

        while(file.exists()) {
            duplicate++;
            file = new File("log " + date + " (" + duplicate + ").txt");
        }

        try(FileWriter logFile = new FileWriter(file.getName())) {
            //Check size make sure we dont produce a crazy large file.
            String text = (output.toString().length() >= MAX_LENGTH) 
                        ? output.substring(0, MAX_LENGTH) 
                        : output.toString();
            
            logText = new PrintWriter(logFile);
            logText.append(text);
            logText.close();
        } catch(Exception ex) {}
        
        System.exit(-1);
    }
    
}