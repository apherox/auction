package com.auction.mapper;

import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import com.auction.model.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BidMapper {

    BidMapper INSTANCE = Mappers.getMapper(BidMapper.class);

    @Mapping(source = "auction.auctionId", target = "auctionId")
    @Mapping(source = "auction.title", target = "auctionTitle")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.userId", target = "userId")
    BidResponse toBidApiModel(Bid bidEntity);

    @Mapping(target = "auction", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bidId", ignore = true)
    @Mapping(target = "bidTime", ignore = true)
    Bid toBidEntity(BidRequest bidRequest);
}
