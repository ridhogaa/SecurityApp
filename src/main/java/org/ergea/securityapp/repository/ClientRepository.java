package org.ergea.securityapp.repository;

import org.ergea.securityapp.model.oauth2.Client;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClientRepository extends PagingAndSortingRepository<Client, Long> {

    Client findOneByClientId(String clientId);

}

