package com.upc.qhurinet.dtos.end48;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-48. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End48ResponseDTO {
    private Map<String, Object> payload;
}
