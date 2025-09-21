package com.drafla.tradestats;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class CsvParserTest {

    private Path temp(String content) throws IOException {
        Path p = Files.createTempFile("csv", ".csv");
        Files.writeString(p, content);
        return p;
    }

    @Test
    void goodFile() throws IOException {
        String c = "date,symbol,qty,price,side\n" +
                   "2024-01-01,AAPL,10,100,BUY\n" +
                   "2024-01-02,AAPL,10,110,SELL\n";
        var res = new CsvParser().parse(temp(c));
        assertEquals(2, res.getTrades().size());
        assertEquals(0, res.getWarnings());
    }

    @Test
    void malformedRowsCountAsWarnings() throws IOException {
        String c = "date,symbol,qty,price,side\n" +
                   "bad,MSFT,5,200,BUY\n" +
                   "2024-01-03,MSFT,5,210,SELL\n";
        var res = new CsvParser().parse(temp(c));
        assertEquals(1, res.getWarnings());
        assertEquals(1, res.getTrades().size());
    }

    @Test
    void negativeRejected() throws IOException {
        String c = "date,symbol,qty,price,side\n" +
                   "2024-01-01,AAPL,-1,100,BUY\n" +
                   "2024-01-02,AAPL,1,-100,SELL\n";
        var res = new CsvParser().parse(temp(c));
        assertEquals(2, res.getWarnings());
        assertEquals(0, res.getTrades().size());
    }
}
