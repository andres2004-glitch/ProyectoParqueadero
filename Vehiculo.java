public abstract class Vehiculo {
    private final String placa;
 
    public Vehiculo(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacia");
        }
        this.placa = placa.trim().toUpperCase();
    }
 
    public String getPlaca() {
        return placa;
    }
 
    public abstract int getTarifaPorHora();
 
    public abstract String getTipo();
 
    public int calcularTotal(int horas) {
        if (horas < 1) {
            throw new IllegalArgumentException("Las horas deben ser al menos 1");
        }
        return horas * getTarifaPorHora();
    }
 
    @Override
    public String toString() {
        return getTipo() + " " + placa;
    }

    void getMarca() {
    
    }

    char[] toCsv() {
    
        return null;
    
    }


    String getModelo() {
     
        return null;
     
    }

}
 