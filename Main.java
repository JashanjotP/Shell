// Uncomment this block to pass the first stage
import java.util.Scanner;
import java.util.HashSet;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.File;

public class Main {
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BRIGHT_GREEN = "\u001B[92m";
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
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
        HashSet <String> BuiltInCommands = new HashSet<String>();
        
        BuiltInCommands.add("echo");
        BuiltInCommands.add("exit");
        BuiltInCommands.add("type");
        BuiltInCommands.add("clear");
        BuiltInCommands.add("pwd");
        BuiltInCommands.add("ls");

        String input;

        do {

            System.out.print(ANSI_BRIGHT_GREEN+user+"@"+hostName+ANSI_RESET+":"+ANSI_BLUE+ currentDirectory+ ANSI_RESET+"$ ");
            input = scanner.nextLine();
            String [] arrofstr = input.split(" ");
            if(arrofstr[0].equals("exit")) {
                break;
            } else if(arrofstr[0].equals("echo")){
                 
                for(int i = 1; i < arrofstr.length; i++){
                    System.out.print(arrofstr[i]);

                    if(i != arrofstr.length - 1){
                        System.out.print(" ");
                    } else{
                        System.out.print("\n");
                    }
                }
                

            } else if(arrofstr[0].equals("type")){
                 if(BuiltInCommands.contains(arrofstr[1])){
                    System.out.println(arrofstr[1] + " is a shell builtin");
                 } else{
                    System.out.println(arrofstr[1] + ": not found");
                 }
            } else if(arrofstr[0].equals("clear")){
                System.out.print("\033[H\033[2J");
                System.out.flush();
            } else if(arrofstr[0].equals("pwd")){
                System.out.println(currentDirectory);
            } else if(arrofstr[0].equals("ls")){
                File directory = new File(currentDirectory);
                File[] files = directory.listFiles();

                for(File file: files){
                    String fileName = file.getName();
                    if(!fileName.startsWith(".")){
                        String toPrint;
                        if(file.isDirectory()){
                            toPrint = ANSI_BLUE + fileName + ANSI_RESET;
                        } else{
                            toPrint = ANSI_BRIGHT_GREEN+fileName +ANSI_RESET;
                        }
                        System.out.print( toPrint+"  ");
                    }
                }
                System.out.println();
            }
            else{
                System.out.println(input + ": command not found");
            }
            
            
        } while (true);

        scanner.close();
    }
}
