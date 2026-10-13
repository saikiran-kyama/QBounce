package com.qbounce.service;

import com.qbounce.model.ContactRecord;
import com.qbounce.repository.ContactRecordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for saving contact records. */
@Service
@Slf4j
@RequiredArgsConstructor
public class ContactRecordService {

  private final ContactRecordRepository contactRecordRepository;

  @Transactional
  public void saveAll(List<ContactRecord> records) {
    log.info("[ContactRecordService] saveAll called with {} entries", records.size());
    contactRecordRepository.saveAll(records);
    contactRecordRepository.flush();
    log.info("[ContactRecordService] saveAll flushed {} entries to DB", records.size());
  }
}
