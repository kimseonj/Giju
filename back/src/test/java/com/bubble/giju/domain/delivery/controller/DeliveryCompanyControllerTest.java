package com.bubble.giju.domain.delivery.controller;

import com.bubble.giju.domain.delivery.dto.DeliveryCompanyResponseDto;
import com.bubble.giju.domain.delivery.dto.DeliveryCompanyUpdateRequestDto;
import com.bubble.giju.domain.delivery.service.DeliveryCompanyService;
import com.bubble.giju.global.config.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeliveryCompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeliveryCompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryCompanyService deliveryCompanyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/delivery-companies - 택배회사 전체 조회")
    void getAll() throws Exception {
        // given
        DeliveryCompanyResponseDto dto = new DeliveryCompanyResponseDto(1, "CJ대한통운");
        when(deliveryCompanyService.findAll()).thenReturn(List.of(dto));

        // when & then
        mockMvc.perform(get("/api/delivery-companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deliveryCompanyId").value(1))
                .andExpect(jsonPath("$[0].deliveryCompanyName").value("CJ대한통운"));
    }

    @Test
    @DisplayName("DELETE /api/admin/delivery-company/{id} - 택배회사 삭제")
    void deleteById() throws Exception {
        // given
        int id = 1;
        DeliveryCompanyResponseDto dto = new DeliveryCompanyResponseDto(id, "우체국택배");
        when(deliveryCompanyService.deleteById(id)).thenReturn(dto);

        // when & then
        mockMvc.perform(delete("/api/admin/delivery-company/{delivery-company-id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("택배회사 삭제에 성공했습니다."))
                .andExpect(jsonPath("$.data.deliveryCompanyId").value(id))
                .andExpect(jsonPath("$.data.deliveryCompanyName").value("우체국택배"));
    }

    @Test
    @DisplayName("PUT /api/admin/delivery-company/{id} - 택배회사 수정")
    void updateById() throws Exception {
        // given
        int id = 1;
        DeliveryCompanyUpdateRequestDto requestDto = new DeliveryCompanyUpdateRequestDto();
        requestDto.setDeliveryCompanyName("로젠택배");

        DeliveryCompanyResponseDto responseDto = new DeliveryCompanyResponseDto(id, "로젠택배");

        when(deliveryCompanyService.update(Mockito.eq(id), Mockito.any())).thenReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/admin/delivery-company/{delivery-company-id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("택배회사 수정에 성공했습니다."))
                .andExpect(jsonPath("$.data.deliveryCompanyId").value(id))
                .andExpect(jsonPath("$.data.deliveryCompanyName").value("로젠택배"));
    }
}
