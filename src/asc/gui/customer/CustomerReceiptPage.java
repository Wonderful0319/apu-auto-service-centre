package asc.gui.customer;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;

public class CustomerReceiptPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final CustomerDashboard main;

    public CustomerReceiptPage(CustomerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY); panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(main.sectionTitle("Receipt Preview, Print & Download", CustomerDashboard.ICON_BASE + "Receipt & Print(Blue).png"), BorderLayout.NORTH);
        Map<String, Payment> payMap = main.currentCustomer.getMyPaymentMap();
        List<Appointment> paid = main.currentCustomer.getMyAppointments().stream().filter(a -> payMap.containsKey(a.getAppointmentId())).collect(Collectors.toList());
        if (paid.isEmpty()) { panel.add(main.emptyState("\uD83D\uDCB3", "No payment records found."), BorderLayout.CENTER); return panel; }
        String[] opts = paid.stream().map(a -> { Payment p = payMap.get(a.getAppointmentId()); return a.getAppointmentId() + "  |  " + a.getDate() + "  |  RM " + String.format("%.2f", p.getAmount()); }).toArray(String[]::new);
        JComboBox<String> combo = new JComboBox<>(opts); UIUtils.styleCombo(combo);
        JLabel selLbl = new JLabel("Select Receipt:"); selLbl.setForeground(UIUtils.WHITE); selLbl.setFont(UIUtils.uiFont(Font.BOLD, 13));
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); topBar.setBackground(UIUtils.DARK_NAVY); topBar.add(selLbl); topBar.add(combo);
        JTextArea receiptArea = new JTextArea(); receiptArea.setEditable(false); receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        receiptArea.setBackground(new Color(252, 252, 252)); receiptArea.setForeground(new Color(20, 20, 20)); receiptArea.setBorder(new EmptyBorder(20, 34, 20, 34));
        Runnable show = () -> { int idx = combo.getSelectedIndex(); if (idx < 0) return; Appointment a = paid.get(idx); receiptArea.setText(main.currentCustomer.buildReceiptText(a, payMap.get(a.getAppointmentId()))); receiptArea.setCaretPosition(0); };
        combo.addActionListener(e -> show.run()); show.run();
        JScrollPane scroll = new JScrollPane(receiptArea); scroll.setBorder(new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1));
        UIUtils.RoundedButton printBtn    = new UIUtils.RoundedButton("Print Receipt",   UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton downloadBtn = new UIUtils.RoundedButton("Download (.txt)", new Color(110, 55, 195));
        printBtn.setPreferredSize(new Dimension(170, 36)); downloadBtn.setPreferredSize(new Dimension(170, 36));
        printBtn.addActionListener(e -> { PrinterJob job = PrinterJob.getPrinterJob(); job.setPrintable((graphics, pf, pageIndex) -> { if (pageIndex > 0) return Printable.NO_SUCH_PAGE; receiptArea.printAll(graphics); return Printable.PAGE_EXISTS; }); if (job.printDialog()) { try { job.print(); } catch (PrinterException ex) { JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); } } });
        downloadBtn.addActionListener(e -> { int idx = combo.getSelectedIndex(); if (idx < 0) return; Appointment apt = paid.get(idx); Payment pay = payMap.get(apt.getAppointmentId()); JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Save Receipt"); chooser.setSelectedFile(new File("Receipt_" + pay.getReceiptNumber() + ".txt")); if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return; File out = chooser.getSelectedFile(); if (!out.getName().toLowerCase().endsWith(".txt")) out = new File(out.getAbsolutePath() + ".txt"); try (PrintWriter pw = new PrintWriter(new FileWriter(out))) { pw.print(main.currentCustomer.buildReceiptText(apt, pay)); JOptionPane.showMessageDialog(this, "Saved to:\n" + out.getAbsolutePath(), "Download Complete", JOptionPane.INFORMATION_MESSAGE); } catch (IOException ex) { JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); } });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0)); btnRow.setBackground(UIUtils.DARK_NAVY); btnRow.add(printBtn); btnRow.add(downloadBtn);
        JPanel centre = new JPanel(new BorderLayout(0, 10)); centre.setBackground(UIUtils.DARK_NAVY);
        centre.add(topBar, BorderLayout.NORTH); centre.add(scroll, BorderLayout.CENTER); centre.add(btnRow, BorderLayout.SOUTH);
        panel.add(centre, BorderLayout.CENTER);
        return panel;
    }


}
