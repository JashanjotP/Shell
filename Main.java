import java.util.Scanner;
import java.util.HashSet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

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
        BuiltInCommands.add("clear");
        BuiltInCommands.add("pwd");
       
        String input;

        do {
            System.out.print(ANSI_BRIGHT_GREEN + user + "@" + hostName + ANSI_RESET + ":" + ANSI_BLUE + currentDirectory + ANSI_RESET + "$ ");
            input = scanner.nextLine();
            String[] arrofstr = input.split(" ");
            if (arrofstr[0].equals("exit")) {
                break;
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
            } else if (arrofstr[0].equals("clear")) {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            } else if (arrofstr[0].equals("pwd")) {
                System.out.println(currentDirectory);
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
            } else {
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
        } while (true);

        scanner.close();
    }
}
