package ARS.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Hash {
    private static class Entry {
        String type;
        File file;

        Entry(String type, File file) {
            this.type = type;
            this.file = file;
        }
    }

    private List<Entry> entries;

    public Hash() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(String type, File file) {
        entries.add(new Entry(type, file));
    }

    public void removeEntry(File file) {
        entries.removeIf(e -> e.file.equals(file));
    }

    public void printEntries() {
        for (Entry e : entries) {
            System.out.println("Type: " + e.type + ", File: " + e.file.getAbsolutePath());
        }
    }
}
