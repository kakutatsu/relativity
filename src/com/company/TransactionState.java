package com.company;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class TransactionState {

    public final TransactionState previousState;
    public final TransactionRow trade;
    public final Double cash;
    public final Double ttl;
    public final HashMap<String, Double> position;
    private static DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static DateTimeFormatter yearMonthFormat = DateTimeFormatter.ofPattern("yyyyMM");
    public final LocalDate date;
    public final Double accumulatedTransfer;

    /***
     * This constructor is for initial state
     *
     * @param date
     * @param cash
     * @param ttl
     * @param position
     */
    public TransactionState(LocalDate date, Double cash, Double ttl, HashMap<String, Double> position) {
        this.date = date;
        this.previousState = null;
        this.trade = null;
        this.cash = cash;
        this.ttl = ttl;
        this.position = new HashMap<>(position);
        this.accumulatedTransfer = 0.0;
    }

    public TransactionState(TransactionState previousState, TransactionRow trade, Double cash, Double ttl, HashMap<String, Double> position, Double accumulatedTransfer) {
        this.accumulatedTransfer = accumulatedTransfer;
        this.date = trade.transactionDate;
        this.previousState = previousState;
        this.trade = trade;
        this.cash = cash;
        this.ttl = ttl;
        this.position = new HashMap<>(position);
    }

    @Override
    public String toString() {
        return String.format("%1$s-Cash:%2$.2f,TTL:%3$.2f,%4$s.         Current Trade: %5$s",
                format.format(date), cash, ttl, position.toString(), trade);
    }

    public String yearMonth() {
        return yearMonthFormat.format(date);
    }
}
