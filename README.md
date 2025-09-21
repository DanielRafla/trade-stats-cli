# trade-stats-cli (Beginner Edition)

![Build](https://github.com/DanielRafla/trade-stats-cli/actions/workflows/ci.yml/badge.svg)

Java 17 CLI that reads a CSV of trades and prints:
- Total realized P&L (long-only, FIFO)
- Win rate (% SELL rows with positive realized P&L)
- Max drawdown (over cumulative realized P&L)
- Per-symbol summary (row count, realized P&L)

## Build / Run / Test
```bash
mvn clean package
java -jar target/trade-stats-cli-1.0.1-beginner.jar --file=sample_trades.csv
java -jar target/trade-stats-cli-1.0.1-beginner.jar --file=sample_trades.csv --symbol=AAPL --since=2024-01-01
mvn -q test

