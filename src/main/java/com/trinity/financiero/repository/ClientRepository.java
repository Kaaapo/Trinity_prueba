package com.trinity.financiero.repository;

import com.trinity.financiero.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    boolean existsByIdentificationTypeAndIdentificationNumber(String identificationType, String identificationNumber);
}
