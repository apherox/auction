package com.auction.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SequenceService {

    private final EntityManager entityManager;

    @Transactional
    public void resetUserSequence() {
        this.resetSequence("USER_ID_SEQ", 10);
    }

    @Transactional
    public void resetAuctionSequence() {
       this.resetSequence("AUCTION_ID_SEQ", 10);
    }

    @Transactional
    public void resetBidSequence() {
        this.resetSequence("BID_ID_SEQ", 10);
    }

    private void resetSequence(String sequenceName, int value) {
        String sql = String.format("ALTER SEQUENCE %s RESTART WITH %d", sequenceName, value);
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }
}
