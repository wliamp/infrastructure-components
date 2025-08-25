package io.wliamp.idp.authn.dto;

import lombok.Builder;

@Builder
public record Tokens(String access, String refresh) {}
