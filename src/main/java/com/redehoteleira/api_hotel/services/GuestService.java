package com.redehoteleira.api_hotel.services;

import com.redehoteleira.api_hotel.dtos.GuestRequestDTO;
import com.redehoteleira.api_hotel.model.entitites.Guest;
import com.redehoteleira.api_hotel.repositories.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional
    public Guest createGuest(GuestRequestDTO data) {
        if (guestRepository.existsByCpf(data.cpf())) {
            throw new IllegalArgumentException("Já existe um hóspede cadastrado com este CPF.");
        }
        if (guestRepository.existsByEmail(data.email())) {
            throw new IllegalArgumentException("Já existe um hóspede cadastrado com este e-mail.");
        }

        Guest guest = new Guest();
        guest.setName(data.name());
        guest.setCpf(data.cpf());
        guest.setEmail(data.email());
        guest.setPhoneNumber(data.phoneNumber());
        guest.setBirthDate(data.birthDate());

        return guestRepository.save(guest);
    }

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public Guest getGuestById(UUID id) {
        return guestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Hóspede não encontrado."));
    }

    @Transactional
    public Guest updateGuest(UUID id, GuestRequestDTO data) {
        Guest guest = getGuestById(id);

        if (!guest.getCpf().equals(data.cpf()) && guestRepository.existsByCpf(data.cpf())) {
            throw new IllegalArgumentException("Já existe outro hóspede cadastrado com este CPF.");
        }
        if (!guest.getEmail().equals(data.email()) && guestRepository.existsByEmail(data.email())) {
            throw new IllegalArgumentException("Já existe outro hóspede cadastrado com este e-mail.");
        }

        guest.setName(data.name());
        guest.setCpf(data.cpf());
        guest.setEmail(data.email());
        guest.setPhoneNumber(data.phoneNumber());
        guest.setBirthDate(data.birthDate());

        return guestRepository.save(guest);
    }

    @Transactional
    public void deleteGuest(UUID id) {
        Guest guest = getGuestById(id);
        guestRepository.delete(guest);
    }
}