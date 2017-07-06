package com.practice.settlement;

import static java.lang.Double.compare;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettlementsReportGenerator {

    private static Map<Date, Double> aggregateIncomingSettlementsInUsd = new HashMap<>();
    private static Map<Date, Double> aggregateOutgoingSettlementsInUsd = new HashMap<>();

    private static Map<String, Double> aggregateEntityIncomingSettlementsInUsd = new HashMap<>();
    private static Map<String, Double> aggregateEntityOutgoingSettlementsInUsd = new HashMap<>();

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    private static Calendar calendar = Calendar.getInstance();
    private static int incomingRaking = 0;
    private static int outgoingRanking = 0;

    public static void main(final String[] args) throws ParseException {
        try {
            System.out.println("Processing started");

            readData("SampleInstructions.log");
            printReport();

            System.out.println("Processing finished");
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void printReport() {
        aggregateIncomingSettlementsInUsd.keySet().stream().forEach((key) -> System.out.println(
                "Amount in USD settled incoming for:" + key + " is :" + aggregateIncomingSettlementsInUsd.get(key)));

        aggregateOutgoingSettlementsInUsd.keySet().stream().forEach((key) -> System.out.println(
                "Amount in USD settled outgoing for:" + key + " is :" + aggregateOutgoingSettlementsInUsd.get(key)));

        aggregateEntityIncomingSettlementsInUsd.entrySet().stream()
                .sorted((f1, f2) -> compare(f2.getValue(), f1.getValue()))
                .forEach((entrySet) -> System.out.println("Ranking of entities based on incoming amount: " + "rank "
                        + (++incomingRaking) + ", " + entrySet.getKey() + ", " + entrySet.getValue()));

        aggregateEntityOutgoingSettlementsInUsd.entrySet().stream()
                .sorted((f1, f2) -> compare(f2.getValue(), f1.getValue()))
                .forEach((entrySet) -> System.out.println("Ranking of entities based on outgoing amount: " + "rank "
                        + (++outgoingRanking) + ", " + entrySet.getKey() + ", " + entrySet.getValue()));
    }

    private static void readData(final String fileName) throws IOException, ParseException {
        final FileReader fileReader = new FileReader(fileName);
        final BufferedReader bufferedReader = new BufferedReader(fileReader);

        final List<String> allLines = bufferedReader.lines().skip(1).collect(Collectors.toList());
        allLines.stream().forEach((line) -> calculateSettlements(line));

        bufferedReader.close();
    }

    private static void calculateSettlements(final String line) {
        final String[] splits = line.split(",");

        try {
            final String entity = splits[0];
            final String tradeType = splits[1];
            final Double agreedFx = parseDouble(splits[2]);
            final String currency = splits[3];

            simpleDateFormat.applyPattern("dd MMM yyyy");
            final Date instructionDate = simpleDateFormat.parse(splits[4]);
            final Date settlementDate = simpleDateFormat.parse(splits[5]);

            final Integer units = parseInt(splits[6]);
            final Double pricePerUnit = parseDouble(splits[7]);

            aggregateSettlementsInUsd(entity, tradeType, currency, settlementDate, (agreedFx * pricePerUnit * units));
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            System.out.println("Skipping record");
            e.printStackTrace();
        }

    }

    private static void aggregateSettlementsInUsd(final String entity, final String tradeType, final String currency,
            Date settlementDate, final Double usdValue) {
        System.out.println("tradeType: " + tradeType + ", currency: " + currency + ", settlementDate: " + settlementDate
                + ", usdValue: " + usdValue);

        simpleDateFormat.applyPattern("u");

        final int dayInWeek = parseInt(simpleDateFormat.format(settlementDate));

        if (dayInWeek > 5 && !(currency.equals("AED") || currency.equals("SAR"))) {
            calendar.setTime(settlementDate);
            calendar.add(Calendar.DAY_OF_WEEK, (8 - dayInWeek));
            settlementDate = calendar.getTime();
            System.out.println("new SettlementDate for weekends trades: " + settlementDate);
        }

        if ((dayInWeek == 5 || dayInWeek == 6) && (currency.equals("AED") || currency.equals("SAR"))) {
            calendar.setTime(settlementDate);
            calendar.add(Calendar.DAY_OF_WEEK, (7 - dayInWeek));
            settlementDate = calendar.getTime();
            System.out.println("new SettlementDate for weekends trades in AED and SAR: " + settlementDate);
        }

        Double tmpDouble = 0d;

        if (tradeType.equalsIgnoreCase("S")) {

            tmpDouble = aggregateIncomingSettlementsInUsd.containsKey(settlementDate)
                    ? aggregateIncomingSettlementsInUsd.get(settlementDate) + usdValue : usdValue;

            aggregateIncomingSettlementsInUsd.put(settlementDate, tmpDouble);

            tmpDouble = aggregateEntityIncomingSettlementsInUsd.containsKey(entity)
                    ? aggregateEntityIncomingSettlementsInUsd.get(entity) + usdValue : usdValue;

            aggregateEntityIncomingSettlementsInUsd.put(entity, tmpDouble);
        }

        if (tradeType.equalsIgnoreCase("B")) {

            tmpDouble = aggregateOutgoingSettlementsInUsd.containsKey(settlementDate)
                    ? aggregateOutgoingSettlementsInUsd.get(settlementDate) + usdValue : usdValue;

            aggregateOutgoingSettlementsInUsd.put(settlementDate, tmpDouble);

            tmpDouble = aggregateEntityOutgoingSettlementsInUsd.containsKey(entity)
                    ? aggregateEntityOutgoingSettlementsInUsd.get(entity) + usdValue : usdValue;

            aggregateEntityOutgoingSettlementsInUsd.put(entity, tmpDouble);

        }

    }

}
