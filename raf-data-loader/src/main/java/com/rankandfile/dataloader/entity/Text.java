package com.rankandfile.dataloader.entity;

import com.rankandfile.dataloader.entity.audit.RAFAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "raf_bill_text")
@Data
public class Text extends RAFAudit {

    @Id
    @Column(name = "text_id", nullable = false, updatable = false)
    private String textId;

    @Column(name = "version_date")
    private LocalDate versionDate;

    @Column(name = "version_type")
    private String versionType;

    @Column(name = "formatted_text_url")
    private String formattedTextUrl;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "xml_url")
    private String xmlUrl;

    @ManyToOne
    @JoinColumn(name = "bill_id", nullable = false)
    @ToString.Exclude
    private Bill bill;
}
