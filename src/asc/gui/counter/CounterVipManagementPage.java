package asc.gui.counter;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class CounterVipManagementPage extends JPanel {

    private static final long serialVersionUID = 1L;
    private final CounterStaffDashboard main;

    public CounterVipManagementPage(CounterStaffDashboard m) {
        main = m;
        setLayout(new BorderLayout(0, 10));
        setBackground(UIUtils.DARK_NAVY);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(main.sectionTitle("VIP Member Management", CounterStaffDashboard.ICON_BASE + "Payments.png"), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildBody() {
        String[] cols = {"VIP ID","Customer","Phone","Points","Tier","Service Disc.","Coupon / Month","Member Since"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIUtils.createStyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {65, 140, 110, 70, 90, 100, 120, 100};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setRowHeight(28);

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = v == null ? "" : v.toString();
                switch (s) {
                    case VipAccount.TIER_BLACKGOLD: setForeground(new Color(255, 215,   0)); break;
                    case VipAccount.TIER_GOLD:      setForeground(new Color(255, 200,  60)); break;
                    case VipAccount.TIER_BRONZE:    setForeground(new Color(205, 127,  50)); break;
                    default:                        setForeground(UIUtils.LIGHT_GRAY);       break;
                }
                setBackground(sel ? UIUtils.ACCENT_BLUE : UIUtils.TABLE_BG);
                setFont(UIUtils.uiFont(Font.BOLD, 12));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (VipAccount v : FileManager.readAllVipAccounts()) {
                Customer c = FileManager.findCustomerById(v.getCustomerId());
                String discStr   = v.getDiscountPct() > 0 ? String.format("%.0f%%", v.getDiscountPct()) : "\u2014";
                String couponStr = VipAccount.TIER_BLACKGOLD.equals(v.getTier()) ? "RM 50 / month"
                        : (VipAccount.TIER_NONE.equals(v.getTier()) ? "None" : "RM 20 / month");
                model.addRow(new Object[]{v.getVipId(), (c != null) ? c.getName() : v.getCustomerId(),
                        v.getPhone(), v.getPoints(), v.getTier(),
                        discStr, couponStr, v.getCreatedDate()});
            }
        };
        refresh.run();

        JScrollPane scroll = new JScrollPane(table);
        UIUtils.styleScrollPane(scroll);

        UIUtils.RoundedButton enrollBtn = new UIUtils.RoundedButton("Enrol VIP Member", UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton addPtsBtn = new UIUtils.RoundedButton("Add Points",        UIUtils.ACCENT_BLUE);
        UIUtils.RoundedButton refBtn    = new UIUtils.RoundedButton("Refresh",            new Color(60, 90, 150));
        for (JButton b : new JButton[]{enrollBtn, addPtsBtn, refBtn})
            b.setPreferredSize(new Dimension(170, 34));

        enrollBtn.addActionListener(e -> { showEnrolVipDialog(); refresh.run(); });
        addPtsBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(main, "Select a VIP member.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
            VipAccount v = FileManager.readAllVipAccounts().stream()
                    .filter(vv -> vv.getVipId().equals(model.getValueAt(row, 0))).findFirst().orElse(null);
            if (v == null) return;
            String input = JOptionPane.showInputDialog(main, "Enter points to add (1 RM = 1 pt):", "Add Points", JOptionPane.QUESTION_MESSAGE);
            if (input == null) return;
            try {
                int pts = Integer.parseInt(input.trim());
                if (pts <= 0) throw new NumberFormatException();
                v.setPoints(v.getPoints() + pts); v.recalcTier(); FileManager.updateVipAccount(v);
                JOptionPane.showMessageDialog(main, "Added " + pts + " pts. New total: " + v.getPoints() + " (" + v.getTier() + ")", "Success", JOptionPane.INFORMATION_MESSAGE);
                refresh.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(main, "Invalid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        refBtn.addActionListener(e -> refresh.run());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(enrollBtn); btnRow.add(addPtsBtn); btnRow.add(refBtn);

        long total     = FileManager.readAllVipAccounts().size();
        long bronze    = FileManager.readAllVipAccounts().stream().filter(v -> VipAccount.TIER_BRONZE.equals(v.getTier())).count();
        long gold      = FileManager.readAllVipAccounts().stream().filter(v -> VipAccount.TIER_GOLD.equals(v.getTier())).count();
        long blackgold = FileManager.readAllVipAccounts().stream().filter(v -> VipAccount.TIER_BLACKGOLD.equals(v.getTier())).count();
        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 0));
        summary.setBackground(UIUtils.DARK_NAVY);
        summary.setBorder(new EmptyBorder(8, 0, 0, 0));
        summary.add(miniCard("Total VIP",  String.valueOf(total),     UIUtils.ACCENT_BLUE));
        summary.add(miniCard("Bronze",     String.valueOf(bronze),    new Color(205, 127,  50)));
        summary.add(miniCard("Gold",       String.valueOf(gold),      new Color(255, 200,  60)));
        summary.add(miniCard("BlackGold",  String.valueOf(blackgold), new Color(255, 215,   0)));

        JPanel centre = new JPanel(new BorderLayout(0, 8));
        centre.setBackground(UIUtils.DARK_NAVY);
        centre.add(scroll,   BorderLayout.CENTER);
        centre.add(summary,  BorderLayout.SOUTH);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setBackground(UIUtils.DARK_NAVY);
        body.add(centre, BorderLayout.CENTER);
        body.add(btnRow, BorderLayout.SOUTH);
        return body;
    }

    private void showEnrolVipDialog() {
        List<Customer> custs = FileManager.readAllCustomers();
        if (custs.isEmpty()) { JOptionPane.showMessageDialog(main, "No customers found.", "Error", JOptionPane.WARNING_MESSAGE); return; }
        JDialog d = new JDialog(main, "Enrol New VIP Member", true);
        d.setSize(460, 260); d.setLocationRelativeTo(main);
        JPanel p = main.dlgPanel(); GridBagConstraints gbc = main.dlgGbc();
        JComboBox<String> custCb = new JComboBox<>(custs.stream()
                .map(c -> c.getUserId() + " - " + c.getName() + " (" + c.getPhone() + ")")
                .toArray(String[]::new));
        UIUtils.styleCombo(custCb);
        JTextField phoneFld = main.tf();
        Runnable fillPhone = () -> { int idx = custCb.getSelectedIndex(); if (idx >= 0) phoneFld.setText(custs.get(idx).getPhone()); };
        fillPhone.run(); custCb.addActionListener(e -> fillPhone.run());
        main.addDlgRow(p, gbc, 0, "Customer:",  custCb);
        main.addDlgRow(p, gbc, 1, "VIP Phone:", phoneFld);
        JLabel note = new JLabel("(1 RM spent = 1 point  |  Starts at NONE tier  |  1,000 pts \u2192 BRONZE)");
        note.setForeground(UIUtils.DIM_TEXT); note.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; p.add(note, gbc);
        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Enrol", UIUtils.SUCCESS_GREEN);
        save.setPreferredSize(new Dimension(160, 34));
        save.addActionListener(e -> {
            int idx = custCb.getSelectedIndex(); if (idx < 0) return;
            Customer sel = custs.get(idx);
            if (FileManager.findVipByCustomerId(sel.getUserId()) != null) {
                JOptionPane.showMessageDialog(d, "Already a VIP member.", "Already Enrolled", JOptionPane.WARNING_MESSAGE); return;
            }
            String phone = phoneFld.getText().trim();
            if (!ValidationUtils.isValidPhone(phone)) { JOptionPane.showMessageDialog(d, "Phone must be 10-11 digits.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            VipAccount vip = VipAccount.enrol(FileManager.generateVipId(), sel.getUserId(), phone);
            FileManager.saveVipAccount(vip);
            JOptionPane.showMessageDialog(d, "VIP enrolled!\nID: " + vip.getVipId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });
        gbc.gridy = 3; p.add(save, gbc);
        d.setContentPane(p); d.setVisible(true);
    }

    private JPanel miniCard(String label, String value, Color accent) {
        JPanel c = new JPanel(new GridLayout(2, 1, 0, 4));
        c.setBackground(UIUtils.TABLE_BG);
        c.setBorder(BorderFactory.createCompoundBorder(new UIUtils.RoundedBorder(accent, 10, 1), new EmptyBorder(8, 12, 8, 12)));
        JLabel vl = new JLabel(value, JLabel.CENTER); vl.setFont(UIUtils.uiFont(Font.BOLD, 18)); vl.setForeground(accent);
        JLabel ll = new JLabel(label, JLabel.CENTER); ll.setFont(UIUtils.uiFont(Font.PLAIN, 11)); ll.setForeground(UIUtils.DIM_TEXT);
        c.add(vl); c.add(ll);
        return c;
    }
}
