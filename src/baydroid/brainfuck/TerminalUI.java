package baydroid.brainfuck;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;



// load file /home/me/IdeaProjects/untitled/src/com/eleanor/brainfuck/test1.bf

public class TerminalUI implements ReflexiveBrainFuck.UIHandler
    {
    private static final int SHOW_NOTHING  = 1;
    private static final int SHOW_ASCII    = 2;
    private static final int SHOW_ALL      = 3;
    private static final int SHOW_SENTINAL = 4;

    private ReflexiveBrainFuck rbf = new ReflexiveBrainFuck(this);
    private Scanner in = new Scanner(System.in);
    private Dictionary<String, Command> commandDictionary = new Hashtable<String, Command>();
    private boolean stepMode = false;
    private boolean isRunning = false;
    private boolean hasRun = true;
    private int showLevel = SHOW_ALL;

    private interface Command
        {
        boolean doCommand(String input, int floor);
        }

    public static void main(String[] args)
        {
        new TerminalUI().run();
        }

    public TerminalUI()
        {
        commandDictionary.put("LOAD",   new Command() { public boolean doCommand(String input, int floor) { return cmdLoad(input, floor);  } });
        commandDictionary.put("RUN",    new Command() { public boolean doCommand(String input, int floor) { return cmdRun();               } });
        commandDictionary.put("STEP",   new Command() { public boolean doCommand(String input, int floor) { return cmdStep(input, floor);  } });
        commandDictionary.put("OUTPUT", new Command() { public boolean doCommand(String input, int floor) { return cmdOutput();            } });
        commandDictionary.put("BREAK",  new Command() { public boolean doCommand(String input, int floor) { return cmdBreak(input, floor); } });
        commandDictionary.put("SHOW",   new Command() { public boolean doCommand(String input, int floor) { return cmdShow(input, floor);  } });
        commandDictionary.put("CELLS",  new Command() { public boolean doCommand(String input, int floor) { return cmdCells(input, floor); } });
        commandDictionary.put("STATUS", new Command() { public boolean doCommand(String input, int floor) { return cmdStatus();            } });
        commandDictionary.put("EXIT",   new Command() { public boolean doCommand(String input, int floor) { return cmdExit();              } });
        commandDictionary.put("HELP",   new Command() { public boolean doCommand(String input, int floor) { return cmdHelp();              } });
        commandDictionary.put("?",      new Command() { public boolean doCommand(String input, int floor) { return cmdHelp();              } });
        }

    public void run()
        {
        System.out.println("ALOHA. Reflexive brainfuck version 1.0. Type ? or help for help.");
        for ( ; ; )
            {
            System.out.print("brainfuck>");
            if (!dispatchCommand(in.nextLine())) break;
            }

        }

    private boolean dispatchCommand(String input)
        {
        int len = input.length();
        int floor = 0;
        while (floor < len && Character.isWhitespace(input.charAt(floor))) floor++;
        if (floor >= len) return cmdNull();
        int roof = floor + 1;
        while (roof < len && !Character.isWhitespace(input.charAt(roof))) roof++;
        Command cmd = commandDictionary.get(input.substring(floor, roof).toUpperCase());
        if (cmd != null) return cmd.doCommand(input, roof);
        System.out.println("ERROR: Unknown command ignored.");
        return true;
        }

    private boolean cmdLoad(String input, int floor)
        {
        hasRun = true;
        int len = input.length();
        while (floor < len && Character.isWhitespace(input.charAt(floor))) floor++;
        if (floor >= len)
            {
            System.out.println("ERROR: Load command ignored because of missing arguments.");
            return true;
            }
        int roof = floor + 1;
        while (roof < len && !Character.isWhitespace(input.charAt(roof))) roof++;
        String modifier = input.substring(floor, roof).toUpperCase();
        String arg = getArgument(input, roof);
        if (arg == null)
            {
            System.out.println("ERROR: Load command ignored because of missing 2nd argument.");
            return true;
            }
        else if ("INLINE".equals(modifier))
            {
            if (rbf.loadCodeFromString(arg)) hasRun = false;
            }
        else if ("FILE".equals(modifier))
            {
            if (rbf.loadCodeFromFile(arg)) hasRun = false;
            }
        else
            System.out.println("ERROR: Load command ignored because of unknown 1st argument.");
        return true;
        }

    private boolean cmdRun()
        {
        if (hasRun)
            System.out.println("ERROR: No program to run.");
        else
            {
            isRunning = true;
            if (!rbf.run()) hasRun = true;
            isRunning = false;
            cmdOutput();
            }
        return true;
        }

    private boolean cmdNull()
        {
        if (stepMode)
            {
            if (hasRun)
                System.out.println("ERROR: No program to run.");
            else
                {
                if (!rbf.step()) hasRun = true;
                dumpCells(showLevel);
                }
            }
        return true;
        }

    private boolean cmdStep(String input, int floor)
        {
        String arg = getArgument(input, floor);
        if (arg != null)
            {
            if ("OFF".equalsIgnoreCase(arg))
                stepMode = false;
            else
                {
                if (!"ON".equalsIgnoreCase(arg))
                    {
                    int sl = parseShowLevel(arg);
                    if (sl == SHOW_SENTINAL)
                        {
                        System.out.println("ERROR: Step command ignored because of an unkown argument.");
                        return true;
                        }
                    showLevel = sl;
                    }
                stepMode = true;
                }
            }
        else
            System.out.println("ERROR: Step command ignored because of a missing argument.");
        return true;
        }

    private boolean cmdOutput()
        {
        int len = rbf.getOutputLength();
        System.out.println(len + " bytes of output have accumulated.");
        if (len > 0)
            {
            for (int i = 0; i < len; i++)
                {
                char ch = (char) (0xFF&rbf.getOutput(i));
                System.out.print(' ' <= ch && ch <= '~' ? ch : '.');
                }
            System.out.println();
            boolean firstTime = true;
            for (int i = 0; i < len; i++)
                {
                if (firstTime)
                    firstTime = false;
                else
                    System.out.print('|');
                System.out.print(0xFF&rbf.getOutput(i));
                }
            System.out.println();
            }
        return true;
        }

    private boolean cmdBreak(String input, int floor)
        {
        String arg = getArgument(input, floor);
        if (arg != null)
            {
            if ("OFF".equalsIgnoreCase(arg))
                rbf.setBreakMode(false);
            else if ("ON".equalsIgnoreCase(arg))
                rbf.setBreakMode(true);
            else
                System.out.println("ERROR: Break command ignored because of an unknown argument.");
            }
        else
            System.out.println("ERROR: Break command ignored because of a missing argument.");
        return true;
        }

    private boolean cmdShow(String input, int floor)
        {
        int sl = parseShowLevel(getArgument(input, floor));
        if (sl != SHOW_SENTINAL)
            showLevel = sl;
        else
            System.out.println("ERROR: Show command ignored because of a missing or unknown argument.");
        return true;
        }

    private int parseShowLevel(String showLevelStr)
        {
        if (showLevelStr == null)
            return SHOW_SENTINAL;
        else if ("NOTHING".equalsIgnoreCase(showLevelStr))
            return SHOW_NOTHING;
        else if ("ASCII".equalsIgnoreCase(showLevelStr))
            return SHOW_ASCII;
        else if ("ALL".equalsIgnoreCase(showLevelStr))
            return SHOW_ALL;
        else
            return SHOW_SENTINAL;
        }

    private boolean cmdCells(String input, int floor)
        {
        String arg = getArgument(input, floor);
        int sl = showLevel;
        if (arg != null)
            {
            sl = parseShowLevel(arg);
            if (sl == SHOW_NOTHING) sl = SHOW_ASCII;
            if (sl == SHOW_SENTINAL)
                {
                System.out.println("ERROR: Cells command ignored because of an unkown argument.");
                return true;
                }
            }
        dumpCells(sl);
        return true;
        }

    private void dumpCells(int sl)
        {
        if (sl == SHOW_NOTHING) return;
        int floor = rbf.getCellFloor();
        int roof = rbf.getCellRoof();
        if (floor >= roof)
            {
            System.out.println("The cells are all inactive.");
            return;
            }
        int dp = rbf.getDataPointer();
        int ip = rbf.getInstructionPointer();
        if (dp < floor) floor = dp;
        if (ip < floor) floor = ip;
        if (dp >= roof) roof = dp + 1;
        if (ip >= roof) roof = ip + 1;
        System.out.println((roof - floor) + " active cells, data pointer " + (rbf.getDataPointer() - floor) + ", instruction pointer " + (rbf.getInstructionPointer() - floor) + ".");
        switch (sl)
            {
            case SHOW_ALL:   dumpCellsAscii(floor, roof); dumpCellsNumeric(floor, roof); break;
            case SHOW_ASCII: dumpCellsAscii(floor, roof);                                break;
            }
        }

    private void dumpCellsAscii(int floor, int roof)
        {
        for (int i = floor; i < roof; i++)
            {
            char ch = (char)(0xFF & rbf.getCell(i));
            System.out.print(' ' <= ch && ch <= '~' ? ch : '.');
            }
        System.out.println();
        }

    private void dumpCellsNumeric(int floor, int roof)
        {
        int dp = rbf.getDataPointer();
        int ip = rbf.getInstructionPointer();
        boolean firstTime = true;
        for (int i = floor; i < roof; i++)
            {
            if (firstTime)
                firstTime = false;
            else
                System.out.print('|');
            System.out.print(0xFF & rbf.getCell(i));
            if (i == dp) System.out.print('@');
            if (i == ip) System.out.print('#');
            }
        System.out.println();
        }

    private boolean cmdStatus()
        {
        System.out.println("Ready to Execute      : " + (rbf.isLoaded() && !hasRun ? "YES" : "NO"));
        System.out.println("Step Mode             : " + (stepMode ? "ON" : "OFF"));
        System.out.println("Break Mode            : " + (rbf.getBreakMode() ? "ON" : "OFF"));
        switch (showLevel)
            {
            default:         System.out.println("Default Cell Display  : NOTHING"); break;
            case SHOW_ASCII: System.out.println("Default Cell Display  : ASCII");   break;
            case SHOW_ALL:   System.out.println("Default Cell Display  : ALL");     break;
            }
        int floor = rbf.getCellFloor();
        int roof = rbf.getCellRoof();
        int dp = rbf.getDataPointer();
        int ip = rbf.getInstructionPointer();
        if (dp < floor) floor = dp;
        if (ip < floor) floor = ip;
        if (dp >= roof) roof = dp + 1;
        if (ip >= roof) roof = ip + 1;
        System.out.println("Active Cell Count     : " + (roof - floor));
        System.out.println("Instructions Executed : " + (rbf.getStepCount()));
        System.out.println("Data Pointer          : " + (rbf.getDataPointer() - floor));
        System.out.println("Instruction Pointer   : " + (rbf.getInstructionPointer() - floor));
        return true;
        }

    private boolean cmdExit()
        {
        return false;
        }

    private boolean cmdHelp()
        {
        System.out.println();
        System.out.println("COMMANDS AVAILABLE AT THE brainfuck> PROMPT.");
        System.out.println("============================================");
        System.out.println();
        System.out.println("All commands are case insensitive.");
        System.out.println();
        System.out.println("LOAD");
        System.out.println("    Loads brainfuck code. 2 forms:");
        System.out.println("    LOAD FILE <filename> // Load code from a file.");
        System.out.println("    LOAD INLINE <code>   // Type in code.");
        System.out.println();
        System.out.println("RUN");
        System.out.println("    Run the previously loaded code, or resume execution after a break (!).");
        System.out.println();
        System.out.println("STEP");
        System.out.println("    Turn on & off stepping (execution of 1 instruction on return) and set the");
        System.out.println("    default cell display (cells are displayed after stepping). 6 forms:");
        System.out.println("    STEP ON      // Turn on stepping, default cell display is unchanged.");
        System.out.println("    STEP NOTHING // Turn on stepping, default cell display is nothing.");
        System.out.println("    STEP         // Same as STEP NOTHING.");
        System.out.println("    STEP ASCII   // Turn on stepping, default cell display is ascii.");
        System.out.println("    STEP ALL     // Turn on stepping, full default cell display.");
        System.out.println("    STEP OFF     // Turn off stepping.");
        System.out.println();
        System.out.println("<return>");
        System.out.println("    If stepping is on, execute 1 instruction, otherwise do nothing.");
        System.out.println();
        System.out.println("OUTPUT");
        System.out.println("    Display all the output accumulated by stepping and running.");
        System.out.println();
        System.out.println("BREAK");
        System.out.println("    Turn on and off break mode. When break mode is on an ! instruction halts");
        System.out.println("    execution. If break mode is off an ! instruction is skipped. 2 forms:");
        System.out.println("    BREAK ON  // Turn on break mode.");
        System.out.println("    BREAK OFF // Turn off break mode.");
        System.out.println();
        System.out.println("SHOW");
        System.out.println("    Set the default cell display. 4 forms:");
        System.out.println("    SHOW NOTHING // Default is no cell display at all.");
        System.out.println("    SHOW         // Same as SHOW NOTHING");
        System.out.println("    SHOW ASCII   // Default display is ascii only.");
        System.out.println("    SHOW ALL     // Default display is ascii and numeric");
        System.out.println();
        System.out.println("CELLS");
        System.out.println("    Display the cells & optionally override the default cell display. (In the");
        System.out.println("    numeric display # = instruction pointer and @ = data poimter.) 3 forms:");
        System.out.println("    CELLS       // Display the cells with the default display.");
        System.out.println("    CELLS ASCII // Display the cells in ascii only.");
        System.out.println("    CELLS ALL   // Display the cells in ascii and numeric form.");
        System.out.println();
        System.out.println("STATUS");
        System.out.println("    Display the current settings and execution status.");
        System.out.println();
        System.out.println("EXIT");
        System.out.println("    Quit the program.");
        System.out.println();
        System.out.println("HELP");
        System.out.println("    Show this text.");
        System.out.println();
        System.out.println("?");
        System.out.println("    Same as HELP.");
        System.out.println();
        System.out.println("BRAINFUCK LANGUAGE");
        System.out.println("==================");
        System.out.println();
        System.out.println(" >  Increment the data pointer (move right).");
        System.out.println();
        System.out.println(" <  Decrement the data pointer (move left).");
        System.out.println();
        System.out.println(" +  Increment the byte at the data pointer.");
        System.out.println();
        System.out.println(" -  Decrement the byte at the data pointer.");
        System.out.println();
        System.out.println(" .  Output the byte at the data pointer.");
        System.out.println();
        System.out.println(" ,  Input a byte at the data pointer. Typing a single character (and return)");
        System.out.println("    enters the ASCII value of the character. Type more than 1 character (and");
        System.out.println("    return) to enter a byte as a decimal number 000 thru 255.");
        System.out.println();
        System.out.println(" [  if the byte at the data pointer is zero, then instead of moving the");
        System.out.println("    instruction pointer forward to the next command, jump it forward to");
        System.out.println("    the command after the matching ] command.");
        System.out.println();
        System.out.println(" ]  if the byte at the data pointer is nonzero, then instead of moving the");
        System.out.println("    instruction pointer forward to the next command, jump it back to the");
        System.out.println("    command after the matching [ command.");
        System.out.println();
        System.out.println("BRAINFUCK LANGUAGE EXTENSIONS");
        System.out.println("=============================");
        System.out.println();
        System.out.println(" !  Set a debug break point. Execution halts at a break point only if break");
        System.out.println("    mode is on.");
        System.out.println();
        System.out.println(" _  Do nothing (skip this instruction).");
        System.out.println();
        System.out.println(" $  Terminate the program.");
        System.out.println();
        return true;
        }

    private String getArgument(String input, int floor)
        {
        int roof = input.length();
        while (floor < roof && Character.isWhitespace(input.charAt(floor))) floor++;
        if (floor >= roof) return null;
        roof--;
        while (roof > floor && Character.isWhitespace(input.charAt(roof))) roof--;
        roof++;
        return floor < roof ? input.substring(floor, roof) : null;
        }

    public void error(int lineNumber, int charNumber, String msg)
        {
        System.out.println("ERROR: at line " + lineNumber + " char " + charNumber + ": " + msg);
        }

    public void info(int lineNumber, int charNumber, String msg)
        {
        System.out.println("INFO: at line " + lineNumber + " char " + charNumber + ": " + msg);
        }

    public void error(int cellIndex, String msg)
        {
        System.out.println("ERROR at cell " + cellIndex + ": " + msg);
        }

    public void info(int cellIndex, String msg)
        {
        System.out.println("INFO at cell " + cellIndex + ": " + msg);
        }

   public byte input()
        {
        for ( ; ; )
            {
            System.out.print("PROGRAM INPUT (brainfuck ,) >");
            String userInput = in.nextLine();
            if (userInput != null)
                {
                userInput = userInput.trim();
                if (userInput.length() == 1)
                    return (byte)(0xFF & (int)userInput.charAt(0));
                else if (userInput.length() > 1)
                    {
                    boolean ok = true;
                    byte b = 0;
                    try
                        {
                        b = (byte)(0xFF & Integer.parseInt(userInput));
                        }
                    catch (NumberFormatException e)
                        {
                        ok = false;
                        System.out.println("INFO: Invalid input ignored. Try again.");
                        }
                    if (ok) return b;
                    }
                }
            else
                System.out.println("INFO: Empty input ignored. Try again.");
            }
        }

    public void output(byte b)
        {
        if (!isRunning)
            {
            char ch = (char)(0xFF & b);
            if (' ' <= ch && ch <= '~')
                System.out.println("PROGRAM OUTPUT: Ascii " + (char) (0xFF & b) + " numeric " + (0xFF & b) + ".");
            else
                System.out.println("PROGRAM OUTPUT: numeric " + (0xFF & b) + ".");
            }
        }
    }
