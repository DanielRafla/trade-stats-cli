package com.drafla.tradestats;

import java.time.LocalDate;
import java.util.*;

/**
 * Beginner edition: long-only FIFO.
 * - SELL qty capped to current position; excess increments a warning counter.
 * - Each SELL row is one realized trade for win-rate.
 * - Max drawdown over cumulative realized P&L.
 */
public class TradeCalculator {

    public static final class SymbolSummary {
        public final String symbol;
        public int tradeRows;
        public double realizedPnl;
        public SymbolSummary(String symbol) { this.symbol = symbol; }
    }

    public static final class Stats {
        public double totalRealizedPnl;
        public double winRatePct;         // 0..100
        public double maxDrawdown;        // <= 0
        public Map<String, SymbolSummary> bySymbol = new TreeMap<>();
        public int warningsFromParser;    // bad CSV rows
        public int warningsSellTrimmed;   // SELL > position trimmed
    }

    private static final class Lot {
        double qty;
        double price;
        Lot(double qty, double price) { this.qty = qty; this.price = price; }
    }

    public Stats compute(List<Trade> trades, int parserWarnings) {
        List<Trade> ordered = new ArrayList<>(trades);
        Collections.sort(ordered);

        Map<String, Deque<Lot>> lotsBySymbol = new HashMap<>();
        Map<String, SymbolSummary> summaries = new TreeMap<>();

        double totalRealized = 0.0;
        int sellRows = 0, sellWins = 0;
        int trimmed = 0;

        List<Double> cum = new ArrayList<>();
        cum.add(0.0);

        for (Trade t : ordered) {
            var lots = lotsBySymbol.computeIfAbsent(t.getSymbol(), k -> new ArrayDeque<>());
            var sum  = summaries.computeIfAbsent(t.getSymbol(), SymbolSummary::new);
            sum.tradeRows++;

            if (t.getSide() == Trade.Side.BUY) {
                lots.addLast(new Lot(t.getQty(), t.getPrice()));
            } else {
                double held = lots.stream().mapToDouble(l -> l.qty).sum();
                double toSell = Math.min(held, t.getQty());
                if (toSell < t.getQty() - 1e-12) trimmed++;

                double pnlRow = 0.0;
                double remaining = toSell;
                while (remaining > 1e-12 && !lots.isEmpty()) {
                    Lot l = lots.peekFirst();
                    double closeQty = Math.min(remaining, l.qty);
                    pnlRow += (t.getPrice() - l.price) * closeQty;
                    l.qty -= closeQty;
                    remaining -= closeQty;
                    if (l.qty <= 1e-12) lots.removeFirst();
                }

                totalRealized += pnlRow;
                sum.realizedPnl += pnlRow;
                sellRows++;
                if (pnlRow > 1e-12) sellWins++;

                cum.add(totalRealized);
            }
        }

        Stats s = new Stats();
        s.totalRealizedPnl = totalRealized;
        s.bySymbol = summaries;
        s.warningsFromParser = parserWarnings;
        s.warningsSellTrimmed = trimmed;
        s.winRatePct = sellRows == 0 ? 0.0 : (sellWins * 100.0 / sellRows);
        s.maxDrawdown = computeMaxDrawdown(cum);
        return s;
    }

    /** Running-peak drawdown over cumulative P&L series. */
    static double computeMaxDrawdown(List<Double> curve) {
        double peak = Double.NEGATIVE_INFINITY;
        double maxDd = 0.0;
        for (double v : curve) {
            if (v > peak) peak = v;
            else {
                double dd = v - peak; // <= 0
                if (dd < maxDd) maxDd = dd;
            }
        }
        return maxDd;
    }

    /** Apply optional filters. */
    public static List<Trade> applyFilters(List<Trade> trades, String symbol, LocalDate sinceInclusive) {
        List<Trade> out = new ArrayList<>();
        for (Trade t : trades) {
            if (symbol != null && !symbol.equalsIgnoreCase(t.getSymbol())) continue;
            if (sinceInclusive != null && t.getDate().isBefore(sinceInclusive)) continue;
            out.add(t);
        }
        return out;
    }
}
