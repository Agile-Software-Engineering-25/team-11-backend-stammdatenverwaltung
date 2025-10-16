package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.dto.UserFilterRequestDTO;
import com.ase.stammdatenverwaltung.dto.UserMasterDataResponseDTO;

import java.util.List;

public interface UserMasterDataService {
    List<UserMasterDataResponseDTO> getAllUsers(UserFilterRequestDTO filterRequest, boolean includeNameAndEmail);

    UserMasterDataResponseDTO getUserById(Long userId, boolean includeNameAndEmail);
}
