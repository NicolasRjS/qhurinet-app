package com.upc.qhurinet.dtos.end50;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-50. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End50ResponseDTO {
    private Map<String, Object> payload;
}
