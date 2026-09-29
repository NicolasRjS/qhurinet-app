package com.upc.qhurinet.dtos.end42;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-42. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End42ResponseDTO {
    private Map<String, Object> payload;
}
