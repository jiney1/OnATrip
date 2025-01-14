package com.naver.OnATrip.entity.pay;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.naver.OnATrip.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자를 통해서 값 변경 목적으로 접근하는 메시지들 차단
public class Subscribe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", referencedColumnName = "email")
    @JsonBackReference //순환참조 시 역참조를 의미
    private Member member;

    @CreationTimestamp
    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private int itemPeriod;         //구독권 기간

    private int itemId;

    @Enumerated(EnumType.STRING)
    private SubscribeStatus status;     //현재 구독 여부

    @Builder
    public Subscribe(Member member, LocalDate endDate, int itemPeriod, SubscribeStatus status, int itemId) {
        this.member = member;
        this.endDate = endDate;
        this.itemPeriod = itemPeriod;
        this.status = status;
        this.itemId = itemId;
    }






}
