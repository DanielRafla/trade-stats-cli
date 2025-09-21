package com.drafla.tradestats;

import java.util.Locale;
import java.util.Map;

public class ReportPrinter {

    public void print(TradeCalculator.Stats stats) {
        if (stats.warningsFromParser > 0) {
            System.err.printf(Locale.US, "WARNING: Skipped %d malformed row(s).%n", stats.warningsFromParser);
        }
        if (stats.warningsSellTrimmed > 0) {
            System.err.printf(Locale.US, "WARNING: %d SELL row(s) exceeded position and were trimmed.%n",
                    stats.warningsSellTrimmed);
        }
        System.out.printf(Locale.US, "Total P&L: %.2f%n", stats.totalRealizedPnl);
        System.out.printf(Locale.US, "Win rate: %.2f%%%n", stats.winRatePct);
        System.out.printf(Locale.US, "Max drawdown: %.2f%n", stats.maxDrawdown);
        System.out.println("By symbol:");
        for (Map.Entry<String, TradeCalculator.SymbolSummary> e : stats.bySymbol.entrySet()) {
            var s = e.getValue();
            System.out.printf(Locale.US, "  - %s: trades=%d, P&L=%.2f%n", s.symbol, s.tradeRows, s.realizedPnl);
        }
    }

    public void printUsage() {
        System.out.println("""
            Usage:
              java -jar trade-stats-cli.jar --file=path.csv [--symbol=SYM] [--since=YYYY-MM-DD]
            """);
    }
}
