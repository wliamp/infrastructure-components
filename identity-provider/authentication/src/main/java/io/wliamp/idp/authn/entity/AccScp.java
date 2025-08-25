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
@Table("account_scope")
public class AccScp {
    private Long accId; // account ID
    private Long scpId; // scope ID
}
