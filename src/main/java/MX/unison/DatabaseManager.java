package MX.unison;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:unison.db";
    private static Connection connection = null;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);

            // Forzar la recreación de tablas eliminándolas primero
            dropTables();

            createTables();
            insertInitialData();

            // Verificar y corregir secuencias después de la inicialización
            verificarYCorregirSecuencias();

        } catch (SQLException e) {
            System.err.println("Error inicializando la base de datos: " + e.getMessage());
        }
    }

    // Método para verificar y corregir secuencias
    private static void verificarYCorregirSecuencias() {
        System.out.println("=== VERIFICANDO SECUENCIAS ===");

        String[] tablas = {"productos", "almacenes", "usuarios"};

        for (String tabla : tablas) {
            try {
                // Obtener máximo ID actual en la tabla
                String sqlMaxId = "SELECT MAX(id) FROM " + tabla;
                Statement stmt1 = connection.createStatement();
                ResultSet rs1 = stmt1.executeQuery(sqlMaxId);
                int maxId = 0;
                if (rs1.next()) {
                    maxId = rs1.getInt(1);
                }
                rs1.close();
                stmt1.close();

                // Verificar si existe entrada en sqlite_sequence
                String sqlSeq = "SELECT seq FROM sqlite_sequence WHERE name='" + tabla + "'";
                Statement stmt2 = connection.createStatement();
                ResultSet rs2 = stmt2.executeQuery(sqlSeq);

                if (rs2.next()) {
                    int currentSeq = rs2.getInt(1);
                    System.out.println("Tabla: " + tabla);
                    System.out.println("  Máximo ID: " + maxId);
                    System.out.println("  Secuencia actual: " + currentSeq);
                    System.out.println("  Próximo ID auto-generado: " + (currentSeq + 1));

                    if (currentSeq > maxId) {
                        System.out.println("  ⚠️  Desfase detectado! Corrigiendo...");
                        // Actualizar secuencia al máximo ID real
                        String sqlUpdate = "UPDATE sqlite_sequence SET seq = " + maxId + " WHERE name='" + tabla + "'";
                        Statement stmt3 = connection.createStatement();
                        stmt3.executeUpdate(sqlUpdate);
                        stmt3.close();
                        System.out.println("  ✅ Secuencia corregida a: " + maxId);
                    }
                } else if (maxId > 0) {
                    // Si la tabla tiene datos pero no tiene entrada en sqlite_sequence
                    System.out.println("Tabla: " + tabla + " - Insertando entrada en sqlite_sequence");
                    String sqlInsert = "INSERT INTO sqlite_sequence (name, seq) VALUES ('" + tabla + "', " + maxId + ")";
                    Statement stmt3 = connection.createStatement();
                    stmt3.executeUpdate(sqlInsert);
                    stmt3.close();
                    System.out.println("  ✅ Entrada creada con secuencia: " + maxId);
                }

                if (rs2 != null) {
                    rs2.close();
                }
                stmt2.close();

            } catch (SQLException e) {
                System.out.println("Tabla: " + tabla + " - Sin datos o no usa AUTOINCREMENT");
            }
        }
        System.out.println("=== FIN VERIFICACIÓN ===\n");
    }

    // Método para eliminar tablas existentes
    private static void dropTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS productos");
            stmt.execute("DROP TABLE IF EXISTS almacenes");
            stmt.execute("DROP TABLE IF EXISTS usuarios");
            System.out.println("Tablas existentes eliminadas para recreación");
        } catch (SQLException e) {
            System.err.println("Error eliminando tablas: " + e.getMessage());
        }
    }

    private static void createTables() {
        String createUsuariosTable = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                rol TEXT NOT NULL
            )
        """;

        String createAlmacenesTable = """
            CREATE TABLE IF NOT EXISTS almacenes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                fecha_hora_creacion TEXT NOT NULL,
                fecha_hora_ultima_modificacion TEXT NOT NULL,
                ultimo_usuario_en_modificar TEXT NOT NULL
            )
        """;

        String createProductosTable = """
            CREATE TABLE IF NOT EXISTS productos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                precio REAL NOT NULL,
                cantidad INTEGER NOT NULL,
                departamento TEXT NOT NULL,
                almacen_id INTEGER NOT NULL,
                fecha_hora_creacion TEXT NOT NULL,
                fecha_hora_ultima_modificacion TEXT NOT NULL,
                ultimo_usuario_en_modificar TEXT NOT NULL
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsuariosTable);
            stmt.execute(createAlmacenesTable);
            stmt.execute(createProductosTable);
            System.out.println("Tablas creadas exitosamente");
        } catch (SQLException e) {
            System.err.println("Error creando tablas: " + e.getMessage());
        }
    }

    private static void insertInitialData() {
        String currentTime = LocalDateTime.now().format(formatter);

        // Insertar usuarios
        String insertUsuarios = """
            INSERT INTO usuarios (username, password_hash, rol) VALUES
            ('admin', ?, 'admin'),
            ('almacen', ?, 'almacen'),
            ('productos', ?, 'productos')
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(insertUsuarios)) {
            pstmt.setString(1, UnisonApp.hashPassword("admin23"));
            pstmt.setString(2, UnisonApp.hashPassword("almacen11"));
            pstmt.setString(3, UnisonApp.hashPassword("productos19"));
            pstmt.executeUpdate();
            System.out.println("Usuarios insertados exitosamente");
        } catch (SQLException e) {
            System.err.println("Error insertando usuarios: " + e.getMessage());
        }

        // Insertar almacenes
        String[] nombresAlmacenes = {"Hermosillo", "Caborca", "Guaymas", "Sonoyta", "Nogales"};
        String insertAlmacen = "INSERT INTO almacenes (nombre, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertAlmacen)) {
            for (String nombre : nombresAlmacenes) {
                pstmt.setString(1, nombre);
                pstmt.setString(2, currentTime);
                pstmt.setString(3, currentTime);
                pstmt.setString(4, "admin");
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Almacenes insertados exitosamente");
        } catch (SQLException e) {
            System.err.println("Error insertando almacenes: " + e.getMessage());
            e.printStackTrace();
        }

        // Insertar productos del CSV - TODOS en almacenes 1-5
        Object[][] productosCSV = {
                {1, "Filamento PLA 1.75", 399.0, 9, "materiales", 1},
                {2, "Filamento ABS 1.75", 400.0, 5, "materiales", 1},
                {3, "Silla negra", 800.0, 5, "mobiliario", 4},
                {4, "Silla naranja", 600.0, 21, "mobiliario", 4},
                {5, "Base de metal para impresión 3D", 350.0, 6, "materiales", 2},
                {6, "Pegamento para impresion 3D", 199.0, 8, "materiales", 2},
                {7, "Soporte para filamento", 400.0, 3, "materiales", 1},
                {8, "Soporte para filamento circular", 200.0, 5, "materiales", 4},
                {9, "Impresora 3D Kingroon KP3", 8000.0, 0, "impresion3D", 4},
                {10, "Impresora 3D Kingroon KP3S", 8000.0, 8, "impresion3D", 3},
                {11, "Impresora 3D FlashForge Adventure 5M", 15000.0, 11, "impresion3D", 3},
                {12, "Impresora 3D FlashForge Adventure 5M Pro", 20000.0, 11, "impresion3D", 1},
                {13, "Filamento PLA reciclado", 4859.31, 17, "mobiliario", 2},
                {14, "Filamento metálico (bronce, cobre)", 1780.24, 10, "mobiliario", 2},
                {15, "Filamento fosforescente", 2869.33, 21, "materiales", 1},
                {16, "Filamento de nylon", 2712.38, 18, "materiales", 4},
                {17, "Filamento ASA", 1882.43, 23, "mobiliario", 4},
                {18, "Filamento HIPS", 114.76, 6, "materiales", 3},
                {19, "Resina flexible", 2182.92, 23, "materiales", 3},
                {20, "Resina dental o médica", 4364.38, 20, "mobiliario", 1},
                {21, "Resina de alta temperatura", 3115.01, 10, "materiales", 1},
                {22, "Kit de mantenimiento para impresoras 3D", 4437.91, 21, "mobiliario", 1},
                {23, "Pantalla táctil de repuesto para impresoras 3D", 2595.52, 17, "mobiliario", 4},
                {24, "Sistema de filtración de aire para impresoras cerradas", 878.05, 18, "mobiliario", 4},
                {25, "Caja secadora de filamentos", 3128.4, 18, "mobiliario", 4},
                {26, "Estación de post-procesado", 2455.36, 20, "mobiliario", 3},
                {27, "Guantes y gafas de seguridad para impresión con resina", 955.32, 17, "materiales", 1},
                {28, "Base magnética para cama de impresión", 4708.75, 24, "impresion3D", 1},
                {29, "Sistema de monitoreo remoto (cámara + app)", 4987.78, 9, "materiales", 3},
                {30, "Kit de modificación para impresoras 3D (upgrade kit)", 2982.62, 8, "materiales", 2},
                {31, "Curso en línea de modelado 3D", 205.69, 0, "materiales", 1},
                {32, "Manual impreso de mantenimiento y solución de problemas", 702.51, 7, "impresion3D", 4},
                {33, "Laptop DELL XPS 15", 25000.0, 6, "computación", 2},
                {34, "Monitor HP 27", 4500.0, 15, "computación", 1},
                {35, "Teclado mecánico ACER Predator", 1800.0, 17, "computación", 3},
                {36, "Mouse Logitech G502 (compatible)", 900.0, 27, "computación", 2},
                {37, "Audífonos Sony (compatibles)", 1200.0, 29, "computación", 1},
                {38, "Laptop HP Spectre x360", 28000.0, 5, "computación", 3},
                {39, "PC de escritorio DELL OptiPlex", 15000.0, 13, "computación", 3},
                {40, "Laptop ACER Aspire 5", 13000.0, 16, "computación", 4},
                {41, "Mouse DELL MS116", 250.0, 52, "computación", 4},
                {42, "Teclado HP inalámbrico", 600.0, 48, "computación", 2},
                {43, "Audífonos HP Omen", 2200.0, 23, "computación", 1},
                {44, "Monitor DELL Ultrasharp 24", 5500.0, 11, "computación", 1},
                {45, "Laptop ACER Swift 3", 16000.0, 23, "computación", 3},
                {46, "Mouse ACER Nitro", 450.0, 28, "computación", 3},
                {47, "Teclado DELL KB216", 300.0, 55, "computación", 4},
                {48, "Laptop DELL Latitude 5420", 19000.0, 10, "computación", 3},
                {49, "PC HP Pavilion Gaming", 21000.0, 2, "computación", 2},
                {50, "Audífonos ACER Nitro", 1500.0, 16, "computación", 4},
                {51, "Monitor ACER 21.5", 2800.0, 23, "computación", 4},
                {52, "Mouse HP Z3700 inalámbrico", 350.0, 45, "computación", 4},
                {53, "Teclado Gamer HP", 1100.0, 28, "computación", 2},
                {54, "Laptop HP Envy 13", 23000.0, 7, "computación", 3},
                {55, "PC DELL Vostro", 14000.0, 15, "computación", 3},
                {56, "Audífonos DELL (básicos)", 400.0, 38, "computación", 2},
                {57, "Monitor HP V22", 3000.0, 16, "computación", 2},
                {58, "Laptop ACER TravelMate", 17500.0, 16, "computación", 2},
                {59, "Mouse inalámbrico ACER", 300.0, 43, "computación", 3},
                {60, "Teclado y mouse DELL (kit)", 750.0, 35, "computación", 1},
                {61, "PC Gamer ACER Nitro 50", 24000.0, 12, "computación", 3},
                {62, "Audífonos (genéricos) para PC", 200.0, 74, "computación", 1}
        };

        String insertProducto = "INSERT INTO productos (id, nombre, precio, cantidad, departamento, almacen_id, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(insertProducto)) {
            for (Object[] producto : productosCSV) {
                pstmt.setInt(1, (Integer) producto[0]); // id
                pstmt.setString(2, (String) producto[1]); // nombre
                pstmt.setDouble(3, (Double) producto[2]); // precio
                pstmt.setInt(4, (Integer) producto[3]); // cantidad
                pstmt.setString(5, (String) producto[4]); // departamento
                pstmt.setInt(6, (Integer) producto[5]); // almacen_id
                pstmt.setString(7, currentTime);
                pstmt.setString(8, currentTime);
                pstmt.setString(9, "admin");
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Productos del CSV insertados exitosamente - Total: " + productosCSV.length);
        } catch (SQLException e) {
            System.err.println("Error insertando productos del CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String[] verificarCredenciales(String usuario, String passwordHash) {
        String sql = "SELECT username, rol FROM usuarios WHERE username = ? AND password_hash = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, passwordHash);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new String[]{rs.getString("username"), rs.getString("rol")};
            }
        } catch (SQLException e) {
            System.err.println("Error verificando credenciales: " + e.getMessage());
        }
        return null;
    }

    public static List<Object[]> obtenerAlmacenes() {
        List<Object[]> almacenes = new ArrayList<>();
        String sql = "SELECT id, nombre, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar FROM almacenes ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] almacen = {
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("fecha_hora_creacion"),
                        rs.getString("fecha_hora_ultima_modificacion"),
                        rs.getString("ultimo_usuario_en_modificar")
                };
                almacenes.add(almacen);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo almacenes: " + e.getMessage());
        }
        return almacenes;
    }

    public static List<Object[]> obtenerProductos() {
        List<Object[]> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, cantidad, departamento, almacen_id, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar FROM productos ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] producto = {
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("cantidad"),
                        rs.getString("departamento"),
                        rs.getInt("almacen_id"),
                        rs.getString("fecha_hora_creacion"),
                        rs.getString("fecha_hora_ultima_modificacion"),
                        rs.getString("ultimo_usuario_en_modificar")
                };
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo productos: " + e.getMessage());
        }
        return productos;
    }

    // NUEVO MÉTODO: Obtener productos con nombres de almacén
    public static List<Object[]> obtenerProductosConNombresAlmacen() {
        List<Object[]> productos = new ArrayList<>();
        String sql = """
            SELECT 
                p.id, 
                p.nombre, 
                p.precio, 
                p.cantidad, 
                p.departamento, 
                a.nombre AS almacen_nombre, 
                p.fecha_hora_creacion, 
                p.fecha_hora_ultima_modificacion, 
                p.ultimo_usuario_en_modificar 
            FROM productos p
            LEFT JOIN almacenes a ON p.almacen_id = a.id
            ORDER BY p.id
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Object[] producto = {
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("cantidad"),
                        rs.getString("departamento"),
                        rs.getString("almacen_nombre"),  // Nombre del almacén
                        rs.getString("fecha_hora_creacion"),
                        rs.getString("fecha_hora_ultima_modificacion"),
                        rs.getString("ultimo_usuario_en_modificar")
                };
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo productos con nombres de almacén: " + e.getMessage());
            e.printStackTrace();
        }
        return productos;
    }

    // NUEVO MÉTODO: Obtener ID de almacén por nombre
    public static int obtenerIdAlmacenPorNombre(String nombreAlmacen) {
        String sql = "SELECT id FROM almacenes WHERE nombre = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombreAlmacen);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            System.err.println("Error obteniendo ID de almacén por nombre: " + e.getMessage());
            e.printStackTrace();
        }
        return -1; // Retorna -1 si no se encuentra
    }

    public static boolean agregarProducto(String nombre, double precio, int cantidad, String departamento, int almacenId, String usuario) {
        String currentTime = LocalDateTime.now().format(formatter);
        String sql = "INSERT INTO productos (nombre, precio, cantidad, departamento, almacen_id, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precio);
            pstmt.setInt(3, cantidad);
            pstmt.setString(4, departamento);
            pstmt.setInt(5, almacenId);
            pstmt.setString(6, currentTime);
            pstmt.setString(7, currentTime);
            pstmt.setString(8, usuario);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error agregando producto: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarProducto(int id, String nombre, double precio, int cantidad, String departamento, int almacenId, String usuario) {
        String currentTime = LocalDateTime.now().format(formatter);
        String sql = "UPDATE productos SET nombre = ?, precio = ?, cantidad = ?, departamento = ?, almacen_id = ?, fecha_hora_ultima_modificacion = ?, ultimo_usuario_en_modificar = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setDouble(2, precio);
            pstmt.setInt(3, cantidad);
            pstmt.setString(4, departamento);
            pstmt.setInt(5, almacenId);
            pstmt.setString(6, currentTime);
            pstmt.setString(7, usuario);
            pstmt.setInt(8, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando producto: " + e.getMessage());
            return false;
        }
    }

    public static boolean agregarAlmacen(String nombre, String usuario) {
        String currentTime = LocalDateTime.now().format(formatter);
        String sql = "INSERT INTO almacenes (nombre, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, currentTime);
            pstmt.setString(3, currentTime);
            pstmt.setString(4, usuario);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error agregando almacén: " + e.getMessage());
            return false;
        }
    }

    public static boolean actualizarAlmacen(int id, String nombre, String usuario) {
        String currentTime = LocalDateTime.now().format(formatter);
        String sql = "UPDATE almacenes SET nombre = ?, fecha_hora_ultima_modificacion = ?, ultimo_usuario_en_modificar = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, currentTime);
            pstmt.setString(3, usuario);
            pstmt.setInt(4, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error actualizando almacén: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarAlmacen(int id) {
        String sql = "DELETE FROM almacenes WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando almacén: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error eliminando producto: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error cerrando conexión: " + e.getMessage());
        }
    }
}