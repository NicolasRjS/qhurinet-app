package com.upc.qhurinet.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthDTO {

    private String idToken;

    private String accessToken;

    private Integer rolId;

    private String telefono;

    private String passwordActual;
}
