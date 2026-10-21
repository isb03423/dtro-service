package com.project.dtro.resource.domain;

import com.project.dtro.common.domain.AuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resource") // tb_resource -> resource 변경
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resource extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT") // NOT NULL 적용
    private String content;

    @Column(name = "public_yn", nullable = false)
    private Boolean publicYn; // Boolean 적용

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    @Builder
    public Resource(String title, String content, Boolean publicYn) {
        this.title = title;
        this.content = content;
        this.publicYn = publicYn != null ? publicYn : false;
    }

    public void update(String title, String content, Boolean publicYn) {
        this.title = title;
        this.content = content;
        this.publicYn = publicYn;
    }

    public void updatePublicYn(Boolean publicYn) {
        this.publicYn = publicYn;
    }

    public void addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        attachment.assignResource(this);
    }
}
