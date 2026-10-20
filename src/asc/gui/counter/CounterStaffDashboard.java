package asc.gui.counter;

import asc.model.*;
import asc.gui.shared.LoginFrame;
import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ImageIcon;

public class CounterStaffDashboard extends JFrame {

    private static final long serialVersionUID = 1L;

    static final Color  ACCENT_YELLOW   = UIUtils.WARN_YELLOW;
    static final Color  GREEN_OK        = UIUtils.GREEN_OK;
    static final Color  PURPLE          = UIUtils.PURPLE;
    static final Color  MENU_ACTIVE     = new Color(14, 28, 58);
    static final Color  CARD2           = new Color(30, 51, 90);
    static final Color  CARD3           = new Color(20, 34, 61);
    static final String ICON_BASE       = "data/Picture/Counter Staff/";
    static final String DISABLED_PREFIX = "__DIS__";
    static final String[] TIME_SLOTS    = {"08:00","09:00","10:00","11:00","13:00","14:00","15:00","16:00"};

    final CounterStaff currentStaff;
    private JLabel  headerAvatarLabel;
    private JPanel  contentArea;
    Image homeBgImage;
    private JButton activeMenuBtn;
    JButton homeMenuBtn;
    JButton customersMenuBtn;
    JButton aptsMenuBtn;
    JButton scheduleMenuBtn;
    JButton paymentsMenuBtn;
    JButton topupMenuBtn;
    JButton vipMenuBtn;

    public CounterStaffDashboard(CounterStaff staff) {
        this.currentStaff = staff;
        buildUI();
    }

    private void buildUI() {
        setTitle("Counter Staff Dashboard - " + currentStaff.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1100, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.DARK_NAVY);

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UIUtils.DARK_NAVY);

        JScrollPane contentScroll = new JScrollPane(contentArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentScroll.setBorder(null);
        contentScroll.getViewport().setBackground(UIUtils.DARK_NAVY);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(contentScroll,  BorderLayout.CENTER);
        setContentPane(root);

        homeBgImage = UIUtils.loadBgImage(currentStaff.getUserId(), "counter");
        SwingUtilities.invokeLater(() -> { if (homeMenuBtn != null) homeMenuBtn.doClick(); });
    }

    private JPanel buildHeader() {
        JPanel p = UIUtils.createGradientHeader();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 22));

        // Logo — custom paintComponent panel, immune to layout squeezing
        JPanel logoPanel = UIUtils.createLogoPanel(125, 70);

        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setForeground(UIUtils.WHITE);
        title.setFont(UIUtils.uiFont(Font.BOLD, 18));

        JLabel sub = new JLabel("Counter Staff Portal");
        sub.setForeground(UIUtils.LIGHT_GRAY);
        sub.setFont(UIUtils.uiFont(Font.PLAIN, 12));

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.setAlignmentY(Component.CENTER_ALIGNMENT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        textCol.add(title);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(sub);

        JPanel logoLeft = new JPanel();
        logoLeft.setLayout(new BoxLayout(logoLeft, BoxLayout.X_AXIS));
        logoLeft.setOpaque(false);
        logoLeft.setAlignmentY(Component.CENTER_ALIGNMENT);
        logoLeft.add(logoPanel);
        logoLeft.add(textCol);

        JPanel right = UIUtils.createHeaderRightPanel(
                currentStaff.getUserId(), currentStaff.getGender(),
                currentStaff.getName(), currentStaff.getEmail(),
                0, this,
                this::openProfileDialog,
                () -> { dispose(); new LoginFrame().setVisible(true); });
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);
        headerAvatarLabel = UIUtils.createAvatarMenuLabel(
                currentStaff.getUserId(), currentStaff.getGender(),
                currentStaff.getName(), currentStaff.getEmail(), this,
                this::openProfileDialog,
                () -> { dispose(); new LoginFrame().setVisible(true); });

        p.add(logoLeft);
        p.add(Box.createHorizontalGlue());
        p.add(right);
        return p;
    }

    private JPanel buildSidebar() {
        JPanel bar = new JPanel();
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));
        bar.setBackground(UIUtils.MEDIUM_NAVY);
        bar.setPreferredSize(new Dimension(218, 0));
        bar.setBorder(new EmptyBorder(20, 0, 20, 0));

        sideLabel(bar, "COUNTER STAFF MENU", UIUtils.ACCENT_BLUE);

        homeMenuBtn      = menuBtn(bar, ICON_BASE + "Home.png",               "Home",                this::showHome);
        customersMenuBtn = menuBtn(bar, ICON_BASE + "Customer Management.png", "Customer Management", this::showCustomers);
        aptsMenuBtn      = menuBtn(bar, ICON_BASE + "Appointments.png",        "Appointments",        this::showAppointments);
        scheduleMenuBtn  = menuBtn(bar, ICON_BASE + "Appointments.png",        "Schedule",            this::showSchedule);
        topupMenuBtn     = menuBtn(bar, ICON_BASE + "Payments.png",            "Top-Up Wallet",       this::showTopup);
        paymentsMenuBtn  = menuBtn(bar, ICON_BASE + "Payments.png",            "Payments & Receipts", this::showPayments);
        vipMenuBtn       = menuBtn(bar, ICON_BASE + "Payments.png",            "VIP Management",      this::showVipManagement);

        bar.add(Box.createVerticalGlue());

        JButton logoutBtn = menuBtn(bar, ICON_BASE + "Log out.png", "Logout", () -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        logoutBtn.setForeground(UIUtils.LIGHT_GRAY);

        return bar;
    }

    private void sideLabel(JPanel bar, String text, Color color) {
        JLabel l = new JLabel("  " + text);
        l.setForeground(Color.WHITE);
        l.setFont(UIUtils.uiFont(Font.BOLD, 15));
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        bar.add(l);
    }

    ImageIcon loadMenuIcon(String path, int w, int h) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            Image img = new ImageIcon(f.getAbsolutePath()).getImage()
                    .getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception ex) { return null; }
    }

    private JButton menuBtn(JPanel bar, String iconPath, String text, Runnable action) {
        ImageIcon icon = loadMenuIcon(iconPath, 20, 20);
        JButton btn = new JButton(text, icon);
        btn.setForeground(UIUtils.WHITE);
        btn.setBackground(UIUtils.MEDIUM_NAVY);
        btn.setFont(UIUtils.uiFont(Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(10);
        btn.setBorder(new EmptyBorder(11, 14, 11, 10));
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeMenuBtn) btn.setBackground(new Color(38, 60, 108));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeMenuBtn) btn.setBackground(UIUtils.MEDIUM_NAVY);
            }
        });

        btn.addActionListener(e -> {
            if (activeMenuBtn != null) {
                activeMenuBtn.setBackground(UIUtils.MEDIUM_NAVY);
                activeMenuBtn.setFont(UIUtils.uiFont(Font.PLAIN, 13));
                activeMenuBtn.setBorder(new EmptyBorder(11, 14, 11, 10));
            }
            activeMenuBtn = btn;
            btn.setBackground(MENU_ACTIVE);
            btn.setFont(UIUtils.uiFont(Font.BOLD, 13));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, UIUtils.ACCENT_BLUE),
                    new EmptyBorder(11, 10, 11, 10)));
            action.run();
        });

        bar.add(btn);
        return btn;
    }

    private void swap(JPanel panel) {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout());
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void navigate(JButton targetBtn, JPanel panel) {
        if (activeMenuBtn != null && activeMenuBtn != targetBtn) {
            activeMenuBtn.setBackground(UIUtils.MEDIUM_NAVY);
            activeMenuBtn.setFont(UIUtils.uiFont(Font.PLAIN, 13));
            activeMenuBtn.setBorder(new EmptyBorder(11, 14, 11, 10));
        }
        activeMenuBtn = targetBtn;
        targetBtn.setBackground(MENU_ACTIVE);
        targetBtn.setFont(UIUtils.uiFont(Font.BOLD, 13));
        targetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, UIUtils.ACCENT_BLUE),
                new EmptyBorder(11, 10, 11, 10)));
        swap(panel);
    }

    void showHome()          { navigate(homeMenuBtn,      new CounterHomePage(this)); }
    void showCustomers()     { navigate(customersMenuBtn, new CounterCustomerPage(this)); }
    void showAppointments()  { navigate(aptsMenuBtn,      new CounterAppointmentPage(this)); }
    void showSchedule()      { navigate(scheduleMenuBtn,  new CounterSchedulePage(this)); }
    void showTopup()         { navigate(topupMenuBtn,     new CounterTopupPage(this)); }
    void showPayments()      { navigate(paymentsMenuBtn,  new CounterPaymentPage(this)); }
    void showVipManagement() { navigate(vipMenuBtn,       new CounterVipManagementPage(this)); }

    void showReceiptWindow(Payment pay, Appointment apt) {
        Customer     c  = FileManager.findCustomerById(pay.getCustomerId());
        Technician   t  = (apt != null) ? FileManager.findTechnicianById(apt.getTechnicianId()) : null;
        CounterStaff cs = (apt != null) ? FileManager.findCounterStaffById(apt.getCounterStaffId()) : null;
        StringBuilder sb = new StringBuilder();
        sb.append("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557\n");
        sb.append("\u2551      APU AUTOMOTIVE SERVICE CENTRE           \u2551\n");
        sb.append("\u2551           OFFICIAL PAYMENT RECEIPT           \u2551\n");
        sb.append("\u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d\n\n");
        sb.append(String.format("  Receipt No.   : %s%n  Payment ID    : %s%n  Payment Date  : %s%n%n",
                pay.getReceiptNumber(), pay.getPaymentId(), pay.getPaymentDate()));
        if (apt != null) {
            sb.append(String.format("  Appointment   : %s%n  Customer      : %s%n  Technician    : %s%n  Counter Staff : %s%n  Service       : %s (%d hr(s))%n  Date & Time   : %s %s%n  Vehicle       : %s%n",
                    apt.getAppointmentId(), c != null ? c.getName() : apt.getCustomerId(),
                    t != null ? t.getName() : apt.getTechnicianId(), cs != null ? cs.getName() : apt.getCounterStaffId(),
                    apt.getServiceType(), apt.getDurationHours(), apt.getDate(), apt.getTime(), apt.getVehicleInfo()));
        } else {
            sb.append(String.format("  Transaction   : Wallet Top-Up%n  Customer      : %s%n  Customer ID   : %s%n",
                    c != null ? c.getName() : pay.getCustomerId(), pay.getCustomerId()));
        }
        sb.append(String.format("%n  Amount Paid   : RM %.2f%n  Payment Method: %s%n", pay.getAmount(), pay.getPaymentMethod()));
        sb.append("\n\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");
        sb.append("       Thank you for choosing APU-ASC!\n");
        sb.append("\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n");
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false); area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBackground(UIUtils.TABLE_BG); area.setForeground(UIUtils.WHITE);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 400)); UIUtils.styleScrollPane(scroll);
        JOptionPane.showMessageDialog(this, scroll, "Receipt - " + pay.getReceiptNumber(), JOptionPane.PLAIN_MESSAGE);
    }

    private void openProfileDialog() {
        JDialog dlg = new JDialog(this, "My Profile", true);
        dlg.setSize(490, 560); dlg.setLocationRelativeTo(this); dlg.setResizable(false);
        JPanel outer = new JPanel(new GridBagLayout()); outer.setBackground(UIUtils.DARK_NAVY);
        JPanel card  = new JPanel(new GridBagLayout()); card.setBackground(UIUtils.TABLE_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 14, 1), new EmptyBorder(20, 50, 25, 50)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(7, 5, 7, 5);
        JLabel titleLbl = new JLabel("Edit My Profile", JLabel.CENTER);
        titleLbl.setFont(UIUtils.uiFont(Font.BOLD, 16)); titleLbl.setForeground(UIUtils.ACCENT_BLUE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; card.add(titleLbl, gbc);
        JLabel avatar = UIUtils.createProfileAvatarLabel(currentStaff.getUserId(), currentStaff.getGender(), 90, dlg,
                () -> headerAvatarLabel.setIcon(UIUtils.loadCircularAvatar(currentStaff.getUserId(), currentStaff.getGender(), 48)));
        JLabel hint = new JLabel("Click photo to change", JLabel.CENTER);
        hint.setForeground(UIUtils.DIM_TEXT); hint.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        JPanel avSec = new JPanel(new BorderLayout(0, 4)); avSec.setBackground(UIUtils.TABLE_BG);
        avSec.add(avatar, BorderLayout.CENTER); avSec.add(hint, BorderLayout.SOUTH);
        gbc.gridy = 1; card.add(avSec, gbc); gbc.gridwidth = 1;
        JTextField     nameF  = new JTextField(currentStaff.getName());  UIUtils.styleTextField(nameF);
        JComboBox<String> genderCb = UIUtils.createGenderCombo(currentStaff.getGender());
        JTextField     emailF = new JTextField(currentStaff.getEmail()); UIUtils.styleTextField(emailF);
        JTextField     phoneF = new JTextField(currentStaff.getPhone()); UIUtils.styleTextField(phoneF);
        JPasswordField pwF    = new JPasswordField(); UIUtils.styleTextField(pwF);
        JPasswordField cpF    = new JPasswordField(); UIUtils.styleTextField(cpF);
        profileRow(card, gbc, 2, "Full Name:",        nameF);
        profileRow(card, gbc, 3, "Gender:",           genderCb);
        profileRow(card, gbc, 4, "Email:",            emailF);
        profileRow(card, gbc, 5, "Phone:",            phoneF);
        profileRow(card, gbc, 6, "New Password:",     pwF);
        profileRow(card, gbc, 7, "Confirm Password:", cpF);
        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Save Profile", UIUtils.SUCCESS_GREEN);
        save.setPreferredSize(new Dimension(200, 36));
        save.addActionListener(e -> {
            String n = nameF.getText().trim(), g = UIUtils.resolveGender(genderCb),
                   em = emailF.getText().trim(), ph = phoneF.getText().trim();
            String pw = new String(pwF.getPassword()), cf = new String(cpF.getPassword());
            if (n.isEmpty() || em.isEmpty() || ph.isEmpty()) { JOptionPane.showMessageDialog(dlg, "Name, email and phone required.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (!ValidationUtils.isValidEmail(em))           { JOptionPane.showMessageDialog(dlg, "Invalid email.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (!ValidationUtils.isValidPhone(ph))           { JOptionPane.showMessageDialog(dlg, "Phone must be 10-11 digits.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            if (!pw.isEmpty() && !pw.equals(cf))             { JOptionPane.showMessageDialog(dlg, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            currentStaff.setName(n); currentStaff.setGender(g); currentStaff.setEmail(em); currentStaff.setPhone(ph);
            if (!pw.isEmpty()) currentStaff.setPassword(pw);
            FileManager.updateCounterStaff(currentStaff);
            headerAvatarLabel.setIcon(UIUtils.loadCircularAvatar(currentStaff.getUserId(), g, 48));
            setTitle("Counter Staff Dashboard - " + currentStaff.getName());
            JOptionPane.showMessageDialog(dlg, "Profile updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; card.add(save, gbc);
        outer.add(card, new GridBagConstraints()); dlg.setContentPane(outer); dlg.setVisible(true);
    }

    JLabel sectionTitle(String text, String iconPath) {
        JLabel l;
        if (iconPath != null) {
            ImageIcon icon = loadMenuIcon(iconPath, 20, 20);
            l = (icon != null) ? new JLabel(text, icon, JLabel.LEFT) : new JLabel(text);
            if (icon != null) l.setIconTextGap(8);
        } else { l = new JLabel(text); }
        l.setForeground(UIUtils.ACCENT_BLUE);
        l.setFont(UIUtils.uiFont(Font.BOLD, 16));
        l.setBorder(new EmptyBorder(0, 0, 10, 0));
        return l;
    }

    JPanel makeCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD2);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1), new EmptyBorder(14, 16, 14, 16)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (title != null && !title.trim().isEmpty()) {
            JLabel t = new JLabel(title);
            t.setForeground(UIUtils.WHITE); t.setFont(UIUtils.uiFont(Font.BOLD, 14));
            t.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(t); card.add(Box.createVerticalStrut(10));
        }
        return card;
    }

    JPanel inputBox(String label, JComponent comp) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(CARD2);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setForeground(UIUtils.LIGHT_GRAY); l.setFont(UIUtils.uiFont(Font.BOLD, 11));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l); p.add(Box.createVerticalStrut(4)); p.add(comp);
        return p;
    }

    JTextField tf() { JTextField f = new JTextField(); UIUtils.styleTextField(f); return f; }

    void darkTable(JTable t) {
        t.setBackground(CARD2); t.setForeground(UIUtils.WHITE); t.setRowHeight(28);
        t.setGridColor(new Color(19, 35, 65));
        t.getTableHeader().setBackground(new Color(43, 59, 91));
        t.getTableHeader().setForeground(UIUtils.WHITE);
    }

    DocumentListener doc(Runnable r) {
        return new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { r.run(); }
            public void removeUpdate(DocumentEvent e)  { r.run(); }
            public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }

    String safe(String s) { return s == null ? "" : s; }
    boolean customerOff(Customer c) { return c != null && safe(c.getUsername()).startsWith(DISABLED_PREFIX); }
    String cleanUser(Customer c) { if (c == null) return ""; String u = safe(c.getUsername()); while (u.startsWith(DISABLED_PREFIX)) u = u.substring(DISABLED_PREFIX.length()); return u; }
    boolean customerFind(Customer c, String q) { if (c == null) return false; String k = safe(q).toLowerCase(); String all = (safe(c.getUserId())+" "+cleanUser(c)+" "+safe(c.getName())+" "+safe(c.getPhone())+" "+safe(c.getEmail())+" "+safe(c.getVehicleInfo())).toLowerCase(); return all.contains(k); }
    String comboId(JComboBox<String> cb) { Object o = cb.getSelectedItem(); if (o==null) return ""; String s=o.toString(); int p=s.indexOf('|'); return (p>=0?s.substring(0,p):s).trim(); }
    Customer comboCustomer(JComboBox<String> cb) { String id=comboId(cb); return id.isEmpty()?null:FileManager.findCustomerById(id); }
    void selectById(JComboBox<String> cb, String id) { if(id!=null&&!id.isEmpty()) for(int i=0;i<cb.getItemCount();i++) if(cb.getItemAt(i).startsWith(id+" ")){cb.setSelectedIndex(i);return;} if(cb.getItemCount()>0) cb.setSelectedIndex(0); }
    void msgErr(Component parent, String msg) { JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    void formRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp) { g.gridy=row; g.gridx=0; g.gridwidth=1; g.weightx=0.35; JLabel l=new JLabel(label); l.setForeground(UIUtils.WHITE); l.setFont(UIUtils.uiFont(Font.BOLD,12)); p.add(l,g); g.gridx=1; g.weightx=0.65; p.add(comp,g); }

    JPanel dlgPanel() { JPanel p=new JPanel(new GridBagLayout()); p.setBackground(UIUtils.DARK_NAVY); p.setBorder(new EmptyBorder(16,28,16,28)); return p; }
    GridBagConstraints dlgGbc() { GridBagConstraints gbc=new GridBagConstraints(); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.insets=new Insets(7,5,7,5); return gbc; }
    void addDlgRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) { gbc.gridy=row; gbc.gridx=0; gbc.gridwidth=1; gbc.weightx=0.38; JLabel l=new JLabel(label); l.setForeground(UIUtils.WHITE); l.setFont(UIUtils.uiFont(Font.BOLD,12)); p.add(l,gbc); gbc.gridx=1; gbc.weightx=0.62; p.add(comp,gbc); }
    private void profileRow(JPanel card, GridBagConstraints g, int row, String labelText, JComponent comp) { g.gridy=row; g.gridx=0; g.gridwidth=1; g.weightx=0.35; JLabel l=new JLabel(labelText); l.setForeground(UIUtils.WHITE); l.setFont(UIUtils.uiFont(Font.BOLD,12)); card.add(l,g); g.gridx=1; g.weightx=0.65; card.add(comp,g); }
}
