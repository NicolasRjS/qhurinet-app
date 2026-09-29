package com.upc.qhurinet.dtos.end38;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-38. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End38ResponseDTO {
    private Map<String, Object> payload;
}
