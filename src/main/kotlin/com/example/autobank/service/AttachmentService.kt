package com.example.autobank.service

import com.example.autobank.data.models.Attachment
import com.example.autobank.repository.receipt.AttachmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class AttachmentService {

    @Autowired
    lateinit var attachmentRepository: AttachmentRepository;

    fun createAttachment(attachment: Attachment): Attachment {
        return attachmentRepository.save(attachment);
    }

    fun getAttachmentsByReceiptId(id: String): List<Attachment> {
        return attachmentRepository.findByReceiptId(id);
    }


}