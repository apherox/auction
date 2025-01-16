package com.auction.scheduler;

import com.auction.model.Auction;
import com.auction.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class AuctionScheduler {

    private final AuctionRepository auctionRepository;

    /**
     * Cron scheduler runs every 5 minutes and processes 100 auctions at a time,
     * as the number of expired auctions can be large
     */
    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 100);

        Page<Auction> expiredAuctionsPage;
        do {
            expiredAuctionsPage = auctionRepository.findExpiredAuctions(now, pageable);
            expiredAuctionsPage.getContent().forEach(auction -> {
                auction.setStatus("CLOSED");
                auctionRepository.save(auction);
            });
            pageable = pageable.next();
        } while (expiredAuctionsPage.hasNext());
    }
}
