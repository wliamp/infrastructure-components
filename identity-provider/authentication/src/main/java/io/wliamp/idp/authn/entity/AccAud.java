package io.wliamp.idp.authn.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("account_audience")
public class AccAud {
    private Long accId; // account ID
    private Long audId; // audience ID
}
