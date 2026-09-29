package com.upc.qhurinet.dtos.end13;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-13. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End13ResponseDTO {
    private Map<String, Object> payload;
}
