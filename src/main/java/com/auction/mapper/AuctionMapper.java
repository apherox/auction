package com.auction.mapper;

import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.auction.AuctionStatusResponse;
import com.auction.api.model.auction.AuctionUpdateRequest;
import com.auction.model.Auction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AuctionMapper {

    AuctionMapper INSTANCE = Mappers.getMapper(AuctionMapper.class);

    @Mapping(source = "highestBidUser.username", target = "highestBidUsername")
    AuctionResponse toAuctionApiModel(Auction auctionEntity);

    AuctionStatusResponse toAuctionStatusApiModel(Auction auction);

    @Mapping(target = "highestBidUser", ignore = true)
    @Mapping(target = "auctionId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "highestBid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Auction toAuctionEntity(AuctionRequest auctionRequest);

    @Mapping(target = "highestBidUser", ignore = true)
    @Mapping(target = "auctionId", ignore = true)
    @Mapping(target = "highestBid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Auction toAuctionEntity(AuctionUpdateRequest auctionUpdateRequest);
}
