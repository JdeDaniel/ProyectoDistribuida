import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RegistroCliente {
  private static final AtomicInteger seq = new AtomicInteger(1);
  private static final Map<String,String> clientes = new ConcurrentHashMap<>(); // id -> nombre

  // XML-RPC: "Registro.registrar"
  public String registrar(String nombre) {
    if (nombre == null) throw new IllegalArgumentException("nombre requerido");
    nombre = nombre.trim();
    if (nombre.isEmpty()) throw new IllegalArgumentException("nombre vacío");
    if (nombre.length() > 20) throw new IllegalArgumentException("nombre > 20");
    String id = "Cliente-" + seq.getAndIncrement();
    clientes.put(id, nombre);
    //System.out.println("Conectado: id=" + id + " nombre=" + nombre);
    return id;
  }

  // XML-RPC: "Registro.listar"
  public Object[] listar() {
    return clientes.entrySet().stream()
      .map(e -> e.getKey() + ":" + e.getValue())
      .toArray(String[]::new);
  }

  // XML-RPC: "Registro.salir"
  public boolean salir(String clientId) {
    return clientes.remove(clientId) != null;
  }
}
