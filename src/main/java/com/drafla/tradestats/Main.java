package com.drafla.tradestats;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        int code = new Main().run(args);
        if (code != 0) System.exit(code);
    }

    int run(String[] args) {
        Map<String, String> flags;
        try {
            flags = parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
            new ReportPrinter().printUsage();
            return 2;
        }

        String file = flags.get("file");
        if (file == null || file.isBlank()) {
            System.err.println("ERROR: --file is required.");
            new ReportPrinter().printUsage();
            return 2;
        }

        String symbol = flags.get("symbol");
        LocalDate since = null;
        if (flags.containsKey("since")) {
            try { since = LocalDate.parse(flags.get("since")); }
            catch (Exception e) { System.err.println("ERROR: Invalid --since date."); return 2; }
        }

        CsvParser.Result parsed;
        try {
            parsed = new CsvParser().parse(Path.of(file));
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return 1;
        }

        var filtered = TradeCalculator.applyFilters(parsed.getTrades(), symbol, since);
        var stats = new TradeCalculator().compute(filtered, parsed.getWarnings());
        new ReportPrinter().print(stats);
        return 0;
    }

    /** Parse flags of form --key=value; allow only file/symbol/since. */
    static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String a : args) {
            if (!a.startsWith("--") || !a.contains("=")) {
                throw new IllegalArgumentException("Unknown or malformed flag: " + a);
            }
            int idx = a.indexOf('=');
            String key = a.substring(2, idx).trim();
            String val = a.substring(idx + 1).trim();
            switch (key) {
                case "file", "symbol", "since" -> map.put(key, val);
                default -> throw new IllegalArgumentException("Unknown flag: --" + key);
            }
        }
        return map;
    }
}
