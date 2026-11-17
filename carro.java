public abstract class carro extends Vehiculo {
 
    private static final int TARIFA = 5000;
 
    public carro() {
        super("carro");
    }
    
    @Override
    public int getTarifaPorHora() {
        return TARIFA;
    }
}
