package cloudsync.client;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerClass {
	public static Logger logger;
	public static FileHandler fh;
	
	public static boolean initializeLogger() 
	{
		try
		{
		    logger = Logger.getLogger("MyLog");  
			fh = new FileHandler("session.log");  
	        logger.addHandler(fh);
	        logger.setUseParentHandlers(false);
	        SimpleFormatter formatter = new SimpleFormatter();  
	        fh.setFormatter(formatter); 
	        return true;
		}catch(Exception e)
		{
			System.out.println("Logger Initialized Failed");
		}
		return false;
	
	}
	
	public static void writeLog(String message)
	{
		logger.info(message);
	}
}
