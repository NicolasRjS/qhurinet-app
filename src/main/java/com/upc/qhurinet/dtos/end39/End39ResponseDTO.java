package com.upc.qhurinet.dtos.end39;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-39. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End39ResponseDTO {
    private Map<String, Object> payload;
}
