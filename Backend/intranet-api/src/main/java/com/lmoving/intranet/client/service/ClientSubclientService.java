package com.lmoving.intranet.client.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.lmoving.intranet.client.Client;
import com.lmoving.intranet.client.ClientSubclient;
import com.lmoving.intranet.client.dto.ClientSubclientRequest;
import com.lmoving.intranet.client.dto.ClientSubclientResponse;
import com.lmoving.intranet.client.repository.ClientRepository;
import com.lmoving.intranet.client.repository.ClientSubclientRepository;

@Service
public class ClientSubclientService {
    private final ClientRepository clients;
    private final ClientSubclientRepository relations;
    public ClientSubclientService(ClientRepository clients, ClientSubclientRepository relations) { this.clients = clients; this.relations = relations; }

    @Transactional(readOnly = true)
    public List<ClientSubclientResponse> list(Long clientId) {
        findClient(clientId);
        return relations.findAllByClient_IdAndActiveTrueOrderBySubclient_BusinessNameAsc(clientId).stream().map(ClientSubclientResponse::from).toList();
    }

    @Transactional
    public ClientSubclientResponse create(Long clientId, ClientSubclientRequest request) {
        Client client = findClient(clientId);
        Client subclient = findClient(request.subclientId());
        if (!client.isActive() || !subclient.isActive()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden relacionar clientes activos");
        ClientSubclient relation = relations.findByClient_IdAndSubclient_Id(clientId, subclient.getId()).orElseGet(ClientSubclient::new);
        relation.setClient(client); relation.setSubclient(subclient); relation.setActive(true);
        return ClientSubclientResponse.from(relations.save(relation));
    }

    @Transactional
    public ClientSubclientResponse changeStatus(Long id, boolean active) {
        ClientSubclient relation = relations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la relación cliente-subcliente"));
        relation.setActive(active);
        return ClientSubclientResponse.from(relations.save(relation));
    }

    private Client findClient(Long id) { return clients.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el cliente solicitado")); }
}
