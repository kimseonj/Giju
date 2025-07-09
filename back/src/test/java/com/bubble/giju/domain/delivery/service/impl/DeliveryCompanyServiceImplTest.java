package com.bubble.giju.domain.delivery.service.impl;

import com.bubble.giju.domain.delivery.dto.DeliveryCompanyResponseDto;
import com.bubble.giju.domain.delivery.dto.DeliveryCompanyUpdateRequestDto;
import com.bubble.giju.domain.delivery.entity.DeliveryCompany;
import com.bubble.giju.domain.delivery.repository.DeliveryCompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryCompanyServiceImplTest {

    @Mock
    private DeliveryCompanyRepository deliveryCompanyRepository;

    @InjectMocks
    private DeliveryCompanyServiceImpl deliveryCompanyService;

    @DisplayName("택배 회사 전체 조회 성공 테스트")
    @Test
    void findAll_shouldReturnListOfDeliveryCompanyResponseDto() {
        // given
        DeliveryCompany cj = new DeliveryCompany("CJ대한통운");

        DeliveryCompany post = new DeliveryCompany("우체국택배");

        List<DeliveryCompany> mockCompanies = List.of(cj, post);
        when(deliveryCompanyRepository.findAll()).thenReturn(mockCompanies);

        // when
        var result = deliveryCompanyService.findAll();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CJ대한통운", result.get(0).getDeliveryCompanyName());
        assertEquals("우체국택배", result.get(1).getDeliveryCompanyName());
        verify(deliveryCompanyRepository).findAll();
    }

    @DisplayName("택배회사 정보 삭제 성공 테스트")
    @Test
    void deleteById() {
        //given
        DeliveryCompany deliveryCompany= new DeliveryCompany("우체국");
        ReflectionTestUtils.setField(deliveryCompany, "id", 1);
        //when
        when(deliveryCompanyRepository.existsById(1)).thenReturn(true);
        when(deliveryCompanyRepository.findById(1)).thenReturn(Optional.of(deliveryCompany));
        DeliveryCompanyResponseDto deliveryCompanyResponseDto = deliveryCompanyService.deleteById(1);
        //then
        verify(deliveryCompanyRepository).delete(deliveryCompany);
        assertNotNull(deliveryCompanyResponseDto);
        assertEquals(deliveryCompanyResponseDto.getDeliveryCompanyId(), deliveryCompany.getId());
        assertEquals(deliveryCompanyResponseDto.getDeliveryCompanyName(), deliveryCompany.getName());



    }

    @DisplayName("택배회사 정보 수정 성공 테스트")
    @Test
    void update() {
        // given
        int deliveryCompanyId = 1;
        DeliveryCompany originalCompany=new DeliveryCompany("CJ대한통운");

        ReflectionTestUtils.setField(originalCompany, "id", deliveryCompanyId);

        String newName= "로젠택배";
        DeliveryCompanyUpdateRequestDto requestDto = new DeliveryCompanyUpdateRequestDto();
        requestDto.setDeliveryCompanyName(newName);

        when(deliveryCompanyRepository.findById(deliveryCompanyId)).thenReturn(Optional.of(originalCompany));
        when(deliveryCompanyRepository.save(originalCompany)).thenReturn(originalCompany);

        // when
        var result= deliveryCompanyService.update(deliveryCompanyId,requestDto);

        //then
        assertNotNull(result);
        assertEquals(deliveryCompanyId,result.getDeliveryCompanyId());
        assertEquals(newName,result.getDeliveryCompanyName());

        verify(deliveryCompanyRepository).findById(deliveryCompanyId);
        verify(deliveryCompanyRepository).save(originalCompany);

    }
}