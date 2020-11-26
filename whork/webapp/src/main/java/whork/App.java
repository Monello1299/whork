package whork;

import java.util.logging.Logger;
import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/*
 * TODO 
 *  * Argument parser
 *  * File deployer
 */

public class App {
	private final static Logger LOGGER = Logger.getLogger("WhorkServer");

	private static boolean startTomcat(short port, String base, String loc) {
		Tomcat tomcat = new Tomcat();
		
		tomcat.setPort(port);
		tomcat.addWebapp(base, loc);
		
		try {
			tomcat.start();
		} catch(LifecycleException e) {
			e.printStackTrace();
			return false;
		}
		
		tomcat.getServer().await();
		
		return true;
	}
	
	private static void cleanup() {
    	LOGGER.info("exiting...");
    	LOGGER.info("bye bye");
	}
	
    public static void main( String[] args ) throws Exception {
    	LOGGER.info("Welcome to Whork server!");
    	LOGGER.info("starting up...");
    	
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		@Override
    		public void run() {
    			App.cleanup();
    		}
    	});
    	
		if(!startTomcat((short)8080, "", new File("src/main/webapp").getAbsolutePath())) {
			LOGGER.severe("unable to start tomcat, details above");
		}
		
    	cleanup();
    }
}
