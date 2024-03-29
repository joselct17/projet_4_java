package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final String TICKET_NUMBER = "ABCDEF";
    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(TICKET_NUMBER);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {

    }

    //TEST QUI VERIFIE QUE LE TICKET EST SAUVEGARDE EN BASE DE DONNEES ET QUE LA TABLE EST MISE A JOUR AVEC DISPONIBILITE
    @Test
    public void testParkingACar() {
        int previousSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability

        // check that a ticket is actually saved in DB
        Ticket ticket = ticketDAO.getTicket(TICKET_NUMBER);
        assertNotNull(ticket);
        assertFalse(ticket.getParkingSpot().isAvailable());

        // check that Parking table is updated with availability
        int newSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertNotEquals(previousSlot, newSlot);
    }

    @Test
    //FONCTION QUE VERIFIE QUE LE TARIF GENERE ET L'HEURE DE SORTI SONT BIEN DANS LA BASE DE DONNEES
    public void testParkingLotExit() {

        testParkingACar();
        Ticket ticket = ticketDAO.getTicket(TICKET_NUMBER);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        //TODO: check that the fare generated and out time are populated correctly in the database

        //on cree une variable heure de sortie avec une heure.
        long outTime = ticket.getInTime().getTime() + 60 * 60 * 1000;
        parkingService.processExitingVehicle(new Date(outTime));
        ticket = ticketDAO.getTicket(TICKET_NUMBER);
        assertNotNull(ticket);

        // on verifie que l'heure de sortie de la voiture correspend a 1h
        assertEquals(outTime, ticket.getOutTime().getTime());

        //on verifie que le tarif correspend.
        assertEquals((Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());

    }

    //On teste si le discount est appliqué
    @Test
    public void testRecurringDiscount() {

        testParkingACar();
        testParkingLotExit();
        Ticket ticket = ticketDAO.getTicket(TICKET_NUMBER);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        //on cree une variable heure de sortie avec une heure.
        long outTime = ticket.getInTime().getTime() + 60 * 60 * 1000;
        parkingService.processExitingVehicle(new Date(outTime));
        ticket = ticketDAO.getTicket(TICKET_NUMBER);
        assertNotNull(ticket);

        // on verifie que l'heure de sortie de la voiture correspend a 1h
        assertEquals(outTime, ticket.getOutTime().getTime());

        //on verifie que le tarif correspend.
        assertEquals((Fare.CAR_RATE_PER_HOUR * Fare.RECURRING_DISCOUNT) , ticket.getPrice());


    }





}


