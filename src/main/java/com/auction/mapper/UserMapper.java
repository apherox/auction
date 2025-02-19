package com.auction.mapper;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.model.Role;
import com.auction.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToRoleNames")
    UserResponse toUserApiModel(User userEntity);

    @Named("rolesToRoleNames")
    default List<String> rolesToRoleNames(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        return roles.stream()
                .map(Role::getRoleName)
                .toList();
    }

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toUserEntity(UserRequest userRequest);
}


