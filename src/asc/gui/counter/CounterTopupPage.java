package asc.gui.counter;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class CounterTopupPage extends JPanel {

    private static final long serialVersionUID = 1L;

    private final CounterStaffDashboard main;
    private DefaultTableModel model;
    private JTable table;

    public CounterTopupPage(CounterStaffDashboard m) {
        main = m;
        setLayout(new BorderLayout(0, 10));
        setBackground(UIUtils.DARK_NAVY);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        add(main.sectionTitle("Top-Up Wallet",
                CounterStaffDashboard.ICON_BASE + "Top-Up Wallet(Blue).png"), BorderLayout.NORTH);
        add(makeBody(), BorderLayout.CENTER);
    }

    // =========================================================================
    //  BODY
    // =========================================================================

    private JScrollPane makeBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIUtils.DARK_NAVY);

        // ── Top-up form card ─────────────────────────────────────────────────
        JPanel top = main.makeCard(null);
        top.setLayout(new BorderLayout(24, 0));

        JPanel left = new JPanel(new GridBagLayout());
        left.setBackground(CounterStaffDashboard.CARD2);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 5, 8, 5);

        JTextField search   = main.tf();
        JComboBox<String> custBox = new JComboBox<>();
        JTextField amountF  = main.tf();
        JComboBox<String> methodCb = new JComboBox<>(
                new String[]{"Cash", "Debit Card", "Credit Card", "E-Wallet", "Online Transfer"});
        UIUtils.styleCombo(custBox);
        UIUtils.styleCombo(methodCb);

        JLabel balLbl = new JLabel("Balance: RM 0.00");
        balLbl.setForeground(UIUtils.LIGHT_GRAY);
        balLbl.setFont(UIUtils.uiFont(Font.BOLD, 14));

        Runnable fill = () -> {
            String old = main.comboId(custBox);
            custBox.removeAllItems();
            String q = search.getText().trim().toLowerCase();
            for (Customer c : FileManager.readAllCustomers()) {
                if (main.customerOff(c)) continue;
                if (!q.isEmpty() && !main.customerFind(c, q)) continue;
                custBox.addItem(c.getUserId() + " | " + c.getName() + " | " + c.getPhone());
            }
            main.selectById(custBox, old);
        };
        Runnable setBal = () -> {
            Customer c = main.comboCustomer(custBox);
            double b = c == null ? 0 : walletBalance(c.getUserId());
            balLbl.setText(String.format("Balance: RM %.2f", b));
            balLbl.setForeground(b > 0 ? UIUtils.GREEN_OK : UIUtils.LIGHT_GRAY);
        };

        fill.run(); setBal.run();
        search.getDocument().addDocumentListener(main.doc(() -> { fill.run(); setBal.run(); }));
        custBox.addActionListener(e -> setBal.run());

        fRow(left, g, 0, "Customer Search:",    search);
        fRow(left, g, 1, "Matched Customer:",   custBox);
        fRow(left, g, 2, "Top-Up Amount (RM):", amountF);
        fRow(left, g, 3, "Payment Method:",     methodCb);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; left.add(balLbl, g);

        // ── Right: live preview panel ────────────────────────────────────────
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(CounterStaffDashboard.CARD3);
        right.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1),
                new EmptyBorder(24, 26, 24, 26)));

        JLabel bigLbl = new JLabel("Wallet Top-Up");
        bigLbl.setForeground(UIUtils.WHITE); bigLbl.setFont(UIUtils.uiFont(Font.BOLD, 18));
        bigLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel moneyLbl = new JLabel("RM 0.00");
        moneyLbl.setForeground(UIUtils.WARN_YELLOW); moneyLbl.setFont(UIUtils.uiFont(Font.BOLD, 32));
        moneyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountF.getDocument().addDocumentListener(main.doc(() ->
                moneyLbl.setText("RM " + safeAmount(amountF.getText()))));

        UIUtils.RoundedButton saveBtn = new UIUtils.RoundedButton("Save Top-Up", UIUtils.SUCCESS_GREEN);
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(180, 40));
        saveBtn.addActionListener(e -> {
            Customer c = main.comboCustomer(custBox);
            if (c == null) { main.msgErr(this, "Select a customer first."); return; }
            double a;
            try { a = Double.parseDouble(amountF.getText().trim()); }
            catch (Exception ex) { main.msgErr(this, "Invalid amount."); return; }
            if (a <= 0) { main.msgErr(this, "Amount must be more than 0."); return; }

            Payment p = new Payment(
                    FileManager.generatePaymentId(),
                    "TOPUP-" + c.getUserId(), c.getUserId(),
                    a, LocalDate.now().toString(),
                    methodCb.getSelectedItem().toString(),
                    FileManager.generateReceiptNumber());
            FileManager.savePayment(p);
            JOptionPane.showMessageDialog(this,
                    "Top-up saved.\nReceipt: " + p.getReceiptNumber(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            main.showReceiptWindow(p, null);
            amountF.setText("");
            setBal.run();
            refresh();
        });

        right.add(bigLbl);
        right.add(Box.createVerticalStrut(20));
        right.add(moneyLbl);
        right.add(Box.createVerticalGlue());
        right.add(saveBtn);

        top.add(left,  BorderLayout.CENTER);
        top.add(right, BorderLayout.EAST);

        // ── Top-up history card ──────────────────────────────────────────────
        JPanel history = main.makeCard("Top-Up History");
        model = new DefaultTableModel(
                new String[]{"Receipt No.","Customer","Amount (RM)","Method","Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = UIUtils.createStyledTable(model);
        main.darkTable(table);
        refresh();

        JScrollPane scroll = new JScrollPane(table);
        UIUtils.styleScrollPane(scroll);
        scroll.setPreferredSize(new Dimension(0, 300));
        history.add(scroll);

        body.add(top);
        body.add(Box.createVerticalStrut(14));
        body.add(history);

        JScrollPane all = new JScrollPane(body);
        all.setBorder(null);
        all.getViewport().setBackground(UIUtils.DARK_NAVY);
        return all;
    }

    // =========================================================================
    //  HELPERS
    // =========================================================================

    private void refresh() {
        if (model == null) return;
        model.setRowCount(0);
        for (Payment p : FileManager.readAllPayments()) {
            if (!p.getAppointmentId().startsWith("TOPUP-")) continue;
            Customer c = FileManager.findCustomerById(p.getCustomerId());
            model.addRow(new Object[]{
                p.getReceiptNumber(),
                c != null ? c.getName() : p.getCustomerId(),
                String.format("%.2f", p.getAmount()),
                p.getPaymentMethod(), p.getPaymentDate()
            });
        }
    }

    private void fRow(JPanel p, GridBagConstraints g, int y, String label, JComponent comp) {
        g.gridy = y; g.gridx = 0; g.gridwidth = 1; g.weightx = 0.32;
        JLabel l = new JLabel(label);
        l.setForeground(UIUtils.WHITE); l.setFont(UIUtils.uiFont(Font.BOLD, 12));
        p.add(l, g);
        g.gridx = 1; g.weightx = 0.68; p.add(comp, g);
    }

    private double walletBalance(String id) {
        double in = 0, out = 0;
        for (Payment p : FileManager.readAllPayments()) {
            if (!p.getCustomerId().equals(id)) continue;
            if (p.getAppointmentId().startsWith("TOPUP-"))                 in  += p.getAmount();
            else if ("Use Balance".equalsIgnoreCase(p.getPaymentMethod())) out += p.getAmount();
        }
        return in - out;
    }

    private String safeAmount(String s) {
        try { return String.format("%.2f", Double.parseDouble(s.trim())); }
        catch (Exception ex) { return "0.00"; }
    }
}
