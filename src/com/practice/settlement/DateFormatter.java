package com.practice.settlement;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateFormatter {

    public static void main(final String args[]) throws ParseException {
        final String dateString = "06 Jul 2017";
        System.out.println("Create date from: " + dateString);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");

        System.out.println("Date is: " + simpleDateFormat.parse(dateString));
    }

}
