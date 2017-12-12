# Reflexive Brainfuck
An interpreter/debugger for the brainfuck language. Data and (brainfuck) code share the same address space, allowing self modifying brainfuck programs to be run.

Run TerminalUI.java and type ? at the brainfuck> prompt to get help.

    COMMANDS AVAILABLE AT THE brainfuck> PROMPT.
    ============================================

    All commands are case insensitive.

    LOAD
        Loads brainfuck code. 2 forms:
        LOAD FILE <filename> // Load code from a file.
        LOAD INLINE <code>   // Type in code.

    RUN
        Run the previously loaded code, or resume execution after a break (!).

    STEP
        Turn on & off stepping (execution of 1 instruction on return) and set the
        default cell display (cells are displayed after stepping). 6 forms:
        STEP ON      // Turn on stepping, default cell display is unchanged.
        STEP NOTHING // Turn on stepping, default cell display is nothing.
        STEP         // Same as STEP NOTHING.
        STEP ASCII   // Turn on stepping, default cell display is ascii.
        STEP ALL     // Turn on stepping, full default cell display.
        STEP OFF     // Turn off stepping.

    <return>
        If stepping is on, execute 1 instruction, otherwise do nothing.

    OUTPUT
        Display all the output accumulated by stepping and running.

    BREAK
        Turn on and off break mode. When break mode is on an ! instruction halts
        execution. If break mode is off an ! instruction is skipped. 2 forms:
        BREAK ON  // Turn on break mode.
        BREAK OFF // Turn off break mode.

    SHOW
        Set the default cell display. 4 forms:
        SHOW NOTHING // Default is no cell display at all.
        SHOW         // Same as SHOW NOTHING
        SHOW ASCII   // Default display is ascii only.
        SHOW ALL     // Default display is ascii and numeric

    CELLS
        Display the cells & optionally override the default cell display. (In the
        numeric display # = instruction pointer and @ = data poimter.) 3 forms:
        CELLS       // Display the cells with the default display.
        CELLS ASCII // Display the cells in ascii only.
        CELLS ALL   // Display the cells in ascii and numeric form.

    STATUS
        Display the current settings and execution status.

    EXIT
        Quit the program.

    HELP
        Show this text.

    ?
        Same as HELP.

    BRAINFUCK LANGUAGE
    ==================

    >  Increment the data pointer (move right).

    <  Decrement the data pointer (move left).

    +  Increment the byte at the data pointer.

    -  Decrement the byte at the data pointer.

    .  Output the byte at the data pointer.

    ,  Input a byte at the data pointer. Typing a single character (and return)
       enters the ASCII value of the character. Type more than 1 character (and
       return) to enter a byte as a decimal number 000 thru 255.

    [  if the byte at the data pointer is zero, then instead of moving the
       instruction pointer forward to the next command, jump it forward to
       the command after the matching ] command.

    ]  if the byte at the data pointer is nonzero, then instead of moving the
       instruction pointer forward to the next command, jump it back to the
       command after the matching [ command.

    BRAINFUCK LANGUAGE EXTENSIONS
    =============================

    !  Set a debug break point. Execution halts at a break point only if break
       mode is on.

    _  Do nothing (skip this instruction).

    $  Terminate the program.
