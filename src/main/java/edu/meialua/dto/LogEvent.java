package edu.meialua.dto;

import edu.meialua.enums.Action;
import edu.meialua.enums.EntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent {

    private Action action;

    private EntityType entity;

    private Long entityId;

    private String user;

    private String description;

    private LocalDateTime timestamp;

    // Espelham os campos opcionais que a API principal passou a enviar em
    // Action.SALE (edu.meialua.morkstore.model.LogEvent) — ficam null em
    // todos os outros eventos.
    private String category;

    private String brand;

    private Float unitValue;

    private Integer quantity;

}