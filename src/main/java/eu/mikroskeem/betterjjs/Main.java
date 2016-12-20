package eu.mikroskeem.betterjjs;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.util.Properties;

public class Main {
    private static Properties properties;
    public Main(){
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("nashorn");
        ScriptEngine groovyShell = manager.getEngineByName("groovy");
        InputThread inputThread = new InputThread(scriptEngine, groovyShell);
        inputThread.start();
    }
    public static void main(String... args){
        OptionParser parser = new OptionParser();
        parser.accepts("v", "Print version");
        parser.accepts("h", "Show help");
        OptionSet opts = parser.parse(args);
        if(opts.has("v")){
            for (String s : new String[]{
                    String.format("btjjs version: %s", getVersion()),
                    String.format("Java version: %s", System.getProperty("java.version"))
            }) {
                System.out.println(s);
            }
        } else if(opts.has("h")){
            printHelp();
        } else {
            new Main();
        }
    }

    public static String getVersion(){
        properties = new Properties();
        try {
            properties.load(Main.class.getResourceAsStream("/version.properties"));
            return properties.getProperty("version");
        } catch (IOException e){
            System.err.println("Failed to get version information");
        }
        return null;
    }

    public static void printHelp(){
        String[] help = new String[]{
                String.format("BetterJJS ver. %s", getVersion()),
                "`:help` - see defined commands",
                "`:clear` - clear screen",
                "`:quit` - quit",
                "`:printenv` - print env",
                "`:printprops` - print system properties",
                "`:gr` - toggle groovy mode"
        };
        for(String s : help) System.out.println(s);
    }
}
