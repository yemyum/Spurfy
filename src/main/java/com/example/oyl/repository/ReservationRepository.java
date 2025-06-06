package com.example.oyl.repository;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {

    List<Reservation> findByUserEmail(String email);

    List<Reservation> findByUser(User user);

    boolean existsByDogAndReservationDate(Dog dog, LocalDate reservationDate);

}
