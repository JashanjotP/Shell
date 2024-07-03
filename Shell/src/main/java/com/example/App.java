package Shell.src.main.java.com.example;

import java.util.regex.*;
import java.util.*;
import java.net.*;
import java.nio.file.*;
import java.io.*;
import org.jline.reader.*;
import org.jline.reader.impl.completer.*;
import org.jline.terminal.*;

public class App {
    private static void redirectStreams(InputStream input, OutputStream output) {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                    output.flush();
                }
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private static List<String> getExecutableNamesFromPath() {
        List<String> executableNames = new ArrayList<>();

        // Get PATH environment variable
        String pathEnvVar = System.getenv("PATH");
        if (pathEnvVar != null) {
            String[] paths = pathEnvVar.split(File.pathSeparator);
            for (String path : paths) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.canExecute() && !file.isDirectory()) {
                                executableNames.add(file.getName());
                            }
                        }
                    }
                }
            }
        }

        return executableNames;
    }
    private static String getPath(String parameter) {
        for (String path : System.getenv("PATH").split(":")) {
            Path fullPath = Path.of(path, parameter);
            if (Files.isRegularFile(fullPath)) {
                return fullPath.toString();
            }
        }
        return null;
    }

    private static String[] parseCommand(String input) {
        ArrayList<String> command = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                command.add(matcher.group(1));
            } else {
                command.add(matcher.group(2));
            }
        }
        return command.toArray(new String[0]);
    }

    private static List<String[]> parsePipedCommands(String input) {
        List<String[]> commands = new ArrayList<>();
        String[] pipedCommands = input.split("\\|");
        for (String cmd : pipedCommands) {
            commands.add(parseCommand(cmd.trim()));
        }
        return commands;
    }

    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BRIGHT_GREEN = "\u001B[92m";
    public static String currentDirectory = System.getProperty("user.dir");
    public static void main(String[] args) throws Exception {
        
        String user = System.getProperty("user.name");
        String hostName = "unknown";

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostName = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Terminal terminal = TerminalBuilder.builder().system(true).build();
        List<String> BuiltInCommands = new ArrayList<>(Arrays.asList(
   "cd","echo", "exit", "type", "pwd", "ls", "history"
    ));

    List<String> ExternalCommands = getExecutableNamesFromPath();

    List<String> AllCommands = new ArrayList<>(BuiltInCommands);
    AllCommands.addAll(ExternalCommands);

        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).completer(new TabCompleter(AllCommands)).build();
        ArrayList<String> history = new ArrayList<>();
       
        String input;

        do {
            String prompt = ANSI_BRIGHT_GREEN + user + "@" + hostName + ANSI_RESET + ":" + ANSI_BLUE + currentDirectory + ANSI_RESET + "$ ";
            input = lineReader.readLine(prompt);
            if (input.trim().isEmpty()) {
                continue;
            }
            history.add(input);
            List<String[]> pipedCommands = parsePipedCommands(input);

            // Handle each command in the pipeline
            if (pipedCommands.size() == 1) {
                String[] arrofstr = pipedCommands.get(0);
                executeSingleCommand(arrofstr, BuiltInCommands, history);
            } else {
                executePipedCommands(pipedCommands);
            }

        } while (true);
    }

    private static void executeSingleCommand(String[] arrofstr, List<String> builtInCommands, ArrayList<String> history) throws IOException {
        if (arrofstr[0].equals("exit")) {
            System.exit(0);
        } else if (arrofstr[0].equals("history")) {
            int i = 0;
            for (String cmd : history) {
                System.out.println(i + " " + cmd);
                i++;
            }
        } else if (arrofstr[0].equals("echo")) {
            for (int i = 1; i < arrofstr.length; i++) {
                System.out.print(arrofstr[i]);
                if (i != arrofstr.length - 1) {
                    System.out.print(" ");
                } else {
                    System.out.print("\n");
                }
            }
        } else if (arrofstr[0].equals("type")) {
            if (builtInCommands.contains(arrofstr[1])) {
                System.out.println(arrofstr[1] + " is a shell builtin");
            } else {
                String path = getPath(arrofstr[1]);
                if (path != null) {
                    System.out.println(arrofstr[1] + " is " + path);
                } else {
                    System.out.println(arrofstr[1] + ": not found");
                }
            }
        } else if (arrofstr[0].equals("pwd")) {
            System.out.println(currentDirectory);
        }  else if (arrofstr[0].equals("cd")) {
            if (arrofstr.length > 1) {
                String newPath = arrofstr[1];
                if (newPath.equals("~")) {
                    newPath = System.getProperty("user.home");
                } else if (!new File(newPath).isAbsolute()) {
                    newPath = currentDirectory + File.separator + newPath;
                }
                File newDir = new File(newPath);
                if (newDir.exists() && newDir.isDirectory()) {
                    try {
                        currentDirectory = newDir.getCanonicalPath();
                    } catch (IOException e) {
                        System.out.println("cd: " + arrofstr[1] + ": No such directory");
                    }
                } else {
                    System.out.println("cd: " + arrofstr[1] + ": No such directory");
                }
            } else {
                currentDirectory = System.getProperty("user.home");
            }
          }
        
         else if (arrofstr[0].equals("ls")) {
            File directory = new File(currentDirectory);
            File[] files = directory.listFiles();

            for (File file : files) {
                String fileName = file.getName();
                if (!fileName.startsWith(".")) {
                    String toPrint;
                    if (file.isDirectory()) {
                        toPrint = ANSI_BLUE + fileName + ANSI_RESET;
                    } else {
                        toPrint = ANSI_BRIGHT_GREEN + fileName + ANSI_RESET;
                    }
                    System.out.print(toPrint + "  ");
                }
            }
            System.out.println();
        } else if (arrofstr[0].startsWith("./")) {
            File executable = new File(currentDirectory, arrofstr[0]);
            if (executable.exists() && executable.canExecute()) {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(arrofstr);
                    processBuilder.directory(new File(currentDirectory));
                    Process process = processBuilder.start();

                    // Capture the output and error streams
                    Scanner processOutputScanner = new Scanner(process.getInputStream());
                    Scanner processErrorScanner = new Scanner(process.getErrorStream());

                    while (processOutputScanner.hasNextLine()) {
                        System.out.println(processOutputScanner.nextLine());
                    }
                    while (processErrorScanner.hasNextLine()) {
                        System.err.println(processErrorScanner.nextLine());
                    }

                    process.waitFor();
                    processOutputScanner.close();
                    processErrorScanner.close();
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error executing command: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println(arrofstr[0] + ": No such file or directory or not executable");
            }
        } else {
            executeExternalCommand(arrofstr);
        }
    }

    private static void executeExternalCommand(String[] arrofstr) {
        String pathEnv = System.getenv("PATH");
        boolean found = false;

        if (pathEnv != null) {
            String[] paths = pathEnv.split(":");
            for (String path : paths) {
                File file = new File(path, arrofstr[0]);
                if (file.exists() && file.canExecute()) {
                    try {
                        ProcessBuilder processBuilder = new ProcessBuilder(arrofstr);
                        processBuilder.directory(new File(currentDirectory));
                        Process process = processBuilder.start();

                        // Capture the output and error streams
                        Scanner processOutputScanner = new Scanner(process.getInputStream());
                        Scanner processErrorScanner = new Scanner(process.getErrorStream());

                        while (processOutputScanner.hasNextLine()) {
                            System.out.println(processOutputScanner.nextLine());
                        }
                        while (processErrorScanner.hasNextLine()) {
                            System.err.println(processErrorScanner.nextLine());
                        }

                        process.waitFor();
                        found = true;
                        processOutputScanner.close();
                        processErrorScanner.close();
                        break;
                    } catch (IOException | InterruptedException e) {
                        System.err.println("Error executing command: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }

        if (!found) {
            System.out.println(arrofstr[0] + ": command not found");
        }
    }

    private static void executePipedCommands(List<String[]> pipedCommands) throws IOException, InterruptedException {
        if (pipedCommands.size() < 2) {
            throw new IllegalArgumentException("Piped commands should contain at least two command arrays.");
        }

        ProcessBuilder pb1 = new ProcessBuilder(pipedCommands.get(0));
        pb1.directory(new File(currentDirectory));
        Process p1 = pb1.start();

        Process previousProcess = p1;

        for (int i = 1; i < pipedCommands.size(); i++) {
            ProcessBuilder pb = new ProcessBuilder(pipedCommands.get(i));
            pb.directory(new File(currentDirectory));

            // Redirect the output of the previous process to the input of the next process
            Process p = pb.start();
            if (!Arrays.asList(pipedCommands.get(i)).contains("echo")) {
                redirectStreams(previousProcess.getInputStream(), p.getOutputStream());
            }

            previousProcess = p;
        }

        // Print the output of the final process
        BufferedReader reader = new BufferedReader(new InputStreamReader(previousProcess.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        previousProcess.waitFor();
    }
}
