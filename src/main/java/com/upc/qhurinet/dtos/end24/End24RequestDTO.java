package com.upc.qhurinet.dtos.end24;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-24. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End24RequestDTO {
    private Map<String, Object> payload;
}
