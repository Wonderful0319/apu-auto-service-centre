package asc.gui.manager;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.*;
import java.util.List;

public class ManagerReportsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final ManagerDashboard main;

    public ManagerReportsPage(ManagerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));
        panel.add(main.sectionTitle("Management Reports", ManagerDashboard.MGR_ICON_BASE + "Reports(Blue).png"), BorderLayout.NORTH);

        // ── Date range picker bar ─────────────────────────────────────────────
        String[] months = {
            "2024-01","2024-02","2024-03","2024-04","2024-05","2024-06",
            "2024-07","2024-08","2024-09","2024-10","2024-11","2024-12",
            "2025-01","2025-02","2025-03","2025-04","2025-05","2025-06",
            "2025-07","2025-08","2025-09","2025-10","2025-11","2025-12",
            "2026-01","2026-02","2026-03","2026-04","2026-05","2026-06",
            "2026-07","2026-08","2026-09","2026-10","2026-11","2026-12"
        };
        JComboBox<String> fromCb = new JComboBox<>(months);
        JComboBox<String> toCb   = new JComboBox<>(months);
        UIUtils.styleCombo(fromCb); UIUtils.styleCombo(toCb);

        // Default: current month
        String curMonth = LocalDate.now().toString().substring(0, 7);
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(curMonth)) { fromCb.setSelectedIndex(i); toCb.setSelectedIndex(i); break; }
        }

        JLabel fromLbl = new JLabel("From:"); fromLbl.setForeground(UIUtils.WHITE); fromLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        JLabel toLbl   = new JLabel("To:");   toLbl.setForeground(UIUtils.WHITE);   toLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));

        UIUtils.RoundedButton genBtn  = new UIUtils.RoundedButton("Generate Report",  UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton pdfBtn  = new UIUtils.RoundedButton("Export PDF",        new Color(160, 60, 200));
        UIUtils.RoundedButton fullBtn = new UIUtils.RoundedButton("All-Time Report",   UIUtils.ACCENT_BLUE);
        UIUtils.RoundedButton clrBtn  = new UIUtils.RoundedButton("Clear",             UIUtils.MEDIUM_NAVY);

        genBtn .setPreferredSize(new Dimension(165, 34));
        pdfBtn .setPreferredSize(new Dimension(130, 34));
        fullBtn.setPreferredSize(new Dimension(155, 34));
        clrBtn .setPreferredSize(new Dimension(90,  34));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterBar.setBackground(new Color(24, 40, 82));
        filterBar.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1),
                new EmptyBorder(6, 10, 6, 10)));
        filterBar.add(fromLbl); filterBar.add(fromCb);
        filterBar.add(toLbl);   filterBar.add(toCb);
        filterBar.add(genBtn);  filterBar.add(fullBtn);
        filterBar.add(pdfBtn);  filterBar.add(clrBtn);

        // ── Report text area ────────────────────────────────────────────────
        main.reportsArea = new JTextArea();
        main.reportsArea.setEditable(false);
        main.reportsArea.setBackground(UIUtils.TABLE_BG);
        main.reportsArea.setForeground(UIUtils.WHITE);
        main.reportsArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        main.reportsArea.setBorder(new EmptyBorder(12, 18, 12, 18));
        JScrollPane sp = new JScrollPane(main.reportsArea);
        UIUtils.styleScrollPane(sp);

        // ── Wire up buttons ─────────────────────────────────────────────────
        genBtn.addActionListener(e -> {
            String from = (String) fromCb.getSelectedItem();
            String to   = (String) toCb.getSelectedItem();
            if (from == null || to == null) return;
            if (from.compareTo(to) > 0) {
                JOptionPane.showMessageDialog(this, "'From' month cannot be later than 'To' month.",
                        "Invalid Range", JOptionPane.WARNING_MESSAGE); return;
            }
            generateFilteredReport(from, to);
        });

        fullBtn.addActionListener(e -> generateFullReport());
        clrBtn .addActionListener(e -> main.reportsArea.setText(""));
        pdfBtn .addActionListener(e -> exportReportToPdf());

        // Auto-generate on load
        generateFilteredReport(curMonth, curMonth);

        JPanel centre = new JPanel(new BorderLayout(0, 8));
        centre.setBackground(UIUtils.DARK_NAVY);
        centre.add(filterBar, BorderLayout.NORTH);
        centre.add(sp,        BorderLayout.CENTER);

        panel.add(centre, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Generates a report filtered to the given yyyy-MM month range (inclusive).
     * E.g. from="2025-09" to="2025-11" covers Sep, Oct, Nov 2025.
     */
    void generateFilteredReport(String fromMonth, String toMonth) {
        List<asc.model.Appointment> allApts = FileManager.readAllAppointments();
        List<Payment>               allPays = FileManager.readAllPayments();
        List<Technician>            techs   = FileManager.readAllTechnicians();
        ServicePrice                pr      = FileManager.readPrices();

        // Filter appointments whose date falls within [fromMonth, toMonth]
        List<asc.model.Appointment> apts = allApts.stream()
                .filter(a -> {
                    if (a.getDate() == null || a.getDate().length() < 7) return false;
                    String m = a.getDate().substring(0, 7);
                    return m.compareTo(fromMonth) >= 0 && m.compareTo(toMonth) <= 0;
                })
                .collect(java.util.stream.Collectors.toList());

        // Filter payments whose date falls within range
        List<Payment> pays = allPays.stream()
                .filter(p -> {
                    if (p.getPaymentDate() == null || p.getPaymentDate().length() < 7) return false;
                    String m = p.getPaymentDate().substring(0, 7);
                    return m.compareTo(fromMonth) >= 0 && m.compareTo(toMonth) <= 0;
                })
                .collect(java.util.stream.Collectors.toList());

        long pending   = apts.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus())).count();
        long completed = apts.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long nc        = apts.stream().filter(a -> "NORMAL".equalsIgnoreCase(a.getServiceType())).count();
        long mc        = apts.stream().filter(a -> "MAJOR".equalsIgnoreCase(a.getServiceType())).count();
        double total   = pays.stream().mapToDouble(Payment::getAmount).sum();
        double nr      = pays.stream().filter(p -> {
            asc.model.Appointment a = FileManager.findAppointmentById(p.getAppointmentId());
            return a != null && "NORMAL".equalsIgnoreCase(a.getServiceType());
        }).mapToDouble(Payment::getAmount).sum();

        String rangeLabel = fromMonth.equals(toMonth)
                ? fromMonth
                : fromMonth + "  to  " + toMonth;

        int lineW = 60;
        String border = "=" .repeat(lineW);
        String dash   = "-" .repeat(lineW);

        StringBuilder sb = new StringBuilder();
        sb.append(border).append("\n");
        sb.append(centre("APU AUTOMOTIVE SERVICE CENTRE (APU-ASC)", lineW)).append("\n");
        sb.append(centre("MANAGEMENT REPORT", lineW)).append("\n");
        sb.append(centre("Period: " + rangeLabel, lineW)).append("\n");
        sb.append(border).append("\n\n");

        sb.append(dash).append("\n");
        sb.append(" APPOINTMENT STATISTICS\n");
        sb.append(dash).append("\n");
        sb.append(String.format("  Total       : %d\n", apts.size()));
        sb.append(String.format("  Pending     : %d\n", pending));
        sb.append(String.format("  Completed   : %d\n", completed));
        sb.append(String.format("  Normal (1hr): %d\n", nc));
        sb.append(String.format("  Major  (3hr): %d\n\n", mc));

        sb.append(dash).append("\n");
        sb.append(" FINANCIAL SUMMARY\n");
        sb.append(dash).append("\n");
        sb.append(String.format("  Service Prices  : Normal RM %.2f  |  Major RM %.2f\n",
                pr.getNormalServicePrice(), pr.getMajorServicePrice()));
        sb.append(String.format("  Revenue (Normal): RM %.2f\n", nr));
        sb.append(String.format("  Revenue (Major) : RM %.2f\n", total - nr));
        sb.append(String.format("  TOTAL REVENUE   : RM %.2f\n\n", total));

        // Monthly breakdown (if multi-month)
        if (!fromMonth.equals(toMonth)) {
            sb.append(dash).append("\n");
            sb.append(" MONTHLY REVENUE BREAKDOWN\n");
            sb.append(dash).append("\n");
            java.util.TreeMap<String, Double> monthly = new java.util.TreeMap<>();
            for (Payment p : pays) {
                String m = p.getPaymentDate().substring(0, 7);
                monthly.merge(m, p.getAmount(), Double::sum);
            }
            if (monthly.isEmpty()) {
                sb.append("  (No payments in this period)\n");
            } else {
                for (java.util.Map.Entry<String, Double> en : monthly.entrySet())
                    sb.append(String.format("  %s  :  RM %.2f\n", en.getKey(), en.getValue()));
            }
            sb.append("\n");
        }

        sb.append(dash).append("\n");
        sb.append(" TECHNICIAN PERFORMANCE\n");
        sb.append(dash).append("\n");
        for (Technician t : techs) {
            long tt = apts.stream().filter(a -> a.getTechnicianId().equals(t.getUserId())).count();
            long tc = apts.stream().filter(a -> a.getTechnicianId().equals(t.getUserId())
                    && "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
            sb.append(String.format("  %-22s : %d assigned, %d completed\n", t.getName(), tt, tc));
        }
        sb.append("\n");

        long fbCount  = FileManager.readAllFeedbacks().stream().filter(f -> {
            asc.model.Appointment a = FileManager.findAppointmentById(f.getAppointmentId());
            if (a == null || a.getDate() == null || a.getDate().length() < 7) return false;
            String m = a.getDate().substring(0, 7);
            return m.compareTo(fromMonth) >= 0 && m.compareTo(toMonth) <= 0;
        }).count();
        long cmtCount = FileManager.readAllComments().stream().filter(c -> {
            asc.model.Appointment a = FileManager.findAppointmentById(c.getAppointmentId());
            if (a == null || a.getDate() == null || a.getDate().length() < 7) return false;
            String m = a.getDate().substring(0, 7);
            return m.compareTo(fromMonth) >= 0 && m.compareTo(toMonth) <= 0;
        }).count();
        sb.append(String.format("  Feedbacks: %d  |  Customer Comments: %d\n", fbCount, cmtCount));
        sb.append("\n").append(border).append("\n");
        sb.append(String.format("  Report generated: %s  |  By: %s (%s)\n",
                LocalDate.now(), main.currentManager.getName(), main.currentManager.getUserId()));
        sb.append(border).append("\n");

        main.reportsArea.setText(sb.toString());
        main.reportsArea.setCaretPosition(0);
    }

    /** All-time report (no date filter). */
    void generateFullReport() {
        List<asc.model.Appointment> apts = FileManager.readAllAppointments();
        List<Payment> pays               = FileManager.readAllPayments();
        List<Technician> techs           = FileManager.readAllTechnicians();
        List<CounterStaff> staffs        = FileManager.readAllCounterStaff();
        List<Customer> custs             = FileManager.readAllCustomers();
        ServicePrice pr                  = FileManager.readPrices();

        long pending   = apts.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus())).count();
        long completed = apts.stream().filter(a -> "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
        long nc        = apts.stream().filter(a -> "NORMAL".equalsIgnoreCase(a.getServiceType())).count();
        long mc        = apts.stream().filter(a -> "MAJOR".equalsIgnoreCase(a.getServiceType())).count();
        double total   = pays.stream().mapToDouble(Payment::getAmount).sum();
        double nr      = pays.stream().filter(p -> {
            asc.model.Appointment a = FileManager.findAppointmentById(p.getAppointmentId());
            return a != null && "NORMAL".equalsIgnoreCase(a.getServiceType());
        }).mapToDouble(Payment::getAmount).sum();
        long pendingRegs = FileManager.readAllPendingRegistrations().stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long partsCount  = FileManager.readAllCarParts().size();

        int lineW    = 60;
        String border = "=".repeat(lineW);
        String dash   = "-".repeat(lineW);

        StringBuilder sb = new StringBuilder();
        sb.append(border).append("\n");
        sb.append(centre("APU AUTOMOTIVE SERVICE CENTRE (APU-ASC)", lineW)).append("\n");
        sb.append(centre("MANAGEMENT REPORT  —  ALL TIME", lineW)).append("\n");
        sb.append(border).append("\n\n");

        sb.append(dash).append("\n");
        sb.append(" SYSTEM OVERVIEW\n");
        sb.append(dash).append("\n");
        sb.append(String.format("  Managers: %d  |  Counter Staff: %d  |  Technicians: %d  |  Customers: %d\n",
                FileManager.readAllManagers().size(), staffs.size(), techs.size(), custs.size()));
        sb.append(String.format("  Pending Registrations: %d  |  Car Parts: %d\n\n", pendingRegs, partsCount));

        sb.append(dash).append("\n");
        sb.append(" APPOINTMENT STATISTICS\n");
        sb.append(dash).append("\n");
        sb.append(String.format("  Total: %d  |  Pending: %d  |  Completed: %d\n", apts.size(), pending, completed));
        sb.append(String.format("  Normal (1hr): %d  |  Major (3hr): %d\n\n", nc, mc));

        sb.append(dash).append("\n");
        sb.append(" FINANCIAL SUMMARY\n");
        sb.append(dash).append("\n");
        sb.append(String.format("  Normal Price: RM %.2f  |  Major Price: RM %.2f\n",
                pr.getNormalServicePrice(), pr.getMajorServicePrice()));
        sb.append(String.format("  Revenue (Normal): RM %.2f  |  Revenue (Major): RM %.2f\n", nr, total - nr));
        sb.append(String.format("  TOTAL REVENUE: RM %.2f\n\n", total));

        sb.append(dash).append("\n");
        sb.append(" TECHNICIAN PERFORMANCE\n");
        sb.append(dash).append("\n");
        for (Technician t : techs) {
            long tt = apts.stream().filter(a -> a.getTechnicianId().equals(t.getUserId())).count();
            long tc = apts.stream().filter(a -> a.getTechnicianId().equals(t.getUserId())
                    && "COMPLETED".equalsIgnoreCase(a.getStatus())).count();
            sb.append(String.format("  %-22s : %d assigned, %d completed\n", t.getName(), tt, tc));
        }
        sb.append(String.format("\n  Feedbacks: %d  |  Customer Comments: %d\n",
                FileManager.readAllFeedbacks().size(), FileManager.readAllComments().size()));
        sb.append("\n").append(border).append("\n");
        sb.append(String.format("  Report generated: %s  |  By: %s (%s)\n",
                LocalDate.now(), main.currentManager.getName(), main.currentManager.getUserId()));
        sb.append(border).append("\n");

        main.reportsArea.setText(sb.toString());
        main.reportsArea.setCaretPosition(0);
    }

    /** Centre-pads a string within a fixed width for the report header. */
    String centre(String text, int width) {
        if (text.length() >= width) return text;
        int pad = (width - text.length()) / 2;
        return " ".repeat(pad) + text;
    }

    /** Export current report text to a PDF file chosen by the user. */
    void exportReportToPdf() {
        if (main.reportsArea == null || main.reportsArea.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Generate a report first.", "No Report", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Report as PDF");
        fc.setSelectedFile(new java.io.File("APU_ASC_Report_" + LocalDate.now() + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File dest = fc.getSelectedFile();
        if (!dest.getName().toLowerCase().endsWith(".pdf"))
            dest = new java.io.File(dest.getAbsolutePath() + ".pdf");

        final java.io.File finalDest = dest;
        final String text = main.reportsArea.getText();

        // Use Java's built-in print machinery to produce a PDF
        // We create an in-memory printable from the text area and print to file
        try {
            // Attempt PDF via javax.print (works when a PDF print service is available)
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setJobName("APU-ASC Report");

            java.awt.print.PageFormat pf = job.defaultPage();
            java.awt.print.Paper paper = pf.getPaper();
            double margin = 36; // 0.5 inch margins
            paper.setImageableArea(margin, margin,
                    paper.getWidth() - margin * 2, paper.getHeight() - margin * 2);
            pf.setPaper(paper);

            // Printable that draws monospaced text line-by-line
            java.awt.print.Printable printable = (graphics, pageFormat, pageIndex) -> {
                String[] lines = text.split("\n", -1);
                Font font = new Font("Monospaced", Font.PLAIN, 11);
                graphics.setFont(font);
                java.awt.FontMetrics fm = graphics.getFontMetrics(font);
                int lineH   = fm.getHeight();
                int x       = (int) pageFormat.getImageableX();
                int y0      = (int) pageFormat.getImageableY();
                int pageH   = (int) pageFormat.getImageableHeight();
                int linesPerPage = pageH / lineH;
                int start   = pageIndex * linesPerPage;
                if (start >= lines.length) return java.awt.print.Printable.NO_SUCH_PAGE;
                int y = y0 + fm.getAscent();
                for (int i = start; i < Math.min(start + linesPerPage, lines.length); i++) {
                    graphics.drawString(lines[i], x, y);
                    y += lineH;
                }
                return java.awt.print.Printable.PAGE_EXISTS;
            };

            job.setPrintable(printable, pf);

            // Try to find a PDF print service
            javax.print.PrintService[] services = javax.print.PrintServiceLookup.lookupPrintServices(
                    javax.print.DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
            javax.print.PrintService pdfService = null;
            for (javax.print.PrintService svc : services) {
                if (svc.getName().toLowerCase().contains("pdf")) { pdfService = svc; break; }
            }

            if (pdfService != null) {
                // Use the OS PDF printer
                job.setPrintService(pdfService);
                javax.print.attribute.PrintRequestAttributeSet attrs =
                        new javax.print.attribute.HashPrintRequestAttributeSet();
                attrs.add(new javax.print.attribute.standard.Destination(finalDest.toURI()));
                job.print(attrs);
            } else {
                // Fallback: write plain-text .pdf (rename) with a header
                try (java.io.PrintWriter pw = new java.io.PrintWriter(
                        new java.io.FileWriter(finalDest))) {
                    pw.print(text);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Report exported to:\n" + finalDest.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            // Final fallback: write as plain text with .pdf extension
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(finalDest))) {
                pw.print(text);
                JOptionPane.showMessageDialog(this,
                        "PDF printer not found. Report saved as text:\n" + finalDest.getAbsolutePath(),
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ioEx) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ioEx.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}
