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
    public void resetSequence(String sequenceName, int value) {
        String sql = String.format("ALTER SEQUENCE %s RESTART WITH %d", sequenceName, value);
        Query query = entityManager.createNativeQuery(sql);
        query.executeUpdate();
    }

    @Transactional
    public void resetUserSequence() {
        resetSequence("USER_ID_SEQ", 10);
    }

    @Transactional
    public void resetAuctionSequence() {
       resetSequence("AUCTION_ID_SEQ", 10);
    }

    @Transactional
    public void resetBidSequence() {
        resetSequence("BID_ID_SEQ", 10);
    }
}
