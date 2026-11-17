public class Moto extends Vehiculo {

    private String yamaha;
    private String fz_150;
    private double d;

    public Moto(String placa) {
        super(placa);
    }

    public Moto(String placa, String yamaha, double d) {
        super(placa);
        this.yamaha = yamaha;
        this.fz_150 = fz_150;
        this.d = d;
    }

    @Override
    public int getTarifaPorHora() {
        return 3000;
    }

    @Override
    public String getTipo() {
        return "Moto";
    }
}
