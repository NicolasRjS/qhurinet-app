package com.upc.qhurinet.dtos.end16;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-16. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End16RequestDTO {
    private Map<String, Object> payload;
}
