package org.zenith.telegram_wakatime.wakatime.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Duration {
    private Double duration;
    private String project;
    private Double time;
}
