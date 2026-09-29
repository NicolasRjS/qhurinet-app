package com.upc.qhurinet.dtos.end02;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-02. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End02RequestDTO {
    private Map<String, Object> payload;
}
