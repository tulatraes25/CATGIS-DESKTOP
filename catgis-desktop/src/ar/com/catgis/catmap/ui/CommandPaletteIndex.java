package ar.com.catgis.catmap.ui;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Searchable command palette index: task-oriented fuzzy search over registered
 * commands. Pure model (no Swing), so the ranking is unit-testable.
 */
public final class CommandPaletteIndex {

    /** One palette command: label shown, description/keywords for task search. */
    public record Command(String id, String label, String description,
                          List<String> keywords, Runnable action) {
        public Command {
            if (id == null || id.isBlank() || label == null || label.isBlank()) {
                throw new IllegalArgumentException("Command id and label are required.");
            }
            keywords = keywords == null ? List.of() : List.copyOf(keywords);
        }
    }

    private final List<Command> commands;

    public CommandPaletteIndex(List<Command> commands) {
        this.commands = commands == null ? List.of() : List.copyOf(commands);
    }

    public List<Command> commands() {
        return commands;
    }

    /** Empty query returns every command; otherwise ranked matches. */
    public List<Command> search(String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isBlank()) {
            return commands;
        }
        String[] tokens = normalizedQuery.split("\\s+");
        List<Scored> matches = new ArrayList<>();
        for (Command command : commands) {
            int score = score(command, normalizedQuery, tokens);
            if (score > 0) {
                matches.add(new Scored(command, score));
            }
        }
        matches.sort((left, right) -> {
            int byScore = Integer.compare(right.score, left.score);
            return byScore != 0 ? byScore
                    : left.command.label().compareToIgnoreCase(right.command.label());
        });
        List<Command> result = new ArrayList<>();
        for (Scored scored : matches) {
            result.add(scored.command);
        }
        return result;
    }

    private static int score(Command command, String query, String[] tokens) {
        String label = normalize(command.label());
        String description = normalize(command.description());
        String keywords = normalize(String.join(" ", command.keywords()));
        int score = 0;
        if (label.equals(query)) {
            score += 100;
        } else if (label.startsWith(query)) {
            score += 60;
        } else if (label.contains(query)) {
            score += 40;
        }
        if (keywords.contains(query)) {
            score += 30;
        }
        if (description.contains(query)) {
            score += 10;
        }
        for (String token : tokens) {
            if (!label.contains(token) && !description.contains(token)
                    && !keywords.contains(token)) {
                return 0;
            }
            if (label.contains(token)) {
                score += 5;
            }
        }
        return score;
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        StringBuilder out = new StringBuilder();
        boolean lastSpace = false;
        for (int i = 0; i < decomposed.length(); i++) {
            char c = decomposed.charAt(i);
            if (Character.getType(c) == Character.NON_SPACING_MARK) {
                continue;
            }
            char lower = Character.toLowerCase(c);
            if (Character.isWhitespace(lower)) {
                if (!lastSpace && out.length() > 0) {
                    out.append(' ');
                }
                lastSpace = true;
            } else {
                out.append(lower);
                lastSpace = false;
            }
        }
        return out.toString().trim();
    }

    private record Scored(Command command, int score) {}
}
