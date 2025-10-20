public class Proceso {

    private int Duracion; //tiempo que tarda en completarse el proceso (t)
    private String Nombre;
    private Integer Creacion; //LLegada del proceso al servidor (C)
    private String Cliente; //Cliente que envio el proceso
    private Integer EnEspera; //Tiempo que el proceso ha estado en espera (E) / E = F - t
    private Integer Finalizacion; //Tiempo en que el proceso se completo (F) / F = t+E
    private Integer Penalizacion; // Proporcion del tiempo de respuesta del proceso en listo (P) = F / t
    private Integer Inicio;
    private int intentos = 0; // Numero de intentos de ingreso al servidor

    public Proceso(){

    }
    
    public Proceso (int Duracion, String Nombre, String Cliente){
        this.Duracion = Duracion;
        this.Nombre = Nombre;
        this.Cliente = Cliente;
    }

    public void setInicio(int Inicio){
        this.Inicio = Inicio;
    }

    public void subirIntentos(){
        this.intentos += 1;
    }

    public void setCreacion(int Creacion){
        this.Creacion = Creacion;
    }

    public void setEnEspera(int EnEspera){
        this.EnEspera = EnEspera;
    }

    public void setFinalizacion(int Finalizacion){
        this.Finalizacion = Finalizacion;
    }

    public void setPenalizacion(int Penalizacion){
        this.Penalizacion = Penalizacion;
    }

    public int getDuracion(){
        return this.Duracion;
    }

    public String getNombre(){
        return this.Nombre;
    }

    public String getCliente(){
        return this.Cliente;
    }

    public Integer getCreacion(){
        return this.Creacion;
    }

    public Integer getEnEspera(){
        return this.EnEspera;
    }

    public Integer getFinalizacion(){
        return this.Finalizacion;
    }

    public Integer getPenalizacion(){
        return this.Penalizacion;
    }

    public Integer getIntentos(){
        return this.intentos;
    }
    
    public Integer getInicio(){
        return this.Inicio;
    }
}
