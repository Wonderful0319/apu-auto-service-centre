package asc.gui.customer;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class CustomerHistoryPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final CustomerDashboard main;

    public CustomerHistoryPage(CustomerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY); panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(main.sectionTitle("Service & Payment History", CustomerDashboard.ICON_BASE + "Service History(Blue).png"), BorderLayout.NORTH);
        List<Appointment>    myApts = main.currentCustomer.getMyAppointments();
        Map<String, Payment> payMap = main.currentCustomer.getMyPaymentMap();
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); filterBar.setBackground(UIUtils.DARK_NAVY);
        JLabel fl = new JLabel("Filter:"); fl.setForeground(UIUtils.WHITE); fl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        JComboBox<String> statusCb = new JComboBox<>(new String[]{"All Status","COMPLETED","PENDING"});
        JComboBox<String> typeCb   = new JComboBox<>(new String[]{"All Types","NORMAL","MAJOR"});
        UIUtils.styleCombo(statusCb); UIUtils.styleCombo(typeCb);
        JTextField searchField = new JTextField(12); UIUtils.styleTextField(searchField);
        JLabel searchLbl = new JLabel("\uD83D\uDD0D"); searchLbl.setForeground(UIUtils.ACCENT_BLUE); searchLbl.setFont(UIUtils.uiSymbolFont(Font.PLAIN, 14));
        JButton applyBtn = main.makeBtn("Apply", UIUtils.ACCENT_BLUE, 88, 28);
        JButton resetBtn = main.makeBtn("Reset", UIUtils.MEDIUM_NAVY, 88, 28);
        filterBar.add(fl); filterBar.add(statusCb); filterBar.add(typeCb); filterBar.add(searchLbl); filterBar.add(searchField); filterBar.add(applyBtn); filterBar.add(resetBtn);
        String[] cols = {"Apt ID","Date","Time","Type","Status","Car","Description","Amount (RM)","Pay Method"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = UIUtils.createStyledTable(model);
        table.setRowHeight(30); table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {65, 90, 58, 110, 105, 130, 185, 105, 115};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                String s = v == null ? "" : v.toString(); boolean done = "COMPLETED".equalsIgnoreCase(s);
                setText((done ? "\u2705 " : "\u23F3 ") + s); setForeground(done ? CustomerDashboard.GREEN_OK : CustomerDashboard.ACCENT_YELLOW);
                setBackground(sel ? UIUtils.ACCENT_BLUE : UIUtils.TABLE_BG);
                setFont(UIUtils.uiFont(Font.BOLD, 12)); setHorizontalAlignment(CENTER); return this;
            }
        });
        Runnable fill = () -> {
            model.setRowCount(0);
            String sf = (String) statusCb.getSelectedItem(), tf = (String) typeCb.getSelectedItem();
            String kw = searchField.getText().trim().toLowerCase();
            for (Appointment a : myApts) {
                if (!"All Status".equals(sf) && !sf.equalsIgnoreCase(a.getStatus())) continue;
                if (!"All Types".equals(tf)  && !tf.equalsIgnoreCase(a.getServiceType())) continue;
                if (!kw.isEmpty() && !a.getAppointmentId().toLowerCase().contains(kw) && !a.getVehicleInfo().toLowerCase().contains(kw)) continue;
                Payment pay = payMap.get(a.getAppointmentId());
                model.addRow(new Object[]{ a.getAppointmentId(), a.getDate(), a.getTime(),
                    "NORMAL".equalsIgnoreCase(a.getServiceType()) ? "Normal (1hr)" : "Major (3hr)",
                    a.getStatus(), a.getVehicleInfo(), a.getNotes(),
                    pay != null ? String.format("RM %.2f", pay.getAmount()) : "\u2013",
                    pay != null ? pay.getPaymentMethod() : "\u2013" });
            }
        };
        fill.run();
        applyBtn.addActionListener(e -> fill.run());
        resetBtn.addActionListener(e -> { statusCb.setSelectedIndex(0); typeCb.setSelectedIndex(0); searchField.setText(""); fill.run(); });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { fill.run(); }
            public void removeUpdate(DocumentEvent e) { fill.run(); }
            public void changedUpdate(DocumentEvent e) { fill.run(); }
        });
        JScrollPane scroll = new JScrollPane(table); UIUtils.styleScrollPane(scroll);
        long completed2 = main.currentCustomer.getCompletedCount();
        double totalPaid = payMap.values().stream().mapToDouble(Payment::getAmount).sum();
        JPanel summary = new JPanel(new GridLayout(1, 4, 10, 0));
        summary.setBackground(UIUtils.DARK_NAVY); summary.setBorder(new EmptyBorder(10, 0, 0, 0));
        summary.add(main.miniCard("Total Appts", String.valueOf(myApts.size()), UIUtils.ACCENT_BLUE));
        summary.add(main.miniCard("Completed",   String.valueOf(completed2),    CustomerDashboard.GREEN_OK));
        summary.add(main.miniCard("Pending",     String.valueOf(myApts.size()-completed2), CustomerDashboard.ACCENT_YELLOW));
        summary.add(main.miniCard("Total Spent", String.format("RM %.2f", totalPaid), CustomerDashboard.PURPLE));
        JPanel centre = new JPanel(new BorderLayout(0, 8)); centre.setBackground(UIUtils.DARK_NAVY);
        centre.add(filterBar, BorderLayout.NORTH); centre.add(scroll, BorderLayout.CENTER); centre.add(summary, BorderLayout.SOUTH);
        panel.add(centre, BorderLayout.CENTER);
        return panel;
    }


}
