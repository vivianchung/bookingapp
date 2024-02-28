package com.bookingapp;

import com.bookingapp.dto.BookingRequest;
import com.bookingapp.model.Booking;
import io.muserver.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingApp {
    private static final Logger log = LoggerFactory.getLogger(BookingApp.class);
    private static List<Booking> bookings = new ArrayList<>();

    public static void main(String[] args) {
        try {
            log.info("Starting mu-server-booking app");
            MuServer server = MuServerBuilder.httpServer()
                    .withHttpPort(8080)
                    .addHandler(new RequestLoggingHandler())
                    .addHandler(Method.POST, "/booking", (request, response, pathParams) -> {
                        createBooking(request, response);
                    })
                    .addHandler(Method.GET, "/bookings", (request, response, pathParams) -> {
                        listBooking(request, response);
                    })
                    .start();

            log.info("Server started at " + server.httpUri() + " and " + server.httpsUri());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down...");
                server.stop();
                log.info("Shut down complete.");
            }));
        }catch (Exception e){
            log.error(e.getMessage());
        }

    }

    public static void listBooking(MuRequest request, MuResponse response) {
        String dateString = request.query().get("date");
        if (dateString != null) {
            LocalDate date = null;
            try {
                date = LocalDate.parse(dateString);
            } catch (Exception e) {
                throw new ClientErrorException("Invalid date parameter", 400);
            }
            if (date == null) {
                throw new ClientErrorException("Invalid date parameter", 400);
            }
            LocalDate finalDate = date;
            List<Booking> bookingsForDate = bookings.stream().filter(booking -> booking.getStartDateTime().toLocalDate().equals(finalDate)).collect(Collectors.toList());
            Map<String, List> map = new HashMap<>();
            map.put("bookings", bookingsForDate.stream()
                    .map(Booking::toJson)
                    .collect(Collectors.toList()));
            response.write(new JSONObject(map).toString());
        } else {
            throw new ClientErrorException("Invalid date parameter", 400);
        }
    }

    //assume there are total 10 table size in the restaurant, every booking will last for 2 hours
    //booking must need to start in 9am and last booking cannot later than 9pm
    public static void createBooking(MuRequest request, MuResponse response) throws Exception {
        try {
            BookingRequest bookingRequest = parseBookingRequest(request);
            if (bookingRequest != null) {
                if (!isBetween(bookingRequest.getDateTime().toLocalTime(), LocalTime.parse("09:00"), LocalTime.parse("19:00"))) {
                    throw new ClientErrorException("Booking time must be within the 9am to 9pm", 400);
                }
                int tablesBooked = getTablesBooked(bookingRequest.getDateTime());
                if (tablesBooked + bookingRequest.getTableSize() > 10) {
                    throw new ClientErrorException("Maximum number of tables booked at this time. Please choose a different time.", 400);
                }

                Booking booking = new Booking(bookingRequest.getCustomerName(), bookingRequest.getTableSize(), bookingRequest.getDateTime());
                bookings.add(booking);
                response.write("Booking created successfully");
            } else {
                throw new ClientErrorException("Invalid booking request", 400);
            }
        } catch (Exception e) {
            throw new ClientErrorException(e.getMessage(), 400);
        }
    }

    private static int getTablesBooked(LocalDateTime dateTime) {
        int tablesBooked = 0;
        for (Booking booking : bookings) {
            if (isBookingOverlap(dateTime, dateTime.plusHours(2), booking.getStartDateTime(), booking.getEndDateTime())) {
                tablesBooked += booking.getTableSize();
            }
        }
        return tablesBooked;
    }

    private static boolean isBookingOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private static <C extends Comparable<? super C>> boolean isBetween(C value, C start, C end) {
        return value.compareTo(start) >= 0 && value.compareTo(end) < 0;
    }

    private static BookingRequest parseBookingRequest(MuRequest request) {
        try {
            String body = request.readBodyAsString();
            log.info(body);
            JSONObject json = new JSONObject(body);
            String customerName = json.getString("customerName");
            int tableSize = json.getInt("tableSize");
            LocalDateTime dateTime = LocalDateTime.parse(json.getString("dateTime"));
            return new BookingRequest(customerName, tableSize, dateTime);
        } catch (Exception e) {
            return null;
        }
    }

    private static class RequestLoggingHandler implements MuHandler {
        public boolean handle(MuRequest request, MuResponse response) throws IOException {
            log.info(request.method() + " " + request.uri() + " " );
            return false;
        }
    }
}
