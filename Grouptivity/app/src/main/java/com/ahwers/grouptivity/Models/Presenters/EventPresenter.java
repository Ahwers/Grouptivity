package com.ahwers.grouptivity.Models.Presenters;

import com.ahwers.grouptivity.Models.DataModels.Event;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventPresenter {

    public String formatDate(Event event) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);
        SimpleDateFormat timeFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.TIME_FORMAT);

        Date startDate;
        Date endDate;

        boolean flexDate = true;

        try {
            startDate = event.getOpenDate().getStartDate();
            endDate = event.getOpenDate().getEndDate();
        } catch (NullPointerException e) {
            flexDate = false;
            startDate = event.getStartDateTime();
            endDate = event.getEndDateTime();
        }

        try {
            startDate = dateFormat.parse(dateFormat.format(startDate));
            endDate = dateFormat.parse(dateFormat.format(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String dateOutput = "";

        if (flexDate) {
            dateOutput = dateOutput + "Between: ";
        }

        dateOutput = dateOutput + dateFormat.format(startDate) + " ";

        if (endDate.after(startDate)) {
            dateOutput = dateOutput + ("to " + dateFormat.format(endDate));
        }

        dateOutput = dateOutput + timeFormat.format(startDate) + " ";

        if (endDate.after(startDate)) {
            dateOutput = dateOutput + ("- " + timeFormat.format(endDate));
        }

        return dateOutput;
    }

}
