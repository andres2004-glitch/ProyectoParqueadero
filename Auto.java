public class Auto extends Vehiculo {

    private String marca;
    private String modelo;
    private double d;

    public Auto(String placa) {
        super(placa);
    }

    public Auto(String placa, String marca, double d) {
        super(placa);
        this.marca = marca;
        this.modelo = modelo;
        this.d = d;
    }

    @Override
    public int getTarifaPorHora() {
        return 4000;
    }

    @Override
    public String getTipo() {
        return "Carro";
    }
}
