package com.qbounce.repository;

import com.qbounce.model.ContactRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for contact record operations. */
@Repository
public interface ContactRecordRepository extends JpaRepository<ContactRecord, String> {
}
