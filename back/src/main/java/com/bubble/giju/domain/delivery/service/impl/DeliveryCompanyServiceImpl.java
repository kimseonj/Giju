package com.bubble.giju.domain.delivery.service.impl;

import com.bubble.giju.domain.delivery.dto.DeliveryCompanyResponseDto;
import com.bubble.giju.domain.delivery.dto.DeliveryCompanyUpdateRequestDto;
import com.bubble.giju.domain.delivery.entity.DeliveryCompany;
import com.bubble.giju.domain.delivery.repository.DeliveryCompanyRepository;
import com.bubble.giju.domain.delivery.service.DeliveryCompanyService;
import com.bubble.giju.global.config.CustomException;
import com.bubble.giju.global.config.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryCompanyServiceImpl implements DeliveryCompanyService {

    private final DeliveryCompanyRepository deliveryCompanyRepository;

    /*
    * 택배회사 리스트(전체)를 가져오는 메서드
    * */
    @Override
    public List<DeliveryCompanyResponseDto> findAll() {
        List<DeliveryCompany> deliveryCompanies = deliveryCompanyRepository.findAll();

        List<DeliveryCompanyResponseDto> deliveryCompanyResponseDtoList = new ArrayList<>();
        for(DeliveryCompany deliveryCompany : deliveryCompanies){
            DeliveryCompanyResponseDto deliveryCompanyResponseDto = new DeliveryCompanyResponseDto(deliveryCompany.getId(),deliveryCompany.getName());
            deliveryCompanyResponseDtoList.add(deliveryCompanyResponseDto);
        }

        return deliveryCompanyResponseDtoList;
    }

    /*
     * 택배회사 삭제하는 메서드
     * */
    @Override
    public DeliveryCompanyResponseDto deleteById(int deliveryCompanyId) {
        if(!deliveryCompanyRepository.existsById(deliveryCompanyId)){
            throw new CustomException(ErrorCode.NON_EXISTENT_DELIVERY_COMPANY);
        }
        DeliveryCompany deliveryCompany = deliveryCompanyRepository.findById(deliveryCompanyId).get();
        deliveryCompanyRepository.delete(deliveryCompany);
        DeliveryCompanyResponseDto deliveryCompanyResponseDto = new DeliveryCompanyResponseDto(deliveryCompany.getId(),deliveryCompany.getName());
        return deliveryCompanyResponseDto;
    }

    /*
     * 택배회사 정보를 수정하는 메서드
     * */
    @Override
    public DeliveryCompanyResponseDto update(int deliveryCompanyId, DeliveryCompanyUpdateRequestDto deliveryCompanyUpdateRequestDto) {
        DeliveryCompany deliveryCompany = deliveryCompanyRepository.findById(deliveryCompanyId).orElseThrow(()-> new CustomException(ErrorCode.NON_EXISTENT_DELIVERY_COMPANY));

        deliveryCompany.modifyName(deliveryCompanyUpdateRequestDto.getDeliveryCompanyName());

        deliveryCompanyRepository.save(deliveryCompany);
        DeliveryCompanyResponseDto deliveryCompanyResponseDto = new DeliveryCompanyResponseDto(deliveryCompanyId,deliveryCompany.getName());

        return deliveryCompanyResponseDto;
    }


}
