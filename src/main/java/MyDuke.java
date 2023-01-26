import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class MyDuke {
    private static ArrayList<Task> allTasks = new ArrayList<Task>();
    private static Map<String, Consumer<String[]>> MAP = new HashMap<>();
    private static int taskCount = 0;

    public void init() {
        DukeIO.printHello();
        populateCommands();
    }

    public void quit() {
        DukeIO.printQuit();
    }

    public void exec(String[] tokens) throws InvalidCommandException {
        try {
            MAP.get(tokens[0]).accept(tokens);
        } catch (NullPointerException n) {
            DukeIO.showInvalidCommand();
            return;
        }
    }

    public static int getTaskCount() { 
        return taskCount; 
    }

    public static ArrayList<Task> getAllTasks() {  
        return allTasks;   
    }

    public static void loadTask(ArrayList<Task> tasks) {
        allTasks = tasks;
        taskCount = tasks.size();
    }

    private void populateCommands() {
        MAP.put("list", (tokens) -> DukeIO.showAll());
        MAP.put("todo", (tokens) -> addTodo(tokens));
        MAP.put("deadline", (tokens) -> addDeadline(tokens));
        MAP.put("event", (tokens) -> addEvent(tokens));
        MAP.put("mark", (tokens) -> toggle(tokens));
        MAP.put("unmark", (tokens) -> toggle(tokens));
        MAP.put("delete", (tokens) -> delete(tokens));
    }


    private void addTask(Task task) {
        allTasks.add(task);
        taskCount++;
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
            if (taskIndex <= 0 || taskIndex > taskCount) {
                throw new InvalidCommandException(
                    InvalidCommandException.TASK_NOT_FOUND_EXCEPTION);
            }
        // NaN input from user to mark/unmark task
        } catch (NumberFormatException e) {
            DukeIO.showError(new InvalidCommandException(
                InvalidCommandException.MARK_FORMAT_EXCEPTION));
            return;
        } catch (InvalidCommandException e) {
            DukeIO.showError(e);
            return;
        }

        Task task = allTasks.get(taskIndex-1);
        if (!task.isDone() && tokens[0].equals("mark")) {
            task.toggleDoneOrNot();
            DukeIO.notifySuccessComplete(task); 
        } else if (task.isDone() && tokens[0].equals("unmark")) {
            task.toggleDoneOrNot();
            DukeIO.notifyUnmark(task);
        } else if (!task.isDone() && tokens[0].equals("unmark")) {
            DukeIO.notifyUnmarkFail(task);
        } else if (task.isDone() && tokens[0].equals("mark")) {
            DukeIO.nofifyMarkFail(task);
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
            DukeIO.showError(e);
            return;
        }

        String t = "";
        for (String s : tokens) {
            if (!s.equals("todo")) {
                t += " " + s;
            }
        }

        ToDo todo = new ToDo(t);
        addTask(todo);
        DukeIO.notifySuccessAdd(todo);
        DukeIO.showCount();
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
            DukeIO.showError(e);
            return;
        }

        String desc = String.join(" ",t.subList(1, byIndex));
        String byString = String.join(" ", t.subList(byIndex+1, t.size()));
        Deadline d = new Deadline(desc, byString);
        addTask(d);
        DukeIO.notifySuccessAdd(d);
        DukeIO.showCount();          
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
            DukeIO.showError(e);
            return;
        }

        String desc = String.join(" ",t.subList(1, fromIndex));
        String from = String.join(" ", t.subList(fromIndex+1, toIndex));
        String to = String.join(" ", t.subList(toIndex+1, t.size()));
        Event e = new Event(desc, from, to);
        addTask(e);
        DukeIO.notifySuccessAdd(e);
        DukeIO.showCount();
    }

    private void delete(String[] tokens) {
        int taskIndex = 0;

        try {
            if (tokens.length == 1 || tokens.length > 2) {
                throw new InvalidCommandException(
                    InvalidCommandException.NULL_DELETE_EXCEPTION);
            }

            taskIndex = Integer.parseInt(tokens[1]);
            if (taskIndex <= 0 || taskIndex > taskCount) {
                throw new InvalidCommandException(
                    InvalidCommandException.TASK_NOT_FOUND_EXCEPTION);
            }
        } catch (NumberFormatException e) {
            DukeIO.showError(new InvalidCommandException(
                InvalidCommandException.DELETE_FORMAT_EXCEPTION));
            return;
        } catch (InvalidCommandException e) {
            DukeIO.showError(e);
            return;
        }

        System.out.println(allTasks.get(taskIndex-1).toString() + " deleted.");
        allTasks.remove(taskIndex-1);
        taskCount--;
        DukeIO.showCount();
    }
}
