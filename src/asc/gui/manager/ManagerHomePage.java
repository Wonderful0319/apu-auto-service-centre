package asc.gui.manager;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class ManagerHomePage extends JPanel {

    private static final long serialVersionUID = 1L;
    final ManagerDashboard main;

    public ManagerHomePage(ManagerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(UIUtils.DARK_NAVY);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        // ── Hero banner (identical structure to CustomerDashboard) ──
        JPanel banner = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (main.homeBgImage != null) {
                    g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                    g2.drawImage(main.homeBgImage, 0, 0, getWidth(), getHeight(), this);
                    g2.setClip(null);
                    g2.setColor(new Color(10, 20, 50, 165));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                } else {
                    g2.setPaint(new GradientPaint(0, 0, new Color(18, 38, 82, 220),
                            getWidth(), getHeight(), new Color(40, 90, 180, 200)));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                }
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 20, 1),
                new EmptyBorder(100, 32, 100, 32)));

        JPanel bannerLeft = new JPanel();
        bannerLeft.setLayout(new BoxLayout(bannerLeft, BoxLayout.Y_AXIS));
        bannerLeft.setOpaque(false);
        JLabel greet = new JLabel("Welcome back, Manager " + main.currentManager.getName() + "!");
        greet.setFont(UIUtils.uiFont(Font.BOLD, 20)); greet.setForeground(UIUtils.WHITE);
        bannerLeft.add(greet); bannerLeft.add(Box.createVerticalStrut(5));

        UIUtils.RoundedButton changeBgBtn = new UIUtils.RoundedButton("Change Background", new Color(50, 90, 170));
        changeBgBtn.setFont(UIUtils.uiFont(Font.BOLD, 11));
        changeBgBtn.setPreferredSize(new Dimension(168, 32));
        changeBgBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String saved = UIUtils.saveBgImage(
                        main.currentManager.getUserId(), "manager", fc.getSelectedFile());
                if (saved != null) {
                    main.homeBgImage = UIUtils.loadBgImage(main.currentManager.getUserId(), "manager");
                    main.showHome();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save background image.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        UIUtils.RoundedButton clearBgBtn = new UIUtils.RoundedButton("Clear", new Color(150, 50, 50));
        clearBgBtn.setFont(UIUtils.uiFont(Font.BOLD, 11));
        clearBgBtn.setPreferredSize(new Dimension(70, 32));
        clearBgBtn.addActionListener(e -> {
            UIUtils.deleteBgImage(main.currentManager.getUserId(), "manager");
            main.homeBgImage = null;
            main.showHome();
        });

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnGroup.setOpaque(false);
        btnGroup.add(changeBgBtn); btnGroup.add(clearBgBtn);
        banner.add(bannerLeft, BorderLayout.CENTER);
        banner.add(btnGroup, BorderLayout.EAST);

        // ── Stat cards (same statCard style as CustomerDashboard) ──
        List<asc.model.Appointment> allApts = FileManager.readAllAppointments();
        List<Customer> allCusts = FileManager.readAllCustomers();
        long completed = allApts.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long pending   = allApts.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus())).count();
        double revenue = FileManager.readAllPayments().stream().mapToDouble(Payment::getAmount).sum();

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setOpaque(false);
        statsWrapper.setPreferredSize(new Dimension(-1, 98));
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.add(statCard("Customers",    String.valueOf(allCusts.size()),       UIUtils.ACCENT_BLUE, "Registered users"));
        statsRow.add(statCard("Appointments", String.valueOf(allApts.size()),        ManagerDashboard.GREEN_OK,            "Total records"));
        statsRow.add(statCard("Completed",    String.valueOf(completed),             ManagerDashboard.ACCENT_YELLOW,       "Services done"));
        statsRow.add(statCard("Revenue",      String.format("RM %.0f", revenue),    ManagerDashboard.PURPLE,              "Total collected"));
        statsWrapper.add(statsRow, BorderLayout.CENTER);

        // ── Bottom row: Pending Approvals card + Quick Actions (same as CustomerDashboard) ──
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(buildPendingApprovalsCard(pending, pending));
        bottomRow.add(buildManagerQuickActions());

        root.add(banner,       BorderLayout.NORTH);
        root.add(statsWrapper, BorderLayout.CENTER);
        root.add(bottomRow,    BorderLayout.SOUTH);
        return root;
    }

    /** Stat card with top accent bar — matches CustomerDashboard's statCard exactly. */
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
                new UIUtils.RoundedBorder(accent, 12, 1),
                new EmptyBorder(15, 10, 10, 10)));
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

    /** Pending approvals reminder card — mirrors CustomerDashboard's buildReminderCard. */
    JPanel buildPendingApprovalsCard(long pendingRegCount, long pendingAptCount) {
        List<PendingRegistration> pending = FileManager.readAllPendingRegistrations().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());

        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(UIUtils.TABLE_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(ManagerDashboard.ACCENT_YELLOW, 12, 1),
                new EmptyBorder(14, 18, 14, 18)));
        JLabel hdr = new JLabel(pending.isEmpty()
                ? "\u23F0  No Pending Registrations"
                : "\u23F0  Pending Registrations (" + pending.size() + ")");
        hdr.setFont(UIUtils.uiSymbolFont(Font.BOLD, 13)); hdr.setForeground(ManagerDashboard.ACCENT_YELLOW);

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(UIUtils.TABLE_BG);

        if (pending.isEmpty()) {
            JLabel none = new JLabel("No pending registrations. All caught up!");
            none.setForeground(UIUtils.LIGHT_GRAY);
            none.setFont(UIUtils.uiFont(Font.ITALIC, 11));
            rows.add(none);
        } else {
            int max = Math.min(pending.size(), 4);
            for (int i = 0; i < max; i++) {
                PendingRegistration r = pending.get(i);
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(new Color(44, 68, 120));
                row.setBorder(BorderFactory.createCompoundBorder(
                        new UIUtils.RoundedBorder(new Color(60, 90, 150), 8, 1),
                        new EmptyBorder(6, 10, 6, 10)));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
                JLabel left  = new JLabel(r.getRegId() + "  \u2022  " + r.getName());
                left.setFont(UIUtils.uiFont(Font.PLAIN, 11)); left.setForeground(UIUtils.WHITE);
                JLabel right = new JLabel(r.getRole());
                right.setFont(UIUtils.uiFont(Font.BOLD, 11)); right.setForeground(ManagerDashboard.ACCENT_YELLOW);
                row.add(left, BorderLayout.WEST); row.add(right, BorderLayout.EAST);
                rows.add(row); rows.add(Box.createVerticalStrut(5));
            }
        }
        card.add(hdr,  BorderLayout.NORTH);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    /** Quick actions card — mirrors CustomerDashboard's buildQuickActions. */
    JPanel buildManagerQuickActions() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UIUtils.TABLE_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 1),
                new EmptyBorder(14, 18, 14, 18)));
        JLabel hdr = new JLabel("\u26A1  Quick Actions");
        hdr.setFont(UIUtils.uiSymbolFont(Font.BOLD, 13)); hdr.setForeground(UIUtils.ACCENT_BLUE);
        JPanel btns = new JPanel(new GridLayout(2, 3, 10, 10));
        btns.setBackground(UIUtils.TABLE_BG);
        btns.add(qBtn("Pending Approvals", new Color(200, 100,   0), main.approvalMenuBtn));
        btns.add(qBtn("User Management",   new Color( 60, 130, 200), main.userMenuBtn));
        btns.add(qBtn("Car Parts",         new Color( 50, 160, 100), main.partsMenuBtn));
        btns.add(qBtn("Set Prices",        new Color(160,  90, 220), main.pricesMenuBtn));
        btns.add(qBtn("Feedbacks",         new Color(200,  70,  70), main.fbMenuBtn));
        btns.add(qBtn("Reports",           new Color(100, 160,  50), main.reportMenuBtn));
        card.add(hdr,  BorderLayout.NORTH);
        card.add(btns, BorderLayout.CENTER);
        return card;
    }

    /** Quick action button — delegates to the corresponding sidebar button. */
    JButton qBtn(String text, Color bg, JButton sidebarBtn) {
        UIUtils.RoundedButton b = new UIUtils.RoundedButton(text, bg);
        b.setFont(UIUtils.uiFont(Font.BOLD, 11));
        b.addActionListener(e -> sidebarBtn.doClick());
        return b;
    }

}
