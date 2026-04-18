package com.trinity.financiero.service;

import com.trinity.financiero.dto.ClientDTO;
import com.trinity.financiero.dto.ClientResponseDTO;

import java.util.List;

public interface ClientService {

    ClientResponseDTO createClient(ClientDTO dto);

    ClientResponseDTO getClientById(Long id);

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO updateClient(Long id, ClientDTO dto);

    void deleteClient(Long id);
}
