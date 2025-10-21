public class Proceso {
    // Datos de entrada
    private String nombre;
    private String cliente;     // opcional
    private int duracion;       // t
    private Integer creacion;   // C

    // Datos opcionales/medidos
    private Integer inicio;        // si no se setea, se calcula como C + E cuando E exista
    private Integer enEspera;      // E
    private Integer finalizacion;  // F
    private Double penalizacion;   // P = F / t

    private int intentos = 0;

    public Proceso() {}

    public Proceso(int duracion, String nombre, String cliente){
        this.duracion = duracion;
        this.nombre = nombre;
        this.cliente = cliente;
    }

    // ---------- Setters explícitos ----------
    public void setCreacion(int creacion){ this.creacion = creacion; }
    public void setInicio(int inicio){ this.inicio = inicio; }
    public void setEnEspera(int enEspera){ this.enEspera = enEspera; }
    public void setFinalizacion(int finalizacion){ this.finalizacion = finalizacion; }
    public void setPenalizacion(double penalizacion){ this.penalizacion = penalizacion; }
    public void subirIntentos(){ this.intentos += 1; }

    // ---------- Getters base ----------
    public String getNombre(){ return nombre; }
    public String getCliente(){ return cliente; }
    public int getDuracion(){ return duracion; }
    public Integer getCreacion(){ return creacion; }
    public Integer getInicio(){ return inicio != null ? inicio : calcInicio(); }
    public Integer getEnEspera(){ return enEspera != null ? enEspera : calcEspera(); }
    public Integer getFinalizacion(){ return finalizacion != null ? finalizacion : calcFinalizacion(); }
    public Double getPenalizacion(){ return penalizacion != null ? penalizacion : calcPenalizacion(); }
    public Integer getIntentos(){ return intentos; }

    // ---------- Cálculos coherentes ----------
    // E = Inicio - C
    private Integer calcEspera(){
        if (enEspera != null) return enEspera;
        Integer C = creacion;
        Integer S = inicio;
        if (S == null && C != null) {
            // si no hay inicio pero hay finalización, usa F - t
            if (finalizacion != null) return (finalizacion - duracion) - C;
            return null;
        }
        if (C == null || S == null) return null;
        return S - C;
    }

    // Inicio = C + E
    private Integer calcInicio(){
        if (inicio != null) return inicio;
        Integer C = creacion;
        Integer E = getEnEspera();
        if (C == null || E == null) return null;
        return C + E;
    }

    // F = t + E  o  F = Inicio + t
    private Integer calcFinalizacion(){
        if (finalizacion != null) return finalizacion;
        Integer E = getEnEspera();
        if (E != null) return duracion + E;
        Integer S = getInicio();
        if (S != null) return S + duracion;
        return null;
    }

    // P = F / t
    private Double calcPenalizacion(){
        Integer F = getFinalizacion();
        if (F == null) return null;
        if (duracion == 0) return Double.NaN;
        return F / (double) duracion;
    }

    @Override
    public String toString() {
        return String.format(
            "Proceso{%s, t=%d, C=%s, S=%s, E=%s, F=%s, P=%s, cliente=%s, intentos=%d}",
            nombre, duracion, creacion, getInicio(), getEnEspera(), getFinalizacion(), getPenalizacion(), cliente, intentos
        );
    }
}
