package com.auction.repository;

import com.auction.model.Auction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    @Override
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Auction> findById(Long id);

    @Query("SELECT a FROM Auction a WHERE a.expirationTime < :now AND a.status != 'CLOSED'")
    Page<Auction> findExpiredAuctions(LocalDateTime now, Pageable pageable);

}
