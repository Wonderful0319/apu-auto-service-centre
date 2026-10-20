package asc.gui.technician;

import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class TechnicianInventoryPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final TechnicianDashboard main;

    public TechnicianInventoryPage(TechnicianDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.DARK_NAVY);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(main.sectionTitle("Parts Inventory", TechnicianDashboard.ICON_BASE + "Parts Inventory(Blue).png"), BorderLayout.NORTH);

        JPanel infoBanner = new JPanel(new BorderLayout());
        infoBanner.setBackground(new Color(30, 55, 90));
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 10, 1),
                new EmptyBorder(10, 16, 10, 16)));
        JLabel infoLbl = new JLabel(
                "ℹ  Read-only view. Stock is automatically deducted when you mark appointments as Completed. Contact your manager to restock.");
        infoLbl.setForeground(new Color(160, 200, 255));
        infoLbl.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        infoBanner.add(infoLbl, BorderLayout.CENTER);

        String[] cols = {"Part ID", "Name", "Category", "In Stock", "Threshold", "Status", "Price (RM)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIUtils.createStyledTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {80, 160, 110, 75, 90, 130, 90};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.setRowHeight(30);

        table.getColumnModel().getColumn(5).setCellRenderer(
            new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    String s   = v == null ? "" : v.toString();
                    boolean low = s.contains("LOW");
                    setText(low ? "\u26A0 " + s : "\u2705 " + s);
                    setForeground(low ? TechnicianDashboard.ACCENT_YELLOW : TechnicianDashboard.GREEN_OK);
                    setBackground(sel ? UIUtils.ACCENT_BLUE : UIUtils.TABLE_BG);
                    setFont(UIUtils.uiSymbolFont(Font.BOLD, 11));
                    setHorizontalAlignment(CENTER);
                    return this;
                }
            });

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        Runnable refresh = () -> {
            model.setRowCount(0);
            for (asc.model.CarPart p : FileManager.readAllCarParts()) {
                String status = p.isLowStock()
                        ? "LOW STOCK (" + p.getStockQuantity() + ")"
                        : "OK";
                model.addRow(new Object[]{
                    p.getPartId(), p.getName(), p.getCategory(),
                    p.getStockQuantity(), p.getLowStockThreshold(),
                    status,
                    String.format("%.2f", p.getPrice())
                });
            }
        };
        refresh.run();

        JScrollPane scroll = new JScrollPane(table);
        UIUtils.styleScrollPane(scroll);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(UIUtils.LIGHT_GRAY);
        searchLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        JTextField searchField = new JTextField(16);
        UIUtils.styleTextField(searchField);
        searchField.setToolTipText("Search by part name, ID or category");

        JLabel statusLbl = new JLabel("Status:");
        statusLbl.setForeground(UIUtils.LIGHT_GRAY);
        statusLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "Low Stock", "OK"});
        UIUtils.styleCombo(statusFilter);

        UIUtils.RoundedButton clearBtn = new UIUtils.RoundedButton("✖ Clear", new Color(180, 60, 60));
        clearBtn.setPreferredSize(new Dimension(90, 30));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            sorter.setRowFilter(null);
        });

        Runnable applyFilter = () -> {
            String kw = searchField.getText().trim();
            String sf = (String) statusFilter.getSelectedItem();
            java.util.List<RowFilter<Object,Object>> filters = new ArrayList<>();
            if (!kw.isEmpty()) {
                try {
                    java.util.List<RowFilter<Object,Object>> or = new ArrayList<>();
                    or.add(RowFilter.regexFilter("(?i)" + kw, 0));
                    or.add(RowFilter.regexFilter("(?i)" + kw, 1));
                    or.add(RowFilter.regexFilter("(?i)" + kw, 2));
                    filters.add(RowFilter.orFilter(or));
                } catch (java.util.regex.PatternSyntaxException ignored) {}
            }
            if ("Low Stock".equals(sf))
                filters.add(RowFilter.regexFilter("(?i)LOW", 5));
            else if ("OK".equals(sf))
                filters.add(RowFilter.regexFilter("(?i)^OK$", 5));
            sorter.setRowFilter(filters.isEmpty() ? null
                    : filters.size() == 1 ? filters.get(0)
                    : RowFilter.andFilter(filters));
        };

        statusFilter.addActionListener(e -> applyFilter.run());
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilter.run(); }
        });

        UIUtils.RoundedButton alertBtn  = new UIUtils.RoundedButton("\u26A0 View Low Stock", new Color(200, 120, 40));
        UIUtils.RoundedButton refreshBtn = new UIUtils.RoundedButton("\u21BB Refresh",       new Color(60, 90, 150));
        alertBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        refreshBtn.setFont(UIUtils.uiSymbolFont(Font.BOLD, 12));
        alertBtn .setPreferredSize(new Dimension(160, 30));
        refreshBtn.setPreferredSize(new Dimension(100, 30));

        alertBtn.addActionListener(e -> {
            java.util.List<asc.model.CarPart> lowParts = FileManager.readAllCarParts().stream()
                    .filter(asc.model.CarPart::isLowStock)
                    .collect(java.util.stream.Collectors.toList());
            if (lowParts.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "\u2705  All parts are well-stocked!",
                        "Inventory Status", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("Low Stock Parts:\n\n");
            for (asc.model.CarPart p : lowParts)
                sb.append("  \u26a0  ").append(p.getName())
                  .append(" (Qty: ").append(p.getStockQuantity())
                  .append(", Threshold: ").append(p.getLowStockThreshold()).append(")\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setBackground(new Color(50, 20, 20));
            ta.setForeground(new Color(255, 180, 60));
            ta.setBorder(new EmptyBorder(10, 14, 10, 14));
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(460, 220));
            UIUtils.styleScrollPane(sp);
            JOptionPane.showMessageDialog(this, sp,
                    "\u26a0  Low Stock Alert", JOptionPane.WARNING_MESSAGE);
        });

        refreshBtn.addActionListener(e -> {
            refresh.run();
            sorter.setRowFilter(null);
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        filterRow.setBackground(UIUtils.DARK_NAVY);
        filterRow.add(searchLbl); filterRow.add(searchField);
        filterRow.add(statusLbl); filterRow.add(statusFilter);
        filterRow.add(clearBtn);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(alertBtn); filterRow.add(refreshBtn);

        java.util.List<asc.model.CarPart> allParts = FileManager.readAllCarParts();
        long lowCount = allParts.stream().filter(asc.model.CarPart::isLowStock).count();
        JPanel summary = new JPanel(new GridLayout(1, 3, 10, 0));
        summary.setOpaque(false);
        summary.setBorder(new EmptyBorder(8, 0, 0, 0));
        summary.add(main.miniCard("Total Parts",   String.valueOf(allParts.size()),       UIUtils.ACCENT_BLUE));
        summary.add(main.miniCard("Low Stock",     String.valueOf(lowCount),              lowCount > 0 ? TechnicianDashboard.ACCENT_YELLOW : TechnicianDashboard.GREEN_OK));
        summary.add(main.miniCard("Healthy Stock", String.valueOf(allParts.size() - lowCount), TechnicianDashboard.GREEN_OK));

        JPanel topSection = new JPanel(new BorderLayout(0, 8));
        topSection.setOpaque(false);
        topSection.add(infoBanner, BorderLayout.NORTH);
        topSection.add(filterRow,  BorderLayout.SOUTH);

        JPanel centre = new JPanel(new BorderLayout(0, 6));
        centre.setBackground(UIUtils.DARK_NAVY);
        centre.add(topSection, BorderLayout.NORTH);
        centre.add(scroll,     BorderLayout.CENTER);
        centre.add(summary,    BorderLayout.SOUTH);

        panel.add(centre, BorderLayout.CENTER);
        return panel;
    }


}
