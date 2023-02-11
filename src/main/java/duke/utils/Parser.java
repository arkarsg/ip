package duke.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import duke.tasks.TaskList;
import duke.commands.Command;
import duke.commands.TodoCommand;
import duke.exception.InvalidCommandException;
import duke.commands.DeadlineCommand;
import duke.commands.DeleteCommand;
import duke.commands.EventCommand;
import duke.commands.FindCommand;
import duke.commands.InvalidCommand;
import duke.commands.ListCommand;
import duke.commands.MarkCommand;
import duke.commands.QuitCommand;
import duke.commands.UnmarkCommand;

/**
 * Interpreter class to format user input into tokens
 */
public class Parser {
    private String originalCmd;
    private String[] tokens;
    private static Map<String, Function<String[], Command>> cmdMap = new HashMap<>();

    public Parser() {
        populateCommands();
    }    
    
    /**
     * Formats user input from standard input to String array
     * @return String array of tokens
     */
    public void tokenise(String input) {
        this.originalCmd = input;
        this.tokens = input.split(" ");
    }

    public Command getCommand() {
        try {
            return cmdMap.get(tokens[0]).apply(tokens);
        } catch (NullPointerException e) {
            return new InvalidCommand(new InvalidCommandException(DukeIo.showInvalidCommand()));
        }
    }

    private void populateCommands() {
        cmdMap.put("list", (String[] tokens) -> parseList());
        cmdMap.put("todo", (String[] tokens) -> parseToDo(tokens));
        cmdMap.put("deadline", (String[] tokens) -> parseDeadline(tokens));
        cmdMap.put("event", (String[] tokens) -> parseEvent(tokens));
        cmdMap.put("delete", (String[] tokens) -> parseDelete(tokens));
        cmdMap.put("find", (String[] tokens) -> parseFind(tokens));
        cmdMap.put("mark", (String[] tokens) -> parseMark(tokens));
        cmdMap.put("unmark", (String[] tokens) -> parseUnmark(tokens));
        cmdMap.put("bye", (String[] tokens) -> parseBye());
    }

    private Command parseList() {
        return new ListCommand();
    }

    private Command parseToDo(String[] tokens) {
        if (tokens.length == 1) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.NAME_FORMAT_EXCEPTION));
        }
        StringBuilder s = new StringBuilder();
        for (String token : tokens) {
            if (!token.equals("todo")) {
                s.append(token);
            }
        }
        return new TodoCommand(s.toString());
    }

    private Command parseDeadline(String[] tokens) {
        List<String> t = Arrays.asList(tokens);
        int byIndex = t.indexOf("/by");
        if (!hasKey(byIndex) || !hasDate(byIndex, t.size())) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.ARG_FORMAT_EXCEPTION));
        }
        if (!hasDesc(byIndex)) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.NAME_FORMAT_EXCEPTION));
        }
        String[] args = originalCmd.split("/by");
        // There should only be 2 arguments if parsed correctly.
        assert args.length == 2;
        return new DeadlineCommand(args);
    }

    private Command parseEvent(String[] tokens) {
        List<String> e = Arrays.asList(tokens);
        int fromIndex = e.indexOf("/from");
        int toIndex = e.indexOf("/to");
        if (!hasKey(fromIndex) || !hasKey(toIndex)) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.ARG_FORMAT_EXCEPTION));
        }
        if (!hasBothDates(fromIndex, toIndex, e.size())) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.ARG_FORMAT_EXCEPTION));
        }
        String desc = String.join(" ", e.subList(1, fromIndex));
        String from = String.join(" ", e.subList(fromIndex + 1, toIndex));
        String to = String.join(" ", e.subList(toIndex + 1, e.size()));
        String[] args = {desc, from, to};
        return new EventCommand(args);
    }

    private Command parseDelete(String[] tokens) {
        int taskIndex = 0;
        if (tokens.length == 1 || tokens.length > 2) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.NULL_DELETE_EXCEPTION));
        }

        try {
            taskIndex = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.DELETE_FORMAT_EXCEPTION));
        }

        if (taskIndex <= 0 || taskIndex > TaskList.getTaskCount()) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.TASK_NOT_FOUND_EXCEPTION));
        }
        // Task index to delete should not exceed task count present
        assert taskIndex - 1 < TaskList.getTaskCount();
        return new DeleteCommand(taskIndex - 1);
    }

    private Command parseFind(String[] tokens) {
        if (tokens.length == 1) {
            return new InvalidCommand(
                    new InvalidCommandException(
                            InvalidCommandException.FIND_FORMAT_EXCEPTION));
        }
        StringBuilder s = new StringBuilder();
        for (String search : tokens) {
            if (!search.equals("find")) {
                s.append(search);
            }
        }
        return new FindCommand(s.toString());
    }

    private Command parseMark(String[] tokens) {
        int taskIndex = 0;
        if (tokens.length == 1 || tokens.length > 2) {
            return new InvalidCommand(
                    new InvalidCommandException(InvalidCommandException.MARK_FORMAT_EXCEPTION));   
        }
        try {
            taskIndex = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            return new InvalidCommand(
                    new InvalidCommandException(InvalidCommandException.MARK_FORMAT_EXCEPTION));
        }
        if (taskIndex <= 0 || taskIndex > TaskList.getTaskCount()) {
            return new InvalidCommand(
                    new InvalidCommandException(InvalidCommandException.TASK_NOT_FOUND_EXCEPTION));
        }
        return new MarkCommand(taskIndex - 1);
    }

    private Command parseUnmark(String[] tokens) {
        int taskIndex = 0;
        if (tokens.length == 1 || tokens.length > 2) {
            return new InvalidCommand(
                    new InvalidCommandException(InvalidCommandException.MARK_FORMAT_EXCEPTION));   
        }
        try {
            taskIndex = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            return new InvalidCommand(
                    new InvalidCommandException(InvalidCommandException.MARK_FORMAT_EXCEPTION));
        }
        if (taskIndex <= 0 || taskIndex > TaskList.getTaskCount()) {
            return new InvalidCommand(
                    new InvalidCommandException(InvalidCommandException.TASK_NOT_FOUND_EXCEPTION));
        }
        return new UnmarkCommand(taskIndex - 1);
    }

    private Command parseBye() {
        Storage s = new Storage();
        return new QuitCommand(s);
    }

    private boolean hasKey(int keyIndex) {
        return keyIndex != -1;
    }

    private boolean hasDate(int keyIndex, int nextIndex) {
        return keyIndex != nextIndex;
    }

    private boolean hasBothDates(int fromIndex, int toIndex, int nextIndex) {
        return (hasDate(fromIndex, toIndex)) && (hasDate(toIndex + 1, nextIndex));
    }

    private boolean hasDesc(int keyIndex) {
        return keyIndex > 1;
    }
}
