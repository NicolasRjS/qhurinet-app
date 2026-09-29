package com.upc.qhurinet.dtos.end37;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-37. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End37RequestDTO {
    private Map<String, Object> payload;
}
