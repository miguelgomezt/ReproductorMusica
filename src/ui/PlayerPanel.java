package ui;

import app.MusicLibraryManager;
import model.Song;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

/**
 * PlayerPanel: parte de la interfaz gráfica encargada de mostrar la canción
 * actual y controlar la reproducción (simulada).
 *
 * INTEGRACIÓN CON EL RESTO DEL EQUIPO
 * ---------------------------------------------------------------------
 * - Recibe MusicLibraryManager por constructor (tal como espera el TODO
 *   de MainFrame: `playerPanel = new PlayerPanel(libraryManager);`).
 * - Se registra como MusicLibraryManager.LibraryChangeListener: así se
 *   entera automáticamente de CUALQUIER cambio relevante (next/previous,
 *   agregar/eliminar/editar canciones, cambio de modo) sin que MainFrame
 *   ni SongLibraryPanel necesiten llamarlo manualmente. Esto es el mismo
 *   patrón Observer que ya usa SongLibraryPanel (Persona 2).
 * - Nunca llama directamente a un PlaybackMode: todo pasa por
 *   MusicLibraryManager (getCurrentSong, next, previous), que es quien
 *   sabe cuál es el modo activo.
 *
 * SOBRE EL BOTÓN "ANTERIOR"
 * ---------------------------------------------------------------------
 * PlaybackMode no expone ninguna forma de preguntar "¿este modo soporta
 * retroceder?". Por eso el botón "Anterior" queda siempre visible y
 * habilitado; simplemente se llama a libraryManager.previous(). Asumo
 * que en el modo que no lo soporte (p. ej. la Cola Simple del Modo 2),
 * previous() no hace nada y devuelve la canción actual sin cambios
 * (comportamiento no-op), en línea con "No será posible regresar a
 * canciones anteriores" del enunciado. Si en la implementación real
 * previous() lanza una excepción en ese caso en lugar de no hacer nada,
 * avísame para agregar un try/catch aquí.
 *
 * REPRODUCCIÓN SIMULADA
 * ---------------------------------------------------------------------
 * Un javax.swing.Timer dispara cada 1000 ms e incrementa un contador de
 * segundos, actualizando la barra de progreso. Al llegar a la duración
 * de la canción, se avanza automáticamente a la siguiente (igual que un
 * reproductor real). Se usa javax.swing.Timer y no java.util.Timer
 * porque sus callbacks corren en el Event Dispatch Thread, el único hilo
 * seguro para tocar componentes Swing.
 */
public class PlayerPanel extends JPanel implements MusicLibraryManager.LibraryChangeListener {

    private final MusicLibraryManager libraryManager;

    private Song cancionActual;
    private boolean reproduciendo;
    private int segundosTranscurridos;
    private final Timer temporizadorProgreso;

    // --- Componentes de la interfaz ---
    private final JLabel lblPortada;
    private final JLabel lblTitulo;
    private final JLabel lblArtista;
    private final JLabel lblAlbum;
    private final JLabel lblGeneroYAnio;
    private final JLabel lblCalificacion;
    private final JLabel lblModoActual;
    private final JProgressBar barraProgreso;
    private final JLabel lblTiempo;
    private final JButton btnReproducir;
    private final JButton btnPausar;
    private final JButton btnAnterior;
    private final JButton btnSiguiente;

    private static final int TAMANO_PORTADA = 100;

    public PlayerPanel(MusicLibraryManager libraryManager) {
        if (libraryManager == null) {
            throw new IllegalArgumentException("PlayerPanel necesita un MusicLibraryManager.");
        }
        this.libraryManager = libraryManager;

        setLayout(new BorderLayout(12, 8));
        setBorder(BorderFactory.createTitledBorder("Reproductor"));

        // ----- Portada -----
        lblPortada = new JLabel();
        lblPortada.setPreferredSize(new Dimension(TAMANO_PORTADA, TAMANO_PORTADA));
        lblPortada.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblPortada, BorderLayout.WEST);

        // ----- Información de la canción -----
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));

        lblTitulo = new JLabel("Sin canción seleccionada");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));

        lblArtista = new JLabel(" ");
        lblAlbum = new JLabel(" ");
        lblGeneroYAnio = new JLabel(" ");
        lblCalificacion = new JLabel(" ");
        lblModoActual = new JLabel(" ");
        lblModoActual.setFont(lblModoActual.getFont().deriveFont(Font.ITALIC));

        panelInfo.add(lblTitulo);
        panelInfo.add(lblArtista);
        panelInfo.add(lblAlbum);
        panelInfo.add(lblGeneroYAnio);
        panelInfo.add(lblCalificacion);
        panelInfo.add(lblModoActual);
        panelInfo.add(Box.createVerticalStrut(6));

        barraProgreso = new JProgressBar(0, 100);
        panelInfo.add(barraProgreso);

        lblTiempo = new JLabel("00:00 / 00:00");
        panelInfo.add(lblTiempo);

        add(panelInfo, BorderLayout.CENTER);

        // ----- Controles -----
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        btnAnterior = new JButton("⏮ Anterior");
        btnReproducir = new JButton("▶ Reproducir");
        btnPausar = new JButton("⏸ Pausar");
        btnSiguiente = new JButton("⏭ Siguiente");

        btnAnterior.addActionListener(e -> libraryManager.previous());
        btnReproducir.addActionListener(e -> reproducir());
        btnPausar.addActionListener(e -> pausar());
        btnSiguiente.addActionListener(e -> libraryManager.next());

        panelControles.add(btnAnterior);
        panelControles.add(btnReproducir);
        panelControles.add(btnPausar);
        panelControles.add(btnSiguiente);
        add(panelControles, BorderLayout.SOUTH);

        // ----- Temporizador de simulación -----
        temporizadorProgreso = new Timer(1000, e -> avanzarUnSegundo());

        // ----- Suscripción al manager (patrón Observer) -----
        libraryManager.addListener(this);

        // Estado inicial
        this.cancionActual = libraryManager.getCurrentSong();
        actualizarInterfazCompleta();
    }

    // =====================================================================
    // CALLBACK DEL OBSERVER: se dispara ante CUALQUIER cambio en la biblioteca
    // (agregar/eliminar/editar canción, next/previous, o cambio de modo)
    // =====================================================================

    @Override
    public void onLibraryChanged() {
        Song nuevaCancionActual = libraryManager.getCurrentSong();
        boolean cambioDeCancion = !Objects.equals(nuevaCancionActual, cancionActual);

        if (cambioDeCancion) {
            boolean seguiaReproduciendo = reproduciendo;
            detenerReproduccion();
            cancionActual = nuevaCancionActual;
            actualizarInterfazCompleta();
            if (seguiaReproduciendo && cancionActual != null) {
                reproducir();
            }
        } else {
            // La canción actual no cambió, pero algo más sí pudo cambiar
            // (p. ej. su calificación vía rateSong) o el modo activo.
            actualizarInterfazCompleta();
        }
    }

    // =====================================================================
    // CONTROLES DE REPRODUCCIÓN (SIMULADA)
    // =====================================================================

    private void reproducir() {
        if (cancionActual == null || reproduciendo) {
            return;
        }
        reproduciendo = true;
        temporizadorProgreso.start();
        actualizarEstadoBotones();
    }

    private void pausar() {
        reproduciendo = false;
        temporizadorProgreso.stop();
        actualizarEstadoBotones();
    }

    private void detenerReproduccion() {
        reproduciendo = false;
        temporizadorProgreso.stop();
        segundosTranscurridos = 0;
    }

    /** Se ejecuta cada segundo mientras se está "reproduciendo". */
    private void avanzarUnSegundo() {
        if (cancionActual == null) {
            temporizadorProgreso.stop();
            return;
        }
        segundosTranscurridos++;
        if (segundosTranscurridos >= cancionActual.getDurationInSeconds()) {
            // La canción "terminó": avanzamos a la siguiente, igual que
            // haría un reproductor real. libraryManager.next() dispara
            // notifyListeners(), que llega a onLibraryChanged() y
            // actualiza todo (incluyendo reiniciar el contador).
            libraryManager.next();
            return;
        }
        actualizarBarraProgreso();
    }

    // =====================================================================
    // ACTUALIZACIÓN DE LA INTERFAZ (solo presentación, sin lógica de negocio)
    // =====================================================================

    private void actualizarInterfazCompleta() {
        lblModoActual.setText("Modo: " + libraryManager.getModeName());

        if (cancionActual == null) {
            lblTitulo.setText("Sin canción seleccionada");
            lblArtista.setText(" ");
            lblAlbum.setText(" ");
            lblGeneroYAnio.setText(" ");
            lblCalificacion.setText(" ");
            lblPortada.setIcon(crearPortadaPlaceholder());
            lblTiempo.setText("00:00 / 00:00");
            barraProgreso.setValue(0);
        } else {
            lblTitulo.setText(cancionActual.getTitle());
            lblArtista.setText(cancionActual.getArtist());
            lblAlbum.setText("Álbum: " + cancionActual.getAlbum());
            lblGeneroYAnio.setText(cancionActual.getGenre() + " · " + cancionActual.getReleaseYear());
            lblCalificacion.setText("Calificación: " + cancionActual.getRating() + "/100");
            lblPortada.setIcon(cargarPortada(cancionActual.getCoverImage()));
            actualizarBarraProgreso();
        }
        actualizarEstadoBotones();
    }

    private void actualizarBarraProgreso() {
        if (cancionActual == null) {
            return;
        }
        int duracionTotal = cancionActual.getDurationInSeconds();
        int porcentaje = duracionTotal == 0 ? 0 : (segundosTranscurridos * 100) / duracionTotal;
        barraProgreso.setValue(Math.min(porcentaje, 100));
        lblTiempo.setText(formatearTiempo(segundosTranscurridos) + " / " + cancionActual.getFormattedDuration());
    }

    private void actualizarEstadoBotones() {
        boolean hayCancion = (cancionActual != null);
        btnReproducir.setEnabled(hayCancion && !reproduciendo);
        btnPausar.setEnabled(hayCancion && reproduciendo);
        btnSiguiente.setEnabled(hayCancion);
        btnAnterior.setEnabled(hayCancion);
    }

    private String formatearTiempo(int totalSegundos) {
        int minutos = totalSegundos / 60;
        int segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    // =====================================================================
    // MANEJO DE LA PORTADA
    // =====================================================================

    private ImageIcon cargarPortada(String rutaImagen) {
        if (rutaImagen != null) {
            File archivo = new File(rutaImagen);
            if (archivo.exists()) {
                ImageIcon original = new ImageIcon(rutaImagen);
                Image escalada = original.getImage()
                        .getScaledInstance(TAMANO_PORTADA, TAMANO_PORTADA, Image.SCALE_SMOOTH);
                return new ImageIcon(escalada);
            }
        }
        return crearPortadaPlaceholder();
    }

    /** Ícono genérico de nota musical cuando la canción no tiene portada. */
    private ImageIcon crearPortadaPlaceholder() {
        BufferedImage imagen = new BufferedImage(TAMANO_PORTADA, TAMANO_PORTADA, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = imagen.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(60, 63, 65));
        g2.fillRoundRect(0, 0, TAMANO_PORTADA, TAMANO_PORTADA, 16, 16);
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40f));
        FontMetrics fm = g2.getFontMetrics();
        String simbolo = "♪";
        int x = (TAMANO_PORTADA - fm.stringWidth(simbolo)) / 2;
        int y = (TAMANO_PORTADA - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(simbolo, x, y);
        g2.dispose();
        return new ImageIcon(imagen);
    }
}
