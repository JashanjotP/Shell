import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;

public class Main {
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

    public static void main(String[] args) throws Exception {
        String currentDirectory = System.getProperty("user.dir");
        String user = System.getProperty("user.name");
        String hostName = "unknown";

        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostName = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner(System.in);
        HashSet<String> BuiltInCommands = new HashSet<>();

        BuiltInCommands.add("echo");
        BuiltInCommands.add("exit");
        BuiltInCommands.add("type");
        BuiltInCommands.add("pwd");
        BuiltInCommands.add("cd");
        BuiltInCommands.add("ls");
        BuiltInCommands.add("history");
        ArrayList<String> history = new ArrayList<>();

        String input;

        do {
            System.out.print(ANSI_BRIGHT_GREEN + user + "@" + hostName + ANSI_RESET + ":" + ANSI_BLUE + currentDirectory + ANSI_RESET + "$ ");
            input = scanner.nextLine();
            if (input.isEmpty()) {
                continue;
            }

            history.add(input);
            List<String[]> pipedCommands = parsePipedCommands(input);

            // Handle each command in the pipeline
            if (pipedCommands.size() == 1) {
                String[] arrofstr = pipedCommands.get(0);
                executeSingleCommand(arrofstr, BuiltInCommands, currentDirectory, history);
            } else {
                executePipedCommands(pipedCommands, currentDirectory);
            }

        } while (true);

   
    }

    private static void executeSingleCommand(String[] arrofstr, HashSet<String> BuiltInCommands, String currentDirectory, ArrayList<String> history) throws IOException {
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
            if (BuiltInCommands.contains(arrofstr[1])) {
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
        } else if (arrofstr[0].equals("cd")) {
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
        } else if (arrofstr[0].equals("ls")) {
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
            executeExternalCommand(arrofstr, currentDirectory);
        }
    }

    private static void executeExternalCommand(String[] arrofstr, String currentDirectory) {
        String pathEnv = System.getenv("PATH");
        boolean found = false;

        if (pathEnv != null) {
            String[] paths = pathEnv.split(":");
            for (String path : paths) {
                File file = new File(path, arrofstr[0]);
                if (file.exists() && file.canExecute()) {
                    try {
                        for(int i=0; i<arrofstr.length; i++){
                            System.out.println(arrofstr[i]);
                        }
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

    private static void executePipedCommands(List<String[]> pipedCommands, String currentDirectory) throws IOException, InterruptedException {
        // List<Process> processes = new ArrayList<>();
        // PipedOutputStream pos = null;
        // PipedInputStream pis = null;
        // Process prevProcess = null; // Initialize prevProcess to null
      
        // for (int i = 0; i < pipedCommands.size(); i++) {
        //   ProcessBuilder processBuilder = new ProcessBuilder(pipedCommands.get(i));
        //   processBuilder.directory(new File(currentDirectory));
      
        //   if (i > 0) {
        //     if (i == 0) { // Ensure pos and pis are created only once per pipe
        //       pos = new PipedOutputStream();
        //       pis = new PipedInputStream(pos);
        //       pos.connect(pis);  // Connect output stream of previous process to input stream of current process
        //     }
        //   }
      
        //   Process process = processBuilder.start();
        //   processes.add(process);
      
        //   final Process finalPrev = prevProcess;
        //   new Thread(() -> {
        //     try (OutputStream os = finalPrev != null ? finalPrev.getOutputStream() : null) {  // Check for null before accessing
        //       if (os != null) {  // Write only if prevProcess is not null
        //         byte[] buffer = new byte[1024];
        //         int bytesRead;
        //         while ((bytesRead = finalPrev.getInputStream().read(buffer)) != -1) {
        //           os.write(buffer, 0, bytesRead);
        //         }
        //       }
        //     } catch (IOException e) {
        //       e.printStackTrace();
        //     }
        //   }).start();
        //   prevProcess = process; // Update prevProcess for next iteration
        // }
      
        // // Handle the output of the last process in the pipeline
        // Process lastProcess = processes.get(processes.size() - 1);
        // Scanner processOutputScanner = new Scanner(lastProcess.getInputStream());
        // Scanner processErrorScanner = new Scanner(lastProcess.getErrorStream());
      
        // while (processOutputScanner.hasNextLine()) {
        //   System.out.println(processOutputScanner.nextLine());
        // }
        // while (processErrorScanner.hasNextLine()) {
        //   System.err.println(processErrorScanner.nextLine());
        // }
      
        // lastProcess.waitFor();
        // processOutputScanner.close();
        // processErrorScanner.close();

        for (String[] command : pipedCommands) {
            System.out.print("[");
            for (int j = 0; j < command.length; j++) {
              System.out.print(command[j] + (j < command.length - 1 ? ", " : ""));
            }
            System.out.println("]");
          }
      }
      
          
      
}
