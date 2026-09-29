package com.upc.qhurinet.dtos.end01;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-01. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End01RequestDTO {
    private Map<String, Object> payload;
}
