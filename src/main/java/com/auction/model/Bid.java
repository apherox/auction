package com.auction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "bid")
@Data
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bid_seq")
    @SequenceGenerator(name = "bid_seq", sequenceName = "BID_ID_SEQ", allocationSize = 1)
    @Column(name = "bid_id")
    private Long bidId;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "bid_time", nullable = false, updatable = false)
    private LocalDateTime bidTime;

}
