package com.upc.qhurinet.dtos.end08;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-08. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End08ResponseDTO {
    private Map<String, Object> payload;
}
