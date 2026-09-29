package com.upc.qhurinet.dtos.end40;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-40. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End40RequestDTO {
    private Map<String, Object> payload;
}
