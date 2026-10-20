package asc.gui.manager;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.List;

public class ManagerPartsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final ManagerDashboard main;

    public ManagerPartsPage(ManagerDashboard m) {
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
        panel.add(main.sectionTitle("Car Parts Catalogue", ManagerDashboard.MGR_ICON_BASE + "Car Parts Catalogue(Blue).png"), BorderLayout.NORTH);

        // Clean table: only essential columns visible on dashboard
        String[] cols = {"Part ID", "Part Name", "Category", "Price (RM)", "Stock Status"};
        DefaultTableModel dm = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = UIUtils.createStyledTable(dm);
        table.setRowHeight(32);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        int[] widths = {70, 280, 130, 100, 120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Stock Status colour renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String s = v == null ? "" : v.toString();
                if ("In Stock".equals(s)) {
                    setForeground(ManagerDashboard.GREEN_OK);
                    setText("\u2714  In Stock");
                } else {
                    setForeground(new Color(255, 100, 80));
                    setText("\u26A0  Out of Stock");
                }
                setBackground(sel ? UIUtils.ACCENT_BLUE : UIUtils.TABLE_BG);
                setFont(UIUtils.uiFont(Font.BOLD, 12));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });

        refreshPartsTable(dm);
        JScrollPane sp = new JScrollPane(table); UIUtils.styleScrollPane(sp);

        // Double-click row to open View Details
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showPartDetails(table, dm, () -> refreshPartsTable(dm));
            }
        });

        // Buttons
        UIUtils.RoundedButton addBtn     = new UIUtils.RoundedButton("+ Add Part",      UIUtils.SUCCESS_GREEN);
        UIUtils.RoundedButton detailBtn  = new UIUtils.RoundedButton("\uD83D\uDD0D  View Details", UIUtils.ACCENT_BLUE);
        UIUtils.RoundedButton delBtn     = new UIUtils.RoundedButton("\u2716 Delete",         UIUtils.ERROR_RED);
        UIUtils.RoundedButton stockBtn   = new UIUtils.RoundedButton("\uD83D\uDCE6 Toggle Stock",  new Color(100, 80, 200));
        UIUtils.RoundedButton refBtn     = new UIUtils.RoundedButton("\u21BB Refresh",         UIUtils.MEDIUM_NAVY);

        addBtn  .setPreferredSize(new Dimension(130, 34));
        detailBtn.setPreferredSize(new Dimension(148, 34));
        delBtn  .setPreferredSize(new Dimension(110, 34));
        stockBtn.setPreferredSize(new Dimension(148, 34));
        refBtn  .setPreferredSize(new Dimension(110, 34));

        addBtn  .addActionListener(e -> { showAddPartDialog(); refreshPartsTable(dm); });
        detailBtn.addActionListener(e -> showPartDetails(table, dm, () -> refreshPartsTable(dm)));
        delBtn  .addActionListener(e -> { deletePartAction(table, dm); refreshPartsTable(dm); });
        stockBtn.addActionListener(e -> { togglePartStock(table, dm); refreshPartsTable(dm); });
        refBtn  .addActionListener(e -> refreshPartsTable(dm));

        JLabel hint = new JLabel("  \uD83D\uDCA1 Double-click a row to view full details & edit");
        hint.setFont(UIUtils.uiFont(Font.ITALIC, 11)); hint.setForeground(UIUtils.DIM_TEXT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(addBtn); btnRow.add(detailBtn); btnRow.add(delBtn); btnRow.add(stockBtn); btnRow.add(refBtn); btnRow.add(hint);

        panel.add(sp,     BorderLayout.CENTER);
        panel.add(btnRow, BorderLayout.SOUTH);
        return panel;
    }

    void refreshPartsTable(DefaultTableModel dm) {
        dm.setRowCount(0);
        for (CarPart cp : FileManager.readAllCarParts()) {
            dm.addRow(new Object[]{
                cp.getPartId(),
                cp.getName(),
                cp.getCategory(),
                String.format("RM %.2f", cp.getPrice()),
                cp.stockLabel()
            });
        }
    }

    /** View Details dialog — shows all fields + image, with Edit button inside */
    void showPartDetails(JTable table, DefaultTableModel dm, Runnable onSaved) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a part to view."); return; }
        String partId = (String) dm.getValueAt(row, 0);
        CarPart cp = FileManager.findCarPartById(partId);
        if (cp == null) return;

        JDialog d = new JDialog((Dialog) null, "Part Details  \u2014  " + cp.getName(), true);
        d.setSize(620, 560); d.setLocationRelativeTo(this); d.setResizable(false);

        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(UIUtils.DARK_NAVY);
        outer.setBorder(new EmptyBorder(18, 22, 18, 22));

        // ── Image panel (left)
        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBackground(UIUtils.TABLE_BG);
        imgPanel.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 1),
                new EmptyBorder(8, 8, 8, 8)));
        imgPanel.setPreferredSize(new Dimension(200, 200));

        JLabel imgLbl = new JLabel("", JLabel.CENTER);
        imgLbl.setForeground(UIUtils.DIM_TEXT);
        imgLbl.setFont(UIUtils.uiFont(Font.PLAIN, 42));
        main.loadPartImageInto(imgLbl, cp.getImageUrl(), 184, 180);

        UIUtils.RoundedButton changeImgBtn = new UIUtils.RoundedButton("\uD83D\uDDBC  Change Image", new Color(60, 100, 160));
        changeImgBtn.setFont(UIUtils.uiFont(Font.BOLD, 11));
        changeImgBtn.setPreferredSize(new Dimension(184, 30));
        changeImgBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION) {
                cp.setImageUrl(fc.getSelectedFile().getAbsolutePath());
                FileManager.updateCarPart(cp);
                main.loadPartImageInto(imgLbl, cp.getImageUrl(), 184, 180);
                imgLbl.repaint();
            }
        });
        imgPanel.add(imgLbl,        BorderLayout.CENTER);
        imgPanel.add(changeImgBtn,  BorderLayout.SOUTH);

        // ── Info panel (right)
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(UIUtils.TABLE_BG);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 1),
                new EmptyBorder(16, 18, 16, 18)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(5, 4, 5, 4); g.anchor = GridBagConstraints.WEST;

        main.detailRow(infoPanel, g, 0, "Part ID:",   cp.getPartId());
        main.detailRow(infoPanel, g, 1, "Category:",  cp.getCategory());
        main.detailRow(infoPanel, g, 2, "Price:",      String.format("RM %.2f", cp.getPrice()));
        main.detailRow(infoPanel, g, 3, "Stock:",
                cp.isInStock() ? "\u2714  In Stock" : "\u26A0  Out of Stock");
        main.detailRow(infoPanel, g, 4, "Visible:",    cp.isVisible() ? "Yes (shown to customers)" : "No (hidden)");
        main.detailRow(infoPanel, g, 5, "Added By:",   cp.getAddedBy());
        main.detailRow(infoPanel, g, 6, "Date Added:", cp.getAddedDate());

        // Description label
        g.gridy = 7; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(10, 4, 2, 4);
        JLabel descHdr = new JLabel("Description:");
        descHdr.setFont(UIUtils.uiFont(Font.BOLD, 12)); descHdr.setForeground(UIUtils.ACCENT_BLUE);
        infoPanel.add(descHdr, g);

        g.gridy = 8;
        JTextArea descArea = new JTextArea(cp.getDescription());
        descArea.setEditable(false); descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(28, 46, 88)); descArea.setForeground(UIUtils.WHITE);
        descArea.setFont(UIUtils.uiFont(Font.PLAIN, 12)); descArea.setBorder(new EmptyBorder(6, 8, 6, 8));
        descArea.setRows(4);
        JScrollPane dsp = new JScrollPane(descArea); dsp.setPreferredSize(new Dimension(340, 90));
        UIUtils.styleScrollPane(dsp); infoPanel.add(dsp, g);

        // ── Left + Right split
        JPanel splitRow = new JPanel(new BorderLayout(12, 0));
        splitRow.setBackground(UIUtils.DARK_NAVY);
        splitRow.add(imgPanel,  BorderLayout.WEST);
        splitRow.add(infoPanel, BorderLayout.CENTER);

        // ── Action buttons at bottom
        UIUtils.RoundedButton editBtn  = new UIUtils.RoundedButton("\u270E  Edit This Part",   UIUtils.ACCENT_BLUE);
        UIUtils.RoundedButton stockBtn = new UIUtils.RoundedButton(
                cp.isInStock() ? "\uD83D\uDCE6  Mark Out of Stock" : "\u2714  Mark In Stock",
                cp.isInStock() ? new Color(180, 60, 60) : new Color(50, 150, 80));
        UIUtils.RoundedButton closeBtn = new UIUtils.RoundedButton("Close", UIUtils.MEDIUM_NAVY);

        editBtn .setPreferredSize(new Dimension(160, 36));
        stockBtn.setPreferredSize(new Dimension(178, 36));
        closeBtn.setPreferredSize(new Dimension(100, 36));

        editBtn.addActionListener(e -> {
            d.dispose();
            showEditPartDialog(cp, onSaved);
        });
        stockBtn.addActionListener(e -> {
            cp.setInStock(!cp.isInStock());
            FileManager.updateCarPart(cp);
            onSaved.run();
            d.dispose();
        });
        closeBtn.addActionListener(e -> d.dispose());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        btnRow.add(editBtn); btnRow.add(stockBtn); btnRow.add(closeBtn);

        outer.add(splitRow, BorderLayout.CENTER);
        outer.add(btnRow,   BorderLayout.SOUTH);
        d.setContentPane(outer); d.setVisible(true);
    }

    void showAddPartDialog() {
        JDialog d = new JDialog((Dialog) null, "Add Car Part", true);
        d.setSize(500, 420); d.setLocationRelativeTo(null);
        JPanel p = main.dlgPanel(); GridBagConstraints gbc = main.dlgGbc();

        JTextField nameF  = main.tf(), catF  = main.tf(), priceF = main.tf(), imageF = main.tf();
        JTextArea  descA  = new JTextArea(4, 30);
        descA.setLineWrap(true); descA.setWrapStyleWord(true);
        descA.setBackground(new Color(40, 62, 110)); descA.setForeground(UIUtils.WHITE);
        descA.setCaretColor(UIUtils.WHITE); descA.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        descA.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane descSp = new JScrollPane(descA); UIUtils.styleScrollPane(descSp);

        UIUtils.RoundedButton browseBtn = new UIUtils.RoundedButton("Browse Image", new Color(60, 100, 160));
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images","png","jpg","jpeg"));
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION)
                imageF.setText(fc.getSelectedFile().getAbsolutePath());
        });

        main.addRow(p, gbc, 0, "Part Name:",   nameF);
        main.addRow(p, gbc, 1, "Category:",    catF);
        main.addRow(p, gbc, 2, "Price (RM):",  priceF);
        main.addRow(p, gbc, 3, "Image Path:",  imageF);
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; p.add(browseBtn, gbc);
        gbc.gridy=5;
        JLabel dLbl = new JLabel("Description:"); dLbl.setForeground(UIUtils.WHITE); dLbl.setFont(UIUtils.uiFont(Font.BOLD,12));
        p.add(dLbl, gbc);
        gbc.gridy=6; p.add(descSp, gbc);

        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Save Part", UIUtils.SUCCESS_GREEN);
        save.setPreferredSize(new Dimension(160, 34));
        save.addActionListener(e -> {
            String nm=nameF.getText().trim(), cat=catF.getText().trim(), img=imageF.getText().trim();
            String desc=descA.getText().trim();
            if (nm.isEmpty()||cat.isEmpty()) { main.err(d,"Name and category required."); return; }
            double price=0;
            try { price=Double.parseDouble(priceF.getText().trim()); } catch(Exception ex) { main.err(d,"Invalid price."); return; }
            CarPart cp = new CarPart(FileManager.generateCarPartId(), nm, cat, desc, price, img, true, true,
                    main.currentManager.getUserId(), LocalDate.now().toString());
            FileManager.saveCarPart(cp);
            JOptionPane.showMessageDialog(d,"Car part added!","Success",JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });
        gbc.gridy=7; p.add(save, gbc);
        d.setContentPane(p); d.setVisible(true);
    }

    /** Edit dialog — opened from View Details dialog */
    void showEditPartDialog(CarPart cp, Runnable onSaved) {
        JDialog d = new JDialog((Dialog) null, "Edit Car Part  \u2014  " + cp.getName(), true);
        d.setSize(500, 420); d.setLocationRelativeTo(null);
        JPanel p = main.dlgPanel(); GridBagConstraints gbc = main.dlgGbc();

        JTextField nameF  = new JTextField(cp.getName());   UIUtils.styleTextField(nameF);
        JTextField catF   = new JTextField(cp.getCategory()); UIUtils.styleTextField(catF);
        JTextField priceF = new JTextField(String.format("%.2f", cp.getPrice())); UIUtils.styleTextField(priceF);
        JTextField imageF = new JTextField(cp.getImageUrl()); UIUtils.styleTextField(imageF);
        JTextArea  descA  = new JTextArea(cp.getDescription(), 4, 30);
        descA.setLineWrap(true); descA.setWrapStyleWord(true);
        descA.setBackground(new Color(40, 62, 110)); descA.setForeground(UIUtils.WHITE);
        descA.setCaretColor(UIUtils.WHITE); descA.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        descA.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane descSp = new JScrollPane(descA); UIUtils.styleScrollPane(descSp);

        UIUtils.RoundedButton browseBtn = new UIUtils.RoundedButton("Browse Image", new Color(60, 100, 160));
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images","png","jpg","jpeg"));
            if (fc.showOpenDialog(d) == JFileChooser.APPROVE_OPTION)
                imageF.setText(fc.getSelectedFile().getAbsolutePath());
        });

        main.addRow(p, gbc, 0, "Part Name:",  nameF);
        main.addRow(p, gbc, 1, "Category:",   catF);
        main.addRow(p, gbc, 2, "Price (RM):", priceF);
        main.addRow(p, gbc, 3, "Image Path:", imageF);
        gbc.gridx=0; gbc.gridy=4; gbc.gridwidth=2; p.add(browseBtn, gbc);
        gbc.gridy=5;
        JLabel dLbl = new JLabel("Description:"); dLbl.setForeground(UIUtils.WHITE); dLbl.setFont(UIUtils.uiFont(Font.BOLD,12));
        p.add(dLbl, gbc);
        gbc.gridy=6; p.add(descSp, gbc);

        UIUtils.RoundedButton save = new UIUtils.RoundedButton("Update Part", UIUtils.ACCENT_BLUE);
        save.setPreferredSize(new Dimension(160, 34));
        save.addActionListener(e -> {
            String nm=nameF.getText().trim(), cat=catF.getText().trim();
            if (nm.isEmpty()||cat.isEmpty()) { main.err(d,"Name and category required."); return; }
            double price=0;
            try { price=Double.parseDouble(priceF.getText().trim()); } catch(Exception ex) { main.err(d,"Invalid price."); return; }
            cp.setName(nm); cp.setCategory(cat);
            cp.setDescription(descA.getText().trim());
            cp.setPrice(price); cp.setImageUrl(imageF.getText().trim());
            FileManager.updateCarPart(cp);
            if (onSaved != null) onSaved.run();
            JOptionPane.showMessageDialog(d,"Updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            d.dispose();
        });
        gbc.gridy=7; p.add(save, gbc);
        d.setContentPane(p); d.setVisible(true);
    }

    void deletePartAction(JTable table, DefaultTableModel dm) {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a part to delete.");return;}
        String partId=(String)dm.getValueAt(row,0), name=(String)dm.getValueAt(row,1);
        if(JOptionPane.showConfirmDialog(this,"Delete part: "+name+"?","Confirm",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        List<CarPart> parts=FileManager.readAllCarParts();
        parts.removeIf(cp->cp.getPartId().equals(partId));
        java.io.File f=new java.io.File("data/car_parts.txt");
        try(java.io.PrintWriter pw=new java.io.PrintWriter(new java.io.FileWriter(f,false))){
            for(CarPart cp:parts) pw.println(cp.toFileString());
        }catch(java.io.IOException ignored){}
        JOptionPane.showMessageDialog(this,"Deleted.","Success",JOptionPane.INFORMATION_MESSAGE);
    }

    void togglePartStock(JTable table, DefaultTableModel dm) {
        int row=table.getSelectedRow();
        if(row<0){JOptionPane.showMessageDialog(this,"Select a part to toggle stock status.");return;}
        String partId=(String)dm.getValueAt(row,0);
        CarPart cp=FileManager.findCarPartById(partId);
        if(cp==null) return;
        cp.setInStock(!cp.isInStock());
        FileManager.updateCarPart(cp);
        String msg = cp.isInStock() ? cp.getName()+" marked as In Stock." : cp.getName()+" marked as Out of Stock.";
        JOptionPane.showMessageDialog(this, msg, "Stock Updated", JOptionPane.INFORMATION_MESSAGE);
    }


}
