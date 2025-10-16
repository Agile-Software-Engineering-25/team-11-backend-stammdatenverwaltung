package com.ase.stammdatenverwaltung.services;

import com.ase.stammdatenverwaltung.dto.UserFilterRequestDTO;
import com.ase.stammdatenverwaltung.dto.UserMasterDataResponseDTO;
import com.ase.stammdatenverwaltung.entities.Person;
import com.ase.stammdatenverwaltung.repositories.PersonRepository;
import com.ase.stammdatenverwaltung.services.filter.UserSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMasterDataServiceImpl implements UserMasterDataService {

    private final PersonRepository personRepository;
    private final UserSpecificationBuilder userSpecificationBuilder;

    @Override
    public List<UserMasterDataResponseDTO> getAllUsers(UserFilterRequestDTO filterRequest, boolean includeNameAndEmail) {
        // TODO: Add pagination or sorting support (later)
        Specification<Person> spec = userSpecificationBuilder.buildSpecification(filterRequest);
        List<Person> persons = personRepository.findAll(spec);

        return persons.stream()
                .map(person -> toResponseDTO(person, includeNameAndEmail))
                .collect(Collectors.toList());
    }

    @Override
    public UserMasterDataResponseDTO getUserById(Long userId, boolean includeNameAndEmail) {
        Person person = personRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId)); // Replace with proper exception
        return toResponseDTO(person, includeNameAndEmail);
    }

    private UserMasterDataResponseDTO toResponseDTO(Person person, boolean includeNameAndEmail) {
        UserMasterDataResponseDTO dto = new UserMasterDataResponseDTO();
        dto.setUserId(person.getId());

        // TODO: Replace mock Keycloak call with real HTTP request
        if (includeNameAndEmail) {
            dto.setFirstname("MockUser" + person.getId());
            dto.setEmail("mockuser" + person.getId() + "@example.com");
        }

        // TODO: Optimize joins or fetch strategies (later)
        dto.setAttributes(mergeAttributes(person));

        return dto;
    }

    private Map<String, Object> mergeAttributes(Person person) {
        Map<String, Object> attributes = new HashMap<>();
        addAttributes(attributes, person);
        return attributes;
    }

    private void addAttributes(Map<String, Object> attributes, Object obj) {
        if (obj == null) {
            return;
        }
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    // prevent overwriting id from Person
                    if (!attributes.containsKey(field.getName())) {
                        attributes.put(field.getName(), field.get(obj));
                    }
                } catch (IllegalAccessException e) {
                    // In a real app, log this error
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
