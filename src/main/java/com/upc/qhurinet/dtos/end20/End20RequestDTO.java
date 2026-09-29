package com.upc.qhurinet.dtos.end20;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-20. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End20RequestDTO {
    private Map<String, Object> payload;
}
