package com.auction.scheduler;

import com.auction.model.Auction;
import com.auction.repository.AuctionRepository;
import com.auction.service.AbstractServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuctionSchedulerTest extends AbstractServiceTest {

    @Autowired
    private AuctionScheduler auctionScheduler;

    @Autowired
    private AuctionRepository auctionRepository;

    @BeforeEach
    void setUp() {
        auctionRepository.deleteAll();

        // Create test auctions (one expired and one active)

        // expired
        Auction auction1 = new Auction();
        auction1.setTitle("Golden Watch");
        auction1.setDescription("A beautiful golden watch");
        auction1.setStatus("OPEN");
        auction1.setExpirationTime(LocalDateTime.now().minusDays(1));
        auction1.setStartingPrice(450.00);
        auctionRepository.save(auction1);

        // non-expired
        Auction auction2 = new Auction();
        auction2.setTitle("Carved Statue");
        auction2.setDescription("A carved statue from a beautiful wooden tree");
        auction2.setStatus("OPEN");
        auction2.setExpirationTime(LocalDateTime.now().plusDays(1));
        auction2.setStartingPrice(300.00);
        auctionRepository.save(auction2);
    }

    @Test
    void testCloseExpiredAuctions() {
        // Trigger the scheduled method
        auctionScheduler.closeExpiredAuctions();

        List<Auction> auctions = auctionRepository.findAll();
        long closedAuctions = auctions.stream().filter(a -> "CLOSED".equals(a.getStatus())).count();
        assertEquals(1, closedAuctions, "Only 1 auction should be closed");

        long openAuctions = auctions.stream().filter(a -> "OPEN".equals(a.getStatus())).count();
        assertEquals(1, openAuctions, "Only 1 auction should remain open");
    }
}