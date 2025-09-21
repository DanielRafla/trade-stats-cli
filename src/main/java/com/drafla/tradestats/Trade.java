package com.drafla.tradestats;

import java.time.LocalDate;

public final class Trade implements Comparable<Trade> {
    public enum Side { BUY, SELL }

    private final LocalDate date;
    private final String symbol;
    private final double qty;
    private final double price;
    private final Side side;
    private final int sourceIndex; // order within file for stable sorting

    public Trade(LocalDate date, String symbol, double qty, double price, Side side, int sourceIndex) {
        this.date = date;
        this.symbol = symbol;
        this.qty = qty;
        this.price = price;
        this.side = side;
        this.sourceIndex = sourceIndex;
    }

    public LocalDate getDate() { return date; }
    public String getSymbol() { return symbol; }
    public double getQty() { return qty; }
    public double getPrice() { return price; }
    public Side getSide() { return side; }
    public int getSourceIndex() { return sourceIndex; }

    @Override
    public int compareTo(Trade o) {
        int d = this.date.compareTo(o.date);
        return (d != 0) ? d : Integer.compare(this.sourceIndex, o.sourceIndex);
    }
}
