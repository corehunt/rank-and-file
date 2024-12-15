package com.rankandfile.backend.entity;

import com.rankandfile.backend.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "RAF_BILL_TEXT")
@Data
public class Text extends RAFAudit {

    @Id
    @Column(name = "TEXT_ID", nullable = false, updatable = false)
    private String textId;

    @Column(name = "VERSION_DATE")
    private LocalDate versionDate;

    @Column(name = "VERSION_TYPE")
    private String versionType;

    @Column(name = "FORMATTED_TEXT_URL")
    private String formattedTextUrl;

    @Column(name = "PDF_URL")
    private String pdfUrl;

    @Column(name = "XML_URL")
    private String xmlUrl;

    @ManyToOne
    @JoinColumn(name = "BILL_ID", nullable = false)
    @ToString.Exclude
    private Bill bill;
}
