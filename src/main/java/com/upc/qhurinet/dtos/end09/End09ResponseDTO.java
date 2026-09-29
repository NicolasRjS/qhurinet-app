package com.upc.qhurinet.dtos.end09;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/** TODO: sustituir payload por los campos documentados de END-09. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class End09ResponseDTO {
    private Map<String, Object> payload;
}
