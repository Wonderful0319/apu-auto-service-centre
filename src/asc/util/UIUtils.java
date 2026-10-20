package asc.util;

import asc.model.VipAccount;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class UIUtils {

    // ── Default image ─────────────────────────────────────────────────────────
    public static final String DEFAULT_PART_IMAGE_PATH = "data/Picture/Default/Black Tick.png";

    public static ImageIcon loadDefaultPartImage(int w, int h) {
        File f = new File(DEFAULT_PART_IMAGE_PATH);
        if (!f.exists()) return null;
        try {
            Image img = ImageIO.read(f).getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (IOException e) { return null; }
    }

    // ── Colour palette ────────────────────────────────────────────────────────
    public static final Color DARK_NAVY    = new Color(18,  32,  62);
    public static final Color MEDIUM_NAVY  = new Color(28,  46,  88);
    public static final Color TABLE_BG     = new Color(36,  58, 102);
    public static final Color ACCENT_BLUE  = new Color(70,  140, 215);
    public static final Color ACCENT_HOVER = new Color(90,  160, 235);
    public static final Color SUCCESS_GREEN= new Color(50,  170, 105);
    public static final Color ERROR_RED    = new Color(210,  70,  70);
    public static final Color WARN_YELLOW  = new Color(255, 200,  80);
    public static final Color PURPLE       = new Color(160, 105, 230);
    public static final Color GREEN_OK     = new Color(75,  205, 125);
    public static final Color WHITE        = Color.WHITE;
    public static final Color LIGHT_GRAY   = new Color(210, 220, 235);
    public static final Color DIM_TEXT     = new Color(150, 170, 200);
    public static final Color TABLE_HEADER = new Color(48,   74, 128);

    // VIP tier colours (match VipAccount.tierColor())
    public static final Color VIP_BRONZE    = new Color(205, 127,  50);
    public static final Color VIP_GOLD      = new Color(255, 200,  60);
    public static final Color VIP_BLACKGOLD = new Color(255, 215,   0);

    private static final Color POPUP_BG      = new Color(32,  52,  96);
    private static final Color POPUP_HOVER   = new Color(50,  78, 138);
    private static final Color POPUP_BORDER  = new Color(60,  95, 165);
    private static final Color POPUP_DIVIDER = new Color(50,  80, 140);

    // ── Font ──────────────────────────────────────────────────────────────────
    public static Font uiFont(int style, int size) {
        for (String name : new String[]{"Segoe UI Symbol", "Segoe UI Emoji", "Segoe UI", "SF Pro Display", "Helvetica Neue", "Arial"}) {
            Font f = new Font(name, style, size);
            if (!f.getFamily().equals("Dialog")) return f;
        }
        return new Font(Font.SANS_SERIF, style, size);
    }

    /** Font that can render Unicode symbols — delegates to uiFont which now prefers Segoe UI Symbol. */
    public static Font uiSymbolFont(int style, int size) {
        return uiFont(style, size);
    }

    // ── Logo panel ────────────────────────────────────────────────────────────
    private static Image logoImage = null;
    private static boolean logoLoaded = false;

    private static void loadLogo() {
        if (logoLoaded) return;
        logoLoaded = true;
        try {
            java.io.File f = new java.io.File("data/Picture/NavBar/Java logo.png");
            if (f.exists()) {
                logoImage = ImageIO.read(f);
            }
        } catch (Exception ignored) {}
    }

    /**
     * Creates a bulletproof logo panel that paints the logo image directly via
     * paintComponent and enforces its size through getPreferredSize/getMinimumSize/getMaximumSize.
     * This bypasses all layout manager sizing issues that plague JLabel+ImageIcon.
     */
    public static JPanel createLogoPanel(int w, int h) {
        loadLogo();
        final Image img = logoImage;
        final int pw = w, ph = h;

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                    g2.dispose();
                }
            }

            @Override
            public Dimension getPreferredSize() { return new Dimension(pw, ph); }
            @Override
            public Dimension getMinimumSize()   { return new Dimension(pw, ph); }
            @Override
            public Dimension getMaximumSize()   { return new Dimension(pw, ph); }
        };
        panel.setOpaque(false);
        return panel;
    }

    /** Creates a header panel with a subtle vertical gradient to visually
     *  separate the top navbar from the identically-coloured sidebar. */
    public static JPanel createGradientHeader() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Left=dark → Right=medium (MEDIUM_NAVY)
                GradientPaint gp = new GradientPaint(0, 0, new Color(18, 34, 68),
                        getWidth(), 0, MEDIUM_NAVY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        p.setOpaque(false);
        return p;
    }

    // ── Rounded border ────────────────────────────────────────────────────────
    public static class RoundedBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final Color color; private final int radius, thickness;
        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color; this.radius = radius; this.thickness = thickness;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(thickness));
            // offset by 1 so the stroke isn't clipped at the component edge
            int off = Math.max(1, thickness);
            g2.drawRoundRect(x + off, y + off, w - off * 2, h - off * 2, radius, radius);
            g2.dispose();
        }
        // insets must be >= offset used in paintBorder so the border is never clipped
        private int inset() { return Math.max(1, thickness) + 1; }
        @Override public Insets getBorderInsets(Component c) { int i = inset(); return new Insets(i, i, i, i); }
        @Override public Insets getBorderInsets(Component c, Insets ins) { int i = inset(); ins.set(i, i, i, i); return ins; }
    }

    // ── Rounded button ────────────────────────────────────────────────────────
    public static class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final Color base; private Color current; private static final int ARC = 5;
        public RoundedButton(String text, Color bg) {
            super(text); this.base = bg; this.current = bg;
            setFont(uiFont(Font.BOLD, 12)); setForeground(WHITE);
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
            setOpaque(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e)  { current = brighten(base, 25);  repaint(); }
                public void mouseExited (MouseEvent e)  { current = base;                 repaint(); }
                public void mousePressed(MouseEvent e)  { current = brighten(base, -20); repaint(); }
                public void mouseReleased(MouseEvent e) { current = base;                 repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(current); g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.dispose(); super.paintComponent(g);
        }
        private static Color brighten(Color c, int d) { return new Color(clamp(c.getRed()+d),clamp(c.getGreen()+d),clamp(c.getBlue()+d)); }
        private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
    }

    // ── Component styling ─────────────────────────────────────────────────────
    public static void styleButton(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(WHITE); btn.setFont(uiFont(Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(160, 32));
        btn.setContentAreaFilled(false); btn.setOpaque(false); btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.addMouseListener(new MouseAdapter() {
            Color base = color;
            public void mouseEntered(MouseEvent e) { btn.setBackground(brighten(base, 20)); btn.repaint(); }
            public void mouseExited (MouseEvent e) { btn.setBackground(base);               btn.repaint(); }
        });
    }

    private static Color brighten(Color c, int d) { return new Color(clamp(c.getRed()+d),clamp(c.getGreen()+d),clamp(c.getBlue()+d)); }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    // Row colours (alternating)
    public static final Color ROW_ODD  = new Color(240, 245, 255); // very light blue-white
    public static final Color ROW_EVEN = new Color(225, 233, 248); // slightly deeper
    public static final Color ROW_TEXT = new Color(15,  25,  50);  // near-black for readability

    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (c instanceof JLabel) ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? ROW_ODD : ROW_EVEN);
                    c.setForeground(ROW_TEXT);
                } else {
                    c.setBackground(ACCENT_BLUE);
                    c.setForeground(WHITE);
                }
                return c;
            }
        };
        table.setBackground(ROW_ODD); table.setForeground(ROW_TEXT); table.setFont(uiFont(Font.PLAIN, 12));
        table.setRowHeight(28); table.setSelectionBackground(ACCENT_BLUE); table.setSelectionForeground(WHITE);
        table.setGridColor(new Color(190, 205, 230)); table.setFillsViewportHeight(true);
        table.setShowHorizontalLines(true); table.setShowVerticalLines(false);
        // Default cell renderer – black text, alternating rows handled by prepareRenderer
        javax.swing.table.DefaultTableCellRenderer center = new javax.swing.table.DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        center.setBackground(ROW_ODD); center.setForeground(ROW_TEXT);
        for (int i = 0; i < model.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(center);
        // Header
        table.getTableHeader().setBackground(TABLE_HEADER); table.getTableHeader().setForeground(WHITE);
        table.getTableHeader().setFont(uiFont(Font.BOLD, 12));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_BLUE));
        table.getTableHeader().setReorderingAllowed(false);
        ((javax.swing.table.DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
        return table;
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.getViewport().setBackground(TABLE_BG);
        sp.setBorder(new RoundedBorder(ACCENT_BLUE, 10, 1));
        sp.getVerticalScrollBar().setBackground(MEDIUM_NAVY);
        sp.getHorizontalScrollBar().setBackground(MEDIUM_NAVY);
    }

    // ── JTabbedPane styling ────────────────────────────────────────────────────
    /**
     * Applies a consistent styled look to any JTabbedPane in the system.
     * Renders a red rounded-rectangle border around the tab area (matching the
     * screenshot design), bold labels, and light background for easy reading.
     */
    public static void styleJTabbedPane(JTabbedPane tabs) {
        tabs.setBackground(new Color(240, 245, 255));
        tabs.setForeground(new Color(15, 25, 50));
        tabs.setFont(uiFont(Font.BOLD, 12));
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(200, 50, 50), 10, 2),
            new EmptyBorder(2, 2, 2, 2)));

        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) { /* suppress individual tab borders */ }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? new Color(200, 50, 50) : new Color(240, 245, 255));
                g2.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 8, 8);
                g2.dispose();
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                    int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) { /* suppress */ }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                /* suppress default content border – our rounded border is on the pane */ }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics,
                    int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(uiFont(isSelected ? Font.BOLD : Font.PLAIN, 12));
                g2.setColor(isSelected ? Color.WHITE : new Color(15, 25, 50));
                FontMetrics fm = g2.getFontMetrics();
                int tx = textRect.x + (textRect.width - fm.stringWidth(title)) / 2;
                int ty = textRect.y + fm.getAscent();
                g2.drawString(title, tx, ty);
                g2.dispose();
            }
        });
    }

    public static void styleTextField(JTextField field) {
        field.setFont(uiFont(Font.PLAIN, 12));
        field.setBackground(new Color(46, 68, 118)); field.setForeground(WHITE); field.setCaretColor(WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(ACCENT_BLUE, 8, 1), new EmptyBorder(5, 9, 5, 9)));
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(46, 68, 118)); combo.setForeground(WHITE);
        combo.setFont(uiFont(Font.PLAIN, 12)); combo.setBorder(new RoundedBorder(ACCENT_BLUE, 8, 1));
    }

    public static JComboBox<String> createGenderCombo(String currentGender) {
        JComboBox<String> cb = new JComboBox<>(new String[]{"-- Choose --", "Male", "Female"});
        styleCombo(cb);
        if ("Male".equalsIgnoreCase(currentGender))        cb.setSelectedItem("Male");
        else if ("Female".equalsIgnoreCase(currentGender)) cb.setSelectedItem("Female");
        else                                                cb.setSelectedIndex(0);
        return cb;
    }

    public static String resolveGender(JComboBox<String> cb) {
        String sel = (String) cb.getSelectedItem();
        return ("-- Choose --".equals(sel) || sel == null) ? "" : sel;
    }

    // ── Background image helpers ───────────────────────────────────────────────
    public static String saveBgImage(String userId, String role, File srcFile) {
        String dir = "data/Picture/" + role + "/";
        new File(dir).mkdirs();
        String lower = srcFile.getName().toLowerCase();
        String ext = (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) ? "jpg" : "png";
        for (String e : new String[]{"png","jpg","jpeg"}) {
            File f = new File(dir + "bg_" + userId + "." + e); if (f.exists()) f.delete();
            for (int n = 1; n <= 50; n++) { File sf = new File(dir+"bg_"+userId+"_"+n+"."+e); if (sf.exists()) sf.delete(); else break; }
        }
        File dest = new File(dir + "bg_" + userId + "." + ext);
        if (dest.exists()) { int n=1; while (dest.exists()) { dest=new File(dir+"bg_"+userId+"_"+n++ +"."+ext); } }
        try { Files.copy(srcFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING); return dest.getPath().replace("\\","/"); }
        catch (IOException e) { System.err.println("Background save error: "+e.getMessage()); return null; }
    }

    public static void deleteBgImage(String userId, String role) {
        String dir = "data/Picture/" + role + "/";
        for (String e : new String[]{"png","jpg","jpeg"}) {
            File f = new File(dir+"bg_"+userId+"."+e); if (f.exists()) f.delete();
            for (int n=1; n<=50; n++) { File sf=new File(dir+"bg_"+userId+"_"+n+"."+e); if (sf.exists()) sf.delete(); else break; }
        }
    }

    public static Image loadBgImage(String userId, String role) {
        String path = getBgImagePath(userId, role);
        if (path == null) return null;
        try { return new ImageIcon(new File(path).getAbsolutePath()).getImage(); } catch (Exception ex) { return null; }
    }

    public static String getBgImagePath(String userId, String role) {
        String dir = "data/Picture/" + role + "/", base = "bg_" + userId;
        for (String e : new String[]{"png","jpg","jpeg"}) {
            File f = new File(dir+base+"."+e); if (f.exists()) return f.getPath().replace("\\","/");
            for (int n=1; n<=50; n++) { File sf=new File(dir+base+"_"+n+"."+e); if (sf.exists()) return sf.getPath().replace("\\","/"); }
        }
        return null;
    }

    // ── Avatar helpers ────────────────────────────────────────────────────────
    private static final String AVATAR_DIR            = "data/avatars/";
    private static final String DEFAULT_AVATAR_MALE   = "data/avatars/boy.jpeg";
    private static final String DEFAULT_AVATAR_FEMALE = "data/avatars/girl.jpeg";
    private static final String DEFAULT_AVATAR_ANON   = "data/avatars/anonymous.jpg";
    private static final int    AVATAR_SIZE            = 48;

    public static File getAvatarFile(String userId) {
        if (userId != null) {
            for (String ext : new String[]{"png","jpg","jpeg"}) {
                File f = new File(AVATAR_DIR + userId + "." + ext); if (f.exists()) return f;
            }
        }
        return null;
    }

    public static boolean saveAvatar(String userId, File srcFile) {
        try {
            new File(AVATAR_DIR).mkdirs();
            for (String ext : new String[]{"png","jpg","jpeg"}) { File old=new File(AVATAR_DIR+userId+"."+ext); if (old.exists()) old.delete(); }
            String name = srcFile.getName().toLowerCase();
            String ext  = (name.endsWith(".jpg")||name.endsWith(".jpeg")) ? "jpg" : "png";
            Files.copy(srcFile.toPath(), new File(AVATAR_DIR+userId+"."+ext).toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) { System.err.println("Avatar save error: "+e.getMessage()); return false; }
    }

    public static ImageIcon loadCircularAvatar(String userId, String gender, int diameter) {
        File f = getAvatarFile(userId);
        if (f == null) {
            if ("Male".equalsIgnoreCase(gender))   { File d=new File(DEFAULT_AVATAR_MALE);   if (d.exists()) f=d; }
            else if ("Female".equalsIgnoreCase(gender)) { File d=new File(DEFAULT_AVATAR_FEMALE); if (d.exists()) f=d; }
            else { File d=new File(DEFAULT_AVATAR_ANON); if (d.exists()) f=d; }
        }
        BufferedImage src = null;
        if (f != null) { try { src = ImageIO.read(f); } catch (IOException ignored) {} }
        BufferedImage result = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setClip(new Ellipse2D.Float(0, 0, diameter, diameter));
        if (src != null) { g2.drawImage(src, 0, 0, diameter, diameter, null); }
        else {
            g2.setPaint(new GradientPaint(0,0,new Color(80,130,200),diameter,diameter,new Color(35,65,135)));
            g2.fillOval(0,0,diameter,diameter);
            g2.setColor(new Color(200,215,235));
            int hd=diameter*2/5, hx=(diameter-hd)/2, hy=diameter/8;
            g2.fillOval(hx,hy,hd,hd); g2.fillOval((diameter-diameter*3/5)/2,hy+hd-2,diameter*3/5,diameter*2/5);
        }
        g2.dispose();
        Graphics2D g3 = result.createGraphics();
        g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g3.setColor(new Color(100,160,235,200)); g3.setStroke(new BasicStroke(2f));
        g3.drawOval(1,1,diameter-2,diameter-2); g3.dispose();
        return new ImageIcon(result);
    }

    public static ImageIcon loadCircularAvatar(String userId, int diameter) { return loadCircularAvatar(userId, "", diameter); }
    public static ImageIcon loadCircularAvatar(String userId) { return loadCircularAvatar(userId, "", AVATAR_SIZE); }

    // ── Notification bell ─────────────────────────────────────────────────────
    /**
     * Creates a notification bell label with an optional red badge.
     * badgeCount > 0 shows a red circle with the count; 0 = no badge.
     * Pass a Runnable onClick to handle clicks (e.g. show notification list).
     */
    public static JLabel createNotificationBell(int badgeCount, Runnable onClick) {
        JLabel bell = new JLabel() {
            private static final long serialVersionUID = 1L;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(); // h not needed — bell is drawn with fixed offsets from top
                // Bell body
                int bx = w/2 - 9, bw = 18, bh = 15;
                g2.setColor(new Color(210, 220, 240));
                g2.fillArc(bx, 5, bw, bh, 0, 180);           // dome
                g2.fillRect(bx, 5 + bh/2, bw, bh/2 + 2);    // body
                // Bell mouth (flare)
                g2.fillRoundRect(bx - 2, 5 + bh - 1, bw + 4, 5, 4, 4);
                // Clapper
                g2.setColor(new Color(180, 195, 220));
                g2.fillOval(w/2 - 3, 5 + bh + 4, 6, 5);
                // Handle
                g2.setColor(new Color(180, 195, 220));
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(w/2 - 4, 1, 8, 8, 0, 180);
                // Badge
                if (badgeCount > 0) {
                    int bSize = badgeCount > 9 ? 16 : 14;
                    int bxPos = w/2 + 4, byPos = 1;
                    g2.setColor(new Color(220, 50, 50));
                    g2.fillOval(bxPos, byPos, bSize, bSize);
                    g2.setColor(Color.WHITE);
                    g2.setFont(uiFont(Font.BOLD, 9));
                    String txt = badgeCount > 9 ? "9+" : String.valueOf(badgeCount);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(txt, bxPos + (bSize - fm.stringWidth(txt))/2, byPos + fm.getAscent() + 1);
                }
                g2.dispose();
            }
        };
        bell.setPreferredSize(new Dimension(36, 36));
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bell.setToolTipText(badgeCount > 0 ? badgeCount + " notification(s)" : "No new notifications");
        if (onClick != null) bell.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
        });
        return bell;
    }

    /**
     * Builds the full header-right widget:
     *   [🔔 bell]  [username / (optional VIP)]  [avatar]
     * All in a horizontal FlowLayout panel, ready to add to EAST of the header.
     */
    public static JPanel createHeaderRightPanel(
            String userId, String gender, String username,
            String userEmail, int notifCount,
            JFrame parentFrame,
            Runnable onMyProfile, Runnable onMyVehicle, Runnable onLogout,
            boolean showVip) {

        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        // ── Live clock (top row, right-aligned) ──────────────────────────
        JLabel clockLabel = new JLabel();
        clockLabel.setFont(uiFont(Font.PLAIN, 11));
        clockLabel.setForeground(DIM_TEXT);
        clockLabel.setHorizontalAlignment(JLabel.RIGHT);
        DateTimeFormatter clockFmt = DateTimeFormatter.ofPattern(
                "EEEE, dd MMMM yyyy  |  hh:mm:ss a", Locale.ENGLISH);
        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e ->
                clockLabel.setText(LocalDateTime.now().format(clockFmt)));
        clockTimer.setInitialDelay(0);
        clockTimer.start();

        // ── Bell + userInfo + avatar row ──────────────────────────────────
        JLabel bell = createNotificationBell(notifCount, () -> {
            String msg = notifCount > 0
                    ? notifCount + " pending notification(s).\n(Full notification centre coming soon.)"
                    : "No new notifications.";
            JOptionPane.showMessageDialog(parentFrame, msg, "Notifications", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setOpaque(false);

        JLabel usernameLbl = new JLabel(username);
        usernameLbl.setFont(uiFont(Font.BOLD, 12));
        usernameLbl.setForeground(WHITE);
        usernameLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);

        userInfo.add(usernameLbl);

        if (showVip) {
            VipAccount vip = asc.util.FileManager.findVipByCustomerId(userId);
            JLabel vipLbl;
            if (vip != null && !VipAccount.TIER_NONE.equals(vip.getTier())) {
                vipLbl = new JLabel(vip.tierBadge());
                vipLbl.setFont(uiFont(Font.BOLD, 10));
                vipLbl.setForeground(vip.tierColor());
            } else {
                vipLbl = new JLabel(vip != null ? "VIP: NONE" : "No VIP");
                vipLbl.setFont(uiFont(Font.PLAIN, 10));
                vipLbl.setForeground(DIM_TEXT);
            }
            vipLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            userInfo.add(Box.createVerticalStrut(1));
            userInfo.add(vipLbl);
        }

        JLabel avatar = createAvatarMenuLabel(userId, gender, username, userEmail,
                parentFrame, onMyProfile, onMyVehicle, onLogout, showVip);

        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        avatarRow.setOpaque(false);
        avatarRow.add(bell);
        avatarRow.add(userInfo);
        avatarRow.add(avatar);

        p.add(clockLabel, BorderLayout.NORTH);
        p.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        p.add(avatarRow,  BorderLayout.SOUTH);
        return p;
    }

    /** Staff/technician variant (no vehicle, no VIP). */
    public static JPanel createHeaderRightPanel(
            String userId, String gender, String username,
            String userEmail, int notifCount,
            JFrame parentFrame,
            Runnable onMyProfile, Runnable onLogout) {
        return createHeaderRightPanel(userId, gender, username, userEmail, notifCount,
                parentFrame, onMyProfile, null, onLogout, false);
    }

    // ── Avatar menu popup ─────────────────────────────────────────────────────
    private static final AtomicReference<JWindow> activePopup = new AtomicReference<>(null);
    private static AWTEventListener globalClickListener = null;

    /** Standard 2-action variant (no My Vehicle). */
    public static JLabel createAvatarMenuLabel(String userId, String gender,
                                                String userName, String userEmail,
                                                JFrame parentFrame,
                                                Runnable onMyProfile, Runnable onLogout) {
        return createAvatarMenuLabel(userId, gender, userName, userEmail, parentFrame, onMyProfile, null, onLogout);
    }

    /**
     * Full variant: the popup shows the user's name, VIP tier badge (only for
     * customers), an optional My Vehicle item, and Log out.
     * Pass showVip=true only for CustomerDashboard.
     */
    public static JLabel createAvatarMenuLabel(String userId, String gender,
                                                String userName, String userEmail,
                                                JFrame parentFrame,
                                                Runnable onMyProfile,
                                                Runnable onMyVehicle,
                                                Runnable onLogout,
                                                boolean showVip) {
        JLabel label = new JLabel(loadCircularAvatar(userId, gender, AVATAR_SIZE));
        label.setToolTipText("Click to open menu");
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JWindow prev = activePopup.get();
                if (prev != null) { closePopup(); return; }
                showAvatarPopup(label, userId, gender, userName, userEmail,
                        parentFrame, onMyProfile, onMyVehicle, onLogout, showVip,
                        () -> label.setIcon(loadCircularAvatar(userId, gender, AVATAR_SIZE)));
            }
        });
        return label;
    }

    /** Backward-compat: no vehicle, no VIP (staff roles). */
    public static JLabel createAvatarMenuLabel(String userId, String gender,
                                                String userName, String userEmail,
                                                JFrame parentFrame,
                                                Runnable onMyProfile,
                                                Runnable onMyVehicle,
                                                Runnable onLogout) {
        return createAvatarMenuLabel(userId, gender, userName, userEmail,
                parentFrame, onMyProfile, onMyVehicle, onLogout, false);
    }

    public static JLabel createAvatarMenuLabel(String userId, String userName,
                                                String userEmail, JFrame parentFrame,
                                                Runnable onMyProfile, Runnable onLogout) {
        return createAvatarMenuLabel(userId, "", userName, userEmail, parentFrame, onMyProfile, onLogout);
    }

    private static void showAvatarPopup(JLabel avatarLabel,
                                         String userId, String gender,
                                         String userName, String userEmail,
                                         JFrame parentFrame,
                                         Runnable onMyProfile, Runnable onMyVehicle, Runnable onLogout,
                                         boolean showVip,
                                         Runnable onAvatarChanged) {
        closePopup();
        JWindow popup = new JWindow(parentFrame);
        popup.setBackground(new Color(0, 0, 0, 0));
        activePopup.set(popup);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(POPUP_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(POPUP_BORDER, 12, 1), new EmptyBorder(0, 0, 6, 0)));

        // ── Info section ─────────────────────────────────────────────────────
        JPanel infoSection = new JPanel(new BorderLayout(12, 0));
        infoSection.setBackground(POPUP_BG);
        infoSection.setBorder(new EmptyBorder(14, 16, 12, 16));
        infoSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel popupAvatar = new JLabel(loadCircularAvatar(userId, gender, 46));
        popupAvatar.setPreferredSize(new Dimension(46, 46));
        popupAvatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        popupAvatar.setToolTipText("Click to change photo");
        popupAvatar.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Choose Profile Picture");
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "png","jpg","jpeg"));
                if (fc.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
                    if (saveAvatar(userId, fc.getSelectedFile())) {
                        popupAvatar.setIcon(loadCircularAvatar(userId, gender, 46));
                        if (onAvatarChanged != null) onAvatarChanged.run();
                    } else {
                        JOptionPane.showMessageDialog(parentFrame, "Failed to save photo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JPanel textInfo = new JPanel();
        textInfo.setLayout(new BoxLayout(textInfo, BoxLayout.Y_AXIS));
        textInfo.setBackground(POPUP_BG);

        JLabel nameLbl  = new JLabel(userName);
        nameLbl.setFont(uiFont(Font.BOLD, 14)); nameLbl.setForeground(WHITE);
        JLabel emailLbl = new JLabel(userEmail);
        emailLbl.setFont(uiFont(Font.PLAIN, 11)); emailLbl.setForeground(new Color(165, 190, 225));

        textInfo.add(nameLbl);
        textInfo.add(Box.createVerticalStrut(2));
        textInfo.add(emailLbl);

        // ── VIP badge (only for Customer role) ────────────────────────────
        if (showVip) {
            VipAccount vip = asc.util.FileManager.findVipByCustomerId(userId);
            JLabel vipLbl;
            if (vip != null && !VipAccount.TIER_NONE.equals(vip.getTier())) {
                vipLbl = new JLabel(vip.tierBadge());
                vipLbl.setFont(uiFont(Font.BOLD, 11));
                vipLbl.setForeground(vip.tierColor());
            } else {
                vipLbl = new JLabel("No VIP Tier");
                vipLbl.setFont(uiFont(Font.ITALIC, 10));
                vipLbl.setForeground(DIM_TEXT);
            }
            textInfo.add(Box.createVerticalStrut(2));
            textInfo.add(vipLbl);
        }

        infoSection.add(popupAvatar, BorderLayout.WEST);
        infoSection.add(textInfo,    BorderLayout.CENTER);
        card.add(infoSection);
        card.add(makeDivider());

        // ── My Profile ───────────────────────────────────────────────────────
        JPanel profileRow = makeMenuRow("My Profile", false,
                loadMenuIcon("data/Picture/ProfileSystem/My Profile.png", 16, 16));
        profileRow.addMouseListener(new MenuRowHover(profileRow) {
            @Override public void mouseClicked(MouseEvent e) { closePopup(); if (onMyProfile != null) onMyProfile.run(); }
        });
        card.add(profileRow);

        // ── My Vehicle (optional) ─────────────────────────────────────────────
        if (onMyVehicle != null) {
            card.add(makeDivider());
            JPanel vehicleRow = makeMenuRow("My Vehicle", false,
                    loadMenuIcon("data/Picture/ProfileSystem/My Profile.png", 16, 16));
            vehicleRow.addMouseListener(new MenuRowHover(vehicleRow) {
                @Override public void mouseClicked(MouseEvent e) { closePopup(); onMyVehicle.run(); }
            });
            card.add(vehicleRow);
        }

        card.add(makeDivider());

        // ── Log out ───────────────────────────────────────────────────────────
        JPanel logoutRow = makeMenuRow("Log out", true,
                loadMenuIcon("data/Picture/ProfileSystem/Log out.png", 16, 16));
        logoutRow.addMouseListener(new MenuRowHover(logoutRow) {
            @Override public void mouseClicked(MouseEvent e) { closePopup(); if (onLogout != null) onLogout.run(); }
        });
        card.add(logoutRow);

        popup.getContentPane().add(card);
        popup.pack();
        Point p = avatarLabel.getLocationOnScreen();
        popup.setLocation(p.x + avatarLabel.getWidth() - popup.getWidth(), p.y + avatarLabel.getHeight() + 6);
        popup.setVisible(true);

        if (globalClickListener != null) { Toolkit.getDefaultToolkit().removeAWTEventListener(globalClickListener); globalClickListener = null; }
        globalClickListener = new AWTEventListener() {
            @Override public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent me = (MouseEvent) event;
                    if (me.getID() == MouseEvent.MOUSE_PRESSED) {
                        JWindow current = activePopup.get();
                        if (current == null) { Toolkit.getDefaultToolkit().removeAWTEventListener(this); globalClickListener = null; return; }
                        Component src = me.getComponent();
                        if (src != null) { Window w = SwingUtilities.getWindowAncestor(src); if (w == current) return; }
                        Toolkit.getDefaultToolkit().removeAWTEventListener(this); globalClickListener = null;
                        SwingUtilities.invokeLater(() -> closePopup());
                    }
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(globalClickListener, AWTEvent.MOUSE_EVENT_MASK);
        popup.addWindowFocusListener(new WindowFocusListener() {
            @Override public void windowGainedFocus(WindowEvent e) {}
            @Override public void windowLostFocus(WindowEvent e)  { SwingUtilities.invokeLater(() -> closePopup()); }
        });
    }

    private static void closePopup() {
        JWindow w = activePopup.getAndSet(null);
        if (w != null) { w.setVisible(false); w.dispose(); }
        if (globalClickListener != null) { Toolkit.getDefaultToolkit().removeAWTEventListener(globalClickListener); globalClickListener = null; }
    }

    public static ImageIcon loadMenuIcon(String path, int w, int h) {
        try {
            File f = new File(path); if (!f.exists()) return null;
            Image img = new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ex) { return null; }
    }

    // =========================================================================
    //  VECTOR ICON FACTORY  – all icons drawn with Java 2D, no image files needed
    // =========================================================================

    /**
     * Renders a named vector icon into a BufferedImage and returns it as an ImageIcon.
     * Supported names (case-insensitive):
     *   home, history, feedback, comment, receipt, chart, vip, topup, products,
     *   appointments, customers, payments, manager_home, reports, parts, prices,
     *   pending, users, technician, logout, profile, vehicle, bell, star
     */
    public static ImageIcon vectorIcon(String name, int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,  RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,       RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(color);

        // Normalised drawing space: treat canvas as 20x20, scale to actual size
        double sc = size / 20.0;
        g.scale(sc, sc);

        BasicStroke thin  = new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke thick = new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke bold  = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        switch (name.toLowerCase()) {

            // ── HOME ──────────────────────────────────────────────────────
            case "home": {
                g.setStroke(thick);
                // Roof (triangle)
                int[] rx = {2, 10, 18};
                int[] ry = {10,  2, 10};
                g.drawPolygon(rx, ry, 3);
                // Door
                g.drawRect(7, 12, 6, 6);
                // Walls
                g.drawLine(3, 10, 3, 18);
                g.drawLine(17,10, 17,18);
                g.drawLine(3, 18, 17,18);
                break;
            }

            // ── SERVICE HISTORY ────────────────────────────────────────
            case "history": {
                g.setStroke(thick);
                // Clock circle
                g.drawOval(3, 2, 14, 14);
                // Clock hands
                g.drawLine(10, 9, 10, 5);
                g.drawLine(10, 9, 13, 11);
                // History arrow (curved, below)
                g.setStroke(thin);
                g.drawArc(5, 13, 10, 6, 20, 140);
                g.drawLine(5, 16, 5, 19);
                g.drawLine(5, 19, 8, 19);
                break;
            }

            // ── VIEW FEEDBACKS ───────────────────────────────────────
            case "feedback": {
                g.setStroke(thick);
                // Speech bubble
                g.drawRoundRect(2, 2, 16, 12, 4, 4);
                // Tail
                g.drawLine(5, 14, 3, 18);
                g.drawLine(3, 18, 8, 14);
                // Lines inside
                g.setStroke(thin);
                g.drawLine(5, 7, 15, 7);
                g.drawLine(5, 10, 12, 10);
                break;
            }

            // ── LEAVE A COMMENT (star rating) ──────────────────────
            case "comment": {
                g.setStroke(thick);
                // Pencil body
                int[] px = {4, 14, 16, 6};
                int[] py = {14,  4,  6, 16};
                g.drawPolygon(px, py, 4);
                // Pencil tip
                g.drawLine(4, 14, 2, 18);
                g.drawLine(2, 18, 6, 16);
                // Eraser line
                g.setStroke(thin);
                g.drawLine(13, 5, 15, 7);
                break;
            }

            // ── RECEIPT ──────────────────────────────────────────────
            case "receipt": {
                g.setStroke(thick);
                // Paper body with torn bottom edge
                int[] bx = {4, 16, 16, 13, 11, 9, 7, 4};
                int[] by = {2,  2, 18, 16, 18, 16, 18, 18};
                g.drawPolygon(bx, by, 8);
                // Lines
                g.setStroke(thin);
                g.drawLine(7,  6, 13,  6);
                g.drawLine(7,  9, 13,  9);
                g.drawLine(7, 12, 10, 12);
                break;
            }

            // ── SPENDING CHART ───────────────────────────────────────
            case "chart": {
                g.setStroke(thick);
                // Axes
                g.drawLine(3, 17, 3,  3);
                g.drawLine(3, 17, 17, 17);
                // Bars
                g.fillRect(5,  12, 3, 5);
                g.fillRect(9,   8, 3, 9);
                g.fillRect(13,  5, 3, 12);
                break;
            }

            // ── VIP & COUPONS ─────────────────────────────────────────
            case "vip": {
                g.setStroke(thick);
                // Crown
                int[] cx = {2, 5, 10, 15, 18, 16, 4};
                int[] cy = {14, 6, 10,  6, 14, 17, 17};
                g.drawPolygon(cx, cy, 7);
                // Crown jewels
                g.fillOval(9, 8, 3, 3);
                g.fillOval(4, 4, 2, 2);
                g.fillOval(14, 4, 2, 2);
                break;
            }

            // ── TOP-UP WALLET ───────────────────────────────────────
            case "topup": {
                g.setStroke(thick);
                // Wallet body
                g.drawRoundRect(2, 5, 16, 12, 4, 4);
                // Coin slot
                g.fillRoundRect(13, 9, 4, 4, 2, 2);
                // Plus symbol (top-up)
                g.setStroke(bold);
                g.drawLine(10, 1, 10, 5);
                g.drawLine(8,  3, 12,  3);
                break;
            }

            // ── PRODUCTS / CAR PARTS ──────────────────────────────
            case "products": {
                g.setStroke(thick);
                // Gear / cog
                int cx2 = 10, cy2 = 10, r1 = 4, r2 = 7;
                // 8 teeth
                for (int i = 0; i < 8; i++) {
                    double a1 = Math.toRadians(i * 45 - 15);
                    double a2 = Math.toRadians(i * 45 + 15);
                    int x1 = (int)(cx2 + r1 * Math.cos(a1));
                    int y1 = (int)(cy2 + r1 * Math.sin(a1));
                    int x2 = (int)(cx2 + r2 * Math.cos(a1));
                    int y2 = (int)(cy2 + r2 * Math.sin(a1));
                    int x3 = (int)(cx2 + r2 * Math.cos(a2));
                    int y3 = (int)(cy2 + r2 * Math.sin(a2));
                    int x4 = (int)(cx2 + r1 * Math.cos(a2));
                    int y4 = (int)(cy2 + r1 * Math.sin(a2));
                    int[] gx = {x1, x2, x3, x4};
                    int[] gy = {y1, y2, y3, y4};
                    g.fillPolygon(gx, gy, 4);
                }
                // Inner circle
                g.setColor(DARK_NAVY);
                g.fillOval(cx2 - 3, cy2 - 3, 6, 6);
                g.setColor(color);
                g.drawOval(cx2 - 3, cy2 - 3, 6, 6);
                break;
            }

            // ── APPOINTMENTS ─────────────────────────────────────────
            case "appointments": {
                g.setStroke(thick);
                // Calendar
                g.drawRoundRect(2, 4, 16, 14, 3, 3);
                g.drawLine(2, 8, 18, 8);
                g.drawLine(7, 2, 7, 6);
                g.drawLine(13,2, 13,6);
                // Dots
                g.setStroke(thin);
                g.fillRect(5,  10, 2, 2);
                g.fillRect(9,  10, 2, 2);
                g.fillRect(13, 10, 2, 2);
                g.fillRect(5,  13, 2, 2);
                g.fillRect(9,  13, 2, 2);
                break;
            }

            // ── CUSTOMER MANAGEMENT ───────────────────────────────
            case "customers": {
                g.setStroke(thick);
                // Person 1 (left)
                g.drawOval(3, 2, 6, 6);
                g.drawArc(1, 11, 8, 8, 0, 180);
                // Person 2 (right)
                g.drawOval(11, 2, 6, 6);
                g.drawArc(9, 11, 8, 8, 0, 180);
                // Plus (management)
                g.setStroke(bold);
                g.drawLine(10, 14, 10, 18);
                g.drawLine(8, 16, 12, 16);
                break;
            }

            // ── PAYMENTS ─────────────────────────────────────────────
            case "payments": {
                g.setStroke(thick);
                // Card body
                g.drawRoundRect(2, 4, 16, 12, 3, 3);
                // Magnetic strip
                g.fillRect(2, 7, 16, 3);
                // Chip
                g.setColor(DARK_NAVY);
                g.fillRect(4, 11, 4, 3);
                g.setColor(color);
                g.drawRect(4, 11, 4, 3);
                // Digits
                g.setStroke(thin);
                g.drawLine(10, 12, 15, 12);
                break;
            }

            // ── MANAGER HOME ───────────────────────────────────────
            case "manager_home": {
                // Reuse home icon
                g.setStroke(thick);
                int[] rx = {2, 10, 18};
                int[] ry = {10,  2, 10};
                g.drawPolygon(rx, ry, 3);
                g.drawRect(7, 12, 6, 6);
                g.drawLine(3, 10, 3, 18);
                g.drawLine(17,10, 17,18);
                g.drawLine(3, 18, 17,18);
                break;
            }

            // ── REPORTS ──────────────────────────────────────────────
            case "reports": {
                g.setStroke(thick);
                // Document
                g.drawRoundRect(3, 1, 14, 18, 3, 3);
                // Line chart inside
                g.setStroke(thin);
                g.drawLine(6, 14, 9, 10);
                g.drawLine(9, 10, 12, 12);
                g.drawLine(12, 12, 15, 6);
                // Axis
                g.drawLine(5, 15, 15, 15);
                g.drawLine(5, 6, 5, 15);
                break;
            }

            // ── CAR PARTS CATALOGUE ──────────────────────────────
            case "parts": {
                g.setStroke(thick);
                // Wrench
                g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Wrench head (ring)
                g.drawOval(2, 2, 7, 7);
                // Wrench handle
                int[] wx = {8, 18, 17, 7};
                int[] wy = {8, 16, 17, 9};
                g.fillPolygon(wx, wy, 4);
                // Inner hole
                g.setColor(DARK_NAVY);
                g.fillOval(4, 4, 3, 3);
                g.setColor(color);
                break;
            }

            // ── SET PRICES ───────────────────────────────────────────
            case "prices": {
                g.setStroke(thick);
                // Tag shape
                int[] tx = {3, 12, 18, 12, 3};
                int[] ty = {3, 3, 10, 17, 17};
                g.drawPolygon(tx, ty, 5);
                // RM text simulation (two lines)
                g.setStroke(thin);
                g.drawLine(7, 8, 10, 8);
                g.drawLine(7, 12, 10, 12);
                // Hole in tag
                g.drawOval(14, 7, 3, 3);
                break;
            }

            // ── PENDING APPROVALS ────────────────────────────────
            case "pending": {
                g.setStroke(thick);
                // Clipboard
                g.drawRoundRect(3, 3, 14, 16, 3, 3);
                g.fillRoundRect(7, 1, 6, 4, 2, 2);
                // Tick / Check
                g.setStroke(bold);
                g.drawLine(6, 11, 9, 14);
                g.drawLine(9, 14, 14, 8);
                break;
            }

            // ── USER MANAGEMENT ───────────────────────────────────
            case "users": {
                g.setStroke(thick);
                // Single person
                g.drawOval(6, 2, 8, 8);
                g.drawArc(3, 13, 14, 8, 0, 180);
                // Settings gear (small, bottom-right)
                g.setStroke(thin);
                g.drawOval(14, 13, 4, 4);
                g.drawLine(16, 11, 16, 13);
                g.drawLine(16, 17, 16, 19);
                g.drawLine(12, 15, 14, 15);
                g.drawLine(18, 15, 20, 15);
                break;
            }

            // ── FEEDBACKS & COMMENTS (MANAGER) ────────────────────
            case "feedbacks": {
                g.setStroke(thick);
                // Two speech bubbles
                g.drawRoundRect(1, 2, 13, 9, 3, 3);
                g.drawLine(4, 11, 2, 14);
                g.drawLine(2, 14, 6, 11);
                g.drawRoundRect(6, 9, 13, 9, 3, 3);
                g.drawLine(16, 18, 14, 21);
                g.drawLine(14, 21, 18, 18);
                break;
            }

            // ── TECHNICIAN DASHBOARD ─────────────────────────────
            case "technician": {
                g.setStroke(thick);
                // Wrench + screwdriver crossed
                // Wrench
                g.drawOval(2, 2, 6, 6);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(7, 7, 16, 16);
                // Screwdriver
                g.setStroke(thin);
                g.drawLine(4, 16, 13, 7);
                g.drawLine(2, 18, 4, 16);
                g.fillRect(1, 17, 3, 2);
                break;
            }

            // ── LOGOUT ───────────────────────────────────────────────
            case "logout": {
                g.setStroke(thick);
                // Door outline
                g.drawRoundRect(2, 2, 11, 16, 2, 2);
                // Arrow pointing right (exit)
                g.drawLine(9, 10, 18, 10);
                g.drawLine(15, 7, 18, 10);
                g.drawLine(15, 13, 18, 10);
                break;
            }

            // ── MY PROFILE ───────────────────────────────────────────
            case "profile": {
                g.setStroke(thick);
                g.drawOval(6, 2, 8, 8);
                g.drawArc(2, 13, 16, 8, 0, 180);
                break;
            }

            // ── MY VEHICLE ───────────────────────────────────────────
            case "vehicle": {
                g.setStroke(thick);
                // Car body
                int[] carx = {2, 5, 7, 13, 15, 18, 18, 2};
                int[] cary = {12, 12, 7, 7, 12, 12, 16, 16};
                g.drawPolygon(carx, cary, 8);
                // Windows
                g.setStroke(thin);
                g.drawLine(7, 12, 8,  8);
                g.drawLine(8, 8, 12,  8);
                g.drawLine(12, 8, 13, 12);
                // Wheels
                g.setStroke(thick);
                g.drawOval(4,  14, 4, 4);
                g.drawOval(12, 14, 4, 4);
                break;
            }

            // ── STAR / RATING ───────────────────────────────────────
            case "star": {
                // 5-point star
                int[] sx = new int[10];
                int[] sy = new int[10];
                for (int i = 0; i < 10; i++) {
                    double angle = Math.toRadians(i * 36 - 90);
                    int r = (i % 2 == 0) ? 9 : 4;
                    sx[i] = (int)(10 + r * Math.cos(angle));
                    sy[i] = (int)(10 + r * Math.sin(angle));
                }
                g.setStroke(thin);
                g.drawPolygon(sx, sy, 10);
                break;
            }

            default:
                // Fallback: simple circle with a dot
                g.setStroke(thick);
                g.drawOval(3, 3, 14, 14);
                g.fillOval(9, 9, 2, 2);
                break;
        }

        g.dispose();
        return new ImageIcon(img);
    }

    /** Convenience: draw icon in white. */
    public static ImageIcon vectorIcon(String name, int size) {
        return vectorIcon(name, size, WHITE);
    }

    private static JPanel makeMenuRow(String text, boolean isDanger, ImageIcon icon) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(POPUP_BG); row.setBorder(new EmptyBorder(10, 16, 10, 20));
        row.setAlignmentX(Component.LEFT_ALIGNMENT); row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel lbl = new JLabel(text); lbl.setFont(uiFont(Font.PLAIN, 13));
        lbl.setForeground(isDanger ? new Color(230, 100, 100) : WHITE);
        if (icon != null) { JLabel iconLbl = new JLabel(icon); iconLbl.setPreferredSize(new Dimension(16,16)); row.add(iconLbl, BorderLayout.WEST); }
        row.add(lbl, BorderLayout.CENTER);
        return row;
    }

    private static JSeparator makeDivider() {
        JSeparator sep = new JSeparator(); sep.setForeground(POPUP_DIVIDER); sep.setBackground(POPUP_DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); return sep;
    }

    private abstract static class MenuRowHover extends MouseAdapter {
        private final JPanel row; MenuRowHover(JPanel r) { this.row = r; }
        @Override public void mouseEntered(MouseEvent e) { row.setBackground(POPUP_HOVER); row.repaint(); }
        @Override public void mouseExited (MouseEvent e) { row.setBackground(POPUP_BG);    row.repaint(); }
    }

    // ── Profile dialog avatar ─────────────────────────────────────────────────
    public static JLabel createProfileAvatarLabel(String userId, String gender, int diameter,
                                                   Component parent, Runnable onChanged) {
        JLabel[] ref = {null};
        ref[0] = new JLabel(loadCircularAvatar(userId, gender, diameter));
        ref[0].setPreferredSize(new Dimension(diameter, diameter));
        ref[0].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ref[0].setToolTipText("Click to change profile picture");
        ref[0].addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser(); fc.setDialogTitle("Choose Profile Picture");
                fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files","png","jpg","jpeg"));
                if (fc.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                    if (saveAvatar(userId, fc.getSelectedFile())) {
                        ref[0].setIcon(loadCircularAvatar(userId, gender, diameter)); ref[0].repaint();
                        if (onChanged != null) onChanged.run();
                    } else { JOptionPane.showMessageDialog(parent,"Failed to save photo.","Error",JOptionPane.ERROR_MESSAGE); }
                }
            }
        });
        return ref[0];
    }

    public static JLabel createProfileAvatarLabel(String userId, int diameter, Component parent, Runnable onChanged) { return createProfileAvatarLabel(userId,"",diameter,parent,onChanged); }
    public static JLabel createClickableAvatarLabel(String userId, int diameter, Component parent, Runnable onChanged) { return createProfileAvatarLabel(userId,"",diameter,parent,onChanged); }
    public static JLabel createAvatarLabel(String userId, int diameter) { JLabel l=new JLabel(loadCircularAvatar(userId,"",diameter)); l.setPreferredSize(new Dimension(diameter,diameter)); return l; }

    // ── Shared dashboard helpers ───────────────────────────────────────────────
    public static JPanel statCard(String title, String value, Color accent, String desc) {
        JPanel c = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TABLE_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(accent); g2.fillRoundRect(0,0,getWidth(),10,12,12); g2.fillRect(0,5,getWidth(),5); g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(accent,12,1),new EmptyBorder(15,10,10,10)));
        GridBagConstraints gbc=new GridBagConstraints(); gbc.gridx=0; gbc.fill=GridBagConstraints.HORIZONTAL; gbc.anchor=GridBagConstraints.CENTER;
        gbc.gridy=0; gbc.insets=new Insets(0,0,2,0); JLabel vl=new JLabel(value,SwingConstants.CENTER); vl.setFont(uiFont(Font.BOLD,22)); vl.setForeground(accent); c.add(vl,gbc);
        gbc.gridy=1; JLabel tl=new JLabel(title,SwingConstants.CENTER); tl.setFont(uiFont(Font.BOLD,12)); tl.setForeground(WHITE); c.add(tl,gbc);
        gbc.gridy=2; gbc.insets=new Insets(0,0,0,0); JLabel dl=new JLabel(desc,SwingConstants.CENTER); dl.setFont(uiFont(Font.PLAIN,10)); dl.setForeground(DIM_TEXT); c.add(dl,gbc);
        return c;
    }

    public static JPanel miniCard(String label, String value, Color accent) {
        JPanel c=new JPanel(new GridLayout(2,1,0,4)); c.setBackground(TABLE_BG);
        c.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(accent,10,1),new EmptyBorder(10,14,10,14)));
        JLabel vl=new JLabel(value,JLabel.CENTER); vl.setFont(uiFont(Font.BOLD,20)); vl.setForeground(accent);
        JLabel ll=new JLabel(label,JLabel.CENTER);  ll.setFont(uiFont(Font.PLAIN,11)); ll.setForeground(DIM_TEXT);
        c.add(vl); c.add(ll); return c;
    }

    public static JLabel sectionTitle(String text, String iconPath) {
        JLabel l;
        if (iconPath != null) { ImageIcon icon=loadMenuIcon(iconPath,20,20); l=(icon!=null)?new JLabel(text,icon,JLabel.LEFT):new JLabel(text); if(icon!=null) l.setIconTextGap(8); } else l=new JLabel(text);
        l.setForeground(ACCENT_BLUE); l.setFont(uiFont(Font.BOLD,16)); l.setBorder(new EmptyBorder(0,0,12,0)); return l;
    }

    public static void sideLabel(JPanel bar, String text) {
        JLabel l=new JLabel("  "+text); l.setForeground(Color.WHITE); l.setFont(uiFont(Font.BOLD,15));
        l.setBorder(new EmptyBorder(0,0,8,0)); l.setAlignmentX(Component.LEFT_ALIGNMENT); bar.add(l);
    }

    public static JButton menuBtn(JPanel bar, String iconPath, String text, JButton[] activeRef, Runnable action) {
        ImageIcon icon=loadMenuIcon(iconPath,20,20);
        JButton btn=new JButton(text,icon); btn.setForeground(WHITE); btn.setBackground(MEDIUM_NAVY);
        btn.setFont(uiFont(Font.PLAIN,13)); btn.setHorizontalAlignment(SwingConstants.LEFT); btn.setIconTextGap(10);
        btn.setBorder(new EmptyBorder(11,14,11,10)); btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE,44)); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false); btn.setOpaque(true);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (btn!=activeRef[0]) btn.setBackground(new Color(38,60,108)); }
            public void mouseExited (MouseEvent e) { if (btn!=activeRef[0]) btn.setBackground(MEDIUM_NAVY); }
        });
        btn.addActionListener(e -> {
            if (activeRef[0]!=null) { activeRef[0].setBackground(MEDIUM_NAVY); activeRef[0].setFont(uiFont(Font.PLAIN,13)); activeRef[0].setBorder(new EmptyBorder(11,14,11,10)); }
            activeRef[0]=btn; btn.setBackground(new Color(14,28,58)); btn.setFont(uiFont(Font.BOLD,13));
            btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,4,0,0,ACCENT_BLUE),new EmptyBorder(11,10,11,10)));
            action.run();
        });
        bar.add(btn); return btn;
    }

    public static JTextField tf() { JTextField f=new JTextField(); styleTextField(f); return f; }

    public static JPanel dlgPanel() { JPanel p=new JPanel(new GridBagLayout()); p.setBackground(DARK_NAVY); p.setBorder(new EmptyBorder(16,28,16,28)); return p; }

    public static GridBagConstraints dlgGbc() { GridBagConstraints gbc=new GridBagConstraints(); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(7,5,7,5); return gbc; }

    public static void addDlgRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy=row; gbc.gridx=0; gbc.gridwidth=1; gbc.weightx=0.38;
        JLabel l=new JLabel(label); l.setForeground(WHITE); l.setFont(uiFont(Font.BOLD,12)); p.add(l,gbc);
        gbc.gridx=1; gbc.weightx=0.62; p.add(comp,gbc);
    }

    public static void err(Component parent, String msg) { JOptionPane.showMessageDialog(parent,msg,"Error",JOptionPane.ERROR_MESSAGE); }

    public static JPanel buildBgButtons(String userId, String role, Image[] bgRef, JFrame frame, Runnable onChanged) {
        RoundedButton changeBgBtn=new RoundedButton("Change Background",new Color(50,90,170));
        changeBgBtn.setFont(uiFont(Font.BOLD,11)); changeBgBtn.setPreferredSize(new Dimension(168,32));
        changeBgBtn.addActionListener(e -> {
            JFileChooser fc=new JFileChooser(); fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images","png","jpg","jpeg"));
            if (fc.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION) {
                String saved=saveBgImage(userId,role,fc.getSelectedFile());
                if (saved!=null) { bgRef[0]=loadBgImage(userId,role); onChanged.run(); }
                else JOptionPane.showMessageDialog(frame,"Failed to save background image.","Error",JOptionPane.ERROR_MESSAGE);
            }
        });
        RoundedButton clearBgBtn=new RoundedButton("Clear",new Color(150,50,50));
        clearBgBtn.setFont(uiFont(Font.BOLD,11)); clearBgBtn.setPreferredSize(new Dimension(70,32));
        clearBgBtn.addActionListener(e -> { deleteBgImage(userId,role); bgRef[0]=null; onChanged.run(); });
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); p.setOpaque(false); p.add(changeBgBtn); p.add(clearBgBtn);
        return p;
    }
}
