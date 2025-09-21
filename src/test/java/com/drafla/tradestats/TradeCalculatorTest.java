package com.drafla.tradestats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradeCalculatorTest {

    private static final double MONEY_EPS = 1e-6;
    private static final double DD_EPS    = 1e-9;

    private Trade mk(int day, String sym, double qty, double px, Trade.Side side) {
        return new Trade(LocalDate.of(2024, 1, day), sym, qty, px, side, day);
    }

    @Test
    @DisplayName("one buy then one sell → +200 P&L, 100% win, 0 drawdown")
    void oneWinningSell() {
        List<Trade> trades = List.of(
            mk(1, "AAPL", 10, 100.0, Trade.Side.BUY),
            mk(2, "AAPL", 10, 120.0, Trade.Side.SELL)
        );
        var s = new TradeCalculator().compute(trades, 0);
        assertEquals(200.0, s.totalRealizedPnl, MONEY_EPS);
        assertEquals(100.0, s.winRatePct,       MONEY_EPS);
        assertEquals(0.0,   s.maxDrawdown,      DD_EPS);
    }

    @Test
    @DisplayName("SELL more than position gets trimmed; warns once; -50 P&L")
    void sellingBeyondPositionGetsTrimmed() {
        List<Trade> trades = List.of(
            mk(1, "MSFT", 5, 100.0, Trade.Side.BUY),
            mk(2, "MSFT",10,  90.0, Trade.Side.SELL) // only 5 available
        );
        var s = new TradeCalculator().compute(trades, 0);
        assertEquals(-50.0, s.totalRealizedPnl, MONEY_EPS);
        assertEquals(0.0,   s.winRatePct,       MONEY_EPS);
        assertEquals(1,     s.warningsSellTrimmed);
        assertEquals(-50.0, s.maxDrawdown, DD_EPS);
    }

    @Test
    @DisplayName("win then loss → net +25; 50% win rate; drawdown -25")
    void mixedWinThenLoss() {
        List<Trade> trades = List.of(
            mk(1, "AAPL", 10, 100.0, Trade.Side.BUY),
            mk(2, "AAPL",  5, 110.0, Trade.Side.SELL), // +50
            mk(3, "AAPL",  5,  95.0, Trade.Side.SELL)  // -25
        );
        var s = new TradeCalculator().compute(trades, 0);
        assertEquals(25.0,  s.totalRealizedPnl, MONEY_EPS);
        assertEquals(50.0,  s.winRatePct,       MONEY_EPS);
        assertEquals(-25.0, s.maxDrawdown,      MONEY_EPS);
    }

    @Test
    @DisplayName("filters: only AAPL on/after 2024-01-02 remains")
    void filtersWork() {
        List<Trade> all = List.of(
            mk(1, "AAPL", 1, 100.0, Trade.Side.BUY),
            mk(2, "MSFT", 1, 200.0, Trade.Side.BUY),
            mk(3, "AAPL", 1, 110.0, Trade.Side.SELL)
        );
        var filtered = TradeCalculator.applyFilters(all, "AAPL", LocalDate.of(2024, 1, 2));
        assertEquals(1, filtered.size());
        assertEquals("AAPL", filtered.get(0).getSymbol());
        assertEquals(3, filtered.get(0).getDate().getDayOfMonth());
    }

    @Test
    @DisplayName("drawdown sanity")
    void drawdownSanity() {
        assertEquals(0.0, TradeCalculator.computeMaxDrawdown(List.of(0.0, 10.0, 20.0)), DD_EPS);
        assertEquals(-40.0, TradeCalculator.computeMaxDrawdown(List.of(0.0, 100.0, 60.0)), DD_EPS);
    }
}
