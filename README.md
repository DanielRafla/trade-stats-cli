# Trade-Stats-Cli

![Build](https://github.com/DanielRafla/trade-stats-cli/actions/workflows/ci.yml/badge.svg)

**Description:** A lightweight Java 17 CLI that ingests a `date,symbol,qty,price,side` CSV of trade fills and instantly reports realized trading performance: **total P&L (FIFO, long-only)**, **win rate**, **max drawdown**, and **per-symbol** stats. Designed to be a small, testable starter project with no external CSV libs, with clear errors, CI, and a single runnable JAR. Great for quick P&L sanity checks from broker exports or journaling trades.

**Goal:** Given a trades CSV, compute **deterministic realized P&L** and risk at a glance:
- Total realized P&L using per-symbol FIFO lots
- Win rate (% profitable SELL rows)
- Max drawdown over cumulative realized P&L
- Per-symbol trade counts and P&L  
Extras: `--symbol` and `--since` filters, oversell **trim** warnings, and unit tests for parser/calculator.

## Build / Run / Test
```bash
mvn clean package
java -jar target/trade-stats-cli-1.0.1-beginner.jar --file=sample_trades.csv
java -jar target/trade-stats-cli-1.0.1-beginner.jar --file=sample_trades.csv --symbol=AAPL --since=2024-01-01
mvn -q test
```
## Sample Output

Total P&L: 415.00
Win rate: 100.00%
Max drawdown: 0.00
By symbol:
  AAPL: trades=6, P&L=250.00
  MSFT: trades=5, P&L=165.00
## Input format For CSV

**Required header (exact):**
date,symbol,qty,price,side

**Field rules**
- `date`: `YYYY-MM-DD` (e.g., `2024-01-15`)
- `symbol`: non-empty string (e.g., `AAPL`)
- `qty`: positive number
- `price`: positive number
- `side`: `BUY` or `SELL`

**Example rows**

2024-01-02,AAPL,10,100,BUY

2024-01-07,AAPL,5,110,SELL

2024-01-09,MSFT,5,320,SELL
