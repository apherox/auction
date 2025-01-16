package com.auction.mapper;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.stream.Collectors;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "removeRolePrefix")
    UserResponse toUserApiModel(User userEntity);

    @Named("removeRolePrefix")
    default String removeRolePrefix(String roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }

        return Arrays.stream(roles.split(","))
                .map(role -> role.replace("ROLE_", "").trim())
                .collect(Collectors.joining(","));
    }

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "convertToRolePrefix")
    User toUserEntity(UserRequest userRequest);

    @Named("convertToRolePrefix")
    default String convertToRolePrefix(String roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }

        return Arrays.stream(roles.split(","))
                .map(role -> "ROLE_" + role.trim())
                .collect(Collectors.joining(","));
    }
}
