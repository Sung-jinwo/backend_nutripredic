package com.backend.nutri_predic.prediccionmodelo.service;

import com.backend.nutri_predic.prediccionmodelo.evento.entity.OrigenResultadoAnalisis;
import com.backend.nutri_predic.prediccionmodelo.entity.PrediccionModelo;

public record ResultadoInferenciaModelo(
        PrediccionModelo prediccion, OrigenResultadoAnalisis origenResultado) {}
