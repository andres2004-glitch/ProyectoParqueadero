
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class Main {

    private static List<Vehiculo> flota = new ArrayList<>();

    private static JFrame ventana;
    private static DefaultTableModel modeloFlota;
    private static JTable tablaFlota;
    private static JComboBox<String> cmbTipo;
    private static JTextField txtPlaca;
    private static JSpinner spHoras;
    private static JLabel lblTotal;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> crearVentana());
    }

    private static void crearVentana() {

        ventana = new JFrame("Sistema de Alquiler de Vehiculos");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(750, 450);
        ventana.setLocationRelativeTo(null);

        JLabel titulo = new JLabel(
                "Sistema de Alquiler de Vehiculos",
                SwingConstants.CENTER
        );

        titulo.setFont(new Font("SansSerif", Font.BOLD, 22));

        modeloFlota = new DefaultTableModel(
                new Object[]{"#", "Tipo", "Placa", "Tarifa por hora"}, 0
        ) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaFlota = new JTable(modeloFlota);
        tablaFlota.setRowHeight(28);

        cmbTipo = new JComboBox<>(
                new String[]{"Moto", "Auto"}
        );

        txtPlaca = new JTextField(12);

        spHoras = new JSpinner(
                new SpinnerNumberModel(1, 1, 365, 1)
        );

        lblTotal = new JLabel("Selecciona un vehiculo");

        JButton btnAgregar = new JButton("Agregar vehiculo");
        btnAgregar.addActionListener(e -> agregarVehiculo());

        JButton btnAlquilar = new JButton("Alquilar seleccionado");
        btnAlquilar.addActionListener(e -> alquilarSeleccionado());

        JPanel formulario = new JPanel(new FlowLayout());

        formulario.add(new JLabel("Tipo:"));
        formulario.add(cmbTipo);

        formulario.add(new JLabel("Placa:"));
        formulario.add(txtPlaca);

        formulario.add(btnAgregar);

        JPanel inferior = new JPanel(new FlowLayout());

        inferior.add(new JLabel("Horas:"));
        inferior.add(spHoras);

        inferior.add(lblTotal);
        inferior.add(btnAlquilar);

        ventana.add(titulo, BorderLayout.NORTH);
        ventana.add(formulario, BorderLayout.PAGE_START);
        ventana.add(new JScrollPane(tablaFlota), BorderLayout.CENTER);
        ventana.add(inferior, BorderLayout.SOUTH);

        tablaFlota.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarTotal();
            }
        });

        spHoras.addChangeListener(e -> actualizarTotal());

        ventana.setVisible(true);
    }

    private static void agregarVehiculo() {

        String placa = txtPlaca.getText().trim();

        if (placa.isEmpty()) {
            JOptionPane.showMessageDialog(
                    ventana,
                    "Debes ingresar la placa."
            );
            return;
        }

        for (Vehiculo v : flota) {
            if (v.getPlaca().equalsIgnoreCase(placa)) {
                JOptionPane.showMessageDialog(
                        ventana,
                        "Esta placa ya se encuentra registrada."
                );
                return;
            }
        }

        try {

            Vehiculo nuevo;

            if (cmbTipo.getSelectedIndex() == 0) {
                nuevo = new Moto(placa);
            } else {
                nuevo = new Auto(placa);
            }

            flota.add(nuevo);

            refrescarFlota();

            txtPlaca.setText("");

            JOptionPane.showMessageDialog(
                    ventana,
                    "Vehiculo agregado correctamente."
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    ventana,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void refrescarFlota() {

        modeloFlota.setRowCount(0);

        for (int i = 0; i < flota.size(); i++) {

            Vehiculo v = flota.get(i);

            modeloFlota.addRow(new Object[]{
                i + 1,
                v.getTipo(),
                v.getPlaca(),
                "$" + v.getTarifaPorHora()
            });
        }

        lblTotal.setText("Selecciona un vehiculo");
    }

    private static void actualizarTotal() {

        int fila = tablaFlota.getSelectedRow();

        if (fila < 0) {
            lblTotal.setText("Selecciona un vehiculo");
            return;
        }

        int horas = (Integer) spHoras.getValue();

        Vehiculo v = flota.get(fila);

        int total = v.calcularTotal(horas);

        lblTotal.setText("Total: $" + total);
    }

    private static void alquilarSeleccionado() {

        int fila = tablaFlota.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    ventana,
                    "Primero selecciona un vehiculo."
            );
            return;
        }

        int horas = (Integer) spHoras.getValue();

        Vehiculo v = flota.get(fila);

        int total = v.calcularTotal(horas);

        JOptionPane.showMessageDialog(
                ventana,
                "Alquiler registrado\n"
                + "Tipo: " + v.getTipo() + "\n"
                + "Placa: " + v.getPlaca() + "\n"
                + "Horas: " + horas + "\n"
                + "Total a pagar: $" + total
        );
    }
}

 
 