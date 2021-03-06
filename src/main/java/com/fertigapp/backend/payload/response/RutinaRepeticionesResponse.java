package com.fertigapp.backend.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fertigapp.backend.recurrentstrategy.RecurrentEntityStrategy;
import com.fertigapp.backend.recurrentstrategy.RutinaRecurrentEntityStrategy;
import com.fertigapp.backend.model.Rutina;

import java.time.OffsetDateTime;
import java.util.List;

//Response con la información de un evento y las fechas de todas sus repeticiones
public class RutinaRepeticionesResponse extends AbstractRecurrenteResponse  {

    @JsonInclude
    private List<OffsetDateTime> completadas;
    @JsonInclude
    private List<OffsetDateTime> futuras;

    @JsonIgnore
    private RecurrentEntityStrategy recurrentEntityStrategy;

    public RutinaRepeticionesResponse() {
        super();
    }

    public RutinaRepeticionesResponse(Rutina rutina, List<OffsetDateTime> completadas) {
        super(rutina);
        this.completadas = completadas;
        this.recurrentEntityStrategy = new RutinaRecurrentEntityStrategy(rutina);
        futuras =  recurrentEntityStrategy.findFechas();
    }

    public List<OffsetDateTime> getCompletadas() {
        return completadas;
    }

    public List<OffsetDateTime> getFuturas() {
        return futuras;
    }
}