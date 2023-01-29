import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MyDuke {
    private static DukeIO dukeIo = new DukeIO();
    private static TaskList allTasks;
    private static Map<String, Consumer<String[]>> cmdMap = new HashMap<>();

    public void init() {
        dukeIo.printHello();
        populateCommands();
    }

    public void quit() {
        dukeIo.printQuit();
    }

    public void exec(String[] tokens, TaskList taskList) throws InvalidCommandException {
        allTasks = taskList;
        try {
            cmdMap.get(tokens[0]).accept(tokens);
        } catch (NullPointerException n) {
            dukeIo.showInvalidCommand();
            return;
        }
    }

    private void populateCommands() {
        cmdMap.put("list", (tokens) -> dukeIo.showAll());
        cmdMap.put("todo", (tokens) -> addTodo(tokens));
        cmdMap.put("deadline", (tokens) -> addDeadline(tokens));
        cmdMap.put("event", (tokens) -> addEvent(tokens));
        cmdMap.put("mark", (tokens) -> toggle(tokens));
        cmdMap.put("unmark", (tokens) -> toggle(tokens));
        cmdMap.put("delete", (tokens) -> delete(tokens));
    }

    private void toggle(String[] tokens) {
        int taskIndex = 0;

        try {
            // Can we handle batch mark/unmark?
            if (tokens.length == 1 || tokens.length > 2) {
                throw new InvalidCommandException(
                    InvalidCommandException.MARK_FORMAT_EXCEPTION);
            }
            taskIndex = Integer.parseInt(tokens[1]);
            if (taskIndex <= 0 || taskIndex > allTasks.getTaskCount()) {
                throw new InvalidCommandException(
                    InvalidCommandException.TASK_NOT_FOUND_EXCEPTION);
            }
        // NaN input from user to mark/unmark task
        } catch (NumberFormatException e) {
            dukeIo.showError(new InvalidCommandException(
                InvalidCommandException.MARK_FORMAT_EXCEPTION));
            return;
        } catch (InvalidCommandException e) {
            dukeIo.showError(e);
            return;
        }

        Task task = allTasks.getTask(taskIndex-1);
        if (!task.isDone() && tokens[0].equals("mark")) {
            task.toggleDoneOrNot();
            dukeIo.notifySuccessComplete(task); 
        } else if (task.isDone() && tokens[0].equals("unmark")) {
            task.toggleDoneOrNot();
            dukeIo.notifyUnmark(task);
        } else if (!task.isDone() && tokens[0].equals("unmark")) {
            dukeIo.notifyUnmarkFail(task);
        } else if (task.isDone() && tokens[0].equals("mark")) {
            dukeIo.nofifyMarkFail(task);
        }
    }

    private void addTodo(String[] tokens) {
        // if input is only "todo"
        try {
            if (tokens.length == 1) {
                // raise invalid command
                throw new InvalidCommandException(
                    InvalidCommandException.NAME_FORMAT_EXCEPTION 
                    + ToDo.showFormat());
            }
        } catch (InvalidCommandException e) {
            dukeIo.showError(e);
            return;
        }

        String t = "";
        for (String s : tokens) {
            if (!s.equals("todo")) {
                t += " " + s;
            }
        }

        ToDo todo = new ToDo(t);
        allTasks.addTask(todo);
        dukeIo.notifySuccessAdd(todo);
        dukeIo.showCount();
    }

    private void addDeadline(String[] tokens) {
        List<String> t = Arrays.asList(tokens);
        int byIndex = t.indexOf("/by");

        try {
            if (byIndex == -1) {
                throw new InvalidCommandException(
                    InvalidCommandException.ARG_FORMAT_EXCEPTION
                    + Deadline.showFormat());
            }
            if (byIndex + 1 == t.size()) {
                throw new InvalidCommandException(
                    InvalidCommandException.ARG_FORMAT_EXCEPTION
                    + Deadline.showFormat());
            }
            if (byIndex == 1) {
                throw new InvalidCommandException(
                    InvalidCommandException.NAME_FORMAT_EXCEPTION
                    + Deadline.showFormat());
            }
        } catch (InvalidCommandException e) {
            dukeIo.showError(e);
            return;
        }

        String desc = String.join(" ",t.subList(1, byIndex));
        String byString = String.join(" ", t.subList(byIndex+1, t.size()));
        Deadline d = new Deadline(desc, byString);
        allTasks.addTask(d);
        dukeIo.notifySuccessAdd(d);
        dukeIo.showCount();          
    }

    private void addEvent(String[] tokens) {
        List<String> t = Arrays.asList(tokens);
        int fromIndex = t.indexOf("/from"); int toIndex = t.indexOf("/to");

        try {
            if (fromIndex == -1 || toIndex == -1) {
                throw new InvalidCommandException(
                    InvalidCommandException.ARG_FORMAT_EXCEPTION
                    + Event.showFomat());
            }

            if (fromIndex + 1 == toIndex || toIndex + 1 == t.size()) {
                throw new InvalidCommandException(
                    InvalidCommandException.ARG_FORMAT_EXCEPTION
                    + Event.showFomat());
            }
        } catch (InvalidCommandException e) {
            dukeIo.showError(e);
            return;
        }

        String desc = String.join(" ",t.subList(1, fromIndex));
        String from = String.join(" ", t.subList(fromIndex+1, toIndex));
        String to = String.join(" ", t.subList(toIndex+1, t.size()));
        Event e = new Event(desc, from, to);
        allTasks.addTask(e);
        dukeIo.notifySuccessAdd(e);
        dukeIo.showCount();
    }

    private void delete(String[] tokens) {
        int taskIndex = 0;

        try {
            if (tokens.length == 1 || tokens.length > 2) {
                throw new InvalidCommandException(
                    InvalidCommandException.NULL_DELETE_EXCEPTION);
            }

            taskIndex = Integer.parseInt(tokens[1]);
            if (taskIndex <= 0 || taskIndex > allTasks.getTaskCount()) {
                throw new InvalidCommandException(
                    InvalidCommandException.TASK_NOT_FOUND_EXCEPTION);
            }
        } catch (NumberFormatException e) {
            dukeIo.showError(new InvalidCommandException(
                InvalidCommandException.DELETE_FORMAT_EXCEPTION));
            return;
        } catch (InvalidCommandException e) {
            dukeIo.showError(e);
            return;
        }

        System.out.println(allTasks.getTask(taskIndex-1).toString() + " deleted.");
        allTasks.deleteTask(taskIndex-1);
        dukeIo.showCount();
    }
}
