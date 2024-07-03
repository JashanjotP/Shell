package Shell.src.main.java.com.example;

import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.jline.reader.impl.completer.StringsCompleter;

import java.io.File;
import java.util.List;

public class TabCompleter implements org.jline.reader.Completer {

    private List<String> candidates;

    public TabCompleter(List<String> candidates) {
        this.candidates = candidates; // Initialize with combined list of commands
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        // Clear candidates that were added by the previous completer
        candidates.clear();

        // Add command candidates here
        String word = line.word();

        // Iterate over the list of candidates
        for (String command : this.candidates) {
            if (command.startsWith(word)) {
                candidates.add(new Candidate(command, command, null, null, null, null, true));
            }
        }
    }
}
