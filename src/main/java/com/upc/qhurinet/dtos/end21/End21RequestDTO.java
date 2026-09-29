package com.upc.qhurinet.dtos.end21;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-21. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End21RequestDTO {
    private Map<String, Object> payload;
}
