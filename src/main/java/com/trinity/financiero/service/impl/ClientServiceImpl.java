package com.trinity.financiero.service.impl;

import com.trinity.financiero.dto.ClientDTO;
import com.trinity.financiero.dto.ClientResponseDTO;
import com.trinity.financiero.entity.Client;
import com.trinity.financiero.repository.AccountRepository;
import com.trinity.financiero.repository.ClientRepository;
import com.trinity.financiero.service.ClientService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public ClientResponseDTO createClient(ClientDTO dto) {
        validateAge(dto.getBirthDate());
        validateUniqueIdentification(dto.getIdentificationType(), dto.getIdentificationNumber());

        Client client = Client.builder()
                .identificationType(dto.getIdentificationType().toUpperCase())
                .identificationNumber(dto.getIdentificationNumber())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .birthDate(dto.getBirthDate())
                .build();

        Client saved = clientRepository.save(client);
        return mapToResponseDTO(saved);
    }

    @Override
    public ClientResponseDTO getClientById(Long id) {
        Client client = findClientOrThrow(id);
        return mapToResponseDTO(client);
    }

    @Override
    public List<ClientResponseDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ClientResponseDTO updateClient(Long id, ClientDTO dto) {
        Client client = findClientOrThrow(id);

        validateAge(dto.getBirthDate());

        client.setIdentificationType(dto.getIdentificationType().toUpperCase());
        client.setIdentificationNumber(dto.getIdentificationNumber());
        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());
        client.setEmail(dto.getEmail());
        client.setBirthDate(dto.getBirthDate());

        Client saved = clientRepository.save(client);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = findClientOrThrow(id);

        if (accountRepository.existsByClientId(id)) {
            throw new IllegalStateException(
                    "No se puede eliminar el cliente porque tiene productos financieros vinculados");
        }

        clientRepository.delete(client);
    }

    private Client findClientOrThrow(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente con ID " + id + " no encontrado"));
    }

    private void validateAge(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            throw new IllegalArgumentException(
                    "El cliente debe ser mayor de edad (18 años o más)");
        }
    }

    private void validateUniqueIdentification(String type, String number) {
        if (clientRepository.existsByIdentificationTypeAndIdentificationNumber(type.toUpperCase(), number)) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente con el tipo y número de identificación proporcionados");
        }
    }

    private ClientResponseDTO mapToResponseDTO(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .identificationType(client.getIdentificationType())
                .identificationNumber(client.getIdentificationNumber())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .birthDate(client.getBirthDate())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
