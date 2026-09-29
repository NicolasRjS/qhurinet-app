package com.upc.qhurinet.dtos.end25;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-25. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End25RequestDTO {
    private Map<String, Object> payload;
}
