package io.wliamp.idp.authn.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("scopes")
public class Scp {
    @Id
    private Long id;

    private Boolean status; // default or not
    private String res; // resource
    private String act; // action
}
