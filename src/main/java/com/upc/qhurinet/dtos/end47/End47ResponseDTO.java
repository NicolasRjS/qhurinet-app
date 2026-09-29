package com.upc.qhurinet.dtos.end47;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-47. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End47ResponseDTO {
    private Map<String, Object> payload;
}
