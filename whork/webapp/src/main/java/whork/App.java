package whork;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class App {
	private static final String BASEOPT = "base";
	private static final String WEBRESOPT = "webRes";
	private static final String WEBROOTOPT = "webRoot";
	private static final String HELPOPT = "help";
	private static final String PORTOPT = "port";
	
	private static ArrayList<String> webResDirectory = new ArrayList<>();
	private static ArrayList<String> webResFiles = new ArrayList<>();
	
	private static final Logger LOGGER = Logger.getLogger("WhorkStartup");
	private static String webRoot;
	private static String base = "";
	private static int port = 8080;
	private static boolean selfExtract = isJarPackaged();
	
	private static void setResources() {
		webResDirectory.add("WEB-INF");
		
		webResFiles.add("/WEB-INF/web.xml");
		webResFiles.add("/index.jsp");
	}
	
	private static void selfExtraction() throws IOException {
		if(selfExtract) {
			LOGGER.info("starting self extraction...");
			setResources();
			
			File f = new File(webRoot);
			f.mkdir();
			
			for(final String dir : webResDirectory) {
				StringBuilder builder = new StringBuilder();
				builder.append(webRoot);
				builder.append("/");
				builder.append(dir);
				
				File newDir = new File(builder.toString());
				newDir.mkdir();
			}
			
			for(String res : webResFiles) {
				StringBuilder builder = new StringBuilder();
				
				builder.append(webRoot);
				builder.append(res);
				
				String path = builder.toString();
				File resFile = new File(path);
				if(resFile.createNewFile())
					LOGGER.info(path + " already exists");
				
				BufferedInputStream istOrigin = new BufferedInputStream(
						App.class.getResourceAsStream(res));
				
				BufferedOutputStream ostDest = new BufferedOutputStream(
						new FileOutputStream(resFile));
				
				byte[] buffer = new byte[1024];
		        int lengthRead;
		        
		        try {
		        	while ((lengthRead = istOrigin.read(buffer)) > 0) {
		        		ostDest.write(buffer, 0, lengthRead);
		        	}
		        } finally {
		        	ostDest.close();
		        	istOrigin.close();
		        }
			}
		} else {
			LOGGER.info("skipping self extraction...");
		}
	}
	
	private static boolean isJarPackaged() {
		return App.class.getResource("App.class")
				.toString().charAt(0) == 'j';
	}

	private static boolean startTomcat() {
		Tomcat tomcat = new Tomcat();
		
		tomcat.setPort(port);
		tomcat.addWebapp(base, webRoot);
		
		try {
			tomcat.start();
		} catch(LifecycleException e) {
			e.printStackTrace();
			return false;
		}
		
		LOGGER.info("startup successful: up and running!");
		
		tomcat.getServer().await();
		
		return true;
	}
	
	private static Options createOptions() {
		Options opt = new Options();
		
		opt.addOption(WEBRESOPT, 
				true, 
				"Provide web resources on your own (default: none)");
		
		opt.addOption(PORTOPT, 
				true, 
				new StringBuilder()
					.append("Provide different port from the standard one (default: ")
					.append(port)
					.append(")")
					.toString());
		
		opt.addOption(BASEOPT, 
				true, 
				new StringBuilder()
					.append("Provide base path for website (default: ")
					.append(base.isEmpty() ? "/" : base)
					.append(")")
					.toString());
		
		opt.addOption(WEBROOTOPT,
				true,
				new StringBuilder()
					.append("Provide different root directory for web resource extraction and usage (default: ")
					.append(webRoot == null ? "webRes must be provided" : webRoot)
					.append(")")
					.toString());
		
		opt.addOption(HELPOPT, 
				false, 
				"Print this help and immediately exit");
		
		return opt;
	}
	
	private static void printProperties() {
		String pBase = base.isEmpty() ? "/" : base;
		LOGGER.info("Settings for Whork server:\n--> port: " + Integer.toString(port) + 
				"\n--> base: " + pBase + 
				"\n--> webroot: " + webRoot + 
				"\n--> self-extract? " + selfExtract);
	}
	
	private static boolean propertySetup(String[] args) throws ParseException {
		Options options = createOptions();
    	CommandLineParser parser = new DefaultParser();
    	CommandLine cmd = parser.parse(options, args);
    	
    	if(cmd.hasOption("help")) {
    		HelpFormatter formatter = new HelpFormatter();
    		formatter.printHelp("whork-webapp", options);
    		return false;
    	}
    	
    	for(Option opt : cmd.getOptions()) {
    		String argName = opt.getOpt();
    		if(argName != null) {
    			if(argName.equals(BASEOPT)) {
    				base = opt.getValue();
    				if(base.equals("/"))
    					base = "";
    				else if(base.charAt(0) != '/') {
    					LOGGER.severe("base must start with /");
    					return false;
    				}
    			} else if(argName.equals(PORTOPT)) {
    				port = Integer.parseInt(opt.getValue());
    				if(port < 0 || port > 65535) {
    					LOGGER.severe("port number must be within range [0-65535]");
    					return false;
    				}
    			} else if(argName.equals(WEBRESOPT)) {
    				if(selfExtract) {
    					LOGGER.warning("ignoring webRes value because self extraction is enabled...");
    				} else {
    					webRoot = new File(opt.getValue()).getAbsolutePath();
    				}
    			} else if(argName.equals(WEBROOTOPT)) {
    				if(!selfExtract) {
    					LOGGER.warning("ignoring webRoot property because self extraction is disabled...");
    				} else {
    					webRoot = new File(opt.getValue()).getAbsolutePath();
    				}
    			}
    		}
    	}
    	
    	if(webRoot == null && !selfExtract) {
    		LOGGER.severe("you must pass webRes in order to specify where to locate web resources");
    		return false;
    	}
    	
    	printProperties();
    	
    	return true;
	}
	
	private static void utilDeleteDirectoryRecursion(Path path) throws IOException {
		if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
			try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
				for (Path entry : entries) {
					utilDeleteDirectoryRecursion(entry);
				}
			}
		}
		Files.delete(path);
	}
	
	private static void cleanup() {
		if(selfExtract) {
			LOGGER.info("deleting webroot...");
			try {
				utilDeleteDirectoryRecursion(Paths.get(webRoot));
			} catch(IOException e) {
				e.getMessage();
				LOGGER.severe(
						new StringBuilder()
							.append("unable to delete webroot @ ")
							.append(webRoot)
							.toString());
			}
		}
    	LOGGER.info("exiting...");
    	LOGGER.info("bye bye");
	}
	
	private static void exceptionMessageBeforeStart(Exception e, String msg) {
		LOGGER.severe(e.getMessage());
		LOGGER.severe(msg);
		LOGGER.severe("whork will not start");
	}
	
    public static void main( String[] args ) {
    	try {
    		if(selfExtract) {
        		webRoot = new File("whork_webroot").getAbsolutePath();
    		} else {
        		webRoot = null;
    		}
    		
    		if(!propertySetup(args)) {
    			return;
    		}
    		
    		selfExtraction();
    	} catch(ParseException e) {
    		exceptionMessageBeforeStart(e, "unable to parse command line");
    		return;
    	} catch(IOException e) {
    		exceptionMessageBeforeStart(e, "unable to correclty self-extract");
    		return;
    	}
    	
    	LOGGER.info("Welcome to Whork server!");
    	LOGGER.info("starting up...");
    	
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		@Override
    		public void run() {
    			App.cleanup();
    		}
    	});
    	
		if(!startTomcat()) {
			LOGGER.severe("unable to start tomcat, details above");
		}
		
    	cleanup();
    }
}
