**Proyecto Final de Sistemas Distribuidos**

Descripción: Implementar un sistema en XML-RPC como mecanismo de comunicación entre procesos que contenga:

    Un servidor RPC que actúa como el planificador de procesos, el servidor asigna el siguiente trabajo disponible, mantiene una lista de espera de procesos y responde con el siguiente proceso disponible.
    Tabla de proceso (Nombre, tiempo de ejecución, tiempo asignado).
    Cola de espera (FIFO que debe actualizarse en cada momento para mostrar los procesos que se van incorporando).
    Tabla de tiempo de espera.
    Tiempo de finalización.
    Tabla de penalización.

Los clientes solicitan tiempo de trabajo (n procesos):

    Verificar la instancia de tiempo para ser procesados, en caso de no poder acceder al recurso, asignar un tiempo de entrada en la ejecución

    La solicitud de procesos se hace mediante el uso de XML-RPC

    Los clientes reportan resultados una vez terminado: Tabla de resumen de procesos (nombre, tiempo de petición, tiempo de asignación, tiempo de término) Para calcular se usa:

        Finalización: F= t + E Donde t es tiempo de CPU.Efecto en el tiempo de ejecución del proceso

        E es tiempo de espera. Efecto en el tiempo de un proceso en Estado Listo

        Espera E = F - t Donde E es la espera F es el tiempo de respuesta o finalizacion. Efecto en el tiempo total para completar el proceso

        Penalizacion P = F / t Donde P es penalizacion

