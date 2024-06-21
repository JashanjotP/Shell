// Uncomment this block to pass the first stage
import java.util.Scanner;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        
        
        Scanner scanner = new Scanner(System.in);
        HashSet <String> BuiltInCommands = new HashSet<String>();
        
        BuiltInCommands.add("echo");
        BuiltInCommands.add("exit");
        BuiltInCommands.add("type");

        String input;

        do {

            System.out.print("$ ");
            input = scanner.nextLine();
            String [] arrofstr = input.split(" ");
            if(input.equals("exit 0")) {
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
            } else{
                System.out.println(input + ": command not found");
            }
            
            
        } while (true);
    }
}
