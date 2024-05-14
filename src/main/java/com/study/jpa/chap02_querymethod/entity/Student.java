package com.study.jpa.chap02_querymethod.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Setter // 실무적 측면에서 setter는 신중하게 선택할 것. (직접 변경하고자 하는 필드만 따로 설정하는 경우가 많음.)
@Getter @ToString
@EqualsAndHashCode(of = "id") // id가 같으면 같은 객체로 인식.
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tbl_student")
public class Student {
    
    @Id
    @Column(name = "stu_id")
//    @GeneratedValue(generator = "uid")
//    @GenericGenerator(name = "uid", strategy = "uuid")
    @GeneratedValue(strategy = GenerationType.UUID) // Spring Boot3에서 JPA를 사용할 때 권장하는 문법.
    private String id;
    
    @Column(name = "stu_name", nullable = false)
    private String name;
    
    private String city;
    
    private String major;
    
}
