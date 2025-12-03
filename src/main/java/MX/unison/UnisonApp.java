package MX.unison;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;

public class UnisonApp extends JFrame {
    private JTextField tfUsuario;
    private JPasswordField pfContrasena;
    private JButton btnLogin;
    private JLabel lbStatus;

    private JPanel mainPanel;
    private CardLayout cardLayout;
    private String usuarioActual;
    private String rolActual;

    // Constantes para los nombres de los paneles
    private static final String PANEL_LOGIN = "LOGIN";
    private static final String PANEL_MENU = "MENU";
    private static final String PANEL_ALMACENES = "ALMACENES";
    private static final String PANEL_PRODUCTOS = "PRODUCTOS";
    private static final String PANEL_FORM_ALMACEN = "FORM_ALMACEN";
    private static final String PANEL_FORM_PRODUCTO = "FORM_PRODUCTO";

    // Colores UNISON para fondos
    private static final Color AZUL_UNISON = Color.decode("#00529e");
    private static final Color AZUL_OSCURO_UNISON = Color.decode("#015294");
    private static final Color DORADO_UNISON = Color.decode("#f8bb00");
    private static final Color DORADO_OSCURO_UNISON = Color.decode("#d99e30");

    // Lista de departamentos para el ComboBox
    private static final String[] DEPARTAMENTOS = {
            "Electrónica", "Ropa", "Alimentos", "Hogar", "Jardinería",
            "Oficina", "Salud", "Deportes", "Juguetes", "Automotriz",
            "Herramientas", "Libros", "Música", "Películas", "Jardín",
            "Cocina", "Baño", "Dormitorio", "Sala", "Tecnología"
    };

    // Variables para formularios
    private JTextField tfNombreAlmacen;
    private JTextField tfNombreProducto;
    private JTextField tfPrecioProducto;
    private JTextField tfCantidadProducto;
    private JComboBox<String> cbDepartamentoProducto;
    private JComboBox<String> cbAlmacenProducto;

    // Variables para filtros de productos
    private JTextField tfFiltroNombre;
    private JTextField tfFiltroPrecioMin;
    private JTextField tfFiltroPrecioMax;
    private JTextField tfFiltroCantidadMin;
    private JTextField tfFiltroCantidadMax;
    private JTextField tfFiltroDepartamento;
    private JTextField tfFiltroAlmacen;
    private JTextField tfFiltroFechaCreacion;
    private JTextField tfFiltroFechaModificacion;
    private JTextField tfFiltroUsuario;

    // Variables para filtros de almacenes
    private JTextField tfFiltroAlmacenID;
    private JTextField tfFiltroAlmacenNombre;
    private JTextField tfFiltroAlmacenFechaCreacion;
    private JTextField tfFiltroAlmacenFechaModificacion;
    private JTextField tfFiltroAlmacenUsuario;

    // Table model y sorter para productos
    private DefaultTableModel productosTableModel;
    private TableRowSorter<DefaultTableModel> productosTableSorter;

    // Table model y sorter para almacenes
    private DefaultTableModel almacenesTableModel;
    private TableRowSorter<DefaultTableModel> almacenesTableSorter;

    // Clase interna para crear bordes redondeados
    static class RoundedBorder extends AbstractBorder {
        private Color color;
        private int radius;
        private int thickness;

        RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness/2, y + thickness/2,
                    width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = thickness + 2;
            return insets;
        }
    }

    // Clase para botones redondeados con efectos hover
    static class RoundedButton extends JButton {
        private Color backgroundColor;
        private Color hoverColor;
        private Color pressedColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));

            addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    isHovered = true;
                    repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    isHovered = false;
                    repaint();
                }

                public void mousePressed(java.awt.event.MouseEvent evt) {
                    isPressed = true;
                    repaint();
                }

                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setBackgroundColor(Color bg) {
            this.backgroundColor = bg;
            this.hoverColor = bg.brighter();
            this.pressedColor = bg.darker();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color currentColor = backgroundColor;
            if (isPressed) {
                currentColor = pressedColor;
            } else if (isHovered) {
                currentColor = hoverColor;
            }

            g2.setColor(currentColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Clase para campos de texto redondeados
    static class RoundedTextField extends JTextField {
        private int radius = 8;

        public RoundedTextField() {
            super();
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(new Color(200, 200, 200));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No pintar el borde por defecto
        }
    }

    // Clase para campos de contraseña redondeados
    static class RoundedPasswordField extends JPasswordField {
        private int radius = 8;

        public RoundedPasswordField() {
            super();
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.setColor(new Color(0, 0, 0));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) {
            // No pintar el borde por defecto
        }
    }

    public UnisonApp() {
        super("Inicio de sesión - UNISON");

        // INICIALIZAR BASE DE DATOS
        DatabaseManager.initializeDatabase();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setResizable(true);

        // Configurar CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);

        // Crear paneles
        mainPanel.add(crearLoginPanel(), PANEL_LOGIN);
        mainPanel.add(crearMenuPanel(), PANEL_MENU);
        mainPanel.add(crearAlmacenesPanel(), PANEL_ALMACENES);
        mainPanel.add(crearProductosPanel(), PANEL_PRODUCTOS);
        mainPanel.add(crearFormAlmacenPanel(), PANEL_FORM_ALMACEN);
        mainPanel.add(crearFormProductoPanel(), PANEL_FORM_PRODUCTO);

        cardLayout.show(mainPanel, PANEL_LOGIN);
    }

    // Método para generar hash SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash", e);
        }
    }

    private JPanel crearLoginPanel() {
        Color colorPrimario = new Color(0, 51, 102);
        Color colorSecundario = new Color(0, 102, 204);

        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBackground(AZUL_UNISON);

        // Panel del formulario
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // Título
        JLabel titulo = new JLabel("Iniciar Sesión");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(colorPrimario);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Subtítulo
        JLabel subtitulo = new JLabel("Ingresa tus credenciales");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(Color.GRAY);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        formPanel.add(titulo);
        formPanel.add(subtitulo);

        // Campo Usuario
        JLabel lbUser = new JLabel("Usuario:");
        lbUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbUser.setForeground(colorPrimario);
        lbUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbUser.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        tfUsuario = new RoundedTextField();
        tfUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        tfUsuario.setBackground(Color.WHITE);
        tfUsuario.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Campo Contraseña
        JLabel lbPass = new JLabel("Contraseña:");
        lbPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbPass.setForeground(colorPrimario);
        lbPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbPass.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        pfContrasena = new RoundedPasswordField();
        pfContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pfContrasena.setAlignmentX(Component.CENTER_ALIGNMENT);
        pfContrasena.setBackground(Color.WHITE);
        pfContrasena.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Botón de login
        btnLogin = new RoundedButton("INICIAR SESIÓN");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ((RoundedButton)btnLogin).setBackgroundColor(colorPrimario);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setPreferredSize(new Dimension(200, 45));

        // Action Listener
        btnLogin.addActionListener(e -> realizarLogin());

        // Label de estado
        lbStatus = new JLabel(" ");
        lbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbStatus.setForeground(Color.RED);
        lbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbStatus.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Agregar componentes al formPanel
        formPanel.add(lbUser);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(tfUsuario);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        formPanel.add(lbPass);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(pfContrasena);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        formPanel.add(btnLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(lbStatus);

        loginPanel.add(formPanel, BorderLayout.CENTER);

        return loginPanel;
    }

    private JPanel crearMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(AZUL_UNISON);

        // Panel de información del usuario en la parte superior
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(DORADO_OSCURO_UNISON);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        if (usuarioActual != null && rolActual != null) {
            JLabel lblBienvenido = new JLabel("¡Bienvenido a la UNISON!");
            lblBienvenido.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblBienvenido.setForeground(Color.WHITE);
            lblBienvenido.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblUsuario = new JLabel("Usuario: " + usuarioActual);
            lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblUsuario.setForeground(Color.WHITE);
            lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblRol = new JLabel("Rol: " + rolActual.toUpperCase());
            lblRol.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblRol.setForeground(Color.WHITE);
            lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblPermisos = new JLabel(getDescripcionPermisos());
            lblPermisos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblPermisos.setForeground(Color.WHITE);
            lblPermisos.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(lblBienvenido);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            infoPanel.add(lblUsuario);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(lblRol);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            infoPanel.add(lblPermisos);
        }

        menuPanel.add(infoPanel, BorderLayout.NORTH);

        // Panel principal para el contenido
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(AZUL_UNISON);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // Logo de la UNISON
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(AZUL_UNISON);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Cargar imagen del logo
        try {
            InputStream is = getClass().getResourceAsStream("/logo_unison.png");
            BufferedImage img = null;
            if (is != null) {
                img = ImageIO.read(is);
            } else {
                String[] possiblePaths = {
                        "resources/logo_unison.png",
                        "src/resources/logo_unison.png",
                        "logo_unison.png"
                };
                for (String path : possiblePaths) {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        img = ImageIO.read(file);
                        break;
                    }
                }
            }

            if (img != null) {
                int originalWidth = img.getWidth();
                int originalHeight = img.getHeight();
                int newWidth = 150;
                int newHeight = (originalHeight * newWidth) / originalWidth;

                Image scaled = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                logoLabel.setIcon(new ImageIcon(scaled));
            } else {
                logoLabel.setText("UNIVERSIDAD DE SONORA");
                logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
                logoLabel.setForeground(Color.WHITE);
            }
        } catch (Exception e) {
            logoLabel.setText("UNIVERSIDAD DE SONORA");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            logoLabel.setForeground(Color.WHITE);
        }

        logoPanel.add(logoLabel);
        contentPanel.add(logoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Título de la aplicación
        JLabel lblTituloApp = new JLabel("INVENTARIO");
        lblTituloApp.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblTituloApp.setForeground(Color.WHITE);
        lblTituloApp.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nombre del autor
        JLabel lblAutor = new JLabel("Guillermo Vazquez Rojas");
        lblAutor.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblAutor.setForeground(Color.WHITE);
        lblAutor.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(lblTituloApp);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(lblAutor);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Botones para las tablas
        RoundedButton btnAlmacenes = new RoundedButton("Almacenes");
        btnAlmacenes.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnAlmacenes.setBackgroundColor(DORADO_UNISON);
        btnAlmacenes.setForeground(Color.BLACK);
        btnAlmacenes.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAlmacenes.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAlmacenes.setMaximumSize(new Dimension(180, 50));
        btnAlmacenes.setPreferredSize(new Dimension(180, 50));
        btnAlmacenes.addActionListener(e -> recargarPanelAlmacenes());

        RoundedButton btnProductos = new RoundedButton("Productos");
        btnProductos.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnProductos.setBackgroundColor(DORADO_UNISON);
        btnProductos.setForeground(Color.BLACK);
        btnProductos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProductos.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnProductos.setMaximumSize(new Dimension(180, 50));
        btnProductos.setPreferredSize(new Dimension(180, 50));
        btnProductos.addActionListener(e -> recargarPanelProductos());

        contentPanel.add(btnAlmacenes);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(btnProductos);

        menuPanel.add(contentPanel, BorderLayout.CENTER);

        return menuPanel;
    }

    private String getDescripcionPermisos() {
        if (rolActual == null) return "";

        switch (rolActual) {
            case "admin":
                return "Permisos: Acceso completo a todas las funciones";
            case "almacen":
                return "Permisos: Gestión de almacenes y consulta de productos";
            case "productos":
                return "Permisos: Gestión de productos y consulta de almacenes";
            default:
                return "Permisos: Consulta básica";
        }
    }

    private JPanel crearAlmacenesPanel() {
        JPanel almacenesPanel = new JPanel(new BorderLayout());
        almacenesPanel.setBackground(AZUL_UNISON);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DORADO_UNISON);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Botón para regresar al menú
        RoundedButton btnRegresar = new RoundedButton("← Menú Principal");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegresar.setBackgroundColor(new Color(0, 51, 102));
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegresar.addActionListener(e -> cardLayout.show(mainPanel, PANEL_MENU));

        JLabel titleLabel = new JLabel("📋 Lista de Almacenes");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel rolLabel = new JLabel("Rol: " + (rolActual != null ? rolActual.toUpperCase() : ""));
        rolLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rolLabel.setForeground(Color.BLACK);

        headerPanel.add(btnRegresar, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(rolLabel, BorderLayout.EAST);

        // Panel de filtros para almacenes
        JPanel filtrosPanel = new JPanel();
        filtrosPanel.setLayout(new BoxLayout(filtrosPanel, BoxLayout.Y_AXIS));
        filtrosPanel.setBackground(Color.WHITE);
        filtrosPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Fila 1 de filtros para almacenes
        JPanel fila1 = new JPanel(new GridLayout(2, 5, 10, 10));
        fila1.setBackground(Color.WHITE);

        // ID Almacén
        JPanel panelID = new JPanel(new BorderLayout(5, 5));
        panelID.setBackground(Color.WHITE);
        JLabel lblID = new JLabel("ID:");
        lblID.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroAlmacenID = new RoundedTextField();
        tfFiltroAlmacenID.setToolTipText("Filtrar por ID");
        panelID.add(lblID, BorderLayout.NORTH);
        panelID.add(tfFiltroAlmacenID, BorderLayout.CENTER);
        fila1.add(panelID);

        // Nombre Almacén
        JPanel panelNombreAlmacen = new JPanel(new BorderLayout(5, 5));
        panelNombreAlmacen.setBackground(Color.WHITE);
        JLabel lblNombreAlmacen = new JLabel("Nombre:");
        lblNombreAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroAlmacenNombre = new RoundedTextField();
        tfFiltroAlmacenNombre.setToolTipText("Filtrar por nombre");
        panelNombreAlmacen.add(lblNombreAlmacen, BorderLayout.NORTH);
        panelNombreAlmacen.add(tfFiltroAlmacenNombre, BorderLayout.CENTER);
        fila1.add(panelNombreAlmacen);

        // Fecha Creación
        JPanel panelFechaCreacionAlmacen = new JPanel(new BorderLayout(5, 5));
        panelFechaCreacionAlmacen.setBackground(Color.WHITE);
        JLabel lblFechaCreacionAlmacen = new JLabel("Fecha Creación:");
        lblFechaCreacionAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroAlmacenFechaCreacion = new RoundedTextField();
        tfFiltroAlmacenFechaCreacion.setToolTipText("Ej: 2025-12-01");
        panelFechaCreacionAlmacen.add(lblFechaCreacionAlmacen, BorderLayout.NORTH);
        panelFechaCreacionAlmacen.add(tfFiltroAlmacenFechaCreacion, BorderLayout.CENTER);
        fila1.add(panelFechaCreacionAlmacen);

        // Fecha Modificación
        JPanel panelFechaModificacionAlmacen = new JPanel(new BorderLayout(5, 5));
        panelFechaModificacionAlmacen.setBackground(Color.WHITE);
        JLabel lblFechaModificacionAlmacen = new JLabel("Última Modificación:");
        lblFechaModificacionAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroAlmacenFechaModificacion = new RoundedTextField();
        tfFiltroAlmacenFechaModificacion.setToolTipText("Ej: 2025-12-01");
        panelFechaModificacionAlmacen.add(lblFechaModificacionAlmacen, BorderLayout.NORTH);
        panelFechaModificacionAlmacen.add(tfFiltroAlmacenFechaModificacion, BorderLayout.CENTER);
        fila1.add(panelFechaModificacionAlmacen);

        // Usuario
        JPanel panelUsuarioAlmacen = new JPanel(new BorderLayout(5, 5));
        panelUsuarioAlmacen.setBackground(Color.WHITE);
        JLabel lblUsuarioAlmacen = new JLabel("Último Usuario:");
        lblUsuarioAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroAlmacenUsuario = new RoundedTextField();
        tfFiltroAlmacenUsuario.setToolTipText("Filtrar por usuario");
        panelUsuarioAlmacen.add(lblUsuarioAlmacen, BorderLayout.NORTH);
        panelUsuarioAlmacen.add(tfFiltroAlmacenUsuario, BorderLayout.CENTER);
        fila1.add(panelUsuarioAlmacen);

        filtrosPanel.add(fila1);
        filtrosPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botones de filtro para almacenes
        JPanel panelBotonesFiltroAlmacen = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotonesFiltroAlmacen.setBackground(Color.WHITE);

        RoundedButton btnFiltrarAlmacen = new RoundedButton("🔍 Aplicar Filtros");
        btnFiltrarAlmacen.setBackgroundColor(new Color(0, 123, 255));
        btnFiltrarAlmacen.setForeground(Color.WHITE);
        btnFiltrarAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrarAlmacen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltrarAlmacen.addActionListener(e -> aplicarFiltrosAlmacenes());

        RoundedButton btnLimpiarFiltrosAlmacen = new RoundedButton("🧹 Limpiar Filtros");
        btnLimpiarFiltrosAlmacen.setBackgroundColor(new Color(108, 117, 125));
        btnLimpiarFiltrosAlmacen.setForeground(Color.WHITE);
        btnLimpiarFiltrosAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiarFiltrosAlmacen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarFiltrosAlmacen.addActionListener(e -> limpiarFiltrosAlmacenes());

        panelBotonesFiltroAlmacen.add(btnFiltrarAlmacen);
        panelBotonesFiltroAlmacen.add(btnLimpiarFiltrosAlmacen);

        filtrosPanel.add(panelBotonesFiltroAlmacen);

        // Panel superior con header y filtros
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filtrosPanel, BorderLayout.CENTER);

        almacenesPanel.add(topPanel, BorderLayout.NORTH);

        // Cargar datos desde la base de datos
        List<Object[]> almacenesDataList = DatabaseManager.obtenerAlmacenes();
        Object[][] almacenesData = almacenesDataList.toArray(new Object[0][]);

        String[] columnNames = {"ID", "Nombre", "Fecha Creación", "Última Modificación", "Último Usuario"};

        almacenesTableModel = new DefaultTableModel(almacenesData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return ("admin".equals(rolActual) || "almacen".equals(rolActual)) && (column == 1);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        JTable table = new JTable(almacenesTableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(0, 51, 102));
        table.getTableHeader().setForeground(Color.BLACK);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);

        // Configurar TableRowSorter para filtrado
        almacenesTableSorter = new TableRowSorter<>(almacenesTableModel);
        table.setRowSorter(almacenesTableSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        almacenesPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones de acción
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botonesPanel.setBackground(DORADO_UNISON);

        if ("admin".equals(rolActual) || "almacen".equals(rolActual)) {
            JPanel panelBotones = crearPanelBotonesAlmacenes(table);
            botonesPanel.add(panelBotones);
        } else {
            JLabel infoLabel = new JLabel("🔒 Solo lectura - Consulte con el administrador para modificaciones");
            infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            infoLabel.setForeground(Color.WHITE);
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            botonesPanel.add(infoLabel);
        }

        almacenesPanel.add(botonesPanel, BorderLayout.SOUTH);

        return almacenesPanel;
    }

    private void aplicarFiltrosAlmacenes() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Filtro por ID
        if (!tfFiltroAlmacenID.getText().trim().isEmpty()) {
            try {
                int id = Integer.parseInt(tfFiltroAlmacenID.getText().trim());
                filters.add(new RowFilter<Object, Object>() {
                    @Override
                    public boolean include(Entry<? extends Object, ? extends Object> entry) {
                        try {
                            Integer entryId = (Integer) entry.getValue(0);
                            return entryId.equals(id);
                        } catch (Exception e) {
                            return false;
                        }
                    }
                });
            } catch (NumberFormatException e) {
                // Si no es un número, buscar como texto
                filters.add(RowFilter.regexFilter("(?i)" + tfFiltroAlmacenID.getText().trim(), 0));
            }
        }

        // Filtro por nombre
        if (!tfFiltroAlmacenNombre.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroAlmacenNombre.getText().trim(), 1));
        }

        // Filtro por fecha creación
        if (!tfFiltroAlmacenFechaCreacion.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroAlmacenFechaCreacion.getText().trim(), 2));
        }

        // Filtro por fecha modificación
        if (!tfFiltroAlmacenFechaModificacion.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroAlmacenFechaModificacion.getText().trim(), 3));
        }

        // Filtro por usuario
        if (!tfFiltroAlmacenUsuario.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroAlmacenUsuario.getText().trim(), 4));
        }

        if (filters.isEmpty()) {
            almacenesTableSorter.setRowFilter(null);
        } else {
            almacenesTableSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void limpiarFiltrosAlmacenes() {
        tfFiltroAlmacenID.setText("");
        tfFiltroAlmacenNombre.setText("");
        tfFiltroAlmacenFechaCreacion.setText("");
        tfFiltroAlmacenFechaModificacion.setText("");
        tfFiltroAlmacenUsuario.setText("");
        almacenesTableSorter.setRowFilter(null);
    }

    private JPanel crearPanelBotonesAlmacenes(JTable table) {
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botonesPanel.setBackground(DORADO_UNISON);

        // Botón Añadir (para admin y almacen)
        if ("admin".equals(rolActual) || "almacen".equals(rolActual)) {
            RoundedButton btnAnadir = new RoundedButton("➕ Añadir Almacén");
            btnAnadir.setBackgroundColor(new Color(40, 167, 69));
            btnAnadir.setForeground(Color.WHITE);
            btnAnadir.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAnadir.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAnadir.addActionListener(e -> mostrarFormAlmacen());
            botonesPanel.add(btnAnadir);
        }

        // Botón Eliminar (para admin y almacen)
        if ("admin".equals(rolActual) || "almacen".equals(rolActual)) {
            RoundedButton btnEliminar = new RoundedButton("🗑️ Eliminar");
            btnEliminar.setBackgroundColor(new Color(220, 53, 69));
            btnEliminar.setForeground(Color.WHITE);
            btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnEliminar.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "¿Estás seguro de que quieres eliminar el almacén seleccionado?",
                            "Confirmar Eliminación",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Object idObj = almacenesTableModel.getValueAt(modelRow, 0);
                        int id = 0;
                        if (idObj instanceof Integer) {
                            id = (Integer) idObj;
                        } else if (idObj != null) {
                            id = Integer.parseInt(idObj.toString());
                        }

                        if (DatabaseManager.eliminarAlmacen(id)) {
                            JOptionPane.showMessageDialog(this, "Almacén eliminado correctamente");
                            recargarPanelAlmacenes();
                        } else {
                            JOptionPane.showMessageDialog(this, "Error al eliminar el almacén");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Por favor, selecciona un almacén para eliminar.",
                            "Selección Requerida",
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            botonesPanel.add(btnEliminar);
        }

        // Botón Modificar (para admin y almacen)
        if ("admin".equals(rolActual) || "almacen".equals(rolActual)) {
            RoundedButton btnModificar = new RoundedButton("✏️ Modificar");
            btnModificar.setBackgroundColor(new Color(23, 162, 184));
            btnModificar.setForeground(Color.WHITE);
            btnModificar.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnModificar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnModificar.addActionListener(e -> {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    String nombreActual = (String) almacenesTableModel.getValueAt(modelRow, 1);
                    RoundedTextField nombreField = new RoundedTextField();
                    nombreField.setText(nombreActual);
                    nombreField.setPreferredSize(new Dimension(200, 40));
                    Object[] fields = {
                            "Nuevo nombre del almacén:", nombreField
                    };

                    int result = JOptionPane.showConfirmDialog(this, fields, "Modificar Almacén",
                            JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        String nuevoNombre = nombreField.getText().trim();
                        if (!nuevoNombre.isEmpty()) {
                            Object idObj = almacenesTableModel.getValueAt(modelRow, 0);
                            int id = 0;
                            if (idObj instanceof Integer) {
                                id = (Integer) idObj;
                            } else if (idObj != null) {
                                id = Integer.parseInt(idObj.toString());
                            }

                            if (DatabaseManager.actualizarAlmacen(id, nuevoNombre, usuarioActual)) {
                                JOptionPane.showMessageDialog(this, "Almacén modificado correctamente");
                                recargarPanelAlmacenes();
                            } else {
                                JOptionPane.showMessageDialog(this, "Error al modificar el almacén");
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Por favor, selecciona un almacén para modificar.",
                            "Selección Requerida",
                            JOptionPane.WARNING_MESSAGE);
                }
            });
            botonesPanel.add(btnModificar);
        }

        return botonesPanel;
    }

    private JPanel crearProductosPanel() {
        JPanel productosPanel = new JPanel(new BorderLayout());
        productosPanel.setBackground(AZUL_UNISON);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DORADO_UNISON);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Botón para regresar al menú
        RoundedButton btnRegresar = new RoundedButton("← Menú Principal");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegresar.setBackgroundColor(new Color(0, 51, 102));
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegresar.addActionListener(e -> cardLayout.show(mainPanel, PANEL_MENU));

        JLabel titleLabel = new JLabel("📦 Lista de Productos");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 51, 102));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel rolLabel = new JLabel("Rol: " + (rolActual != null ? rolActual.toUpperCase() : ""));
        rolLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        rolLabel.setForeground(Color.BLACK);

        headerPanel.add(btnRegresar, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(rolLabel, BorderLayout.EAST);

        // Panel de filtros
        JPanel filtrosPanel = new JPanel();
        filtrosPanel.setLayout(new BoxLayout(filtrosPanel, BoxLayout.Y_AXIS));
        filtrosPanel.setBackground(Color.WHITE);
        filtrosPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Fila 1 de filtros
        JPanel fila1 = new JPanel(new GridLayout(2, 4, 10, 10));
        fila1.setBackground(Color.WHITE);

        // Nombre
        JPanel panelNombre = new JPanel(new BorderLayout(5, 5));
        panelNombre.setBackground(Color.WHITE);
        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroNombre = new RoundedTextField();
        panelNombre.add(lblNombre, BorderLayout.NORTH);
        panelNombre.add(tfFiltroNombre, BorderLayout.CENTER);
        fila1.add(panelNombre);

        // Precio (rango)
        JPanel panelPrecio = new JPanel(new BorderLayout(5, 5));
        panelPrecio.setBackground(Color.WHITE);
        JLabel lblPrecio = new JLabel("Precio (Mín-Máx):");
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JPanel panelPrecioFields = new JPanel(new GridLayout(1, 2, 5, 5));
        panelPrecioFields.setBackground(Color.WHITE);
        tfFiltroPrecioMin = new RoundedTextField();
        tfFiltroPrecioMin.setToolTipText("Precio mínimo");
        tfFiltroPrecioMax = new RoundedTextField();
        tfFiltroPrecioMax.setToolTipText("Precio máximo");
        panelPrecioFields.add(tfFiltroPrecioMin);
        panelPrecioFields.add(tfFiltroPrecioMax);
        panelPrecio.add(lblPrecio, BorderLayout.NORTH);
        panelPrecio.add(panelPrecioFields, BorderLayout.CENTER);
        fila1.add(panelPrecio);

        // Cantidad (rango)
        JPanel panelCantidad = new JPanel(new BorderLayout(5, 5));
        panelCantidad.setBackground(Color.WHITE);
        JLabel lblCantidad = new JLabel("Cantidad (Mín-Máx):");
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JPanel panelCantidadFields = new JPanel(new GridLayout(1, 2, 5, 5));
        panelCantidadFields.setBackground(Color.WHITE);
        tfFiltroCantidadMin = new RoundedTextField();
        tfFiltroCantidadMin.setToolTipText("Cantidad mínima");
        tfFiltroCantidadMax = new RoundedTextField();
        tfFiltroCantidadMax.setToolTipText("Cantidad máxima");
        panelCantidadFields.add(tfFiltroCantidadMin);
        panelCantidadFields.add(tfFiltroCantidadMax);
        panelCantidad.add(lblCantidad, BorderLayout.NORTH);
        panelCantidad.add(panelCantidadFields, BorderLayout.CENTER);
        fila1.add(panelCantidad);

        // Departamento
        JPanel panelDepartamento = new JPanel(new BorderLayout(5, 5));
        panelDepartamento.setBackground(Color.WHITE);
        JLabel lblDepartamento = new JLabel("Departamento:");
        lblDepartamento.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroDepartamento = new RoundedTextField();
        panelDepartamento.add(lblDepartamento, BorderLayout.NORTH);
        panelDepartamento.add(tfFiltroDepartamento, BorderLayout.CENTER);
        fila1.add(panelDepartamento);

        // Almacén
        JPanel panelAlmacen = new JPanel(new BorderLayout(5, 5));
        panelAlmacen.setBackground(Color.WHITE);
        JLabel lblAlmacen = new JLabel("Almacén:");
        lblAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroAlmacen = new RoundedTextField();
        panelAlmacen.add(lblAlmacen, BorderLayout.NORTH);
        panelAlmacen.add(tfFiltroAlmacen, BorderLayout.CENTER);
        fila1.add(panelAlmacen);

        // Fecha Creación
        JPanel panelFechaCreacion = new JPanel(new BorderLayout(5, 5));
        panelFechaCreacion.setBackground(Color.WHITE);
        JLabel lblFechaCreacion = new JLabel("Fecha Creación:");
        lblFechaCreacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroFechaCreacion = new RoundedTextField();
        panelFechaCreacion.add(lblFechaCreacion, BorderLayout.NORTH);
        panelFechaCreacion.add(tfFiltroFechaCreacion, BorderLayout.CENTER);
        fila1.add(panelFechaCreacion);

        // Fecha Modificación
        JPanel panelFechaModificacion = new JPanel(new BorderLayout(5, 5));
        panelFechaModificacion.setBackground(Color.WHITE);
        JLabel lblFechaModificacion = new JLabel("Última Modificación:");
        lblFechaModificacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroFechaModificacion = new RoundedTextField();
        panelFechaModificacion.add(lblFechaModificacion, BorderLayout.NORTH);
        panelFechaModificacion.add(tfFiltroFechaModificacion, BorderLayout.CENTER);
        fila1.add(panelFechaModificacion);

        // Usuario
        JPanel panelUsuario = new JPanel(new BorderLayout(5, 5));
        panelUsuario.setBackground(Color.WHITE);
        JLabel lblUsuario = new JLabel("Último Usuario:");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tfFiltroUsuario = new RoundedTextField();
        panelUsuario.add(lblUsuario, BorderLayout.NORTH);
        panelUsuario.add(tfFiltroUsuario, BorderLayout.CENTER);
        fila1.add(panelUsuario);

        filtrosPanel.add(fila1);
        filtrosPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botones de filtro
        JPanel panelBotonesFiltro = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotonesFiltro.setBackground(Color.WHITE);

        RoundedButton btnFiltrar = new RoundedButton("🔍 Aplicar Filtros");
        btnFiltrar.setBackgroundColor(new Color(0, 123, 255));
        btnFiltrar.setForeground(Color.WHITE);
        btnFiltrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFiltrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltrar.addActionListener(e -> aplicarFiltros());

        RoundedButton btnLimpiarFiltros = new RoundedButton("🧹 Limpiar Filtros");
        btnLimpiarFiltros.setBackgroundColor(new Color(108, 117, 125));
        btnLimpiarFiltros.setForeground(Color.WHITE);
        btnLimpiarFiltros.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiarFiltros.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLimpiarFiltros.addActionListener(e -> limpiarFiltros());

        panelBotonesFiltro.add(btnFiltrar);
        panelBotonesFiltro.add(btnLimpiarFiltros);

        filtrosPanel.add(panelBotonesFiltro);

        // Panel superior con header y filtros
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filtrosPanel, BorderLayout.CENTER);

        productosPanel.add(topPanel, BorderLayout.NORTH);

        // Cargar datos desde la base de datos con nombres de almacén
        List<Object[]> productosDataList = DatabaseManager.obtenerProductosConNombresAlmacen();
        Object[][] productosData = productosDataList.toArray(new Object[0][]);

        String[] columnNames = {"ID", "Nombre", "Precio", "Cantidad", "Departamento", "Almacén",
                "Fecha Creación", "Última Modificación", "Último Usuario"};

        productosTableModel = new DefaultTableModel(productosData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return ("admin".equals(rolActual) || "productos".equals(rolActual)) &&
                        (column == 1 || column == 2 || column == 3 || column == 4 || column == 5);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {  // Precio
                    return Double.class;
                }
                if (columnIndex == 3) {  // Cantidad
                    return Integer.class;
                }
                if (columnIndex == 0) {  // ID
                    return Integer.class;
                }
                return String.class;
            }
        };

        JTable table = new JTable(productosTableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(0, 51, 102));
        table.getTableHeader().setForeground(Color.BLACK);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(150);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);
        table.getColumnModel().getColumn(8).setPreferredWidth(120);

        // Configurar TableRowSorter para filtrado
        productosTableSorter = new TableRowSorter<>(productosTableModel);
        table.setRowSorter(productosTableSorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Panel de botones de acción
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botonesPanel.setBackground(DORADO_UNISON);

        if ("admin".equals(rolActual) || "productos".equals(rolActual)) {
            JPanel panelBotones = crearPanelBotonesProductos(table);
            botonesPanel.add(panelBotones);
        } else {
            JLabel infoLabel = new JLabel("🔒 Solo lectura - Consulte con el administrador para modificaciones");
            infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            infoLabel.setForeground(Color.WHITE);
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            botonesPanel.add(infoLabel);
        }

        productosPanel.add(scrollPane, BorderLayout.CENTER);
        productosPanel.add(botonesPanel, BorderLayout.SOUTH);

        return productosPanel;
    }

    private void aplicarFiltros() {
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        // Filtro por nombre
        if (!tfFiltroNombre.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroNombre.getText().trim(), 1));
        }

        // Filtro por precio (rango)
        if (!tfFiltroPrecioMin.getText().trim().isEmpty() || !tfFiltroPrecioMax.getText().trim().isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    try {
                        Object precioObj = entry.getValue(2);
                        Double precio = 0.0;

                        if (precioObj instanceof Double) {
                            precio = (Double) precioObj;
                        } else if (precioObj instanceof Integer) {
                            precio = ((Integer) precioObj).doubleValue();
                        } else if (precioObj != null) {
                            precio = Double.parseDouble(precioObj.toString());
                        }

                        Double min = null, max = null;

                        if (!tfFiltroPrecioMin.getText().trim().isEmpty()) {
                            min = Double.parseDouble(tfFiltroPrecioMin.getText().trim());
                        }
                        if (!tfFiltroPrecioMax.getText().trim().isEmpty()) {
                            max = Double.parseDouble(tfFiltroPrecioMax.getText().trim());
                        }

                        if (min != null && precio < min) return false;
                        if (max != null && precio > max) return false;
                        return true;
                    } catch (Exception e) {
                        return true;
                    }
                }
            });
        }

        // Filtro por cantidad (rango)
        if (!tfFiltroCantidadMin.getText().trim().isEmpty() || !tfFiltroCantidadMax.getText().trim().isEmpty()) {
            filters.add(new RowFilter<Object, Object>() {
                @Override
                public boolean include(Entry<? extends Object, ? extends Object> entry) {
                    try {
                        Object cantidadObj = entry.getValue(3);
                        Integer cantidad = 0;

                        if (cantidadObj instanceof Integer) {
                            cantidad = (Integer) cantidadObj;
                        } else if (cantidadObj instanceof Double) {
                            cantidad = ((Double) cantidadObj).intValue();
                        } else if (cantidadObj != null) {
                            cantidad = Integer.parseInt(cantidadObj.toString());
                        }

                        Integer min = null, max = null;

                        if (!tfFiltroCantidadMin.getText().trim().isEmpty()) {
                            min = Integer.parseInt(tfFiltroCantidadMin.getText().trim());
                        }
                        if (!tfFiltroCantidadMax.getText().trim().isEmpty()) {
                            max = Integer.parseInt(tfFiltroCantidadMax.getText().trim());
                        }

                        if (min != null && cantidad < min) return false;
                        if (max != null && cantidad > max) return false;
                        return true;
                    } catch (Exception e) {
                        return true;
                    }
                }
            });
        }

        // Filtro por departamento
        if (!tfFiltroDepartamento.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroDepartamento.getText().trim(), 4));
        }

        // Filtro por almacén
        if (!tfFiltroAlmacen.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroAlmacen.getText().trim(), 5));
        }

        // Filtro por fecha creación
        if (!tfFiltroFechaCreacion.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroFechaCreacion.getText().trim(), 6));
        }

        // Filtro por fecha modificación
        if (!tfFiltroFechaModificacion.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroFechaModificacion.getText().trim(), 7));
        }

        // Filtro por usuario
        if (!tfFiltroUsuario.getText().trim().isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + tfFiltroUsuario.getText().trim(), 8));
        }

        if (filters.isEmpty()) {
            productosTableSorter.setRowFilter(null);
        } else {
            productosTableSorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    private void limpiarFiltros() {
        tfFiltroNombre.setText("");
        tfFiltroPrecioMin.setText("");
        tfFiltroPrecioMax.setText("");
        tfFiltroCantidadMin.setText("");
        tfFiltroCantidadMax.setText("");
        tfFiltroDepartamento.setText("");
        tfFiltroAlmacen.setText("");
        tfFiltroFechaCreacion.setText("");
        tfFiltroFechaModificacion.setText("");
        tfFiltroUsuario.setText("");
        productosTableSorter.setRowFilter(null);
    }

    private JPanel crearPanelBotonesProductos(JTable table) {
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botonesPanel.setBackground(DORADO_UNISON);

        // Botón Añadir
        RoundedButton btnAnadir = new RoundedButton("➕ Añadir");
        btnAnadir.setBackgroundColor(new Color(40, 167, 69));
        btnAnadir.setForeground(Color.WHITE);
        btnAnadir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAnadir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAnadir.addActionListener(e -> mostrarFormProducto());
        botonesPanel.add(btnAnadir);

        // Botón Eliminar
        RoundedButton btnEliminar = new RoundedButton("🗑️ Eliminar");
        btnEliminar.setBackgroundColor(new Color(220, 53, 69));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro de que quieres eliminar el producto seleccionado?",
                        "Confirmar Eliminación",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Object idObj = productosTableModel.getValueAt(modelRow, 0);
                    int id = 0;
                    if (idObj instanceof Integer) {
                        id = (Integer) idObj;
                    } else if (idObj != null) {
                        id = Integer.parseInt(idObj.toString());
                    }

                    if (DatabaseManager.eliminarProducto(id)) {
                        JOptionPane.showMessageDialog(this, "Producto eliminado correctamente");
                        recargarPanelProductos();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al eliminar el producto");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Por favor, selecciona un producto para eliminar.",
                        "Selección Requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        botonesPanel.add(btnEliminar);

        // Botón Modificar - CORREGIDO para manejar tipos de datos correctamente
        RoundedButton btnModificar = new RoundedButton("✏️ Modificar");
        btnModificar.setBackgroundColor(new Color(23, 162, 184));
        btnModificar.setForeground(Color.WHITE);
        btnModificar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnModificar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnModificar.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);

                // Obtener ID
                Object idObj = productosTableModel.getValueAt(modelRow, 0);
                int id = 0;
                if (idObj instanceof Integer) {
                    id = (Integer) idObj;
                } else if (idObj != null) {
                    id = Integer.parseInt(idObj.toString());
                }

                // Obtener nombre
                String nombreActual = (String) productosTableModel.getValueAt(modelRow, 1);

                // Obtener precio (manejar Integer o Double)
                Object precioObj = productosTableModel.getValueAt(modelRow, 2);
                double precioActual = 0.0;
                if (precioObj instanceof Double) {
                    precioActual = (Double) precioObj;
                } else if (precioObj instanceof Integer) {
                    precioActual = ((Integer) precioObj).doubleValue();
                } else if (precioObj != null) {
                    try {
                        precioActual = Double.parseDouble(precioObj.toString());
                    } catch (NumberFormatException ex) {
                        precioActual = 0.0;
                    }
                }

                // Obtener cantidad (manejar Integer o Double)
                Object cantidadObj = productosTableModel.getValueAt(modelRow, 3);
                int cantidadActual = 0;
                if (cantidadObj instanceof Integer) {
                    cantidadActual = (Integer) cantidadObj;
                } else if (cantidadObj instanceof Double) {
                    cantidadActual = ((Double) cantidadObj).intValue();
                } else if (cantidadObj != null) {
                    try {
                        cantidadActual = Integer.parseInt(cantidadObj.toString());
                    } catch (NumberFormatException ex) {
                        cantidadActual = 0;
                    }
                }

                // Obtener departamento
                String departamentoActual = (String) productosTableModel.getValueAt(modelRow, 4);

                // Obtener almacén
                String almacenNombreActual = (String) productosTableModel.getValueAt(modelRow, 5);

                // Obtener ID del almacén por su nombre
                int almacenIdActual = DatabaseManager.obtenerIdAlmacenPorNombre(almacenNombreActual);

                // Crear campos del formulario
                RoundedTextField nombreField = new RoundedTextField();
                nombreField.setText(nombreActual);
                nombreField.setPreferredSize(new Dimension(250, 35));

                RoundedTextField precioField = new RoundedTextField();
                precioField.setText(String.valueOf(precioActual));
                precioField.setPreferredSize(new Dimension(250, 35));

                RoundedTextField cantidadField = new RoundedTextField();
                cantidadField.setText(String.valueOf(cantidadActual));
                cantidadField.setPreferredSize(new Dimension(250, 35));

                // ComboBox para departamento
                JComboBox<String> departamentoCombo = new JComboBox<>(DEPARTAMENTOS);
                departamentoCombo.setSelectedItem(departamentoActual);
                departamentoCombo.setEditable(true);
                departamentoCombo.setPreferredSize(new Dimension(250, 35));

                // ComboBox para almacén
                List<Object[]> almacenes = DatabaseManager.obtenerAlmacenes();
                String[] nombresAlmacenes = new String[almacenes.size()];
                for (int i = 0; i < almacenes.size(); i++) {
                    nombresAlmacenes[i] = (String) almacenes.get(i)[1];
                }
                JComboBox<String> almacenCombo = new JComboBox<>(nombresAlmacenes);
                almacenCombo.setSelectedItem(almacenNombreActual);
                almacenCombo.setPreferredSize(new Dimension(250, 35));

                Object[] fields = {
                        "Nombre:", nombreField,
                        "Precio:", precioField,
                        "Cantidad:", cantidadField,
                        "Departamento:", departamentoCombo,
                        "Almacén:", almacenCombo
                };

                int result = JOptionPane.showConfirmDialog(this, fields, "Modificar Producto",
                        JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        String nombre = nombreField.getText();
                        double precio = Double.parseDouble(precioField.getText());
                        int cantidad = Integer.parseInt(cantidadField.getText());
                        String departamento = (String) departamentoCombo.getSelectedItem();
                        if (departamento == null || departamento.trim().isEmpty()) {
                            departamento = departamentoCombo.getEditor().getItem().toString().trim();
                        }
                        String almacenNombre = (String) almacenCombo.getSelectedItem();
                        int almacenId = DatabaseManager.obtenerIdAlmacenPorNombre(almacenNombre);

                        if (DatabaseManager.actualizarProducto(id, nombre, precio, cantidad, departamento, almacenId, usuarioActual)) {
                            JOptionPane.showMessageDialog(this, "Producto modificado correctamente");
                            recargarPanelProductos();
                        } else {
                            JOptionPane.showMessageDialog(this, "Error al modificar el producto");
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Error en el formato de los números");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Por favor, selecciona un producto para modificar.",
                        "Selección Requerida",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        botonesPanel.add(btnModificar);

        return botonesPanel;
    }

    // FORMULARIO DE ALMACÉN
    private JPanel crearFormAlmacenPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(AZUL_UNISON);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DORADO_UNISON);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Botón para regresar a almacenes
        RoundedButton btnRegresar = new RoundedButton("← Volver a Almacenes");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegresar.setBackgroundColor(new Color(0, 51, 102));
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegresar.addActionListener(e -> cardLayout.show(mainPanel, PANEL_ALMACENES));

        JLabel titleLabel = new JLabel("🏪 Formulario de Almacén");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(btnRegresar, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        formPanel.add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBackground(Color.WHITE);
        formContent.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblNombre = new JLabel("Nombre del Almacén:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setForeground(AZUL_UNISON);
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfNombreAlmacen = new RoundedTextField();
        tfNombreAlmacen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfNombreAlmacen.setBackground(Color.WHITE);
        tfNombreAlmacen.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Panel de botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        botonesPanel.setBackground(Color.WHITE);
        botonesPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        RoundedButton btnGuardarAlmacen = new RoundedButton("💾 Guardar");
        btnGuardarAlmacen.setBackgroundColor(new Color(40, 167, 69));
        btnGuardarAlmacen.setForeground(Color.WHITE);
        btnGuardarAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardarAlmacen.setCursor(new Cursor(Cursor.HAND_CURSOR));

        RoundedButton btnCancelarAlmacen = new RoundedButton("❌ Cancelar");
        btnCancelarAlmacen.setBackgroundColor(new Color(220, 53, 69));
        btnCancelarAlmacen.setForeground(Color.WHITE);
        btnCancelarAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelarAlmacen.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGuardarAlmacen.addActionListener(e -> guardarAlmacen());
        btnCancelarAlmacen.addActionListener(e -> cardLayout.show(mainPanel, PANEL_ALMACENES));

        botonesPanel.add(btnGuardarAlmacen);
        botonesPanel.add(btnCancelarAlmacen);

        formContent.add(lblNombre);
        formContent.add(Box.createRigidArea(new Dimension(0, 10)));
        formContent.add(tfNombreAlmacen);
        formContent.add(Box.createRigidArea(new Dimension(0, 20)));
        formContent.add(botonesPanel);

        formPanel.add(formContent, BorderLayout.CENTER);

        return formPanel;
    }

    // FORMULARIO DE PRODUCTO
    private JPanel crearFormProductoPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(AZUL_UNISON);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DORADO_UNISON);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Botón para regresar a productos
        RoundedButton btnRegresar = new RoundedButton("← Volver a Productos");
        btnRegresar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRegresar.setBackgroundColor(new Color(0, 51, 102));
        btnRegresar.setForeground(Color.WHITE);
        btnRegresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegresar.addActionListener(e -> cardLayout.show(mainPanel, PANEL_PRODUCTOS));

        JLabel titleLabel = new JLabel("📦 Formulario de Producto");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(btnRegresar, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        formPanel.add(headerPanel, BorderLayout.NORTH);

        // Formulario
        JPanel formContent = new JPanel();
        formContent.setLayout(new BoxLayout(formContent, BoxLayout.Y_AXIS));
        formContent.setBackground(Color.WHITE);
        formContent.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Campo Nombre
        JLabel lblNombre = new JLabel("Nombre del Producto:");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNombre.setForeground(AZUL_UNISON);
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfNombreProducto = new RoundedTextField();
        tfNombreProducto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfNombreProducto.setBackground(Color.WHITE);
        tfNombreProducto.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Campo Precio
        JLabel lblPrecio = new JLabel("Precio:");
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPrecio.setForeground(AZUL_UNISON);
        lblPrecio.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfPrecioProducto = new RoundedTextField();
        tfPrecioProducto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfPrecioProducto.setBackground(Color.WHITE);
        tfPrecioProducto.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Campo Cantidad
        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCantidad.setForeground(AZUL_UNISON);
        lblCantidad.setAlignmentX(Component.LEFT_ALIGNMENT);

        tfCantidadProducto = new RoundedTextField();
        tfCantidadProducto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tfCantidadProducto.setBackground(Color.WHITE);
        tfCantidadProducto.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Campo Departamento (ComboBox)
        JLabel lblDepartamento = new JLabel("Departamento:");
        lblDepartamento.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDepartamento.setForeground(AZUL_UNISON);
        lblDepartamento.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbDepartamentoProducto = new JComboBox<>(DEPARTAMENTOS);
        cbDepartamentoProducto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cbDepartamentoProducto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbDepartamentoProducto.setBackground(Color.WHITE);
        cbDepartamentoProducto.setEditable(true);

        // Campo Almacén (ComboBox con nombres)
        JLabel lblAlmacen = new JLabel("Almacén:");
        lblAlmacen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAlmacen.setForeground(AZUL_UNISON);
        lblAlmacen.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Cargar almacenes para el ComboBox
        List<Object[]> almacenes = DatabaseManager.obtenerAlmacenes();
        String[] nombresAlmacenes = new String[almacenes.size()];
        for (int i = 0; i < almacenes.size(); i++) {
            nombresAlmacenes[i] = (String) almacenes.get(i)[1];
        }
        cbAlmacenProducto = new JComboBox<>(nombresAlmacenes);
        cbAlmacenProducto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cbAlmacenProducto.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Panel de botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        botonesPanel.setBackground(Color.WHITE);
        botonesPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        RoundedButton btnGuardarProducto = new RoundedButton("💾 Guardar");
        btnGuardarProducto.setBackgroundColor(new Color(40, 167, 69));
        btnGuardarProducto.setForeground(Color.WHITE);
        btnGuardarProducto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));

        RoundedButton btnCancelarProducto = new RoundedButton("❌ Cancelar");
        btnCancelarProducto.setBackgroundColor(new Color(220, 53, 69));
        btnCancelarProducto.setForeground(Color.WHITE);
        btnCancelarProducto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGuardarProducto.addActionListener(e -> guardarProducto());
        btnCancelarProducto.addActionListener(e -> cardLayout.show(mainPanel, PANEL_PRODUCTOS));

        botonesPanel.add(btnGuardarProducto);
        botonesPanel.add(btnCancelarProducto);

        // Agregar componentes al formulario
        formContent.add(lblNombre);
        formContent.add(Box.createRigidArea(new Dimension(0, 10)));
        formContent.add(tfNombreProducto);
        formContent.add(Box.createRigidArea(new Dimension(0, 15)));
        formContent.add(lblPrecio);
        formContent.add(Box.createRigidArea(new Dimension(0, 10)));
        formContent.add(tfPrecioProducto);
        formContent.add(Box.createRigidArea(new Dimension(0, 15)));
        formContent.add(lblCantidad);
        formContent.add(Box.createRigidArea(new Dimension(0, 10)));
        formContent.add(tfCantidadProducto);
        formContent.add(Box.createRigidArea(new Dimension(0, 15)));
        formContent.add(lblDepartamento);
        formContent.add(Box.createRigidArea(new Dimension(0, 10)));
        formContent.add(cbDepartamentoProducto);
        formContent.add(Box.createRigidArea(new Dimension(0, 15)));
        formContent.add(lblAlmacen);
        formContent.add(Box.createRigidArea(new Dimension(0, 10)));
        formContent.add(cbAlmacenProducto);
        formContent.add(Box.createRigidArea(new Dimension(0, 25)));
        formContent.add(botonesPanel);

        formPanel.add(formContent, BorderLayout.CENTER);

        return formPanel;
    }

    private void guardarAlmacen() {
        String nombre = tfNombreAlmacen.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DatabaseManager.agregarAlmacen(nombre, usuarioActual)) {
            JOptionPane.showMessageDialog(this, "Almacén guardado correctamente");
            tfNombreAlmacen.setText("");
            cardLayout.show(mainPanel, PANEL_ALMACENES);
            recargarPanelAlmacenes();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar el almacén", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarProducto() {
        try {
            String nombre = tfNombreProducto.getText().trim();
            double precio = Double.parseDouble(tfPrecioProducto.getText());
            int cantidad = Integer.parseInt(tfCantidadProducto.getText());
            String departamento = (String) cbDepartamentoProducto.getSelectedItem();

            if (departamento == null || departamento.trim().isEmpty()) {
                departamento = cbDepartamentoProducto.getEditor().getItem().toString().trim();
            }

            String almacenNombre = (String) cbAlmacenProducto.getSelectedItem();

            // Obtener ID del almacén por su nombre
            int almacenId = DatabaseManager.obtenerIdAlmacenPorNombre(almacenNombre);

            if (nombre.isEmpty() || departamento.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nombre y departamento no pueden estar vacíos", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DatabaseManager.agregarProducto(nombre, precio, cantidad, departamento, almacenId, usuarioActual)) {
                JOptionPane.showMessageDialog(this, "Producto guardado correctamente");
                limpiarFormularioProducto();
                cardLayout.show(mainPanel, PANEL_PRODUCTOS);
                recargarPanelProductos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar el producto", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en el formato de los números", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormularioProducto() {
        tfNombreProducto.setText("");
        tfPrecioProducto.setText("");
        tfCantidadProducto.setText("");
        cbDepartamentoProducto.setSelectedIndex(0);
        if (cbAlmacenProducto.getItemCount() > 0) {
            cbAlmacenProducto.setSelectedIndex(0);
        }
    }

    // MÉTODOS PARA MOSTRAR FORMULARIOS (solo para roles específicos)
    public void mostrarFormAlmacen() {
        if ("admin".equals(rolActual) || "almacen".equals(rolActual)) {
            tfNombreAlmacen.setText("");
            cardLayout.show(mainPanel, PANEL_FORM_ALMACEN);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No tiene permisos para acceder a esta función",
                    "Acceso Denegado",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public void mostrarFormProducto() {
        if ("admin".equals(rolActual) || "productos".equals(rolActual)) {
            limpiarFormularioProducto();
            cardLayout.show(mainPanel, PANEL_FORM_PRODUCTO);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No tiene permisos para acceder a esta función",
                    "Acceso Denegado",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    // Métodos para recargar paneles
    private void recargarPanelAlmacenes() {
        mainPanel.remove(2);
        mainPanel.add(crearAlmacenesPanel(), PANEL_ALMACENES, 2);
        cardLayout.show(mainPanel, PANEL_ALMACENES);
    }

    private void recargarPanelProductos() {
        mainPanel.remove(3);
        mainPanel.add(crearProductosPanel(), PANEL_PRODUCTOS, 3);
        cardLayout.show(mainPanel, PANEL_PRODUCTOS);
    }

    private void realizarLogin() {
        String usuario = tfUsuario.getText().trim();
        String contrasena = new String(pfContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            lbStatus.setText("Todos los campos son obligatorios");
            return;
        }

        String contrasenaHash = hashPassword(contrasena);
        String[] credencial = DatabaseManager.verificarCredenciales(usuario, contrasenaHash);

        if (credencial != null) {
            usuarioActual = credencial[0];
            rolActual = credencial[1];

            lbStatus.setForeground(new Color(40, 167, 69));
            lbStatus.setText("✓ Inicio de sesión exitoso - Rol: " + rolActual.toUpperCase());

            Timer timer = new Timer(1000, e -> {
                cardLayout.show(mainPanel, PANEL_MENU);
                setTitle("Sistema UNISON - Bienvenido " + usuarioActual + " (" + rolActual.toUpperCase() + ")");
                crearMenuBar();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            lbStatus.setForeground(Color.RED);
            lbStatus.setText("✗ Credenciales incorrectas");
        }
    }

    private void crearMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuSesion = new JMenu("Sesión");
        menuSesion.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JMenuItem itemInfo = new JMenuItem("👤 " + usuarioActual + " (" + rolActual.toUpperCase() + ")");
        itemInfo.setEnabled(false);
        itemInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        itemCerrarSesion.addActionListener(e -> {
            tfUsuario.setText("");
            pfContrasena.setText("");
            lbStatus.setText(" ");
            usuarioActual = null;
            rolActual = null;
            setTitle("Inicio de sesión - UNISON");
            menuBar.setVisible(false);
            cardLayout.show(mainPanel, PANEL_LOGIN);
        });

        menuSesion.add(itemInfo);
        menuSesion.addSeparator();
        menuSesion.add(itemCerrarSesion);

        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(menuSesion);

        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new UnisonApp().setVisible(true);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseManager.closeConnection();
        }));
    }
}