package com.drafla.tradestats;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class CsvParser {

    public static final class Result {
        private final List<Trade> trades;
        private final int warnings;
        public Result(List<Trade> trades, int warnings) { this.trades = trades; this.warnings = warnings; }
        public List<Trade> getTrades() { return trades; }
        public int getWarnings() { return warnings; }
    }

    /** Parse CSV with header: date,symbol,qty,price,side */
    public Result parse(Path file) throws IOException {
        if (file == null || !Files.isRegularFile(file)) throw new NoSuchFileException(String.valueOf(file));

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return new Result(List.of(), 0);

        String header = lines.get(0).trim();
        if (!header.equalsIgnoreCase("date,symbol,qty,price,side")) {
            throw new IllegalArgumentException("Invalid header. Expected: date,symbol,qty,price,side");
        }

        List<Trade> out = new ArrayList<>();
        int warnings = 0;

        for (int i = 1; i < lines.size(); i++) {
            String raw = lines.get(i).trim();
            if (raw.isEmpty()) continue;
            String[] p = raw.split(",", -1);
            if (p.length != 5) { warnings++; continue; }

            try {
                var date = LocalDate.parse(p[0].trim());
                var sym  = p[1].trim();
                double qty = Double.parseDouble(p[2].trim());
                double price = Double.parseDouble(p[3].trim());
                var side = Trade.Side.valueOf(p[4].trim().toUpperCase(Locale.ROOT));
                if (sym.isEmpty() || qty <= 0 || price <= 0) { warnings++; continue; }
                out.add(new Trade(date, sym, qty, price, side, i));
            } catch (Exception e) {
                warnings++;
            }
        }
        return new Result(out, warnings);
    }
}
