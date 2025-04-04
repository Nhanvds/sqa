package com.doan.backend.repositories;

import com.doan.backend.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Optional<Invoice> findByOrderId(String orderId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT i FROM Invoice i " +
            "JOIN i.order o " +
            "JOIN o.user u " +
            "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :customerEmail, '%'))")
    Page<Invoice> findByAllSearchEmail(@Param("customerEmail") String customerEmail, Pageable pageable);
}
