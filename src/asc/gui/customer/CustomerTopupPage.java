package asc.gui.customer;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class CustomerTopupPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final CustomerDashboard main;

    public CustomerTopupPage(CustomerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        // ── Outer page panel ─────────────────────────────────────────────────
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));

        // ══════════════════════════════════════════════════════════════════════
        // SECTION TITLE  –  image icon + "Top-Up My Wallet"
        // ══════════════════════════════════════════════════════════════════════
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setBackground(UIUtils.DARK_NAVY);
        titleRow.setBorder(new EmptyBorder(0, 0, 6, 0));

        // Load the PNG icon (20×20)
        ImageIcon rawIcon = null;
        try {
            java.io.File iconFile = new java.io.File(
                    "data/Picture/Customer/Top-Up Wallet(Blue).png");
            if (iconFile.exists()) {
                Image img = new ImageIcon(iconFile.getAbsolutePath())
                        .getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                rawIcon = new ImageIcon(img);
            }
        } catch (Exception ignored) {}

        JLabel titleLbl = rawIcon != null
                ? new JLabel("Top-Up My Wallet", rawIcon, JLabel.LEFT)
                : new JLabel("Top-Up My Wallet");
        titleLbl.setFont(UIUtils.uiFont(Font.BOLD, 17));
        titleLbl.setForeground(UIUtils.ACCENT_BLUE);
        if (rawIcon != null) titleLbl.setIconTextGap(8);
        titleRow.add(titleLbl);
        panel.add(titleRow, BorderLayout.NORTH);

        // ══════════════════════════════════════════════════════════════════════
        // BALANCE BANNER  –  full-width gradient card at the top of content
        // ══════════════════════════════════════════════════════════════════════
        double walletBal = main.computeWalletBalance();

        JPanel[] bannerRef = {null};
        bannerRef[0] = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                        0, 0,           new Color(28, 68, 140),
                        getWidth(), 0,  new Color(18, 42, 96)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        bannerRef[0].setOpaque(false);
        bannerRef[0].setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 14, 1),
                new EmptyBorder(16, 26, 16, 26)));

        JLabel balTitleLbl = new JLabel("Current Wallet Balance");
        balTitleLbl.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        balTitleLbl.setForeground(UIUtils.DIM_TEXT);

        JLabel[] balValRef = {new JLabel(String.format("RM  %.2f", walletBal))};
        balValRef[0].setFont(UIUtils.uiFont(Font.BOLD, 30));
        balValRef[0].setForeground(walletBal > 0 ? UIUtils.GREEN_OK : UIUtils.LIGHT_GRAY);

        JPanel balLeft = new JPanel();
        balLeft.setLayout(new BoxLayout(balLeft, BoxLayout.Y_AXIS));
        balLeft.setOpaque(false);
        balLeft.add(balTitleLbl);
        balLeft.add(Box.createVerticalStrut(4));
        balLeft.add(balValRef[0]);

        // VIP badge on the right side of banner
        VipAccount topupVip = FileManager.findVipByCustomerId(main.currentCustomer.getUserId());
        JPanel balRight = new JPanel();
        balRight.setLayout(new BoxLayout(balRight, BoxLayout.Y_AXIS));
        balRight.setOpaque(false);
        if (topupVip != null) {
            JLabel tierBadge = new JLabel(topupVip.tierBadge(), JLabel.RIGHT);
            tierBadge.setFont(UIUtils.uiFont(Font.BOLD, 16));
            tierBadge.setForeground(topupVip.tierColor());
            tierBadge.setAlignmentX(Component.RIGHT_ALIGNMENT);
            JLabel ptLbl = new JLabel(topupVip.getPoints() + " pts", JLabel.RIGHT);
            ptLbl.setFont(UIUtils.uiFont(Font.PLAIN, 11));
            ptLbl.setForeground(UIUtils.DIM_TEXT);
            ptLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            JLabel earnLbl = new JLabel("1 pt per RM spent", JLabel.RIGHT);
            earnLbl.setFont(UIUtils.uiFont(Font.ITALIC, 10));
            earnLbl.setForeground(UIUtils.DIM_TEXT);
            earnLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            balRight.add(tierBadge);
            balRight.add(Box.createVerticalStrut(3));
            balRight.add(ptLbl);
            balRight.add(earnLbl);
        } else {
            JLabel noVip = new JLabel("No VIP Tier", JLabel.RIGHT);
            noVip.setFont(UIUtils.uiFont(Font.ITALIC, 12));
            noVip.setForeground(UIUtils.DIM_TEXT);
            noVip.setAlignmentX(Component.RIGHT_ALIGNMENT);
            JLabel hint = new JLabel("Enrol at counter to earn pts", JLabel.RIGHT);
            hint.setFont(UIUtils.uiFont(Font.ITALIC, 10));
            hint.setForeground(UIUtils.DIM_TEXT);
            hint.setAlignmentX(Component.RIGHT_ALIGNMENT);
            balRight.add(noVip);
            balRight.add(Box.createVerticalStrut(3));
            balRight.add(hint);
        }
        bannerRef[0].add(balLeft,  BorderLayout.WEST);
        bannerRef[0].add(balRight, BorderLayout.EAST);

        // ══════════════════════════════════════════════════════════════════════
        // BODY  –  LEFT form card  |  RIGHT history card  (GridLayout 1×2)
        // ══════════════════════════════════════════════════════════════════════
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setBackground(UIUtils.DARK_NAVY);

        // ─────────────────────────────────────────────────────────────────────
        // LEFT CARD  –  Top-Up Form
        // ─────────────────────────────────────────────────────────────────────
        JPanel leftCard = new JPanel(new BorderLayout(0, 0));
        leftCard.setBackground(new Color(30, 50, 92));
        leftCard.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 14, 1),
                new EmptyBorder(0, 0, 0, 0)));

        // Left card header bar
        JPanel leftHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 72, 140));
                // Only round top corners
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 14, 14, 14);
                g2.dispose();
            }
        };
        leftHeader.setOpaque(false);
        leftHeader.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel leftHdrLbl = new JLabel("  New Top-Up");
        leftHdrLbl.setFont(UIUtils.uiFont(Font.BOLD, 14));
        leftHdrLbl.setForeground(Color.WHITE);
        // Small wallet dot indicator
        JLabel leftDot = new JLabel("\u25CF");
        leftDot.setFont(UIUtils.uiFont(Font.BOLD, 10));
        leftDot.setForeground(UIUtils.GREEN_OK);
        leftHeader.add(leftDot,   BorderLayout.WEST);
        leftHeader.add(leftHdrLbl, BorderLayout.CENTER);
        leftCard.add(leftHeader, BorderLayout.NORTH);

        // Left card form body  – GridBagLayout centres content vertically
        JPanel formBody = new JPanel(new GridBagLayout());
        formBody.setBackground(new Color(30, 50, 92));

        // Inner form panel (fixed width, centres inside GridBagLayout)
        JPanel formInner = new JPanel();
        formInner.setLayout(new BoxLayout(formInner, BoxLayout.Y_AXIS));
        formInner.setBackground(new Color(30, 50, 92));
        formInner.setMaximumSize(new Dimension(420, Integer.MAX_VALUE));

        // ── Amount input ─────────────────────────────────────────────────────
        JLabel amtLbl = new JLabel("Amount to Top-Up (RM)");
        amtLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        amtLbl.setForeground(UIUtils.LIGHT_GRAY);
        amtLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amountF = new JTextField();
        UIUtils.styleTextField(amountF);
        amountF.setFont(UIUtils.uiFont(Font.BOLD, 20));
        amountF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        amountF.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Live preview ─────────────────────────────────────────────────────
        JLabel previewLbl = new JLabel("RM  0.00", JLabel.CENTER);
        previewLbl.setFont(UIUtils.uiFont(Font.BOLD, 36));
        previewLbl.setForeground(new Color(120, 140, 180));
        previewLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        previewLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        amountF.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { update(); }
            public void removeUpdate(DocumentEvent e)  { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
            void update() {
                try {
                    double v = Double.parseDouble(amountF.getText().trim());
                    previewLbl.setText(String.format("RM  %.2f", v));
                    previewLbl.setForeground(UIUtils.WARN_YELLOW);
                } catch (Exception ex) {
                    previewLbl.setText("RM  0.00");
                    previewLbl.setForeground(new Color(120, 140, 180));
                }
            }
        });

        // ── Payment method ───────────────────────────────────────────────────
        JLabel methodLbl = new JLabel("Payment Method");
        methodLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        methodLbl.setForeground(UIUtils.LIGHT_GRAY);
        methodLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> methodCb = new JComboBox<>(
                new String[]{"Cash", "Debit Card", "Credit Card", "E-Wallet", "Online Transfer"});
        UIUtils.styleCombo(methodCb);
        methodCb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        methodCb.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Confirm button ───────────────────────────────────────────────────
        UIUtils.RoundedButton confirmBtn =
                new UIUtils.RoundedButton("  Confirm Top-Up", UIUtils.SUCCESS_GREEN);
        confirmBtn.setFont(UIUtils.uiFont(Font.BOLD, 13));
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.setPreferredSize(new Dimension(300, 44));

        // Balance label (updated after confirm)
        JLabel[] balLblRef = {new JLabel(String.format(
                "Current Balance: RM %.2f", walletBal), JLabel.CENTER)};
        balLblRef[0].setFont(UIUtils.uiFont(Font.BOLD, 12));
        balLblRef[0].setForeground(walletBal > 0 ? UIUtils.GREEN_OK : UIUtils.DIM_TEXT);
        balLblRef[0].setAlignmentX(Component.CENTER_ALIGNMENT);

        confirmBtn.addActionListener(e -> {
            double amt;
            try { amt = Double.parseDouble(amountF.getText().trim()); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Please enter a valid amount.",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE); return;
            }
            if (amt < 1.0) {
                JOptionPane.showMessageDialog(panel, "Minimum top-up is RM 1.00.",
                        "Too Low", JOptionPane.ERROR_MESSAGE); return;
            }
            int choice = JOptionPane.showConfirmDialog(panel,
                    String.format("Confirm top-up of RM %.2f via %s?", amt, methodCb.getSelectedItem()),
                    "Confirm Top-Up", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;

            Payment pay = FileManager.processCustomerTopup(
                    main.currentCustomer.getUserId(), amt,
                    methodCb.getSelectedItem().toString());
            VipAccount updVip = FileManager.findVipByCustomerId(main.currentCustomer.getUserId());
            String vipMsg = updVip != null
                    ? String.format("\nVIP Points: %d pts  (%s)",
                            updVip.getPoints(), updVip.getTier()) : "";
            JOptionPane.showMessageDialog(panel,
                    String.format("Top-up successful!\nReceipt: %s\nAmount: RM %.2f%s",
                            pay.getReceiptNumber(), amt, vipMsg),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            amountF.setText("");
            previewLbl.setText("RM  0.00");
            previewLbl.setForeground(new Color(120, 140, 180));
            main.showTopup();
        });

        // ── Quick amount shortcuts ────────────────────────────────────────────
        JLabel quickLbl = new JLabel("Quick select:");
        quickLbl.setFont(UIUtils.uiFont(Font.PLAIN, 10));
        quickLbl.setForeground(UIUtils.DIM_TEXT);
        quickLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel quickRow = new JPanel(new GridLayout(1, 4, 8, 0));
        quickRow.setBackground(new Color(30, 50, 92));
        quickRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        quickRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int amt2 : new int[]{10, 20, 50, 100}) {
            UIUtils.RoundedButton qb = new UIUtils.RoundedButton(
                    "RM " + amt2, new Color(40, 68, 130));
            qb.setFont(UIUtils.uiFont(Font.BOLD, 11));
            qb.addActionListener(e -> amountF.setText(String.valueOf(amt2)));
            quickRow.add(qb);
        }

        // Thin divider
        JSeparator fSep = new JSeparator();
        fSep.setForeground(new Color(60, 90, 150));
        fSep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        fSep.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Assemble formInner
        formInner.add(amtLbl);
        formInner.add(Box.createVerticalStrut(6));
        formInner.add(amountF);
        formInner.add(Box.createVerticalStrut(10));
        formInner.add(previewLbl);
        formInner.add(Box.createVerticalStrut(10));
        formInner.add(fSep);
        formInner.add(Box.createVerticalStrut(12));
        formInner.add(methodLbl);
        formInner.add(Box.createVerticalStrut(6));
        formInner.add(methodCb);
        formInner.add(Box.createVerticalStrut(16));
        formInner.add(quickLbl);
        formInner.add(Box.createVerticalStrut(5));
        formInner.add(quickRow);
        formInner.add(Box.createVerticalStrut(18));
        formInner.add(confirmBtn);
        formInner.add(Box.createVerticalStrut(8));
        formInner.add(balLblRef[0]);

        // Centre formInner inside formBody via GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(20, 28, 20, 28);
        formBody.add(formInner, gbc);

        leftCard.add(formBody, BorderLayout.CENTER);

        // ─────────────────────────────────────────────────────────────────────
        // RIGHT CARD  –  History
        // ─────────────────────────────────────────────────────────────────────
        JPanel rightCard = new JPanel(new BorderLayout(0, 0));
        rightCard.setBackground(new Color(30, 50, 92));
        rightCard.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 14, 1),
                new EmptyBorder(0, 0, 0, 0)));

        // Right card header bar
        JPanel rightHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 72, 140));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 14, 14, 14);
                g2.dispose();
            }
        };
        rightHeader.setOpaque(false);
        rightHeader.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel rightHdrLbl = new JLabel("  Transaction History");
        rightHdrLbl.setFont(UIUtils.uiFont(Font.BOLD, 14));
        rightHdrLbl.setForeground(Color.WHITE);
        JLabel rightDot = new JLabel("\u25CF");
        rightDot.setFont(UIUtils.uiFont(Font.BOLD, 10));
        rightDot.setForeground(UIUtils.ACCENT_BLUE);
        rightHeader.add(rightDot,    BorderLayout.WEST);
        rightHeader.add(rightHdrLbl, BorderLayout.CENTER);

        // Stat pills in right header
        List<Payment> topups = new ArrayList<>();
        for (Payment p : FileManager.readAllPayments()) {
            if (!p.getCustomerId().equals(main.currentCustomer.getUserId())) continue;
            if (!p.getAppointmentId().startsWith("TOPUP-")) continue;
            topups.add(p);
        }
        topups.sort((a, b) -> b.getPaymentDate().compareTo(a.getPaymentDate()));
        double totalLoaded = topups.stream().mapToDouble(Payment::getAmount).sum();

        JPanel statPills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        statPills.setOpaque(false);
        statPills.add(buildHistStatPill(String.valueOf(topups.size()),
                "records", UIUtils.ACCENT_BLUE));
        statPills.add(buildHistStatPill(
                String.format("RM %.0f", totalLoaded), "loaded", UIUtils.GREEN_OK));
        rightHeader.add(statPills, BorderLayout.EAST);
        rightCard.add(rightHeader, BorderLayout.NORTH);

        // History table
        DefaultTableModel histModel = new DefaultTableModel(
                new String[]{"Receipt No.", "Amount (RM)", "Method", "Date"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Payment p : topups) {
            histModel.addRow(new Object[]{
                p.getReceiptNumber(),
                String.format("RM %.2f", p.getAmount()),
                p.getPaymentMethod(),
                p.getPaymentDate()
            });
        }

        JTable histTable = UIUtils.createStyledTable(histModel);
        histTable.setRowHeight(32);
        int[] histWidths = {130, 110, 130, 110};
        for (int i = 0; i < histWidths.length; i++)
            histTable.getColumnModel().getColumn(i).setPreferredWidth(histWidths[i]);

        JScrollPane histScroll = new JScrollPane(histTable);
        UIUtils.styleScrollPane(histScroll);

        JPanel histBody = new JPanel(new BorderLayout());
        histBody.setBackground(new Color(30, 50, 92));
        histBody.setBorder(new EmptyBorder(12, 14, 14, 14));

        if (topups.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(new Color(30, 50, 92));
            JPanel emptyInner = new JPanel();
            emptyInner.setLayout(new BoxLayout(emptyInner, BoxLayout.Y_AXIS));
            emptyInner.setBackground(new Color(30, 50, 92));
            JLabel emptyIcon = new JLabel("\uD83D\uDCB3", JLabel.CENTER);
            emptyIcon.setFont(new Font("Dialog", Font.PLAIN, 40));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel emptyLbl = new JLabel("No transactions yet", JLabel.CENTER);
            emptyLbl.setFont(UIUtils.uiFont(Font.ITALIC, 13));
            emptyLbl.setForeground(UIUtils.DIM_TEXT);
            emptyLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel emptyHint = new JLabel("Top-up to see your history here", JLabel.CENTER);
            emptyHint.setFont(UIUtils.uiFont(Font.PLAIN, 11));
            emptyHint.setForeground(new Color(100, 130, 175));
            emptyHint.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyInner.add(Box.createVerticalStrut(20));
            emptyInner.add(emptyIcon);
            emptyInner.add(Box.createVerticalStrut(12));
            emptyInner.add(emptyLbl);
            emptyInner.add(Box.createVerticalStrut(6));
            emptyInner.add(emptyHint);
            empty.add(emptyInner);
            histBody.add(empty, BorderLayout.CENTER);
        } else {
            histBody.add(histScroll, BorderLayout.CENTER);
        }
        rightCard.add(histBody, BorderLayout.CENTER);

        // Right card footer  – balance summary strip
        JPanel rightFooter = new JPanel(new GridLayout(1, 3, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(24, 40, 80));
                g2.fillRoundRect(0, -14, getWidth(), getHeight() + 14, 14, 14);
                g2.dispose();
            }
        };
        rightFooter.setOpaque(false);
        rightFooter.setBorder(new EmptyBorder(10, 16, 10, 16));
        rightFooter.add(main.buildFooterStat("Total Records",
                String.valueOf(topups.size()), UIUtils.ACCENT_BLUE));
        rightFooter.add(main.buildFooterStat("Total Loaded",
                String.format("RM %.2f", totalLoaded), UIUtils.GREEN_OK));
        rightFooter.add(main.buildFooterStat("Wallet Balance",
                String.format("RM %.2f", walletBal), UIUtils.WARN_YELLOW));
        rightCard.add(rightFooter, BorderLayout.SOUTH);

        // ── Assemble body ────────────────────────────────────────────────────
        body.add(leftCard);
        body.add(rightCard);

        // ── Outer wrapper: banner on top, body below ─────────────────────────
        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(UIUtils.DARK_NAVY);
        content.add(bannerRef[0], BorderLayout.NORTH);
        content.add(body,         BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    /** Tiny pill label used in the history header stat row. */
    JPanel buildHistStatPill(String value, String label, Color accent) {
        JPanel pill = new JPanel(new GridLayout(2, 1, 0, 1));
        pill.setOpaque(false);
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 1, true),
                new EmptyBorder(2, 8, 2, 8)));
        JLabel vl = new JLabel(value, JLabel.CENTER);
        vl.setFont(UIUtils.uiFont(Font.BOLD, 11)); vl.setForeground(accent);
        JLabel ll = new JLabel(label, JLabel.CENTER);
        ll.setFont(UIUtils.uiFont(Font.PLAIN, 9)); ll.setForeground(UIUtils.DIM_TEXT);
        pill.add(vl); pill.add(ll);
        return pill;
    }


}
