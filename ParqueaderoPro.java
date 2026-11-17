import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ParqueaderoPro extends JFrame {

    private static final String ARCHIVO = "datos.txt";
    private static final String CLAVE_ADMIN = "1234";

    private static final String[][] MAPA = {
        {"P", "P", "P", "E", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "X", "P", "P", "P"},
        {"P", "P", "P", "S", "P", "P", "P"}
    };
    private static final String[] COLUMNAS = {"A", "B", "C", " ", "D", "E", "F"};
    private static final int COLUMNA_PASILLO = 3;
    private static final int PUESTOS_POR_FILA = 6;

    private final DateTimeFormatter formatoReloj = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private final DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    private JTable table;
    private final DefaultTableModel model;
    private JLabel reloj;
    private LocalDateTime horaManual = null;
    private final Puesto[] puestos = new Puesto[MAPA.length * PUESTOS_POR_FILA];
    private ConsumoElectrico consumoPanel; 

    public ParqueaderoPro() {

        for (int i = 0; i < puestos.length; i++) {
            puestos[i] = new Puesto();
        }

        setTitle("Parqueadero PRO");
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelParqueadero = new JPanel(new BorderLayout());

        reloj = new JLabel();
        reloj.setFont(new Font("Arial", Font.BOLD, 18));
        reloj.setHorizontalAlignment(SwingConstants.CENTER);
        panelParqueadero.add(reloj, BorderLayout.NORTH);
        reloj.setText("Hora actual: " + ahora().format(formatoReloj));

        iniciarReloj();

        JButton cambiarHoraBtn = new JButton("Cambiar Hora");
        cambiarHoraBtn.addActionListener(e -> cambiarHora());
        panelParqueadero.add(cambiarHoraBtn, BorderLayout.WEST);

        model = new DefaultTableModel(copiaDelMapa(), COLUMNAS) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        table = new JTable(model);
        table.setRowHeight(58);
        table.setDefaultRenderer(Object.class, new Renderer());

        cargarDatos();

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col >= 0) {
                    clickEnCelda(row, col);
                }
            }
        });

        JButton reservarBtn = new JButton("Reservar");
        reservarBtn.addActionListener(e -> reservar());

        JButton adminBtn = new JButton("ADMIN");
        adminBtn.addActionListener(e -> mostrarAdmin());

        panelParqueadero.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton grafica1Btn = new JButton("Gráfica 1");
        grafica1Btn.addActionListener(e -> mostrarGrafica());

        JButton grafica2Btn = new JButton("Gráfica 2");
        grafica2Btn.addActionListener(e -> mostrarGrafica2());

        JPanel abajo = new JPanel();
        abajo.add(reservarBtn);
        abajo.add(grafica1Btn);
        abajo.add(grafica2Btn);
        abajo.add(adminBtn);
        panelParqueadero.add(abajo, BorderLayout.SOUTH);

        add(panelParqueadero, BorderLayout.CENTER);

        setLocationRelativeTo(null);
    }

    private static Object[][] copiaDelMapa() {
        Object[][] copia = new Object[MAPA.length][];
        for (int i = 0; i < MAPA.length; i++) {
            copia[i] = MAPA[i].clone();
        }
        return copia;
    }

    // ========== RELOJ =========

    private LocalDateTime ahora() {
        return (horaManual != null) ? horaManual : LocalDateTime.now();
    }

    private void iniciarReloj() {
        Timer timer = new Timer(1000, e -> {
            if (horaManual != null) {
                horaManual = horaManual.plusSeconds(1);
            }
            LocalDateTime hora = ahora();
            reloj.setText("Hora actual: " + hora.format(formatoReloj));
            liberarVencidos(hora);
        });
        timer.start();
    }

    private void cambiarHora() {
        String h = JOptionPane.showInputDialog(this, "Nueva hora (HH:mm)");
        if (h == null) {
            return;
        }
        try {
            LocalTime hora = LocalTime.parse(h.trim(), DateTimeFormatter.ofPattern("HH:mm"));
            horaManual = LocalDateTime.of(LocalDate.now(), hora);
            liberarVencidos(horaManual);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato inválido (use HH:mm, por ejemplo 14:30)");
        }
    }
    
    private void liberarVencidos(LocalDateTime hora) {
        for (int i = 0; i < puestos.length; i++) {
            Puesto p = puestos[i];
            if (p.isOcupado() && p.getHoraSalida() != null && !hora.isBefore(p.getHoraSalida())) {
                liberarPuesto(i);
                JOptionPane.showMessageDialog(this, "El puesto P" + (i + 1) + " ya quedó libre");
            }
        }
    }

    private void liberarPuesto(int i) {
        puestos[i].liberar();
        model.setValueAt("P", filaDe(i), columnaDe(i));
        guardarDatos();
    }

    private int indiceDe(int row, int col) {
        if (col == COLUMNA_PASILLO) {
            return -1;
        }
        int c = (col < COLUMNA_PASILLO) ? col : col - 1;
        return row * PUESTOS_POR_FILA + c;
    }

    private int filaDe(int i) {
        return i / PUESTOS_POR_FILA;
    }

    private int columnaDe(int i) {
        int c = i % PUESTOS_POR_FILA;
        return (c < COLUMNA_PASILLO) ? c : c + 1;
    }


    private void clickEnCelda(int row, int col) {
        String val = String.valueOf(model.getValueAt(row, col));

        if (val.equals("X")) {
            JOptionPane.showMessageDialog(this, "Pasillo");
            return;
        }
        if (val.equals("E")) {
            JOptionPane.showMessageDialog(this, "Entrada del parqueadero");
            return;
        }
        if (val.equals("S")) {
            JOptionPane.showMessageDialog(this, "Salida del parqueadero");
            return;
        }

        int index = indiceDe(row, col);
        Puesto p = puestos[index];

        if (p.isOcupado()) {
            JOptionPane.showMessageDialog(this,
                    "Puesto P" + (index + 1) + " ocupado\n"
                    + "Nombre: " + p.getNombre() + "\n"
                    + "Vehículo: " + p.getVehiculo() + "\n"
                    + "Sale: " + p.getHoraSalida().format(formatoSalida));
            return;
        }
        if (p.tieneReserva()) {
            JOptionPane.showMessageDialog(this,
                    "Puesto P" + (index + 1) + " reservado por " + p.getReserva());
            return;
        }

        registrarEntrada(index);
    }

    private void registrarEntrada(int index) {
        String nombre = pedirNombre();
        if (nombre == null) {
            return;
        }
        String documento = pedirDocumento();
        if (documento == null) {
            return;
        }
        String placa = pedirPlaca();
        if (placa == null) {
            return;
        }
        Vehiculo vehiculo = pedirVehiculo(placa);
        if (vehiculo == null) {
            return;
        }
        Integer horas = pedirHoras();
        if (horas == null) {
            return;
        }

        Puesto p = puestos[index];
        p.ocupar(nombre, documento, horas, vehiculo, ahora());

        model.setValueAt("R", filaDe(index), columnaDe(index));
        guardarDatos();

        JOptionPane.showMessageDialog(this,
                "Puesto P" + (index + 1) + " asignado\n"
                + vehiculo + " - " + horas + " hora(s) x $" + vehiculo.getTarifaPorHora() + "\n"
                + "Total: $" + p.getTotal() + "\n"
                + "Sale: " + p.getHoraSalida().format(formatoSalida));
    }

    private String pedirNombre() {
        while (true) {
            String nom = JOptionPane.showInputDialog(this, "Nombre:");
            if (nom == null) {
                return null;
            }
            nom = nom.trim();
            if (!nom.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                JOptionPane.showMessageDialog(this, "Nombre inválido");
                continue;
            }
            if (nom.split("\\s+").length < 2) {
                JOptionPane.showMessageDialog(this, "Ingrese nombre y apellido");
                continue;
            }
            return nom;
        }
    }

    private String pedirDocumento() {
        while (true) {
            String doc = JOptionPane.showInputDialog(this, "Documento:");
            if (doc == null) {
                return null;
            }
            doc = doc.trim();
            if (doc.matches("\\d+") && doc.length() >= 7 && doc.length() <= 10) {
                return doc;
            }
            JOptionPane.showMessageDialog(this, "Cédula inválida");
        }
    }

    private String pedirPlaca() {
        while (true) {
            String pla = JOptionPane.showInputDialog(this, "Placa:");
            if (pla == null) {
                return null;
            }
            pla = pla.trim().toUpperCase();
            if (pla.matches("[A-Z]{3}[0-9]{2}[A-Z0-9]")) {
                return pla;
            }
            JOptionPane.showMessageDialog(this, "Placa inválida");
        }
    }

    private Vehiculo pedirVehiculo(String placa) {
        Object[] opciones = {"Moto", "Carro"};
        int r = JOptionPane.showOptionDialog(this, "Tipo de vehículo:", "Vehículo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        
        if (r == 0) {
            return new Moto(placa);
        }
        if (r == 1) {
            return new Auto(placa);
        }
        return null;
    }

    private Integer pedirHoras() {
        while (true) {
            String texto = JOptionPane.showInputDialog(this, "Horas (1 a 24):");
            if (texto == null) {
                return null;
            }
            try {
                int horas = Integer.parseInt(texto.trim());
                if (horas >= 1 && horas <= 24) {
                    return horas;
                }
            } catch (NumberFormatException e) {
                
            }
            JOptionPane.showMessageDialog(this, "Ingrese un número de horas entre 1 y 24");
        }
    }

    // ========== RESERVAR ========

    private void reservar() {
        String input = JOptionPane.showInputDialog(this, "Número de puesto:");
        if (input == null) {
            return;
        }
        try {
            int numero = Integer.parseInt(input.trim());
            if (numero < 1 || numero > puestos.length) {
                JOptionPane.showMessageDialog(this, "Número inválido (1 a " + puestos.length + ")");
                return;
            }
            int i = numero - 1;
            Puesto p = puestos[i];

            if (p.isOcupado() || p.tieneReserva()) {
                JOptionPane.showMessageDialog(this, "Ese puesto no está disponible");
                return;
            }

            String nombreReserva = JOptionPane.showInputDialog(this, "Nombre de quien reserva:");
            if (nombreReserva == null || nombreReserva.trim().isEmpty()) {
                return;
            }

            p.setReserva(nombreReserva);
            model.setValueAt("V", filaDe(i), columnaDe(i));
            guardarDatos();
            JOptionPane.showMessageDialog(this, "Puesto reservado");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese solo números");
        }
    }


    private void guardarDatos() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ARCHIVO))) {
            for (Puesto p : puestos) {
                bw.write(p.toCsvLine());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar " + ARCHIVO + ": " + e.getMessage());
        }
    }

    private void cargarDatos() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            int i = 0;
            while ((linea = br.readLine()) != null && i < puestos.length) {
                try {
                    Puesto p = Puesto.fromCsvLine(linea);
                    puestos[i] = p;
                    if (p.isOcupado()) {
                        model.setValueAt("R", filaDe(i), columnaDe(i));
                    } else if (p.tieneReserva()) {
                        model.setValueAt("V", filaDe(i), columnaDe(i));
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Línea inválida ignorada (puesto " + (i + 1) + "): " + linea);
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error al leer " + ARCHIVO + ": " + e.getMessage());
        }
    }

    // ========== ADMIN =========

    private void mostrarAdmin() {
        String clave = JOptionPane.showInputDialog(this, "Clave:");
        if (!CLAVE_ADMIN.equals(clave)) {
            return;
        }
        abrirVentanaAdmin();
    }

    private void abrirVentanaAdmin() {
        JFrame v = new JFrame("Admin");
        v.setLayout(new BorderLayout());

        String[] cols = {
            "Puesto", "Estado", "Nombre", "Documento",
            "Placa", "Horas", "Vehículo", "Total", "Ganancia acumulada"
        };

        Object[][] datos = new Object[puestos.length][cols.length];
        for (int i = 0; i < puestos.length; i++) {
            Puesto p = puestos[i];
            datos[i][0] = "P" + (i + 1);
            datos[i][1] = p.getEstado();
            datos[i][2] = p.getNombre();
            datos[i][3] = p.getDocumento();
            datos[i][4] = p.getPlaca();
            datos[i][5] = (p.getHoras() == 0) ? "" : p.getHoras();
            datos[i][6] = (p.getVehiculo() == null) ? "" : p.getVehiculo().getTipo();
            datos[i][7] = (p.getTotal() == 0) ? "" : p.getTotal();
            datos[i][8] = (p.getgananciaAcumulada() == 0) ? "" : p.getgananciaAcumulada();
        }

        JTable tabla = new JTable(datos, cols) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        if (consumoPanel == null) {
            consumoPanel = new ConsumoElectrico();
        }
        CardLayout cartas = new CardLayout();
        JPanel centro = new JPanel(cartas);
        centro.add(new JScrollPane(tabla), "puestos");
        centro.add(consumoPanel, "consumo");
        v.add(centro, BorderLayout.CENTER);

        JButton eliminar = new JButton("Eliminar puesto");
        eliminar.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(v, "Número de puesto:");
            if (input == null) {
                return;
            }
            try {
                int numero = Integer.parseInt(input.trim());
                if (numero < 1 || numero > puestos.length) {
                    JOptionPane.showMessageDialog(v, "Número inválido (1 a " + puestos.length + ")");
                    return;
                }
                liberarPuesto(numero - 1);
                JOptionPane.showMessageDialog(v, "Puesto eliminado correctamente");
                v.dispose();
                abrirVentanaAdmin();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(v, "Ingrese solo números");
            }
        });

        JButton grafica = new JButton("Ver gráfica");
        grafica.addActionListener(e -> mostrarGrafica());

        JButton grafica2 = new JButton("Gráfica 2");
        grafica2.addActionListener(e -> mostrarGrafica2());

        JButton consumoBtn = new JButton("Consumo eléctrico");
        consumoBtn.addActionListener(e -> {
            if (consumoBtn.getText().equals("Consumo eléctrico")) {
                cartas.show(centro, "consumo");
                consumoBtn.setText("Ver puestos");
            } else {
                cartas.show(centro, "puestos");
                consumoBtn.setText("Consumo eléctrico");
            }
        });

        JButton aceleracionBtn = new JButton("Aceleración");
        aceleracionBtn.addActionListener(e -> calcularAceleracion(v));

        JPanel panel = new JPanel();
        panel.add(eliminar);
        panel.add(grafica);
        panel.add(grafica2);
        panel.add(consumoBtn);
        panel.add(aceleracionBtn);
        v.add(panel, BorderLayout.SOUTH);

        v.setSize(1000, 540);
        v.setLocationRelativeTo(this);
        v.setVisible(true);
    }

    private void calcularAceleracion(JFrame padre) {
        String sMarzo = JOptionPane.showInputDialog(padre, "Parqueaderos registrados en marzo:");
        if (sMarzo == null) {
            return;
        }
        String sMayo = JOptionPane.showInputDialog(padre, "Parqueaderos registrados en mayo:");
        if (sMayo == null) {
            return;
        }
        try {
            int marzo = Integer.parseInt(sMarzo.trim());
            int mayo = Integer.parseInt(sMayo.trim());
            double aceleracion = (double) (mayo - marzo) / 2;
            JOptionPane.showMessageDialog(padre,
                    "Aceleración de incremento: " + aceleracion + " registros por mes²");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(padre, "Ingrese números válidos");
        }
    }

    // ========== GRAFICA (JFreeChart) ==========
    private void mostrarGrafica() {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < puestos.length; i++) {
                dataset.addValue(puestos[i].getgananciaAcumulada(), "Ganancia", "P" + (i + 1));
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Ganancias por puesto", "Puestos", "Dinero ($)", dataset,
                    PlotOrientation.VERTICAL, false, true, false);

            abrirVentanaGrafica("Gráfica 1 - Ganancias", chart);
        } catch (Throwable ex) {
            errorGrafica(ex);
        }
    }

    private void mostrarGrafica2() {
        JTextField txtM = new JTextField("2", 6);
        JTextField txtB = new JTextField("1", 6);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("m (pendiente):"));
        form.add(txtM);
        form.add(new JLabel("b (intercepto):"));
        form.add(txtB);

        int r = JOptionPane.showConfirmDialog(this, form, "Recta y = mx + b",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        double m;
        double b;
        try {
            m = Double.parseDouble(txtM.getText().trim().replace(',', '.'));
            b = Double.parseDouble(txtB.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese números válidos para m y b");
            return;
        }

        try {
            String nombreRecta = "y = " + numero(m) + "x " + (b < 0 ? "- " : "+ ") + numero(Math.abs(b));
            XYSeries recta = new XYSeries(nombreRecta);
            XYSeries parabola = new XYSeries("y = 3x² + 1");

            for (double x = -5; x <= 5.0001; x += 0.25) {
                recta.add(x, m * x + b);
                parabola.add(x, 3 * x * x + 1);
            }

            XYSeriesCollection datos = new XYSeriesCollection();
            datos.addSeries(recta);
            datos.addSeries(parabola);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Funciones", "x", "y", datos,
                    PlotOrientation.VERTICAL, true, true, false);

            abrirVentanaGrafica("Gráfica 2 - Funciones", chart);
        } catch (Throwable ex) {
            errorGrafica(ex);
        }
    }
    
    private static String numero(double v) {
        return (v == Math.rint(v)) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private void abrirVentanaGrafica(String titulo, JFreeChart chart) {
        ChartFrame frame = new ChartFrame(titulo, chart);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
        frame.toFront();
    }

    private void errorGrafica(Throwable ex) {
        JOptionPane.showMessageDialog(this,
                "No se pudo abrir la gráfica:\n" + ex,
                "Error de gráfica", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================================
    //  CONSUMO ELECTRICO (se ve con el boton "Consumo electrico" de ADMIN)
    // =====================================================================

    private static class Elemento {

        private static final int DIAS_MES = 30;
        private static final double MAXIMO = 1_000_000;

        private String nombre;
        private double cantidad;
        private double potenciaW;
        private double horasDia;

        public Elemento(String nombre, double cantidad, double potenciaW, double horasDia) {
            setNombre(nombre);
            setCantidad(cantidad);
            setPotenciaW(potenciaW);
            setHorasDia(horasDia);
        }

        public String getNombre() {
            return nombre;
        }

        public double getCantidad() {
            return cantidad;
        }

        public double getPotenciaW() {
            return potenciaW;
        }

        public double getHorasDia() {
            return horasDia;
        }

        public final void setNombre(String nombre) {
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            }
            this.nombre = nombre.trim();
        }

        public final void setCantidad(double cantidad) {
            validarRango(cantidad, 0, MAXIMO, "La cantidad");
            this.cantidad = cantidad;
        }

        public final void setPotenciaW(double potenciaW) {
            validarRango(potenciaW, 0, MAXIMO, "La potencia");
            this.potenciaW = potenciaW;
        }

        public final void setHorasDia(double horasDia) {
            validarRango(horasDia, 0, 24, "Las horas");
            this.horasDia = horasDia;
        }

        private static void validarRango(double valor, double min, double max, String campo) {
            if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < min || valor > max) {
                throw new IllegalArgumentException(campo + " debe ser un número entre " + formato(min) + " y " + formato(max));
            }
        }

        private static String formato(double v) {
            return (v == Math.rint(v)) ? String.valueOf((long) v) : String.valueOf(v);
        }

        public double consumoDiarioKWh() {
            return cantidad * potenciaW * horasDia / 1000.0;
        }

        public double costoDiario(double precioKWh) {
            return consumoDiarioKWh() * precioKWh;
        }

        public double costoMensual(double precioKWh) {
            return costoDiario(precioKWh) * DIAS_MES;
        }
    }

    private static class ConsumoElectrico extends JPanel {

        private static final int COL_CANTIDAD = 1;
        private static final int COL_POTENCIA = 2;
        private static final int COL_HORAS = 3;

        private final List<Elemento> elementos = new ArrayList<>();
        private double precioKWh = 1239.98;
        private final NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));

        private final DefaultTableModel modelo;
        private final JTable tabla;
        private final JLabel lblPrecio = new JLabel();
        private final JLabel lblTotales = new JLabel();

        public ConsumoElectrico() {
            setLayout(new BorderLayout());

            elementos.add(new Elemento("Luminarias LED", 40, 18, 12));
            elementos.add(new Elemento("Cámaras de seguridad", 8, 12, 24));
            elementos.add(new Elemento("Barrera de entrada", 1, 250, 2));
            elementos.add(new Elemento("Barrera de salida", 1, 250, 2));
            elementos.add(new Elemento("Computador de administración", 1, 150, 10));
            elementos.add(new Elemento("Pantalla informativa", 1, 100, 24));

            String[] columnas = {
                "Elemento", "Cantidad", "Potencia (W)", "Horas/día",
                "Consumo diario", "Costo diario", "Costo mensual"
            };
            modelo = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };
            tabla = new JTable(modelo);
            tabla.setRowHeight(40);
            int[] anchos = {230, 80, 100, 80, 120, 150, 170};
            for (int c = 0; c < anchos.length; c++) {
                tabla.getColumnModel().getColumn(c).setPreferredWidth(anchos[c]);
            }
            tabla.getColumnModel().getColumn(0).setCellRenderer(new RendererTextoMultilinea());

            DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
            centro.setHorizontalAlignment(SwingConstants.CENTER);
            for (int c = 1; c < columnas.length; c++) {
                tabla.getColumnModel().getColumn(c).setCellRenderer(centro);
            }

            tabla.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() != 2) {
                        return;
                    }
                    int fila = tabla.rowAtPoint(e.getPoint());
                    int col = tabla.columnAtPoint(e.getPoint());
                    if (fila >= 0 && (col == COL_CANTIDAD || col == COL_POTENCIA || col == COL_HORAS)) {
                        editarValor(fila, col);
                    }
                }
            });

            JLabel ayuda = new JLabel("Doble clic en Cantidad, Potencia o Horas/día para cambiar el valor");
            ayuda.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

            JButton btnPrecio = new JButton("Cambiar precio del kWh");
            btnPrecio.addActionListener(e -> cambiarPrecio());

            JButton btnAgregar = new JButton("Agregar elemento");
            btnAgregar.addActionListener(e -> agregarElemento());

            JButton btnEliminar = new JButton("Eliminar seleccionado");
            btnEliminar.addActionListener(e -> eliminarElemento());

            JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            botones.add(btnPrecio);
            botones.add(btnAgregar);
            botones.add(btnEliminar);
            botones.add(lblPrecio);

            lblTotales.setFont(new Font("Arial", Font.BOLD, 14));
            lblTotales.setBorder(BorderFactory.createEmptyBorder(6, 12, 10, 12));

            JPanel sur = new JPanel(new BorderLayout());
            sur.add(botones, BorderLayout.NORTH);
            sur.add(lblTotales, BorderLayout.SOUTH);

            add(ayuda, BorderLayout.NORTH);
            add(new JScrollPane(tabla), BorderLayout.CENTER);
            add(sur, BorderLayout.SOUTH);

            refrescar();
        }

        // ======== TABLA ========

        private void refrescar() {
            modelo.setRowCount(0);
            double totalKWh = 0;
            double totalDiario = 0;
            double totalMensual = 0;

            for (Elemento el : elementos) {
                double consumo = el.consumoDiarioKWh();
                double diario = el.costoDiario(precioKWh);
                double mensual = el.costoMensual(precioKWh);
                totalKWh += consumo;
                totalDiario += diario;
                totalMensual += mensual;

                modelo.addRow(new Object[]{
                    el.getNombre(),
                    numero(el.getCantidad()),
                    numero(el.getPotenciaW()),
                    numero(el.getHorasDia()),
                    String.format("%.3f kWh", consumo),
                    moneda(diario),
                    moneda(mensual)
                });
            }

            lblPrecio.setText("Precio kWh: " + moneda(precioKWh));
            lblTotales.setText(String.format("TOTAL: %.3f kWh/día   |   Costo diario: %s   |   Costo mensual (30 días): %s",
                    totalKWh, moneda(totalDiario), moneda(totalMensual)));
        }

        private String moneda(double valor) {
            return formatoMoneda.format(valor) + " COP";
        }

        private static String numero(double v) {
            return (v == Math.rint(v)) ? String.valueOf((long) v) : String.valueOf(v);
        }


        private void editarValor(int fila, int col) {
            Elemento el = elementos.get(fila);
            String que = (col == COL_CANTIDAD) ? "la cantidad"
                    : (col == COL_POTENCIA) ? "la potencia en W" : "las horas de uso promedio diario";

            while (true) {
                String entrada = JOptionPane.showInputDialog(this,
                        "Ingrese " + que + " de " + el.getNombre() + ":");
                if (entrada == null) {
                    return;
                }
                try {
                    double valor = parseNumero(entrada, col == COL_POTENCIA);
                    switch (col) {
                        case COL_CANTIDAD -> el.setCantidad(valor);
                        case COL_POTENCIA -> el.setPotenciaW(valor);
                        default -> el.setHorasDia(valor);
                    }
                    refrescar();
                    return;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        }

        private static double parseNumero(String entrada, boolean permitirW) {
            String texto = entrada.trim();
            if (permitirW && (texto.endsWith("W") || texto.endsWith("w"))) {
                texto = texto.substring(0, texto.length() - 1).trim();
            }
            try {
                double v = Double.parseDouble(texto.replace(',', '.'));
                if (Double.isNaN(v) || Double.isInfinite(v)) {
                    throw new NumberFormatException();
                }
                return v;
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Ingrese un número válido. Puede usar decimales.");
            }
        }

        private void cambiarPrecio() {
            while (true) {
                String entrada = JOptionPane.showInputDialog(this,
                        "Precio del kWh en COP:", numero(precioKWh));
                if (entrada == null) {
                    return;
                }
                try {
                    double nuevo = parseNumero(entrada, false);
                    if (nuevo < 0) {
                        throw new IllegalArgumentException("El precio no puede ser negativo");
                    }
                    precioKWh = nuevo;
                    refrescar();
                    return;
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        }

        private void agregarElemento() {
            JTextField txtNombre = new JTextField(15);
            JTextField txtCantidad = new JTextField("1", 15);
            JTextField txtPotencia = new JTextField(15);
            JTextField txtHoras = new JTextField(15);

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel("Elemento:"));
            form.add(txtNombre);
            form.add(new JLabel("Cantidad:"));
            form.add(txtCantidad);
            form.add(new JLabel("Potencia (W):"));
            form.add(txtPotencia);
            form.add(new JLabel("Horas de uso por día (0 a 24):"));
            form.add(txtHoras);

            int r = JOptionPane.showConfirmDialog(this, form, "Agregar elemento",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                elementos.add(new Elemento(
                        txtNombre.getText(),
                        parseNumero(txtCantidad.getText(), false),
                        parseNumero(txtPotencia.getText(), true),
                        parseNumero(txtHoras.getText(), false)));
                refrescar();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void eliminarElemento() {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Primero selecciona un elemento de la tabla.");
                return;
            }
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Eliminar \"" + elementos.get(fila).getNombre() + "\"?",
                    "Eliminar", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                elementos.remove(fila);
                refrescar();
            }
        }

        private static class RendererTextoMultilinea extends JTextArea implements TableCellRenderer {

            RendererTextoMultilinea() {
                setLineWrap(true);
                setWrapStyleWord(true);
                setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                    boolean tieneFoco, int fila, int columna) {
                setText(valor == null ? "" : valor.toString());
                setFont(t.getFont());
                setForeground(seleccionada ? t.getSelectionForeground() : t.getForeground());
                setBackground(seleccionada ? t.getSelectionBackground() : t.getBackground());
                return this;
            }
        }
    }

    // ========= COLORES DE LA TABLA ======

    private class Renderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean s, boolean f, int r, int c) {

            super.getTableCellRendererComponent(t, v, false, false, r, c);
            setHorizontalAlignment(CENTER);
            setForeground(Color.BLACK);

            String valor = String.valueOf(v);
            switch (valor) {
                case "X" -> {
                    setBackground(Color.LIGHT_GRAY);
                    setText("");
                }
                case "E" -> {
                    setBackground(Color.GREEN);
                    setText("ENTRADA");
                }
                case "S" -> {
                    setBackground(Color.PINK);
                    setText("SALIDA");
                }
                case "R" -> {
                    setBackground(Color.DARK_GRAY);
                    setForeground(Color.WHITE);
                    setText("OCUPADO");
                }
                case "V" -> {
                    setBackground(Color.YELLOW);
                    setText("RESERVADO");
                }
                default -> {
                    setBackground(Color.WHITE);
                    setText("P" + (indiceDe(r, c) + 1));
                }
            }
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ParqueaderoPro().setVisible(true));
    }
}
