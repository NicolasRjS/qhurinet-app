package com.upc.qhurinet.dtos.end52;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-52. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End52ResponseDTO {
    private Map<String, Object> payload;
}
