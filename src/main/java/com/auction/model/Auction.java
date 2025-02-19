package com.auction.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "auction")
@Data
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_seq")
    @SequenceGenerator(name = "auction_seq", sequenceName = "AUCTION_ID_SEQ", allocationSize = 1)
    @Column(name = "auction_id")
    private Long auctionId;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "starting_price", nullable = false)
    private Double startingPrice;

    @Column(name = "expiration_time", nullable = false)
    private LocalDateTime expirationTime;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "highest_bid")
    private Double highestBid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bid_user_id")
    @JsonIgnore
    private User highestBidUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = AuctionStatus.OPEN.name();
        }
    }
}
