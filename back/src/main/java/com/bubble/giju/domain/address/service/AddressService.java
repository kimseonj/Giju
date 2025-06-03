package com.bubble.giju.domain.address.service;

import com.bubble.giju.domain.address.dto.AddressDto;

import java.util.List;

public interface AddressService {
    AddressDto.Response createAddress(String userId, AddressDto.Request request);
    List<AddressDto.Response> getAddress(String userId);
    AddressDto.Response updateAddress(String userId, Long addressId, AddressDto.Request request);
    Long deleteAddress(String userId, Long addressId);
    AddressDto.Response getDefaultAddress(String userId);
}
