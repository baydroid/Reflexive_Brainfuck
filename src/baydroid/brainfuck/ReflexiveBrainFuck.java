package baydroid.brainfuck;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;



public class ReflexiveBrainFuck
    {
    private UIHandler ui = null;
    private ExtensibleArray cells = new ExtensibleArray();
    private ExtensibleArray output = new ExtensibleArray();
    private int dataPointer = 0;
    private int instructionPointer = 0;
    private int outputPointer = 0;
    private int stepCount = 0;
    private boolean breakMode = false;

    public interface UIHandler
        {
        void error(int lineNumber, int charNumber, String msg);
        void info(int lineNumber, int charNumber, String msg);
        void error(int cellIndex, String msg);
        void info(int cellIndex, String msg);
        byte input();
        void output(byte b);
        }

    public ReflexiveBrainFuck(UIHandler ui)
        {
        setUIHandler(ui);
        }

    public void setUIHandler(UIHandler ui)
        {
        this.ui = ui;
        }

    public boolean loadCodeFromString(String code)
        {
        return loadCode(new StringReader(code));
        }

    public boolean loadCodeFromFile(String filename)
        {
        FileReader fr = null;
        try
            {
            fr = new FileReader(filename);
            }
        catch (FileNotFoundException e)
            {
            ui.error(0, 0, e.getMessage());
            return false;
            }
        return loadCode(fr);
        }

    public boolean loadCode(Reader input)
        {
        clear();
        int lineNumber = 1;
        int charNumber = 0;
        int loopDepth = 0;
        try
            {
            int crCount = 0;
            int lfCount = 0;
            boolean eol = false;
            for ( ; ; )
                {
                int ch = input.read();
                if (ch == -1) break;
                eol = false;
                switch (ch)
                    {
                    case '<':  cells.set(dataPointer++, (byte)ch);              break;
                    case '>':  cells.set(dataPointer++, (byte)ch);              break;
                    case '+':  cells.set(dataPointer++, (byte)ch);              break;
                    case '-':  cells.set(dataPointer++, (byte)ch);              break;
                    case '.':  cells.set(dataPointer++, (byte)ch);              break;
                    case ',':  cells.set(dataPointer++, (byte)ch);              break;
                    case '[':  cells.set(dataPointer++, (byte)ch); loopDepth++; break;
                    case ']':  cells.set(dataPointer++, (byte)ch); loopDepth--; break;
                    case '!':  cells.set(dataPointer++, (byte)ch);              break;
                    case '_':  cells.set(dataPointer++, (byte)ch);              break;
                    case '$':  cells.set(dataPointer++, (byte)ch);              break;
                    case '\r': eol = true; crCount++;                           break;
                    case '\n': eol = true; lfCount++;                           break;
                    default:
                        if (!Character.isWhitespace((char)ch)) ui.info(lineNumber, charNumber + 1, "Ignored bad character in input: \"" + (char)ch + "\".");
                        break;
                    }
                if (eol)
                    charNumber = 1;
                else
                    {
                    lineNumber += crCount > lfCount ? crCount : lfCount;
                    crCount = lfCount = 0;
                    charNumber++;
                    }
                if (loopDepth < 0) ui.info(lineNumber, charNumber + 1, "Occurance of ] without matching [.");
                }
            }
        catch (IOException e)
            {
            ui.error(lineNumber, charNumber, e.getMessage());
            clear();
            return false;
            }
        if (loopDepth != 0) ui.info(lineNumber, charNumber, loopDepth + " occurances of an [ without a matching ].");
        if (dataPointer == 0)
            {
            ui.error(0, 0, "Nothing to load.");
            return false;
            }
        cells.set(dataPointer++, (byte)'$');
        ui.info(lineNumber, charNumber, dataPointer + " instructions loaded OK.");
        return true;
        }

    public boolean run()
        {
        while (internalStep());
        return breakMode && cells.get(instructionPointer - 1) == (byte)'!';
        }

    public boolean step()
        {
        return internalStep() || (breakMode && cells.get(instructionPointer - 1) == (byte)'!');
        }

    private boolean internalStep()
        {
        stepCount++;
        byte opCode = cells.get(instructionPointer++);
        switch (opCode)
            {
            case '<': dataPointer--;                                                                                        break;
            case '>': dataPointer++;                                                                                        break;
            case '+': cells.set(dataPointer, (byte)(0xFF & (cells.get(dataPointer) + 1)));                                  break;
            case '-': cells.set(dataPointer, (byte)(0xFF & (cells.get(dataPointer) - 1)));                                  break;
            case '_':                                                                                                       break;
            case '$': ui.info(instructionPointer - 1, "Normal sucessful completion after " + stepCount + " instructions."); return false;
            case '.': ui.output(output.set(outputPointer++, cells.get(dataPointer)));                                       break;
            case ',': cells.set(dataPointer, ui.input());                                                                   break;
            case '[':
                if (cells.get(dataPointer) == 0)
                    {
                    int loopStartInstructionPointer = instructionPointer - 1;
                    int loopDepth = 0;
                    int roof = cells.roof();
                    while (instructionPointer < roof)
                        {
                        byte scanOpCode = cells.get(instructionPointer++);
                        if (scanOpCode == ']')
                            {
                            if (loopDepth > 0)
                                loopDepth--;
                            else
                                return true;
                            }
                        else if (scanOpCode == '[')
                            loopDepth++;
                        }
                    ui.error(loopStartInstructionPointer, "Execution failed due to a [ without a matching ].");
                    return false;
                    }
                break;
            case ']':
                if (cells.get(dataPointer) != 0)
                    {
                    int loopEndInstructionPointer = --instructionPointer;
                    instructionPointer--;
                    int loopDepth = 0;
                    int floor = cells.floor();
                    while (instructionPointer >= floor)
                        {
                        byte scanOpCode = cells.get(instructionPointer--);
                        if (scanOpCode == '[')
                            {
                            if (loopDepth > 0)
                                loopDepth--;
                            else
                                {
                                instructionPointer += 2;
                                return true;
                                }
                            }
                        else if (scanOpCode == ']')
                            loopDepth++;
                        }
                    ui.error(loopEndInstructionPointer, "Execution failed due to a ] without a matching [.");
                    return false;
                    }
                break;
            case '!':
                if (breakMode)
                    {
                    ui.info(instructionPointer - 1, "Execution halted by break point after " + stepCount + " instructions.");
                    return false;
                    }
                else
                    return true;
            default:
                ui.error(instructionPointer - 1, "Execution failed due to an invalid instruction " + opCode + ".");
                return false;
            }
        return true;
        }

    public void setBreakMode(boolean breakMode)
        {
        this.breakMode = breakMode;
        }

    public boolean getBreakMode()
        {
        return breakMode;
        }

    public int getOutputLength()
        {
        return outputPointer;
        }

    public byte getOutput(int index)
        {
        if (index < 0 || index >= outputPointer) throw new Error("Out of range index " + index + " (output length is " + outputPointer + ").");
        return output.get(index);
        }

    public int getStepCount()
        {
        return stepCount;
        }

    public boolean isLoaded()
        {
        return cells.length() != 0;
        }

    public int getCellFloor()
        {
        return cells.floor();
        }

    public int getCellRoof()
        {
        return cells.roof();
        }

    public int getDataPointer()
        {
        return dataPointer;
        }

    public int getInstructionPointer()
        {
        return instructionPointer;
        }

    public byte getCell(int index)
        {
        return cells.get(index);
        }

    private void clear()
        {
        cells.clear();
        output.clear();
        dataPointer = instructionPointer = outputPointer = stepCount = 0;
        }
    }
