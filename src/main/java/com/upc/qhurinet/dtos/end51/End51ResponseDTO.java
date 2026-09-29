package com.upc.qhurinet.dtos.end51;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-51. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End51ResponseDTO {
    private Map<String, Object> payload;
}
