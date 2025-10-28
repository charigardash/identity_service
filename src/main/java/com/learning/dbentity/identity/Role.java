package com.learning.dbentity.identity;

import com.learning.enums.RolesEnum;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Role {
    @Id
    private Long Id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RolesEnum name;
}
