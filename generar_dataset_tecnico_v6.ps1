param(
  [ValidateRange(1,100)]
  [int]$CasosPorClase=20
)
$ErrorActionPreference="Stop"
function Clamp($value,$min,$max){
    if($value -lt $min){return $min}
    if($value -gt $max){return $max}
    return $value
}
$runId=Get-Date -Format "yyyyMMddHHmmss"
$script:nextTechnicalIndex=1
$fechaCorteDate=(Get-Date).Date
$fechaCorte=$fechaCorteDate.ToString("yyyy-MM-dd")
$fechas=0..6 | ForEach-Object { $fechaCorteDate.AddDays($_ - 6).ToString("yyyy-MM-dd") }
$fechaInicio=$fechas[0]
Write-Host "RunId: $runId" -ForegroundColor Cyan
$wc=New-Object System.Net.WebClient; $wc.Headers.Add("Content-Type","application/json")
$resp=$wc.UploadString("http://localhost:8080/api/auth/login","POST",'{"email":"admin@nutripredic.local","password":"ChangeMe123!"}')
$token=($resp|ConvertFrom-Json).token; $wc.Headers.Add("Authorization","Bearer $token")
Write-Host "Login admin OK" -ForegroundColor Green
function Ensure-ComposicionSuplementoTecnica{
  param($suplementoId,$nombre,$energia,$componentes)
  $activa=$null
  try {$activa=$wc.DownloadString("http://localhost:8080/api/admin/suplementos/$suplementoId/composiciones/activa?fecha=$fechaCorte")|ConvertFrom-Json}catch{}
  if($null -eq $activa){
    $unidad=if($nombre -eq "Creatina mono"){"G"}else{"SCOOP"}
    $porcion=if($nombre -eq "Creatina mono"){5}else{1}
    $body=@{cantidadPorcionReferencia=$porcion;unidadPorcionCodigo=$unidad;fechaDesde="2025-01-01";activo=$true;fuenteDatos="SYNTHETIC_TECHNICAL"}
    if($energia -gt 0){$body.energiaKcalPorcion=$energia}
    $activa=$wc.UploadString("http://localhost:8080/api/admin/suplementos/$suplementoId/composiciones","POST",($body|ConvertTo-Json -Depth 3))|ConvertFrom-Json
  }
  $actuales=$wc.DownloadString("http://localhost:8080/api/admin/suplementos/composiciones/$($activa.id)/componentes")|ConvertFrom-Json
  foreach($componente in $componentes){
    if($null -eq ($actuales|Where-Object{$_.tipo -eq $componente.tipo}|Select-Object -First 1)){
      $wc.UploadString("http://localhost:8080/api/admin/suplementos/composiciones/$($activa.id)/componentes","POST",($componente|ConvertTo-Json -Depth 3))|Out-Null
    }
  }
}
function Get-CatalogoIdempotente{
  $listaA=$wc.DownloadString("http://localhost:8080/api/alimentos")|ConvertFrom-Json
  $listaS=$wc.DownloadString("http://localhost:8080/api/suplementos")|ConvertFrom-Json
  if($null -eq $listaA){$listaA=@()}
  if($null -eq $listaS){$listaS=@()}
  $mapA=@{}; $mapS=@{}
  $defsA=@(
    @{n="Pechuga pollo";t="PROTEINA_ANIMAL";k=165;p=31;c=0;g=3.6;f=0;s=74;az=0},
    @{n="Arroz integral";t="CEREAL";k=130;p=2.6;c=28;g=0.9;f=1.8;s=1;az=0.3},
    @{n="Brocoli";t="VERDURA";k=34;p=2.8;c=6.6;g=0.4;f=2.6;s=33;az=1.7},
    @{n="Platano";t="FRUTA";k=89;p=1.1;c=23;g=0.3;f=2.6;s=1;az=12},
    @{n="Aguacate";t="FRUTA";k=160;p=2;c=8.5;g=14.7;f=6.7;s=7;az=0.7},
    @{n="Huevo";t="PROTEINA_ANIMAL";k=155;p=13;c=1.1;g=11;f=0;s=124;az=1.1},
    @{n="Leche descremada";t="LACTEO";k=34;p=3.4;c=5;g=0.1;f=0;s=44;az=5},
    @{n="Avena";t="CEREAL";k=389;p=16.9;c=66;g=6.9;f=10.6;s=2;az=0.9},
    @{n="Aceite oliva";t="GRASA";k=884;p=0;c=0;g=100;f=0;s=0;az=0},
    @{n="Pan integral";t="CEREAL";k=247;p=13;c=41;g=3.5;f=6;s=400;az=4}
  )
  foreach($a in $defsA){
    $ex=$listaA|Where-Object{$_.nombre -eq $a.n}|Select-Object -First 1
    if($ex) {$newId=$ex.id}
    else {
        Write-Host "Creando alimento: $($a.n)"
        $j = @{
            nombre = $a.n
            categoria = $a.t
            unidadBaseCodigo = "G"
        } | ConvertTo-Json -Depth 3
        Write-Host "JSON enviado: $j"
        $r = $wc.UploadString("http://localhost:8080/api/alimentos","POST",$j)
        $newId = ($r | ConvertFrom-Json).id
    }
    $mapA[$a.n] = $newId
    try {$null=$wc.DownloadString("http://localhost:8080/api/admin/alimentos/$newId/composiciones/activa?fecha=$fechaCorte")}
    catch {
        # El catálogo técnico necesita composición factual para que las X nutricionales sean calculables.
        $comp = @{
            cantidadReferencia = 100
            unidadReferenciaCodigo = "G"
            kcal = $a.k
            proteinaG = $a.p
            carbohidratosG = $a.c
            grasasG = $a.g
            fibraG = $a.f
            azucarG = $a.az
            sodioMg = $a.s
            fuenteDatos = "SYNTHETIC_TECHNICAL"
            fechaDesde = "2025-01-01"
            activo = $true
        } | ConvertTo-Json -Depth 3
        $wc.UploadString("http://localhost:8080/api/admin/alimentos/$newId/composiciones","POST",$comp) | Out-Null
    }
  }
  $defsS=@(
    @{n="Whey isolate";t="PROTEINA";p=25;c=2;g=1;cr=0;cf=0;e=110},
    @{n="Creatina mono";t="CREATINA";p=0;c=0;g=0;cr=5;cf=0;e=0},
    @{n="Pre-workout";t="PRE_ENTRENO";p=0;c=3;g=0;cr=0;cf=200;e=12},
    @{n="Omega-3";t="OTRO";p=0;c=0;g=1;cr=0;cf=0;e=9},
    @{n="Multivitaminico";t="OTRO";p=0;c=1;g=0;cr=0;cf=0;e=5}
  )
  foreach($s in $defsS){
    $ex=$listaS|Where-Object{$_.nombre -eq $s.n}|Select-Object -First 1
    if($ex){$mapS[$s.n]=$ex.id}else{$j=@{nombre=$s.n;tipoSuplemento=$s.t;descripcion="Tecnico";beneficios="Tecnico";recomendaciones="Tecnico";energiaKcalPorUnidad=$s.e;proteinaG=$s.p;carbohidratoG=$s.c;grasaG=$s.g;creatinaG=$s.cr;cafeinaMg=$s.cf}|ConvertTo-Json -Depth 3;$r=$wc.UploadString("http://localhost:8080/api/suplementos","POST",$j);$mapS[$s.n]=($r|ConvertFrom-Json).id}
    # Cada componente se declara incluso cuando vale cero: en datos técnicos eso diferencia
    # una ausencia conocida de una composición no calculable.
    $componentes=@(
      @{tipo="PROTEINA";cantidad=$s.p;unidadCodigo="G"},
      @{tipo="CARBOHIDRATOS";cantidad=$s.c;unidadCodigo="G"},
      @{tipo="GRASAS";cantidad=$s.g;unidadCodigo="G"},
      @{tipo="CREATINA";cantidad=$s.cr;unidadCodigo="G"},
      @{tipo="CAFEINA";cantidad=$s.cf;unidadCodigo="MG"}
    )
    Ensure-ComposicionSuplementoTecnica -suplementoId $mapS[$s.n] -nombre $s.n -energia $s.e -componentes $componentes
  }
  return @{alimentos=$mapA; suplementos=$mapS}
}
$catalog=Get-CatalogoIdempotente
Write-Host "Catalogo: $($catalog.alimentos.Count) alimentos, $($catalog.suplementos.Count) suplementos" -ForegroundColor Green
function CrearCasoTecnico{
  param([int]$idx,[string]$claseObjetivo,[int]$edad,[double]$peso,[int]$altura,[string]$objFisico,[string]$tipoObj,[int]$diasEntr,[int]$durSes,[string]$objEnerg,[bool]$gv,[bool]$gm,[bool]$gt,[bool]$grv,[bool]$grm,[int]$sent,[int]$comD,[bool]$desay,[bool]$snack,[int]$cocD,[double]$aguaD,[bool]$swhey,[bool]$screat,[bool]$spre)
  $email="v6-tech-$runId-{0:D3}@e2e.nutripredic.local" -f $idx; $nombre="Tech $runId-{0:D3}" -f $idx
  try{
    $reg=$wc.UploadString("http://localhost:8080/api/auth/register","POST",(@{email=$email;password="Tech123!";nombre=$nombre}|ConvertTo-Json -Depth 3))
    $clienteId=($reg|ConvertFrom-Json).clienteId
    $wc.UploadString("http://localhost:8080/api/clientes/$clienteId","PUT",(@{edad=$edad;pesoKg=$peso;alturaCm=$altura;objetivoFisico=$objFisico;tipoObjetivoFisico=$tipoObj;diasEntrenamientoSemana=$diasEntr;duracionPromedioSesionMinutos=$durSes;objetivoEnergetico=$objEnerg}|ConvertTo-Json -Depth 3))|Out-Null
    $gBody=@{fechaEvaluacion=$fechaCorte;trabajoVigoroso=$gv;trabajoVigorosoDias=3;trabajoVigorosoMinutosDia=45;trabajoModerado=$gm;trabajoModeradoDias=2;trabajoModeradoMinutosDia=30;transporteActivo=$gt;transporteActivoDias=5;transporteActivoMinutosDia=20;recreacionVigorosa=$grv;recreacionVigorosaDias=2;recreacionVigorosaMinutosDia=40;recreacionModerada=$grm;recreacionModeradaDias=3;recreacionModeradaMinutosDia=30;tiempoSentadoMinutosDia=$sent}
    if(-not $gv){$gBody.Remove("trabajoVigorosoDias");$gBody.Remove("trabajoVigorosoMinutosDia")}
    if(-not $gm){$gBody.Remove("trabajoModeradoDias");$gBody.Remove("trabajoModeradoMinutosDia")}
    if(-not $gt){$gBody.Remove("transporteActivoDias");$gBody.Remove("transporteActivoMinutosDia")}
    if(-not $grv){$gBody.Remove("recreacionVigorosaDias");$gBody.Remove("recreacionVigorosaMinutosDia")}
    if(-not $grm){$gBody.Remove("recreacionModeradaDias");$gBody.Remove("recreacionModeradaMinutosDia")}
    $wc.UploadString("http://localhost:8080/api/clientes/$clienteId/actividad-fisica/gpaq/evaluaciones","POST",($gBody|ConvertTo-Json -Depth 3))|Out-Null
    $consumeSuplementos=$swhey -or $screat -or $spre
    foreach($d in $fechas){
      $wc.UploadString("http://localhost:8080/api/habitos","POST",(@{clienteId=$clienteId;fecha=$d;cantidadComidas=$comD;consumoAgua=$aguaD;proteinas=110;tipoAlimentacion="OMNIVORA";nivelOrganizacion="ALTA";desayuno=$desay;snacks=$snack;alimentos="Pollo, arroz, verduras, frutas, huevo";comidasCocinadas=$cocD;restricciones="Ninguna";consumeSuplementos=$consumeSuplementos}|ConvertTo-Json -Depth 3))|Out-Null
    }
    $habitos=$wc.DownloadString("http://localhost:8080/api/habitos/cliente/$clienteId")|ConvertFrom-Json
    switch($claseObjetivo){
      "ADECUADO"  {$q=@{pollo=150;arroz=100;bro=120;plat=100;agu=80;hue=120;lech=200;aven=60;ace=10;pan=40}}
      "MEJORABLE" {$q=@{pollo=120;arroz=150;bro=60;plat=80;agu=40;hue=100;lech=150;aven=40;ace=15;pan=80}}
      default     {$q=@{pollo=100;arroz=200;bro=30;plat=50;agu=20;hue=80;lech=100;aven=30;ace=20;pan=120}}
    }
    $jitter={param($v) [math]::Round($v*(1+(Get-Random -Minimum -0.1 -Maximum 0.1)),0)}
    foreach($h in $habitos){
      $hid=$h.id
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Huevo"];cantidad=& $jitter $q.hue;unidadCodigo="G";momentoComida="DESAYUNO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Avena"];cantidad=& $jitter $q.aven;unidadCodigo="G";momentoComida="DESAYUNO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Leche descremada"];cantidad=& $jitter $q.lech;unidadCodigo="G";momentoComida="DESAYUNO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Platano"];cantidad=& $jitter $q.plat;unidadCodigo="G";momentoComida="DESAYUNO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Pechuga pollo"];cantidad=& $jitter $q.pollo;unidadCodigo="G";momentoComida="ALMUERZO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Arroz integral"];cantidad=& $jitter $q.arroz;unidadCodigo="G";momentoComida="ALMUERZO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Brocoli"];cantidad=& $jitter $q.bro;unidadCodigo="G";momentoComida="ALMUERZO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Aceite oliva"];cantidad=& $jitter $q.ace;unidadCodigo="G";momentoComida="ALMUERZO"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Pechuga pollo"];cantidad=& $jitter $q.pollo;unidadCodigo="G";momentoComida="CENA"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Aguacate"];cantidad=& $jitter $q.agu;unidadCodigo="G";momentoComida="CENA"}|ConvertTo-Json -Depth 3))|Out-Null
      $wc.UploadString("http://localhost:8080/api/habitos/$hid/alimentos","POST",(@{alimentoId=$catalog.alimentos["Pan integral"];cantidad=& $jitter $q.pan;unidadCodigo="G";momentoComida="CENA"}|ConvertTo-Json -Depth 3))|Out-Null
    }
    $ids=@{}
    if($swhey){$r=$wc.UploadString("http://localhost:8080/api/clientes/$clienteId/suplementos","POST",(@{suplementoId=$catalog.suplementos["Whey isolate"];cantidad=1;unidad="SCOOP";frecuencia="DIARIA";fechaInicio=$fechaInicio;estado="ACTIVO"}|ConvertTo-Json -Depth 3));$ids.whey=($r|ConvertFrom-Json).id}
    if($screat){$r=$wc.UploadString("http://localhost:8080/api/clientes/$clienteId/suplementos","POST",(@{suplementoId=$catalog.suplementos["Creatina mono"];cantidad=5;unidad="GRAMOS";frecuencia="DIARIA";fechaInicio=$fechaInicio;estado="ACTIVO"}|ConvertTo-Json -Depth 3));$ids.creat=($r|ConvertFrom-Json).id}
    if($spre){$r=$wc.UploadString("http://localhost:8080/api/clientes/$clienteId/suplementos","POST",(@{suplementoId=$catalog.suplementos["Pre-workout"];cantidad=1;unidad="SCOOP";frecuencia="DIARIA";fechaInicio=$fechaInicio;estado="ACTIVO"}|ConvertTo-Json -Depth 3));$ids.pre=($r|ConvertFrom-Json).id}
    if($consumeSuplementos){
      $cantidadPre=if($claseObjetivo -eq "CRITICO"){3}else{1}
      foreach($d in $fechas){
        if($ids.whey){$wc.UploadString("http://localhost:8080/api/clientes/$clienteId/suplementos/consumo","POST",(@{suplementoClienteId=$ids.whey;fecha=$d;cantidad=1;unidad="SCOOP";estado="CONSUMIDO"}|ConvertTo-Json -Depth 3))|Out-Null}
        if($ids.creat){$wc.UploadString("http://localhost:8080/api/clientes/$clienteId/suplementos/consumo","POST",(@{suplementoClienteId=$ids.creat;fecha=$d;cantidad=5;unidad="GRAMOS";estado="CONSUMIDO"}|ConvertTo-Json -Depth 3))|Out-Null}
        if($ids.pre){$wc.UploadString("http://localhost:8080/api/clientes/$clienteId/suplementos/consumo","POST",(@{suplementoClienteId=$ids.pre;fecha=$d;cantidad=$cantidadPre;unidad="SCOOP";estado="CONSUMIDO"}|ConvertTo-Json -Depth 3))|Out-Null}
      }
    }
    $ev=$wc.DownloadString("http://localhost:8080/api/admin/evaluaciones-perfil/candidata-v6/clientes/$clienteId/evidencia?fechaCorte=$fechaCorte")|ConvertFrom-Json
    $manuales=@()
    $factorManual=switch($claseObjetivo){"ADECUADO"{1.0};"MEJORABLE"{0.2};default{0.0}}
    foreach($c in $ev.criterios){if($c.tipoEvaluacion -eq "MANUAL_ESPECIALISTA" -and $c.estado -eq "MANUAL_PENDIENTE"){$manuales+=@{codigoCriterio=$c.codigo;puntosObtenidos=[math]::Round($c.puntosMaximos*$factorManual,2);observacion="SYNTHETIC_TECHNICAL"}}}
    $gtBody=@{criteriosManuales=$manuales; observacion="SYNTHETIC_TECHNICAL - puntuacion manual simulada para integracion idx $idx objetivo $claseObjetivo" }
    $gtResp=$wc.UploadString("http://localhost:8080/api/admin/evaluaciones-perfil/candidata-v6/clientes/$clienteId`?fechaCorte=$fechaCorte","POST",($gtBody|ConvertTo-Json -Depth 5))
    $gtJson=$gtResp|ConvertFrom-Json
    $clasif=$gtJson.clasificacionReal
    if(-not $clasif){$clasif=$gtJson.evaluacion.clasificacionReal}
    if(-not $clasif){$clasif=$gtJson.evaluacionResponse.clasificacionReal}
    $estado=$wc.DownloadString("http://localhost:8080/api/admin/ml/dataset/v6/clientes/$clienteId/estado?fechaCorte=$fechaCorte")|ConvertFrom-Json
    $motivosCount=0; if($estado.motivosXNull){$motivosCount=($estado.motivosXNull.PSObject.Properties|Measure-Object).Count}
    $ok=$estado.perfilHistorico -and $estado.gpaqValido -and ($estado.xDisponibles -eq 28) -and $estado.groundTruthValido -and $estado.smokeTecnico -and (-not $estado.aptoParaEntrenamiento) -and ($motivosCount -eq 0)
    if($ok){Write-Host "  [$idx] $clasif real | 28/28 X | GT OK | smoke" -ForegroundColor Green}else{Write-Host "  [$idx] FALLA x=$($estado.xDisponibles)/28 GT=$($estado.groundTruthValido) smoke=$($estado.smokeTecnico) apto=$($estado.aptoParaEntrenamiento) nulls=$motivosCount clasif=$clasif" -ForegroundColor Red}
    return @{ok=$ok; clienteId=$clienteId; clasif=$clasif; estado=$estado}
  }catch{Write-Host "  [$idx] EX: $($_.Exception.Message)" -ForegroundColor Red; return @{ok=$false}}
}
$grupos=@(
  @{clase="ADECUADO";edad=28;peso=75;altura=180;objF="GANAR_MASA_MUSCULAR";tipo="GANAR_MASA_MUSCULAR";diasEntr=5;durSes=90;objE="SUPERAVIT";gv=$true;gm=$true;gt=$true;grv=$true;grm=$true;sent=420;comD=5;desay=$true;snack=$true;cocD=3;aguaD=3.0;swhey=$true;screat=$true;spre=$false},
  @{clase="MEJORABLE";edad=32;peso=80;altura=175;objF="PERDER_PESO";tipo="PERDER_PESO";diasEntr=3;durSes=60;objE="DEFICIT";gv=$true;gm=$true;gt=$true;grv=$false;grm=$false;sent=540;comD=4;desay=$true;snack=$true;cocD=2;aguaD=2.2;swhey=$true;screat=$false;spre=$false},
  @{clase="CRITICO";edad=35;peso=90;altura=170;objF="OTRO";tipo="OTRO";diasEntr=1;durSes=30;objE="MANTENIMIENTO";gv=$false;gm=$false;gt=$false;grv=$false;grm=$false;sent=600;comD=3;desay=$false;snack=$false;cocD=1;aguaD=1.0;swhey=$false;screat=$false;spre=$true}
)
$casosOk=@()
foreach($g in $grupos){
  Write-Host "=== $($g.clase) ===" -ForegroundColor Yellow
  for($i=1;$i -le $CasosPorClase;$i++){
    $v=$g.Clone()
    $v.edad+=Get-Random -Minimum -2 -Maximum 3
    $v.peso+=[math]::Round((Get-Random -Minimum -30 -Maximum 30)/10,1)
    $v.altura+=Get-Random -Minimum -2 -Maximum 3
    $v.diasEntr=Clamp ($v.diasEntr+(Get-Random -Minimum -1 -Maximum 2)) 0 7
    $v.durSes=Clamp ($v.durSes+(Get-Random -Minimum -10 -Maximum 11)) 15 180
    $v.sent=Clamp ($v.sent+(Get-Random -Minimum -30 -Maximum 31)) 240 900
    $v.comD=Clamp ($v.comD+(Get-Random -Minimum -1 -Maximum 2)) 2 6
    $v.cocD=Clamp ($v.cocD+(Get-Random -Minimum -1 -Maximum 2)) 0 5
    $v.aguaD=[math]::Round((Clamp ($v.aguaD+((Get-Random -Minimum -30 -Maximum 31)/100)) 0.5 4.0),1)
    $idx=$script:nextTechnicalIndex; $script:nextTechnicalIndex++
    $r=CrearCasoTecnico -idx $idx -claseObjetivo $v.clase -edad $v.edad -peso $v.peso -altura $v.altura -objFisico $v.objF -tipoObj $v.tipo -diasEntr $v.diasEntr -durSes $v.durSes -objEnerg $v.objE -gv $v.gv -gm $v.gm -gt $v.gt -grv $v.grv -grm $v.grm -sent $v.sent -comD $v.comD -desay $v.desay -snack $v.snack -cocD $v.cocD -aguaD $v.aguaD -swhey $v.swhey -screat $v.screat -spre $v.spre
    if($r.ok){$casosOk+=$r}
  }
}
$csv=$wc.DownloadString("http://localhost:8080/api/admin/ml/dataset/v6/technical-export")
$csv|Out-File -FilePath "dataset_nutripredict_v6_technical.csv" -Encoding utf8
$rows=$csv|ConvertFrom-Csv
$filas=$rows.Count
$clientesDistintos=($rows.cliente_group_id|Sort-Object -Unique).Count
$ade=($rows|Where-Object{$_.clasificacion_real -eq "ADECUADO"}).Count
$mej=($rows|Where-Object{$_.clasificacion_real -eq "MEJORABLE"}).Count
$cri=($rows|Where-Object{$_.clasificacion_real -eq "CRITICO"}).Count
$nullX=0; foreach($r in $rows){ foreach($p in $r.PSObject.Properties){ if($p.Name -notin @("case_id","cliente_group_id","fecha_corte","schema_version","rubrica_codigo","rubrica_version","fuente_perfil","clasificacion_real","data_type") -and [string]::IsNullOrEmpty($p.Value)){$nullX++} } }
$dupCase=($rows|Group-Object case_id|Where-Object Count -gt 1).Count
$dupGrupoFecha=($rows|Group-Object -Property {$_.cliente_group_id+"|"+$_.fecha_corte}|Where-Object Count -gt 1).Count
$schemaOk=($rows|Where-Object{$_.schema_version -ne "variables-modelo-v6"}).Count -eq 0
$typeOk=($rows|Where-Object{$_.data_type -ne "SYNTHETIC_TECHNICAL"}).Count -eq 0
$minFecha=($rows.fecha_corte|Sort-Object|Select-Object -First 1)
$maxFecha=($rows.fecha_corte|Sort-Object|Select-Object -Last 1)
Write-Host "`n=== RESULTADO REAL ===" -ForegroundColor Magenta
Write-Host "Archivo: $(Resolve-Path dataset_nutripredict_v6_technical.csv)"
Write-Host "Filas: $filas"
Write-Host "Clientes distintos: $clientesDistintos"
Write-Host "ADECUADO: $ade"
Write-Host "MEJORABLE: $mej"
Write-Host "CRITICO: $cri"
$camposExcluidos = @(
    'case_id',
    'cliente_group_id',
    'fecha_corte',
    'schema_version',
    'rubrica_codigo',
    'rubrica_version',
    'fuente_perfil',
    'clasificacion_real',
    'data_type'
)

$filasConNull = $rows | Where-Object {
    $props = $_.PSObject.Properties | Where-Object {
        $_.Name -notin $camposExcluidos -and
        [string]::IsNullOrEmpty([string]$_.Value)
    }
    $props.Count -gt 0
}

$ok28 = ($filasConNull.Count -eq 0)

Write-Host "28/28 X: $ok28"
Write-Host "Nulls X: $nullX"
Write-Host "Duplicados case_id: $dupCase"
Write-Host "Duplicados cliente_group_id+fecha_corte: $dupGrupoFecha"
Write-Host "Rango fechas: $minFecha a $maxFecha"
Write-Host "schema_version OK: $schemaOk"
Write-Host "data_type OK: $typeOk"
