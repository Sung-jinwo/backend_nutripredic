# Propuesta metodológica para `RUBRICA_PERFIL_HABITOS_V1`

**Fecha de propuesta:** 1 de septiembre de 2026  
**Estado:** BORRADOR METODOLÓGICO  
**Uso previsto:** revisión y validación por especialista competente

> **PROPUESTA SUJETA A VALIDACIÓN DE ESPECIALISTA.** Ninguna definición, escala, ponderación, corte o regla incluida en este documento debe considerarse definitiva ni utilizarse para clasificar casos reales antes de su aprobación formal.

## 1. Alcance y principios de aplicación

La rúbrica propuesta busca obtener una clasificación experta del perfil de hábitos a partir de cinco dimensiones observacionales. No incluye PCC, PCS, TPP, resultados predictivos ni variables destinadas a equilibrar clases del dataset.

La valoración debe respetar estos principios:

- La referencia clínica o nutricional de cada persona debe ser individualizada por el especialista. No se presupone una cantidad universal de comidas, agua o suplementos.
- La ventana observacional propuesta es de siete días completos.
- La disponibilidad de los datos es un requisito previo, no un mérito puntuable. Si falta información necesaria para valorar una dimensión, el caso queda **NO EVALUABLE**; el dato ausente no se convierte en cero.
- La suplementación no se valora por la cantidad de productos consumidos. La ausencia de suplementación puede ser plenamente adecuada cuando no existe indicación y está correctamente documentada.
- Un suplemento no sustituye una alimentación variada ni una indicación clínica. Deben revisarse dosis, contraindicaciones e interacciones.
- La clasificación obtenida debe proceder exclusivamente de la aplicación de la rúbrica validada por el especialista.

## 2. Sustento metodológico de los dominios

La selección de dominios y la forma de observarlos se apoya en estas fuentes:

- Las [Guías Alimentarias para la Población Peruana](https://www.gob.pe/institucion/ins/informes-publicaciones/1844104-guias-alimentarias-para-la-poblacion-peruana), elaboradas por MINSA/INS, aportan el referente alimentario contextualizado para la población peruana.
- La guía de la [Healthy Diets Monitoring Initiative (HDMI)](https://www.who.int/publications/i/item/9789240094383), iniciativa conjunta WHO/FAO/UNICEF, sustenta el uso de datos dietéticos y métodos de evaluación para observar y monitorear alimentación saludable.
- El [Diet Quality Questionnaire (DQQ) para Perú](https://www.dietquality.org/countries/per) demuestra la disponibilidad de un instrumento de calidad de dieta adaptado al país y orienta la observación estructurada de la alimentación; no se replica ni modifica el DQQ en esta rúbrica.
- La [Office of Dietary Supplements de NIH](https://ods.od.nih.gov/factsheets/dietarysupplements-Consumer/) sustenta la revisión de identificación del producto, dosis, seguridad, calidad e interacciones al observar suplementos.

Estas fuentes **sustentan los dominios y la forma de observar alimentación y suplementación**, pero **no validan directamente los pesos, la fórmula, la escala ni los cortes propuestos por NutriPredict**. Esos elementos son una propuesta preliminar que requiere juicio de expertos y validación documentada antes de emplearse como ground truth.

## 3. Escala común propuesta

Cada dimensión se puntúa provisionalmente en una escala ordinal de **0 a 4**:

| Puntaje | Interpretación general propuesta |
|---:|---|
| 0 | Situación crítica, incumplimiento muy marcado o condición potencialmente insegura que requiere revisión profesional. |
| 1 | Cumplimiento bajo, con deficiencias importantes. |
| 2 | Cumplimiento parcial o intermedio. |
| 3 | Cumplimiento adecuado, con desviaciones menores. |
| 4 | Cumplimiento adecuado y consistente durante la ventana evaluada. |

**Puntaje mínimo por dimensión:** 0.  
**Puntaje máximo por dimensión:** 4.

> **PROPUESTA SUJETA A VALIDACIÓN DE ESPECIALISTA.** La escala y sus umbrales son preliminares.

## 4. Definición y medición de las dimensiones

### 4.1 Organización alimentaria

**Fundamento:** las guías peruanas y HDMI respaldan observar patrones y prácticas alimentarias dentro del contexto de la persona; no imponen una cantidad universal de comidas.

**Dato observado en NutriPredict:** registros diarios de hábitos de la ventana de siete días, especialmente fecha, `cantidadComidas`, `comidasCocinadas`, y registros alimentarios con `momentoComida`.

**Qué observa:** la regularidad y coherencia de la estructura alimentaria diaria respecto del plan o patrón individual acordado con el especialista. Considera planificación, distribución de comidas y consistencia de horarios o momentos de consumo cuando estos sean pertinentes para el caso.

**Forma de evaluación propuesta:** el especialista define previamente qué constituye un “día organizado” para la persona y contabiliza cuántos de los siete días cumplen ese patrón. No se exige una cantidad universal de comidas, desayuno o refrigerios.

| Puntaje | Criterio preliminar |
|---:|---|
| 0 | 0 a 1 días organizados de 7. |
| 1 | 2 días organizados de 7. |
| 2 | 3 a 4 días organizados de 7. |
| 3 | 5 a 6 días organizados de 7. |
| 4 | 7 días organizados de 7. |

**Rango:** 0–4.

### 4.2 Alimentación registrada

**Fundamento:** las Guías Alimentarias para la Población Peruana, HDMI y el DQQ para Perú respaldan observar alimentos y patrones dietéticos mediante registros estructurados y con interpretación contextualizada.

**Dato observado en NutriPredict:** registros de alimentos por día, alimento, cantidad, unidad y `momentoComida`; composición nutricional asociada cuando está disponible; y completitud de alimentación de la ventana V5.

**Qué observa:** la adecuación del consumo alimentario realmente registrado, considerando composición, variedad, equilibrio, moderación y correspondencia con los objetivos individualizados. No puntúa solamente la existencia del registro.

**Forma de evaluación propuesta:** una vez confirmados siete días completos y suficientemente detallados, el especialista determina para cada día si cumple los criterios alimentarios individualizados. La completitud del registro es una condición de evaluabilidad; si no permite valorar la ingesta, el caso queda no evaluable.

| Puntaje | Criterio preliminar |
|---:|---|
| 0 | 0 a 1 días adecuados de 7. |
| 1 | 2 días adecuados de 7. |
| 2 | 3 a 4 días adecuados de 7. |
| 3 | 5 a 6 días adecuados de 7, con desviaciones menores. |
| 4 | 7 días adecuados de 7 y coherencia global con el objetivo individual. |

**Rango:** 0–4.

### 4.3 Hidratación

**Fundamento:** las guías peruanas y el enfoque de observación dietética de HDMI respaldan registrar la práctica de hidratación dentro del patrón habitual; el rango adecuado debe ser definido individualmente por el especialista.

**Dato observado en NutriPredict:** `RegistroHabito.consumoAgua` por cada día de la ventana de siete días y la completitud de hábitos V5.

**Qué observa:** la suficiencia y consistencia del consumo real de líquidos respecto de un rango individual seguro establecido por el especialista, considerando contexto clínico, actividad, ambiente y otras condiciones pertinentes.

**Forma de evaluación propuesta:** el especialista fija el rango individual aceptable y contabiliza los días en que el consumo registrado se encuentra dentro de él. No se incorpora una meta universal de litros en esta propuesta.

| Puntaje | Criterio preliminar |
|---:|---|
| 0 | 0 a 1 días dentro del rango individual de 7, o patrón que requiere revisión profesional inmediata. |
| 1 | 2 días dentro del rango individual de 7. |
| 2 | 3 a 4 días dentro del rango individual de 7. |
| 3 | 5 a 6 días dentro del rango individual de 7. |
| 4 | 7 días dentro del rango individual de 7. |

**Rango:** 0–4.

### 4.4 Suplementación habitual

**Fundamento:** NIH ODS recomienda considerar identificación del producto, ingredientes, dosis, seguridad y potenciales interacciones; no asume que consumir más suplementos sea mejor.

**Dato observado en NutriPredict:** asignación habitual `SuplementoCliente` —producto, cantidad por toma, unidad, frecuencia, periodo, estado y vigencia— más catálogo y composición del suplemento cuando correspondan. La indicación clínica y la revisión de interacciones deben ser confirmadas por el especialista; no se infieren automáticamente.

**Qué observa:** la pertinencia y trazabilidad del plan habitual de suplementación: indicación u objetivo, producto y componentes, dosis, unidad, frecuencia, duración, responsable de la indicación, contraindicaciones e interacciones relevantes.

**Forma de evaluación propuesta:** revisión cualitativa del plan vigente por el especialista. No se otorgan más puntos por consumir más suplementos. Cuando no existe indicación, la no utilización debidamente confirmada puede alcanzar el puntaje máximo.

| Puntaje | Criterio preliminar |
|---:|---|
| 0 | Uso potencialmente inseguro, contraindicado o sin información mínima para identificar producto y dosis. |
| 1 | Deficiencias importantes de justificación, dosificación, trazabilidad o seguridad. |
| 2 | Información parcial; pertinencia o seguridad aún no completamente resuelta. |
| 3 | Plan mayormente pertinente y documentado, con brechas menores. |
| 4 | Plan pertinente, seguro y completamente documentado, o ausencia apropiada de suplementación cuando no está indicada. |

**Rango:** 0–4.

### 4.5 Consumo real de suplementos

**Fundamento:** NIH ODS sustenta comprobar el consumo efectivo, la dosis, la seguridad y los posibles productos o interacciones no previstos, siempre bajo revisión profesional.

**Dato observado en NutriPredict:** `RegistroConsumoSuplemento` por día, suplemento asignado, cantidad consumida, unidad, número de tomas, composición aplicada y observación; junto con el plan habitual vigente.

**Qué observa:** la concordancia entre el consumo ocurrido durante los siete días y el plan de suplementación previamente validado, incluyendo omisiones, dosis incorrectas, exceso y productos no previstos.

**Forma de evaluación propuesta:** cuando existe un plan, se calcula provisionalmente:

```text
adherencia (%) = dosis tomadas correctamente / dosis planificadas × 100
```

Además, el especialista verifica que no haya consumos no indicados ni excesos. Si la ausencia de suplementación es la conducta apropiada, siete días sin consumo no previsto pueden recibir el puntaje máximo.

| Puntaje | Criterio preliminar |
|---:|---|
| 0 | Adherencia menor de 40 %, o consumo potencialmente inseguro, excesivo o no previsto. |
| 1 | Adherencia de 40 % a 59 %. |
| 2 | Adherencia de 60 % a 79 %. |
| 3 | Adherencia de 80 % a 94 %, sin desviaciones relevantes de seguridad. |
| 4 | Adherencia de 95 % a 100 %, sin consumo no previsto ni exceso; o no consumo apropiado cuando no existe indicación. |

**Rango:** 0–4.

## 5. Pesos preliminares

| Dimensión | Peso propuesto |
|---|---:|
| Organización alimentaria | 25 % |
| Alimentación registrada | 35 % |
| Hidratación | 20 % |
| Suplementación habitual | 10 % |
| Consumo real de suplementos | 10 % |
| **Total** | **100 %** |

La ponderación propuesta asigna 60 % al patrón alimentario, 20 % a hidratación y 20 % al bloque de suplementación. Su finalidad es ofrecer un punto concreto de discusión; no representa una ponderación validada ni una conclusión clínica.

> **PROPUESTA PRELIMINAR SUJETA A JUICIO DE EXPERTOS:** los pesos 25 / 35 / 20 / 10 / 10 no están validados por las fuentes citadas ni por NutriPredict.

## 6. Fórmula preliminar

Sea `pᵢ` el puntaje de una dimensión, en el rango 0–4, y `wᵢ` su peso porcentual:

```text
PuntajeTotal = Σ ((pᵢ / 4) × wᵢ)
```

Forma desarrollada:

```text
PuntajeTotal =
    (organización / 4 × 25)
  + (alimentación / 4 × 35)
  + (hidratación / 4 × 20)
  + (suplementación habitual / 4 × 10)
  + (consumo real de suplementos / 4 × 10)
```

**Rango del resultado:** 0 a 100 puntos.

No se calcula el puntaje total si alguna dimensión necesaria no puede evaluarse. No se permite reemplazar datos ausentes por cero ni redistribuir automáticamente su peso.

## 7. Cortes preliminares de clasificación

| Clasificación | Intervalo preliminar | Lectura orientativa |
|---|---:|---|
| CRITICO | 0,00 a menos de 50,00 | Promedio ponderado inferior al nivel parcial de la escala. |
| MEJORABLE | 50,00 a menos de 75,00 | Cumplimiento parcial; todavía no alcanza adecuación consistente. |
| ADECUADO | 75,00 a 100,00 | Equivale, como referencia, a un promedio ponderado de al menos 3 sobre 4. |

Los intervalos se expresan sin solapamientos: `[0, 50)`, `[50, 75)` y `[75, 100]`.

> **PROPUESTA PRELIMINAR SUJETA A JUICIO DE EXPERTOS:** los cortes CRITICO `[0,50)`, MEJORABLE `[50,75)` y ADECUADO `[75,100]` no están validados y no deben utilizarse para crear ground truth hasta contar con aprobación documentada.

## 8. Matriz de validación por dimensión

| Dimensión | Fundamento | Dato observado en NutriPredict | Forma de evaluación propuesta | Puntaje 0–4 | Peso propuesto | Decisión | Valor recomendado por especialista | Observación |
|---|---|---|---|---|---:|---|---|---|
| Organización alimentaria | Guías peruanas y HDMI: observación contextualizada de patrones alimentarios. | `cantidadComidas`, `comidasCocinadas`, fecha y `momentoComida` de registros diarios. | Días organizados según patrón individual: 0–1 / 2 / 3–4 / 5–6 / 7 de 7. | 0 crítico; 1 bajo; 2 parcial; 3 adecuado; 4 consistente. | 25 % | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Alimentación registrada | Guías peruanas, HDMI y DQQ Perú: observación estructurada de alimentos y patrón dietético. | Alimento, cantidad, unidad, `momentoComida`, composición y completitud 7/7. | Días adecuados según criterio individual: 0–1 / 2 / 3–4 / 5–6 / 7 de 7. | 0 crítico; 1 bajo; 2 parcial; 3 adecuado; 4 consistente. | 35 % | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Hidratación | Guías peruanas y HDMI: práctica observada dentro del patrón individual. | `consumoAgua` diario y completitud de hábitos 7/7. | Días dentro del rango individual: 0–1 / 2 / 3–4 / 5–6 / 7 de 7. | 0 crítico; 1 bajo; 2 parcial; 3 adecuado; 4 consistente. | 20 % | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Suplementación habitual | NIH ODS: identificación, dosis, seguridad e interacciones. | Plan `SuplementoCliente`, catálogo y composición vigente; indicación clínica por confirmar. | Revisión cualitativa de pertinencia, seguridad y trazabilidad. | 0 inseguro; 1 deficiencias mayores; 2 parcial; 3 adecuado; 4 seguro/documentado o no uso apropiado. | 10 % | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Consumo real de suplementos | NIH ODS: dosis real, seguridad y productos no previstos. | Registro diario de consumo, cantidad, unidad, tomas, composición y observación. | Adherencia y seguridad: <40 / 40–59 / 60–79 / 80–94 / 95–100 %, sin excesos. | 0 inseguro o <40 %; 1–4 conforme a escala. | 10 % | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |

## 9. Matriz independiente de validación metodológica

| Elemento a validar | Propuesta preliminar NutriPredict | Decisión | Valor recomendado por especialista | Observación |
|---|---|---|---|---|
| Pesos | Organización 25 %, alimentación 35 %, hidratación 20 %, suplementación habitual 10 %, consumo real 10 %. Total: 100 %. | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Fórmula | `Σ ((puntaje de dimensión / 4) × peso de dimensión)`, rango 0–100. | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Cortes | CRITICO `[0,50)`; MEJORABLE `[50,75)`; ADECUADO `[75,100]`. | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |
| Reglas de datos faltantes | Si falta información necesaria para una dimensión, el caso es NO EVALUABLE; no se convierte a 0 ni se redistribuye peso. | [ ] APROBAR  [ ] MODIFICAR | __________________ | __________________ |

> **PROPUESTA PRELIMINAR SUJETA A JUICIO DE EXPERTOS:** esta matriz documenta la revisión requerida; no constituye aprobación.

## 10. Estado actual de implementación

- Backend de rúbrica implementado.
- Dataset V5 implementado.
- Cliente candidato con 21/21 X disponible.
- Ground truth bloqueado hasta la validación formal de esta rúbrica.
- Entrenamiento Random Forest V5 no realizado para evitar etiquetas artificiales.

## 11. Puntos que requieren decisión expresa del especialista

1. Aprobar o modificar las definiciones operativas de cada dimensión.
2. Confirmar si la ventana de siete días es suficiente para emitir ground truth.
3. Definir los parámetros individualizados mínimos para considerar adecuado cada día.
4. Aprobar o modificar la escala 0–4 y todos sus umbrales.
5. Aprobar o modificar los pesos propuestos.
6. Aprobar o modificar los cortes de clasificación.
7. Decidir si un puntaje 0 por riesgo en hidratación o suplementación debe activar una regla especial, como revisión obligatoria o clasificación crítica, independientemente del total.
8. Definir la documentación mínima aceptable de indicación y seguridad de suplementos.
9. Confirmar el manejo de casos sin indicación de suplementación.

## 12. Constancia de revisión y validación

**Decisión del especialista:**

- [ ] APROBADO
- [ ] REQUIERE CAMBIOS

**Código de la rúbrica revisada:** `RUBRICA_PERFIL_HABITOS_V1`  
**Versión revisada:** ______________________________  
**Nombre del validador:** __________________________  
**Profesión/especialidad:** ________________________  
**Número de colegiatura o identificación profesional (si corresponde):** ________________________  
**Fecha:** ____ / ____ / ______  

**Observaciones:**

______________________________________________________________________________

______________________________________________________________________________

______________________________________________________________________________

**Firma o constancia de validación:**

______________________________________________________________________________

> La aprobación sólo será efectiva cuando la decisión, identidad del validador, fecha, observaciones y firma o constancia queden registradas. Hasta entonces, el documento conserva el estado de propuesta no validada.
