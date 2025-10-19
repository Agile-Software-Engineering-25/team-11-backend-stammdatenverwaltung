package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMasterDataResponseDTO {
    private String userId;
    private String firstname;
    private String email;
    private Map<String, Object> attributes;
}
