package eu.mikroskeem.betterjjs;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class InputThread extends Thread {
    private ScriptEngine engine;
    public InputThread(ScriptEngine engine){
        super("InputThread");
        this.engine = engine;
    }

    @Override public void run(){
        boolean exit = false;
        ConsoleReader reader = null;
        FileHistory hist = null;
        try {
            hist = new FileHistory(new File(
                    System.getProperty("user.home"),
                    ".btjjs_history").getAbsoluteFile());
            reader = new ConsoleReader();
            reader.setHistory(hist);
            reader.setHistoryEnabled(true);

            reader.setPrompt("btjjs> ");
            PrintWriter out = new PrintWriter(reader.getOutput());

            String line;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith(":")) {
                    switch (line.substring(1)) {
                        case "clear":
                            reader.clearScreen();
                            break;
                        case "quit":
                            exit = true;
                            break;
                        case "printenv":
                            System.getenv().forEach((k,v)->out.println(String.format("%s=%s", k, v)));
                            break;
                        case "printprops":
                            System.getProperties().forEach((k,v)->out.println(String.format("-D%s=%s", k, v)));
                            break;
                        case "help":
                            Main.printHelp();
                            break;
                        default:
                            out.println("btjjs: No such command");
                            break;
                    }
                    if(exit) break;
                } else if(line.length() != 0){
                    try {
                        engine.eval(line);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }
                out.flush();
            }
        } catch (IOException e){ e.printStackTrace(); } finally {
            if(hist != null)
                try { hist.flush(); } catch (IOException e){ e.printStackTrace(); }
            if(reader != null)
                reader.shutdown();
        }
    }
}