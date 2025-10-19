package com.ase.stammdatenverwaltung.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMasterDataResponseDTO {
  private String userId;
  private String firstname;
  private String email;
  private Map<String, Object> attributes;
}
