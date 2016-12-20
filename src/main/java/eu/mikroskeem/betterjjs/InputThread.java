package eu.mikroskeem.betterjjs;


import org.jline.reader.*;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class InputThread extends Thread {
    private ScriptEngine engine;
    private ScriptEngine groovyShell;
    public InputThread(ScriptEngine engine, ScriptEngine groovyShell){
        super("InputThread");
        this.engine = engine;
        this.groovyShell = groovyShell;
    }

    @Override public void run(){
        boolean exit = false;
        boolean groovyMode = false;
        String prompt = "btjjs> ";
        String rightPrompt = "";
        Character mask = null;
        String trigger = null;

        TerminalBuilder builder = TerminalBuilder.builder().system(true);
        Terminal terminal = null;

        try {
            terminal = builder.build();
        } catch (IOException e){
            e.printStackTrace();
            return;
        }
        final PrintWriter out = terminal.writer();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        reader.setVariable(LineReader.HISTORY_FILE,
                new File(System.getProperty("user.home"), ".btjjs_history").getAbsoluteFile());
        final History history = new DefaultHistory(reader);
        Runtime.getRuntime().addShutdownHook(new Thread(history::save));

        while (true) {
            String line = null;
            try {
                line = reader.readLine(prompt, rightPrompt, null, null);
            } catch (UserInterruptException e) {
                terminal.writer().println("Ctrl-C! (Press Ctrl-D if you want to exit)");
            } catch (EndOfFileException e) {
                break;
            }
            if (line == null) {
                continue;
            }
            if(line.startsWith(":")) {
                switch (line.substring(1)) {
                    case "clear":
                        terminal.puts(InfoCmp.Capability.clear_screen);
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
                    case "gr":
                        groovyMode = !groovyMode;
                        if(groovyMode){
                            prompt = "(gr) btjjs> ";
                            out.println("groovy mode: on");
                        } else {
                            prompt = "btjjs> ";
                            out.println("groovy mode: off");
                        }
                        break;
                    default:
                        out.println("btjjs: No such command");
                        break;
                }
                if(exit) break;
            } else if(line.length() != 0){
                try {
                    out.println(groovyMode?groovyShell.eval(line):engine.eval(line));
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
            terminal.flush();
        }
    }
}