package com.example.oyl.repository;

import com.example.oyl.domain.Dog;
import com.example.oyl.domain.Reservation;
import com.example.oyl.domain.ReservationStatus;
import com.example.oyl.domain.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {

    @Query("SELECT r FROM Reservation r JOIN FETCH r.dog d JOIN FETCH r.user u JOIN FETCH r.spaService s WHERE u.userId = :userId")
    List<Reservation> findByUser_UserIdWithDetails(@Param("userId") String userId);

    boolean existsByDogAndReservationDate(Dog dog, LocalDate reservationDate);

    List<Reservation> findByReservationDateBeforeAndReservationStatus(
            LocalDate date, ReservationStatus status);

}