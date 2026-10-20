package asc.gui.technician;

import asc.model.*;
import asc.gui.shared.LoginFrame;
import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.table.TableRowSorter;
import java.util.ArrayList;
import javax.swing.ImageIcon;

public class TechnicianDashboard extends JFrame {

    private static final long serialVersionUID = 1L;

    // Ã¢â€â‚¬Ã¢â€â‚¬ constants exposed to page classes Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬Ã¢â€â‚¬
    static final Color ACCENT_YELLOW = UIUtils.WARN_YELLOW;
    static final Color GREEN_OK      = UIUtils.GREEN_OK;
    static final Color PURPLE        = UIUtils.PURPLE;
    static final Color MENU_ACTIVE   = new Color(14, 28, 58);
    static final String ICON_BASE    = "data/Picture/Technician/";

    final Technician currentTech;
    JLabel  headerAvatarLabel;
    JPanel  buildUI_contentArea;
    Image  homeBgImage     = null;
    JButton activeMenuBtn = null;
    JButton homeMenuBtn   = null;
    JButton aptsMenuBtn   = null;
    JButton healthMenuBtn = null;
    JButton analyticsMenuBtn;
    JButton scheduleMenuBtn;
    JButton inventoryMenuBtn;
    DefaultTableModel appointmentsModel;
    JTable            appointmentsTable;

    public TechnicianDashboard(Technician tech) {
        this.currentTech = tech;
        buildUI();
    }

    private void buildUI() {
        setTitle("Technician Dashboard - " + currentTech.getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 580));
        setSize(1080, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.DARK_NAVY);

        buildUI_contentArea = new JPanel(new BorderLayout());
        buildUI_contentArea.setBackground(UIUtils.DARK_NAVY);

        // Wrap in scroll pane so nothing clips when window is resized small
        JScrollPane contentScroll = new JScrollPane(buildUI_contentArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentScroll.setBorder(null);
        contentScroll.getViewport().setBackground(UIUtils.DARK_NAVY);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(contentScroll,  BorderLayout.CENTER);
        setContentPane(root);

        homeBgImage = UIUtils.loadBgImage(currentTech.getUserId(), "technician");
        SwingUtilities.invokeLater(() -> { if (homeMenuBtn != null) homeMenuBtn.doClick(); });
    }

    // =========================================================================
    //  HEADER  Ã¢â‚¬â€œ matches the CounterStaff style: big title top, subtitle+status below
    // =========================================================================
    private JPanel buildHeader() {
        JPanel p = UIUtils.createGradientHeader();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 22));

        // Logo — custom paintComponent panel, immune to layout squeezing
        JPanel logoPanel = UIUtils.createLogoPanel(125, 70);

        JLabel title = new JLabel("APU Automotive Service Centre");
        title.setForeground(UIUtils.WHITE);
        title.setFont(UIUtils.uiFont(Font.BOLD, 18));

        JLabel sub = new JLabel("Technician Portal");
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

        // Right: bell + username/VIP + avatar
        JPanel right = UIUtils.createHeaderRightPanel(
                currentTech.getUserId(), currentTech.getGender(),
                currentTech.getName(), currentTech.getEmail(),
                0, this,
                this::openProfileDialog,
                () -> { dispose(); new LoginFrame().setVisible(true); });
        right.setOpaque(false);
        right.setAlignmentY(Component.CENTER_ALIGNMENT);
        headerAvatarLabel = UIUtils.createAvatarMenuLabel(
                currentTech.getUserId(), currentTech.getGender(),
                currentTech.getName(), currentTech.getEmail(), this,
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

        sideLabel(bar, "TECHNICIAN MENU", UIUtils.ACCENT_BLUE);

        homeMenuBtn = menuBtn(bar, ICON_BASE + "Home.png",                       "Home",                  this::showHome);
        aptsMenuBtn = menuBtn(bar, ICON_BASE + "My Appointments.png",             "My Appointments",       this::showAppointments);
        healthMenuBtn = menuBtn(bar, ICON_BASE + "Vehicle Health Records.png",    "Vehicle Health Records", this::showHealthRecords);
        analyticsMenuBtn = menuBtn(bar, ICON_BASE + "My Analytics.png",           "My Analytics",          this::showMyAnalytics);
        scheduleMenuBtn = menuBtn(bar, ICON_BASE + "My Schedule.png",             "My Schedule",           this::showSchedule);
        inventoryMenuBtn = menuBtn(bar, ICON_BASE + "Parts Inventory.png",        " Parts Inventory",      this::showInventory);

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

    private ImageIcon loadMenuIcon(String path, int w, int h) {
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
    
    void showMyAnalytics()      { navigate(analyticsMenuBtn,  new TechnicianAnalyticsPage(this)); }
    void showSchedule()         { navigate(scheduleMenuBtn,   new TechnicianSchedulePage(this)); }
    void showInventory()        { navigate(inventoryMenuBtn,  new TechnicianInventoryPage(this)); }

    void swap(JPanel panel) {
        buildUI_contentArea.removeAll();
        buildUI_contentArea.setLayout(new BorderLayout());
        buildUI_contentArea.add(panel, BorderLayout.CENTER);
        buildUI_contentArea.revalidate();
        buildUI_contentArea.repaint();
    }

    void navigate(JButton targetBtn, JPanel panel) {
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

    void showHome()           { navigate(homeMenuBtn,   new TechnicianHomePage(this)); }
    void showAppointments()    { navigate(aptsMenuBtn,   new TechnicianAppointmentPage(this)); }
    void showHealthRecords()   { navigate(healthMenuBtn, new TechnicianHealthRecordPage(this)); }

    // APPOINTMENTS PANEL

    void refreshAppointmentsTable() {
        if (appointmentsModel == null) return;
        appointmentsModel.setRowCount(0);
        for (Appointment a : FileManager.readAllAppointments()) {
            if (!a.getTechnicianId().equals(currentTech.getUserId())) continue;
            Customer c  = FileManager.findCustomerById(a.getCustomerId());
            Feedback fb = FileManager.findFeedbackByAppointmentId(a.getAppointmentId());
            appointmentsModel.addRow(new Object[]{
                a.getAppointmentId(), (c != null) ? c.getName() : a.getCustomerId(),
                a.getServiceType(), a.getStatus(), a.getDate(), a.getTime(),
                a.getVehicleInfo(), a.getNotes(), (fb != null) ? "\u2713 Yes" : "No"
            });
        }
    }

    void viewAppointmentDetails() {
        int row = appointmentsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,"Select an appointment.","No Selection",JOptionPane.WARNING_MESSAGE); return; }
        String aptId = (String) appointmentsModel.getValueAt(row, 0);
        Appointment apt = FileManager.findAppointmentById(aptId); if (apt == null) return;
        Customer     c  = FileManager.findCustomerById(apt.getCustomerId());
        CounterStaff cs = FileManager.findCounterStaffById(apt.getCounterStaffId());
        Feedback fb  = FileManager.findFeedbackByAppointmentId(aptId);
        Comment  cmt = FileManager.findCommentByAppointmentId(aptId);

        // ── Dialog ────────────────────────────────────────────────────────────
        JDialog dlg = new JDialog(this, "Details \u2014 " + aptId, true);
        dlg.setSize(720, 620);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(true);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UIUtils.DARK_NAVY);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));

        // ── TOP: appointment info card ─────────────────────────────────────────
        JPanel infoCard = new JPanel(new GridBagLayout());
        infoCard.setBackground(UIUtils.TABLE_BG);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1),
                new EmptyBorder(14, 18, 14, 18)));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(3, 6, 3, 6);
        int gr = 0;
        addDetailRow(infoCard, g, gr++, "Appointment ID:",  apt.getAppointmentId());
        addDetailRow(infoCard, g, gr++, "Service:",         apt.getServiceType() + "  (" + apt.getDurationHours() + " hr)");
        addDetailRow(infoCard, g, gr++, "Status:",          apt.getStatus());
        addDetailRow(infoCard, g, gr++, "Date & Time:",     apt.getDate() + "  " + apt.getTime());
        addDetailRow(infoCard, g, gr++, "Vehicle:",         apt.getVehicleInfo());
        addDetailRow(infoCard, g, gr++, "Customer:",        (c  != null) ? c.getName() + "  (" + c.getPhone() + ")" : apt.getCustomerId());
        addDetailRow(infoCard, g, gr++, "Counter Staff:",   (cs != null) ? cs.getName() : apt.getCounterStaffId());
        if (!apt.getNotes().isEmpty())
            addDetailRow(infoCard, g, gr++, "Notes:", apt.getNotes());
        if (fb != null)
            addDetailRow(infoCard, g, gr++, "Your Feedback:", fb.getContent());
        if (cmt != null)
            addDetailRow(infoCard, g, gr++, "Customer Comment:", cmt.getTechnicianComment());

        // ── BOTTOM: vehicle history table for this car plate ──────────────────
        String vehicleInfo = apt.getVehicleInfo();
        List<Appointment> sameVehicle = FileManager.readAllAppointments().stream()
                .filter(a -> a.getTechnicianId().equals(currentTech.getUserId())
                          && vehicleInfo != null
                          && vehicleInfo.equalsIgnoreCase(a.getVehicleInfo()))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .collect(java.util.stream.Collectors.toList());

        JLabel histTitle = new JLabel("\uD83D\uDE97  Vehicle Service History  \u2014  " + vehicleInfo
                + "   (" + sameVehicle.size() + " visit" + (sameVehicle.size() == 1 ? "" : "s") + ")");
        histTitle.setForeground(UIUtils.ACCENT_BLUE);
        histTitle.setFont(UIUtils.uiFont(Font.BOLD, 13));
        histTitle.setBorder(new EmptyBorder(4, 0, 6, 0));

        String[] hcols = {"Appt ID", "Date", "Service", "Status", "Notes"};
        DefaultTableModel histModel = new DefaultTableModel(hcols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Appointment a : sameVehicle) {
            histModel.addRow(new Object[]{
                a.getAppointmentId(), a.getDate(), a.getServiceType(),
                a.getStatus(), a.getNotes()
            });
        }
        JTable histTable = UIUtils.createStyledTable(histModel);
        histTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] hw = {80, 90, 100, 130, 200};
        for (int i = 0; i < hw.length; i++)
            histTable.getColumnModel().getColumn(i).setPreferredWidth(hw[i]);
        histTable.setRowHeight(26);

        // Highlight the current appointment row
        histTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, col);
                String rowAptId = (String) histModel.getValueAt(r, 0);
                boolean isCurrent = aptId.equals(rowAptId);
                boolean done = "COMPLETED".equalsIgnoreCase((String) histModel.getValueAt(r, 3));
                if (sel)            setBackground(UIUtils.ACCENT_BLUE);
                else if (isCurrent) setBackground(new Color(20, 60, 110));
                else if (done)      setBackground(new Color(25, 60, 40));
                else                setBackground(UIUtils.TABLE_BG);
                setForeground(UIUtils.WHITE);
                setFont(UIUtils.uiFont(isCurrent ? Font.BOLD : Font.PLAIN, 12));
                setBorder(new EmptyBorder(0, 6, 0, 6));
                if (col == 3) {
                    setText((done ? "\u2705 " : "\u23F3 ") + v);
                    setForeground(done ? GREEN_OK : ACCENT_YELLOW);
                    setHorizontalAlignment(CENTER);
                } else {
                    setHorizontalAlignment(LEFT);
                }
                return this;
            }
        });

        JScrollPane histScroll = new JScrollPane(histTable);
        UIUtils.styleScrollPane(histScroll);
        histScroll.setPreferredSize(new Dimension(0, 180));

        JPanel histPanel = new JPanel(new BorderLayout(0, 4));
        histPanel.setBackground(UIUtils.DARK_NAVY);
        histPanel.add(histTitle,  BorderLayout.NORTH);
        histPanel.add(histScroll, BorderLayout.CENTER);

        // ── Close button ──────────────────────────────────────────────────────
        UIUtils.RoundedButton closeBtn = new UIUtils.RoundedButton("Close", UIUtils.ACCENT_BLUE);
        closeBtn.setPreferredSize(new Dimension(110, 34));
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(closeBtn);

        root.add(infoCard,  BorderLayout.NORTH);
        root.add(histPanel, BorderLayout.CENTER);
        root.add(btnRow,    BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    void markCompleted() {
        int row = appointmentsTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,"Select an appointment.","No Selection",JOptionPane.WARNING_MESSAGE); return; }
        String aptId  = (String) appointmentsModel.getValueAt(row, 0);
        String status = (String) appointmentsModel.getValueAt(row, 3);
        if ("COMPLETED".equalsIgnoreCase(status) || Appointment.STATUS_AWAITING_PAYMENT.equals(status)) {
            JOptionPane.showMessageDialog(this,"Already completed / awaiting payment.","Info",JOptionPane.INFORMATION_MESSAGE); return;
        }

        Appointment a = FileManager.findAppointmentById(aptId);
        if (a == null) return;

        // ── Parts selection dialog ────────────────────────────────────────────
        List<CarPart> allParts = FileManager.readAllCarParts().stream()
                .filter(CarPart::isInStock).collect(java.util.stream.Collectors.toList());

        // Build checklist UI
        JPanel dlgPanel = new JPanel(new BorderLayout(0, 10));
        dlgPanel.setBackground(UIUtils.DARK_NAVY);
        dlgPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel infoLbl = new JLabel("<html>Appointment: <b>" + aptId + "</b> &nbsp;|&nbsp; Service: <b>" + a.getServiceType() + "</b></html>");
        infoLbl.setForeground(UIUtils.ACCENT_BLUE);
        infoLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));

        JLabel hintLbl = new JLabel("Select any additional parts consumed during this service:");
        hintLbl.setForeground(UIUtils.LIGHT_GRAY);
        hintLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));

        JPanel topInfo = new JPanel(new GridLayout(2, 1, 0, 4));
        topInfo.setBackground(UIUtils.DARK_NAVY);
        topInfo.add(infoLbl); topInfo.add(hintLbl);

        // Parts list with qty spinners
        JPanel partsPanel = new JPanel();
        partsPanel.setLayout(new BoxLayout(partsPanel, BoxLayout.Y_AXIS));
        partsPanel.setBackground(UIUtils.TABLE_BG);
        partsPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        java.util.Map<CarPart, JCheckBox>  checkMap = new java.util.LinkedHashMap<>();
        java.util.Map<CarPart, JSpinner>   spinMap  = new java.util.LinkedHashMap<>();

        if (allParts.isEmpty()) {
            JLabel none = new JLabel("No parts available in inventory.");
            none.setForeground(UIUtils.DIM_TEXT); none.setFont(UIUtils.uiFont(Font.ITALIC, 12));
            partsPanel.add(none);
        } else {
            for (CarPart cp : allParts) {
                JPanel row2 = new JPanel(new BorderLayout(8, 0));
                row2.setBackground(UIUtils.TABLE_BG);
                row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                row2.setBorder(new EmptyBorder(4, 4, 4, 4));

                JCheckBox chk = new JCheckBox(cp.getName() + "  (RM " + String.format("%.2f", cp.getPrice()) + " / unit)");
                chk.setBackground(UIUtils.TABLE_BG);
                chk.setForeground(UIUtils.WHITE);
                chk.setFont(UIUtils.uiFont(Font.PLAIN, 12));

                SpinnerNumberModel spinModel = new SpinnerNumberModel(1, 1, 99, 1);
                JSpinner spinner = new JSpinner(spinModel);
                spinner.setPreferredSize(new Dimension(60, 28));
                spinner.setEnabled(false);
                chk.addActionListener(ev -> spinner.setEnabled(chk.isSelected()));

                checkMap.put(cp, chk);
                spinMap.put(cp, spinner);

                JPanel right2 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                right2.setBackground(UIUtils.TABLE_BG);
                JLabel qtyLbl = new JLabel("Qty:");
                qtyLbl.setForeground(UIUtils.DIM_TEXT);
                qtyLbl.setFont(UIUtils.uiFont(Font.PLAIN, 11));
                right2.add(qtyLbl); right2.add(spinner);

                row2.add(chk,    BorderLayout.CENTER);
                row2.add(right2, BorderLayout.EAST);
                partsPanel.add(row2);
            }
        }

        JScrollPane partsScroll = new JScrollPane(partsPanel);
        partsScroll.setPreferredSize(new Dimension(520, 260));
        UIUtils.styleScrollPane(partsScroll);

        dlgPanel.add(topInfo,     BorderLayout.NORTH);
        dlgPanel.add(partsScroll, BorderLayout.CENTER);

        // Show dialog
        int choice = JOptionPane.showConfirmDialog(
                this, dlgPanel,
                "Complete Job — Select Extra Parts Used",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) return;

        // Save selected AppointmentParts
        for (CarPart cp : allParts) {
            JCheckBox chk = checkMap.get(cp);
            if (chk != null && chk.isSelected()) {
                int qty = (int) spinMap.get(cp).getValue();
                AppointmentPart ap = new AppointmentPart(
                        aptId,
                        cp.getPartId(),
                        cp.getName(),
                        cp.getPrice(),
                        qty
                );
                FileManager.saveAppointmentPart(ap);
            }
        }

        // Mark as AWAITING_PAYMENT so counter staff can collect
        a.setStatus(Appointment.STATUS_AWAITING_PAYMENT);
        FileManager.updateAppointment(a);
        refreshAppointmentsTable();

        JOptionPane.showMessageDialog(this,
                "Job marked as done!\nAppointment is now awaiting payment by Counter Staff.",
                "Success", JOptionPane.INFORMATION_MESSAGE);

        // ── Auto-prompt: fill Vehicle Health Record ───────────────────────────
        int fillHealth = JOptionPane.showConfirmDialog(this,
                "<html>Would you like to fill in the <b>Vehicle Health Record</b> for this appointment now?</html>",
                "Fill Health Record?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (fillHealth == JOptionPane.YES_OPTION) {
            TechnicianHealthRecordPage healthPage = new TechnicianHealthRecordPage(this);
            navigate(healthMenuBtn, healthPage);
            healthPage.showAddForAppointment(a);
        }
    }

    // VEHICLE HEALTH RECORDS PANEL

    void addDRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy=row; gbc.gridx=0; gbc.gridwidth=1; gbc.weightx=0.38;
        JLabel l = new JLabel(label); l.setForeground(UIUtils.WHITE); l.setFont(UIUtils.uiFont(Font.BOLD, 12));
        p.add(l, gbc);
        gbc.gridx=1; gbc.weightx=0.62; p.add(comp, gbc);
    }

    // PROFILE DIALOG
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
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; card.add(titleLbl, gbc);
        JLabel avatar = UIUtils.createProfileAvatarLabel(currentTech.getUserId(), currentTech.getGender(), 90, dlg,
                () -> headerAvatarLabel.setIcon(UIUtils.loadCircularAvatar(currentTech.getUserId(), currentTech.getGender(), 48)));
        JLabel hint = new JLabel("Click photo to change", JLabel.CENTER);
        hint.setForeground(UIUtils.DIM_TEXT); hint.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        JPanel avSec = new JPanel(new BorderLayout(0, 4)); avSec.setBackground(UIUtils.TABLE_BG);
        avSec.add(avatar, BorderLayout.CENTER); avSec.add(hint, BorderLayout.SOUTH);
        gbc.gridy=1; card.add(avSec, gbc); gbc.gridwidth=1;
        JTextField     nameF  = new JTextField(currentTech.getName());  UIUtils.styleTextField(nameF);
        JComboBox<String> genderCb = UIUtils.createGenderCombo(currentTech.getGender());
        JTextField     emailF = new JTextField(currentTech.getEmail()); UIUtils.styleTextField(emailF);
        JTextField     phoneF = new JTextField(currentTech.getPhone()); UIUtils.styleTextField(phoneF);
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
            String n=nameF.getText().trim(), g=UIUtils.resolveGender(genderCb), em=emailF.getText().trim(), ph=phoneF.getText().trim();
            String pw=new String(pwF.getPassword()), cf=new String(cpF.getPassword());
            if (n.isEmpty()||em.isEmpty()||ph.isEmpty()) { JOptionPane.showMessageDialog(dlg,"Name, email and phone required.","Error",JOptionPane.ERROR_MESSAGE); return; }
            if (!ValidationUtils.isValidEmail(em))       { JOptionPane.showMessageDialog(dlg,"Invalid email.","Error",JOptionPane.ERROR_MESSAGE); return; }
            if (!ValidationUtils.isValidPhone(ph))       { JOptionPane.showMessageDialog(dlg,"Phone must be 10-11 digits.","Error",JOptionPane.ERROR_MESSAGE); return; }
            if (!pw.isEmpty()&&!pw.equals(cf))           { JOptionPane.showMessageDialog(dlg,"Passwords do not match.","Error",JOptionPane.ERROR_MESSAGE); return; }
            currentTech.setName(n); currentTech.setGender(g); currentTech.setEmail(em); currentTech.setPhone(ph);
            if (!pw.isEmpty()) currentTech.setPassword(pw);
            FileManager.updateTechnician(currentTech);
            headerAvatarLabel.setIcon(UIUtils.loadCircularAvatar(currentTech.getUserId(), g, 48));
            setTitle("Technician Dashboard - " + currentTech.getName());
            JOptionPane.showMessageDialog(dlg,"Profile updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });
        gbc.gridx=0; gbc.gridy=8; gbc.gridwidth=2; card.add(save, gbc);
        outer.add(card, new GridBagConstraints()); dlg.setContentPane(outer); dlg.setVisible(true);
    }

    // HELPERS
    JLabel sectionTitle(String text, String iconPath) {
        JLabel l;
        if (iconPath != null) {
            ImageIcon icon = loadMenuIcon(iconPath, 20, 20);
            l = (icon != null) ? new JLabel(text, icon, JLabel.LEFT) : new JLabel(text);
            if (icon != null) l.setIconTextGap(8);
        } else { l = new JLabel(text); }
        l.setForeground(UIUtils.ACCENT_BLUE);
        l.setFont(UIUtils.uiFont(Font.BOLD, 16));
        l.setBorder(new EmptyBorder(0, 0, 12, 0));
        return l;
    }

    JPanel miniCard(String label, String value, Color accent) {
        JPanel c = new JPanel(new GridLayout(2, 1, 0, 4));
        c.setBackground(UIUtils.TABLE_BG);
        c.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(accent, 10, 1), new EmptyBorder(10, 14, 10, 14)));
        JLabel vl = new JLabel(value, JLabel.CENTER); vl.setFont(UIUtils.uiFont(Font.BOLD, 20)); vl.setForeground(accent);
        JLabel ll = new JLabel(label, JLabel.CENTER); ll.setFont(UIUtils.uiFont(Font.PLAIN, 11)); ll.setForeground(UIUtils.DIM_TEXT);
        c.add(vl); c.add(ll);
        return c;
    }

    void profileRow(JPanel card, GridBagConstraints g, int row, String labelText, JComponent comp) {
        g.gridy=row; g.gridx=0; g.gridwidth=1; g.weightx=0.35;
        JLabel l = new JLabel(labelText); l.setForeground(UIUtils.WHITE); l.setFont(UIUtils.uiFont(Font.BOLD, 12));
        card.add(l, g);
        g.gridx=1; g.weightx=0.65; card.add(comp, g);
    }
    
    void filterAppointmentsTable(String keyword, String statusFilter,
            TableRowSorter<DefaultTableModel> sorter) {

        // If both empty/All, show everything
        if (keyword.isEmpty() && "All".equals(statusFilter)) {
            sorter.setRowFilter(null);
            return;
        }

        List<RowFilter<Object, Object>> andFilters = new ArrayList<>();

        // Status filter (col 3) Ã¢â‚¬â€ skip if "All"
        if (!"All".equals(statusFilter)) {
            andFilters.add(RowFilter.regexFilter("(?i)^" + statusFilter + "$", 3));
        }

        // Keyword filter Ã¢â‚¬â€ searches across Appt ID(0), Customer(1), Service(2), Date(4)
        if (!keyword.isEmpty()) {
            try {
                List<RowFilter<Object, Object>> orFilters = new ArrayList<>();
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 0)); // Appt ID
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 1)); // Customer
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 2)); // Service
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 4)); // Date
                andFilters.add(RowFilter.orFilter(orFilters));
            } catch (java.util.regex.PatternSyntaxException ex) {
                // Invalid regex typed by user Ã¢â‚¬â€ ignore and show all
                sorter.setRowFilter(null);
                return;
            }
        }

        // Apply combined filter
        sorter.setRowFilter(andFilters.size() == 1
                ? andFilters.get(0)
                : RowFilter.andFilter(andFilters));
    }
    
    void filterHealthRecords(String keyword, String oilFilter,
            TableRowSorter<DefaultTableModel> sorter) {

        // If both empty, show everything
        if (keyword.isEmpty() && "All".equals(oilFilter)) {
            sorter.setRowFilter(null);
            return;
        }

        List<RowFilter<Object, Object>> andFilters = new ArrayList<>();

        // Oil type filter (col 6)
        if (!"All".equals(oilFilter)) {
            try {
                andFilters.add(RowFilter.regexFilter(
                        "(?i)^" + java.util.regex.Pattern.quote(oilFilter) + "$", 6));
            } catch (Exception ex) { /* ignore */ }
        }

        // Keyword filter Ã¢â‚¬â€ searches Record ID(0), Date(1), Appointment(2), Customer(3)
        if (!keyword.isEmpty()) {
            try {
                List<RowFilter<Object, Object>> orFilters = new ArrayList<>();
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 0)); // Record ID
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 1)); // Date
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 2)); // Appointment
                orFilters.add(RowFilter.regexFilter("(?i)" + keyword, 3)); // Customer
                andFilters.add(RowFilter.orFilter(orFilters));
            } catch (java.util.regex.PatternSyntaxException ex) {
                sorter.setRowFilter(null);
                return;
            }
        }

        sorter.setRowFilter(andFilters.size() == 1
                ? andFilters.get(0)
                : RowFilter.andFilter(andFilters));
    } 
    

    void addDetailRow(JPanel panel, GridBagConstraints gbc,
            int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.4;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        lbl.setForeground(UIUtils.LIGHT_GRAY);
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.6;
        JLabel val = new JLabel(value);
        val.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        val.setForeground(UIUtils.WHITE);
        panel.add(val, gbc);
    }
    
    JPanel statCard(String title, String value, Color accent, String desc) {
        JPanel c = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.TABLE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 10, 12, 12);
                g2.fillRect(0, 5, getWidth(), 5);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(accent, 12, 1), new EmptyBorder(15, 10, 10, 10)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 2, 0);
        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(UIUtils.uiFont(Font.BOLD, 22)); valLbl.setForeground(accent); c.add(valLbl, gbc);
        gbc.gridy = 1;
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UIUtils.uiFont(Font.BOLD, 12)); titleLbl.setForeground(UIUtils.WHITE); c.add(titleLbl, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        JLabel descLbl = new JLabel(desc, SwingConstants.CENTER);
        descLbl.setFont(UIUtils.uiFont(Font.PLAIN, 10)); descLbl.setForeground(UIUtils.DIM_TEXT); c.add(descLbl, gbc);
        return c;
    }
    
    // READ-ONLY INVENTORY PANEL (Technician)

    JLabel legendItem(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(UIUtils.uiSymbolFont(Font.PLAIN, 11));
        l.setForeground(color);
        return l;
    }
    
    // buildStatusToggle left as dead code Ã¢â‚¬â€ status toggle is in the header subRow
}
