# Trade-Stats-Cli

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

## Sample Output

Total P&L: 415.00
Win rate: 100.00%
Max drawdown: 0.00
By symbol:
  - AAPL: trades=6, P&L=250.00
  - MSFT: trades=5, P&L=165.00
