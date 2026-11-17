import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
 

public class Puesto {
 
    private boolean ocupado;
    private String nombre = "";
    private String documento = "";
    private String reserva = "";
    private int horas;
    private int total;
    private int gananciaAcumulada; 
    private Vehiculo vehiculo;   
    private LocalDateTime horaEntrada;
    private LocalDateTime horaSalida;
 
    public Puesto() {
    }
 
    public void ocupar(String nombre, String documento, int horas, Vehiculo vehiculo, LocalDateTime horaEntrada) {
        if (ocupado) {
            throw new IllegalStateException("El puesto ya esta ocupado");
        }
        if (vehiculo == null || horaEntrada == null) {
            throw new IllegalArgumentException("Faltan datos del vehiculo o de la hora");
        }
        int nuevoTotal = vehiculo.calcularTotal(horas);
 
        this.ocupado = true;
        this.nombre = nombre;
        this.documento = documento;
        this.horas = horas;
        this.vehiculo = vehiculo;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaEntrada.plusHours(horas);
        this.total = nuevoTotal;
        this.gananciaAcumulada += nuevoTotal;
        this.reserva = "";
    }
 

    public void liberar() {
        ocupado = false;
        nombre = "";
        documento = "";
        reserva = "";
        horas = 0;
        total = 0;
        vehiculo = null;
        horaEntrada = null;
        horaSalida = null;
    }
 
    public void setReserva(String nombreReserva) {
        if (ocupado) {
            throw new IllegalStateException("No se puede reservar un puesto ocupado");
        }
       
        this.reserva = (nombreReserva == null) ? "" : nombreReserva.trim().replace(";", " ");
    }
 
    public boolean tieneReserva() {
        return !reserva.isEmpty();
    }
 
    public boolean isOcupado() {
        return ocupado;
    }
 
    public String getEstado() {
        if (ocupado) {
            return "OCUPADO";
        }
        return tieneReserva() ? "RESERVADO" : "LIBRE";
    }
 
    public String getNombre() {
        return nombre;
    }
 
    public String getDocumento() {
        return documento;
    }
 
    public String getReserva() {
        return reserva;
    }
 
    public String getPlaca() {
        return (vehiculo == null) ? "" : vehiculo.getPlaca();
    }
 
    public int getHoras() {
        return horas;
    }
 
    public Vehiculo getVehiculo() {
        return vehiculo;
    }
 
    public int getTotal() {
        return total;
    }
 
 
    public LocalDateTime getHoraEntrada() {
        return horaEntrada;
    }
 
    public LocalDateTime getHoraSalida() {
        return horaSalida;
    }
    public String toCsvLine() {
        return String.join(";",
                ocupado ? "1" : "0",
                nombre,
                documento,
                String.valueOf(horas),
                vehiculo == null ? "" : vehiculo.getTipo(),
                vehiculo == null ? "" : vehiculo.getPlaca(),
                String.valueOf(total),
                String.valueOf(gananciaAcumulada),
                horaEntrada == null ? "" : horaEntrada.toString(),
                horaSalida == null ? "" : horaSalida.toString(),
                reserva);
    }
 
    public static Puesto fromCsvLine(String linea) {
        String[] c = linea.split(";", -1);
        if (c.length != 11) {
            throw new IllegalArgumentException("Linea con formato incorrecto");
        }
        Puesto p = new Puesto();
        try {
            p.ocupado = c[0].equals("1");
            p.nombre = c[1];
            p.documento = c[2];
            p.horas = Integer.parseInt(c[3]);
            p.vehiculo = c[4].isEmpty() ? null : crearVehiculo(c[4], c[5]);
            p.total = Integer.parseInt(c[6]);
            p.gananciaAcumulada = Integer.parseInt(c[7]);
            p.horaEntrada = c[8].isEmpty() ? null : LocalDateTime.parse(c[8]);
            p.horaSalida = c[9].isEmpty() ? null : LocalDateTime.parse(c[9]);
            p.reserva = c[10];
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("Datos invalidos en la linea", e);
        }
        if (p.ocupado && (p.vehiculo == null || p.horaSalida == null)) {
            throw new IllegalArgumentException("Puesto ocupado sin vehiculo u hora de salida");
        }
        return p;
    }
 
    private static Vehiculo crearVehiculo(String tipo, String placa) {
        switch (tipo.toLowerCase()) {
            case "moto":
                return new Moto(placa);
            case "carro":
                return new Auto(placa);
            default:
                throw new IllegalArgumentException("Tipo de vehiculo desconocido: " + tipo);
        }
    }

    void getGananciaAcumulada() {

    }

    int getgananciaAcumulada() {
       
        return 0;
       
    }
}

 