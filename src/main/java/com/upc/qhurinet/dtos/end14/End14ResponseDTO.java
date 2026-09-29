package com.upc.qhurinet.dtos.end14;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-14. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End14ResponseDTO {
    private Map<String, Object> payload;
}
