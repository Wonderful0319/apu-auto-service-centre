package asc.gui.customer;

import asc.model.*;
import asc.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class CustomerProductsPage extends JPanel {

    private static final long serialVersionUID = 1L;
    final CustomerDashboard main;
    java.util.Map<String, Integer> cartItems = new java.util.HashMap<>();
    Runnable cartBadgeRefresh;

    public CustomerProductsPage(CustomerDashboard m) {
        main = m;
        setLayout(new BorderLayout());
        setBackground(asc.util.UIUtils.DARK_NAVY);
        setBorder(new javax.swing.border.EmptyBorder(18, 18, 14, 18));
        add(buildPanel(), java.awt.BorderLayout.CENTER);
    }

    JPanel buildPanel() {
        main.productsActiveTab      = 0;
        main.productsActiveCategory = "All";
        main.productsSearchKw       = "";
        main.productsSortMode       = 0;

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIUtils.DARK_NAVY);
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        // â”€â”€ Page header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JLabel pageTitle = new JLabel("Products & Services");
        pageTitle.setFont(UIUtils.uiFont(Font.BOLD, 18));
        pageTitle.setForeground(UIUtils.WHITE);
        JLabel pageSub = new JLabel("Browse car parts and view service pricing");
        pageSub.setFont(UIUtils.uiFont(Font.PLAIN, 12));
        pageSub.setForeground(UIUtils.DIM_TEXT);
        JPanel headerBox = new JPanel();
        headerBox.setLayout(new BoxLayout(headerBox, BoxLayout.Y_AXIS));
        headerBox.setBackground(UIUtils.DARK_NAVY);
        headerBox.add(pageTitle);
        headerBox.add(Box.createVerticalStrut(2));
        headerBox.add(pageSub);
        headerBox.setBorder(new EmptyBorder(0, 0, 12, 0));

        // â”€â”€ Tab bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setBackground(UIUtils.DARK_NAVY);
        tabBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 75, 130)));

        // Content swap area
        JPanel contentSwap = new JPanel(new BorderLayout());
        contentSwap.setBackground(UIUtils.DARK_NAVY);

        JButton[] tabBtns = new JButton[2];
        String[] tabNames = {"Car Parts", "Services"};
        Color TAB_ACTIVE_LINE = UIUtils.ACCENT_BLUE;

        for (int ti = 0; ti < 2; ti++) {
            JButton tb = new JButton(tabNames[ti]);
            tb.setFont(UIUtils.uiFont(Font.PLAIN, 13));
            tb.setFocusPainted(false);
            tb.setContentAreaFilled(false);
            tb.setOpaque(false);
            tb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tb.setBorder(new EmptyBorder(9, 18, 9, 18));
            tb.setForeground(UIUtils.DIM_TEXT);
            tabBtns[ti] = tb;
            tabBar.add(tb);
        }

        Runnable[] selectTab = {null};
        selectTab[0] = () -> {
            for (int i = 0; i < 2; i++) {
                boolean active = (i == main.productsActiveTab);
                tabBtns[i].setForeground(active ? UIUtils.ACCENT_BLUE : UIUtils.DIM_TEXT);
                tabBtns[i].setFont(UIUtils.uiFont(active ? Font.BOLD : Font.PLAIN, 13));
                tabBtns[i].setBorder(active
                    ? BorderFactory.createCompoundBorder(
                          BorderFactory.createMatteBorder(0, 0, 2, 0, TAB_ACTIVE_LINE),
                          new EmptyBorder(9, 18, 7, 18))
                    : new EmptyBorder(9, 18, 9, 18));
            }
            contentSwap.removeAll();
            contentSwap.add(main.productsActiveTab == 0 ? buildPartsContent(contentSwap, selectTab)
                                                   : main.buildServicesContent(), BorderLayout.CENTER);
            contentSwap.revalidate();
            contentSwap.repaint();
        };

        for (int ti = 0; ti < 2; ti++) {
            final int idx = ti;
            tabBtns[ti].addActionListener(e -> {
                main.productsActiveTab = idx;
                selectTab[0].run();
            });
        }

        JPanel northBox = new JPanel(new BorderLayout());
        northBox.setBackground(UIUtils.DARK_NAVY);
        northBox.add(headerBox, BorderLayout.NORTH);
        northBox.add(tabBar,    BorderLayout.SOUTH);

        root.add(northBox,    BorderLayout.NORTH);
        root.add(contentSwap, BorderLayout.CENTER);

        // Trigger initial tab render
        selectTab[0].run();
        return root;
    }

    // â”€â”€ Car Parts tab content â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    JPanel buildPartsContent(JPanel contentSwap, Runnable[] selectTab) {
        List<CarPart> allVisible = FileManager.readAllCarParts().stream()
                .filter(CarPart::isVisible).collect(Collectors.toList());

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(UIUtils.DARK_NAVY);
        wrapper.setBorder(new EmptyBorder(14, 0, 0, 0));

        if (allVisible.isEmpty()) {
            wrapper.add(main.emptyState("\uD83D\uDD27", "No car parts information available yet."), BorderLayout.CENTER);
            return wrapper;
        }

        // â”€â”€ Collect unique categories â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        List<String> categories = new ArrayList<>();
        categories.add("All");
        allVisible.stream().map(CarPart::getCategory).distinct().sorted().forEach(categories::add);

        // â”€â”€ Filter bar: search + category pills + sort â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel filterBar = new JPanel();
        filterBar.setLayout(new BoxLayout(filterBar, BoxLayout.Y_AXIS));
        filterBar.setBackground(UIUtils.DARK_NAVY);

        // Row 1: search field + sort
        JTextField searchField = new JTextField();
        UIUtils.styleTextField(searchField);
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.setMaximumSize(new Dimension(280, 32));
        searchField.putClientProperty("JTextField.placeholderText", "Search parts...");

        JComboBox<String> sortCb = new JComboBox<>(new String[]{"Price: low to high", "Price: high to low", "Name A-Z"});
        UIUtils.styleCombo(sortCb);
        sortCb.setPreferredSize(new Dimension(170, 32));
        sortCb.setMaximumSize(new Dimension(200, 32));

        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setForeground(UIUtils.ACCENT_BLUE);
        searchIcon.setFont(UIUtils.uiFont(Font.PLAIN, 14));

        JLabel sortLbl = new JLabel("Sort:");
        sortLbl.setForeground(UIUtils.DIM_TEXT);
        sortLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setBackground(UIUtils.DARK_NAVY);
        row1.add(searchIcon);
        row1.add(searchField);
        row1.add(Box.createHorizontalStrut(16));
        row1.add(sortLbl);
        row1.add(sortCb);

        // Row 2: category pills
        JPanel pillRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pillRow.setBackground(UIUtils.DARK_NAVY);
        JButton[] pillBtns = new JButton[categories.size()];

        JPanel gridScroll   = new JPanel(new BorderLayout());
        gridScroll.setBackground(UIUtils.DARK_NAVY);
        JLabel countLbl = new JLabel();
        countLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        countLbl.setForeground(UIUtils.DIM_TEXT);
        countLbl.setBorder(new EmptyBorder(0, 0, 6, 0));

        Runnable rebuildGrid = () -> {
            String kw   = main.productsSearchKw.toLowerCase().trim();
            String cat  = main.productsActiveCategory;
            int    sort = main.productsSortMode;

            List<CarPart> filtered = allVisible.stream()
                .filter(p -> "All".equals(cat) || p.getCategory().equals(cat))
                .filter(p -> kw.isEmpty()
                    || p.getName().toLowerCase().contains(kw)
                    || p.getCategory().toLowerCase().contains(kw)
                    || p.getDescription().toLowerCase().contains(kw))
                .sorted((a, b) -> {
                    if (sort == 0) return Double.compare(a.getPrice(), b.getPrice());
                    if (sort == 1) return Double.compare(b.getPrice(), a.getPrice());
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .collect(Collectors.toList());

            countLbl.setText(filtered.size() + " part" + (filtered.size() != 1 ? "s" : "") + " found");

            // Build grid
            JPanel grid = new JPanel(new GridLayout(0, 4, 12, 12));
            grid.setBackground(UIUtils.DARK_NAVY);
            grid.setBorder(new EmptyBorder(4, 0, 8, 0));

            if (filtered.isEmpty()) {
                JLabel none = new JLabel("No parts match your filter.", JLabel.CENTER);
                none.setForeground(UIUtils.DIM_TEXT);
                none.setFont(UIUtils.uiFont(Font.ITALIC, 13));
                grid.setLayout(new BorderLayout());
                grid.add(none, BorderLayout.CENTER);
            } else {
                for (CarPart cp : filtered) {
                    grid.add(buildPartCard(cp));
                }
            }

            JScrollPane sp = new JScrollPane(grid,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sp.getVerticalScrollBar().setUnitIncrement(20);
            sp.setBorder(null);
            sp.getViewport().setBackground(UIUtils.DARK_NAVY);

            gridScroll.removeAll();
            gridScroll.add(sp, BorderLayout.CENTER);
            gridScroll.revalidate();
            gridScroll.repaint();
        };

        // Wire up pills
        for (int pi = 0; pi < categories.size(); pi++) {
            final String cat = categories.get(pi);
            JButton pill = main.makePillBtn(cat, cat.equals(main.productsActiveCategory));
            pillBtns[pi] = pill;
            pill.addActionListener(e -> {
                main.productsActiveCategory = cat;
                for (int pj = 0; pj < pillBtns.length; pj++)
                    main.stylePill(pillBtns[pj], categories.get(pj).equals(cat));
                rebuildGrid.run();
            });
            pillRow.add(pill);
        }

        // Wire search + sort
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { main.productsSearchKw = searchField.getText(); rebuildGrid.run(); }
            public void removeUpdate(DocumentEvent e)  { main.productsSearchKw = searchField.getText(); rebuildGrid.run(); }
            public void changedUpdate(DocumentEvent e) { main.productsSearchKw = searchField.getText(); rebuildGrid.run(); }
        });
        sortCb.addActionListener(e -> { main.productsSortMode = sortCb.getSelectedIndex(); rebuildGrid.run(); });

        filterBar.add(row1);
        filterBar.add(Box.createVerticalStrut(6));
        filterBar.add(pillRow);

        JPanel topBar = new JPanel(new BorderLayout(0, 4));
        topBar.setBackground(UIUtils.DARK_NAVY);
        topBar.add(filterBar,  BorderLayout.NORTH);
        topBar.add(countLbl,   BorderLayout.SOUTH);

        wrapper.add(topBar,   BorderLayout.NORTH);
        wrapper.add(gridScroll, BorderLayout.CENTER);

        // Add a note at bottom linking to Services tab
        JLabel noteLink = new JLabel("\u2139  Parts marked as 'Included' are bundled in your service â€” switch to the Services tab for full details.");
        noteLink.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        noteLink.setForeground(UIUtils.DIM_TEXT);
        noteLink.setBorder(new EmptyBorder(6, 0, 0, 0));
        wrapper.add(noteLink, BorderLayout.SOUTH);

        // First render
        rebuildGrid.run();
        return wrapper;
    }

    // Build a single product card
    JPanel buildPartCard(CarPart cp) {
        boolean free    = cp.getPrice() == 0.0;
        boolean inStock = cp.isInStock();
        Color borderCol = inStock ? new Color(50, 85, 150) : new Color(80, 40, 40);

        // â”€â”€ Resolve image path (4-strategy lookup, same as before) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        File imgFile = null;

        // Strategy 1: stored imageUrl
        String storedPath = cp.getImageUrl();
        if (storedPath != null && !storedPath.trim().isEmpty()) {
            File f1 = new File(storedPath.trim());
            if (f1.exists()) imgFile = f1;
        }
        // Strategy 2: exact name match in CustomerDashboard.ICON_BASE
        if (imgFile == null) {
            String[] exts = {".png", ".jpg", ".jpeg"};
            String[] candidates = {
                cp.getName().trim(),
                cp.getName().trim().replace("/", " "),
                cp.getName().trim().replace(" / ", " ")
            };
            outer:
            for (String c2 : candidates)
                for (String ext : exts) {
                    File f2 = new File(CustomerDashboard.ICON_BASE + c2 + ext);
                    if (f2.exists()) { imgFile = f2; break outer; }
                }
        }
        // Strategy 3: fuzzy keyword scan
        if (imgFile == null) {
            try {
                File dir = new File(CustomerDashboard.ICON_BASE);
                if (dir.isDirectory()) {
                    String partLower = cp.getName().trim().toLowerCase()
                            .replaceAll("[^a-z0-9 ]", " ").trim();
                    String[] words = partLower.split("\\s+");
                    File[] files = dir.listFiles();
                    if (files != null) {
                        File best = null; int bestScore = 0;
                        for (File f3 : files) {
                            String fn = f3.getName().toLowerCase();
                            if (!fn.endsWith(".png") && !fn.endsWith(".jpg")) continue;
                            if (fn.contains("blue") || fn.contains("home")
                                    || fn.contains("receipt") || fn.contains("spending")
                                    || fn.contains("star") || fn.contains("vip")
                                    || fn.contains("feedback") || fn.contains("comment")
                                    || fn.contains("service history") || fn.contains("top-up")
                                    || fn.contains("car parts info") || fn.contains("leave a comment")
                                    || fn.contains("view feedback")) continue;
                            int score = 0;
                            for (String w : words)
                                if (w.length() > 3 && fn.contains(w)) score++;
                            if (score > bestScore) { bestScore = score; best = f3; }
                        }
                        if (best != null && bestScore > 0) imgFile = best;
                    }
                }
            } catch (Exception ignored) {}
        }

        // Pre-load the image once (shared by paintComponent)
        final Image[] productImg = {null};
        if (imgFile != null) {
            try { productImg[0] = new ImageIcon(imgFile.getAbsolutePath()).getImage(); }
            catch (Exception ignored) {}
        }

        // â”€â”€ Outer card panel â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(new Color(30, 48, 88));
        card.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(borderCol, 12, 1),
                new EmptyBorder(0, 0, 0, 0)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // â”€â”€ Image area: paints the image stretched to fill the entire panel â”€â”€
        JPanel imgArea = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth(), h = getHeight();

                if (productImg[0] != null) {
                    // â€” Fill the entire area with the image (cover mode) â€”
                    int imgW = productImg[0].getWidth(null);
                    int imgH = productImg[0].getHeight(null);
                    if (imgW > 0 && imgH > 0) {
                        // Scale to cover: find the ratio that fills both dimensions
                        double scaleW = (double) w / imgW;
                        double scaleH = (double) h / imgH;
                        double scale  = Math.max(scaleW, scaleH); // cover
                        int dw = (int) (imgW * scale);
                        int dh = (int) (imgH * scale);
                        int dx = (w - dw) / 2;  // centre crop
                        int dy = (h - dh) / 2;
                        // Clip to panel bounds so image doesn't bleed outside
                        g2.setClip(0, 0, w, h);
                        g2.drawImage(productImg[0], dx, dy, dw, dh, null);
                        // Subtle dark gradient overlay at bottom for text legibility
                        g2.setClip(null);
                        g2.setPaint(new GradientPaint(
                                0, h * 0.55f, new Color(0, 0, 0, 0),
                                0, h,          new Color(20, 32, 62, 200)));
                        g2.fillRect(0, 0, w, h);
                    } else {
                        // Image not ready yet: plain background
                        g2.setColor(new Color(22, 36, 70));
                        g2.fillRect(0, 0, w, h);
                    }
                } else {
                    // Strategy 4 fallback: gradient + first letter
                    g2.setPaint(new GradientPaint(
                            0, 0, new Color(28, 50, 100),
                            0, h, new Color(20, 36, 74)));
                    g2.fillRect(0, 0, w, h);
                    // Draw first letter
                    String letter = cp.getName().trim().isEmpty() ? "?"
                            : String.valueOf(Character.toUpperCase(cp.getName().trim().charAt(0)));
                    Font lf = UIUtils.uiFont(Font.BOLD, 48);
                    g2.setFont(lf);
                    g2.setColor(new Color(60, 100, 170, 120));
                    FontMetrics fm2 = g2.getFontMetrics(lf);
                    g2.drawString(letter,
                            (w - fm2.stringWidth(letter)) / 2,
                            (h + fm2.getAscent() - fm2.getDescent()) / 2);
                }
                g2.dispose();
            }
        };
        imgArea.setOpaque(true);
        imgArea.setBackground(new Color(22, 36, 70));
        imgArea.setPreferredSize(new Dimension(0, 150));

        // â”€â”€ Stock / Included badge (top-right overlay) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String badgeText = free ? "Included" : (inStock ? "In stock" : "Out of stock");
        Color badgeBg = free ? new Color(15, 70, 35)
                : (inStock ? new Color(15, 60, 30) : new Color(80, 20, 20));
        Color badgeFg = free ? new Color(80, 220, 130)
                : (inStock ? new Color(70, 210, 110) : new Color(230, 90, 90));

        JLabel badge = new JLabel(badgeText);
        badge.setFont(UIUtils.uiFont(Font.BOLD, 10));
        badge.setOpaque(true);
        badge.setBackground(badgeBg);
        badge.setForeground(badgeFg);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);
        imgArea.add(badgeWrap, BorderLayout.NORTH);

        // â”€â”€ Category chip (bottom-left overlay) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JLabel catChip = new JLabel(cp.getCategory());
        catChip.setFont(UIUtils.uiFont(Font.BOLD, 9));
        catChip.setOpaque(true);
        catChip.setBackground(new Color(18, 32, 68, 210));
        catChip.setForeground(new Color(160, 200, 245));
        catChip.setBorder(new EmptyBorder(2, 7, 2, 7));
        JPanel catChipWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        catChipWrap.setOpaque(false);
        catChipWrap.add(catChip);
        imgArea.add(catChipWrap, BorderLayout.SOUTH);

        // â”€â”€ Text body â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(new Color(30, 48, 88));
        body.setBorder(new EmptyBorder(10, 12, 12, 12));

        JLabel nameLbl = new JLabel(
                "<html><div style='width:140px; word-wrap:break-word;'>"
                + cp.getName() + "</div></html>");
        nameLbl.setFont(UIUtils.uiFont(Font.BOLD, 12));
        nameLbl.setForeground(UIUtils.WHITE);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLbl = new JLabel(free ? "Complimentary"
                : String.format("RM %.2f", cp.getPrice()));
        priceLbl.setFont(UIUtils.uiFont(Font.BOLD, 13));
        priceLbl.setForeground(free ? CustomerDashboard.GREEN_OK : CustomerDashboard.ACCENT_YELLOW);
        priceLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        priceLbl.setBorder(new EmptyBorder(5, 0, 0, 0));

        body.add(nameLbl);
        body.add(priceLbl);

        card.add(imgArea, BorderLayout.NORTH);
        card.add(body,    BorderLayout.CENTER);

        // â”€â”€ Hover effect â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Color baseCard  = new Color(30, 48, 88);
        Color hoverCard = new Color(38, 62, 112);
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverCard);
                body.setBackground(hoverCard);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 2),
                        new EmptyBorder(0, 0, 0, 0)));
                card.repaint();
            }
            public void mouseExited(MouseEvent e) {
                card.setBackground(baseCard);
                body.setBackground(baseCard);
                card.setBorder(BorderFactory.createCompoundBorder(
                        new UIUtils.RoundedBorder(borderCol, 12, 1),
                        new EmptyBorder(0, 0, 0, 0)));
                card.repaint();
            }
            public void mouseClicked(MouseEvent e) { showPartDetailDialog(cp); }
        });
        return card;
    }

    // Detail dialog when clicking a product card
    void showPartDetailDialog(CarPart cp) {
        boolean free    = cp.getPrice() == 0.0;
        boolean inStock = cp.isInStock();

        // â”€â”€ Dialog setup â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JDialog dlg = new JDialog((Dialog) null, cp.getName(), true);
        dlg.setSize(580, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBackground(UIUtils.DARK_NAVY);
        outer.setBorder(new EmptyBorder(16, 18, 16, 18));

        // â”€â”€ TOP ROW: image (left) + info (right) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel topCard = new JPanel(new BorderLayout(14, 0));
        topCard.setBackground(UIUtils.TABLE_BG);
        topCard.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE, 12, 1),
                new EmptyBorder(14, 16, 14, 16)));

        // â€” Image box (cover-paint, 150Ã—150) â€”
        File dlgImgFile = null;
        if (cp.getImageUrl() != null && !cp.getImageUrl().trim().isEmpty()) {
            File f0 = new File(cp.getImageUrl().trim());
            if (f0.exists()) dlgImgFile = f0;
        }
        if (dlgImgFile == null) {
            for (String ext : new String[]{".png",".jpg",".jpeg"}) {
                File fx = new File(CustomerDashboard.ICON_BASE + cp.getName().trim() + ext);
                if (fx.exists()) { dlgImgFile = fx; break; }
            }
        }
        if (dlgImgFile == null) {
            try {
                File dir = new File(CustomerDashboard.ICON_BASE);
                if (dir.isDirectory()) {
                    String partLower = cp.getName().trim().toLowerCase()
                            .replaceAll("[^a-z0-9 ]"," ").trim();
                    String[] words = partLower.split("\\s+");
                    File best = null; int bestScore = 0;
                    for (File fx2 : dir.listFiles()) {
                        String fn = fx2.getName().toLowerCase();
                        if (!fn.endsWith(".png") && !fn.endsWith(".jpg")) continue;
                        if (fn.contains("blue")||fn.contains("home")||fn.contains("receipt")
                                ||fn.contains("spending")||fn.contains("star")||fn.contains("vip")
                                ||fn.contains("feedback")||fn.contains("comment")
                                ||fn.contains("service history")||fn.contains("top-up")
                                ||fn.contains("car parts")||fn.contains("leave a")
                                ||fn.contains("view feed")) continue;
                        int sc = 0;
                        for (String w : words) if (w.length()>3 && fn.contains(w)) sc++;
                        if (sc > bestScore) { bestScore=sc; best=fx2; }
                    }
                    if (best != null && bestScore > 0) dlgImgFile = best;
                }
            } catch (Exception ignored) {}
        }
        final Image[] dlgImg = {dlgImgFile!=null
                ? new ImageIcon(dlgImgFile.getAbsolutePath()).getImage() : null};

        JPanel imgBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                int w=getWidth(), h=getHeight();
                g2.setColor(new Color(20,34,68));
                g2.fillRoundRect(0,0,w,h,10,10);
                if (dlgImg[0]!=null) {
                    int iw=dlgImg[0].getWidth(null), ih=dlgImg[0].getHeight(null);
                    if (iw>0&&ih>0) {
                        double sc=Math.max((double)w/iw,(double)h/ih);
                        int dw=(int)(iw*sc),dh=(int)(ih*sc);
                        g2.setClip(0,0,w,h);
                        g2.drawImage(dlgImg[0],(w-dw)/2,(h-dh)/2,dw,dh,null);
                    }
                } else {
                    g2.setFont(UIUtils.uiFont(Font.BOLD,48));
                    g2.setColor(new Color(60,100,160,100));
                    String lt=cp.getName().isEmpty()?"?":
                            String.valueOf(Character.toUpperCase(cp.getName().charAt(0)));
                    FontMetrics fm5=g2.getFontMetrics();
                    g2.drawString(lt,(w-fm5.stringWidth(lt))/2,
                            (h+fm5.getAscent()-fm5.getDescent())/2);
                }
                g2.dispose();
            }
        };
        imgBox.setPreferredSize(new Dimension(160, 160));
        imgBox.setBackground(new Color(20,34,68));
        imgBox.setOpaque(true);
        imgBox.setBorder(new UIUtils.RoundedBorder(new Color(50,80,140),10,1));

        // â€” Info panel (right side) â€”
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(UIUtils.TABLE_BG);
        infoPanel.setBorder(new EmptyBorder(0,4,0,0));

        JLabel nameL = new JLabel(
                "<html><div style='width:300px;word-wrap:break-word;'>"+cp.getName()+"</div></html>");
        nameL.setFont(UIUtils.uiFont(Font.BOLD,16));
        nameL.setForeground(UIUtils.WHITE);
        nameL.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catL = new JLabel(cp.getCategory()+"  Â·  "+cp.getPartId());
        catL.setFont(UIUtils.uiFont(Font.PLAIN,11));
        catL.setForeground(UIUtils.DIM_TEXT);
        catL.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceL = new JLabel(free?"Complimentary":String.format("RM %.2f",cp.getPrice()));
        priceL.setFont(UIUtils.uiFont(Font.BOLD,26));
        priceL.setForeground(free?CustomerDashboard.GREEN_OK:CustomerDashboard.ACCENT_YELLOW);
        priceL.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel stockL = new JLabel(inStock||free ? "âœ… "+(free?"Included in service":"In stock")
                : "âŒ Out of stock");
        stockL.setFont(UIUtils.uiFont(Font.BOLD,12));
        stockL.setForeground(inStock||free ? CustomerDashboard.GREEN_OK : new Color(220,80,80));
        stockL.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(nameL);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(catL);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(priceL);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(stockL);

        // â€” Quantity selector (only for purchasable, in-stock items) â€”
        int[] qty = {1};
        if (!free && inStock) {
            infoPanel.add(Box.createVerticalStrut(12));

            JPanel qtyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            qtyRow.setBackground(UIUtils.TABLE_BG);
            qtyRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel qtyHdr = new JLabel("Quantity: ");
            qtyHdr.setFont(UIUtils.uiFont(Font.BOLD,12));
            qtyHdr.setForeground(UIUtils.LIGHT_GRAY);

            UIUtils.RoundedButton minusBtn = new UIUtils.RoundedButton("-", new Color(50,75,130));
            UIUtils.RoundedButton plusBtn  = new UIUtils.RoundedButton("+", new Color(50,75,130));
            JLabel qLbl = new JLabel("  1  ", JLabel.CENTER);
            qLbl.setFont(UIUtils.uiFont(Font.BOLD,14));
            qLbl.setForeground(UIUtils.WHITE);
            qLbl.setPreferredSize(new Dimension(36,28));

            for (UIUtils.RoundedButton qb : new UIUtils.RoundedButton[]{minusBtn,plusBtn}) {
                qb.setFont(UIUtils.uiFont(Font.BOLD,14));
                qb.setPreferredSize(new Dimension(30,28));
            }
            minusBtn.addActionListener(e->{
                if(qty[0]>1){ qty[0]--; qLbl.setText("  "+qty[0]+"  "); }
            });
            plusBtn.addActionListener(e->{
                if(qty[0]<99){ qty[0]++; qLbl.setText("  "+qty[0]+"  "); }
            });

            qtyRow.add(qtyHdr);
            qtyRow.add(minusBtn);
            qtyRow.add(qLbl);
            qtyRow.add(plusBtn);
            infoPanel.add(qtyRow);
        }

        topCard.add(imgBox,    BorderLayout.WEST);
        topCard.add(infoPanel, BorderLayout.CENTER);

        // â”€â”€ MIDDLE: Description â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel descCard = new JPanel(new BorderLayout(0,8));
        descCard.setBackground(UIUtils.TABLE_BG);
        descCard.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(new Color(50,80,130),12,1),
                new EmptyBorder(12,14,12,14)));

        JLabel descHdr = new JLabel("Product Description");
        descHdr.setFont(UIUtils.uiFont(Font.BOLD,12));
        descHdr.setForeground(UIUtils.ACCENT_BLUE);

        JTextArea descArea = new JTextArea(cp.getDescription());
        descArea.setEditable(false); descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(24,38,72)); descArea.setForeground(UIUtils.LIGHT_GRAY);
        descArea.setFont(UIUtils.uiFont(Font.PLAIN,12)); descArea.setBorder(new EmptyBorder(6,8,6,8));
        descArea.setRows(3);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(null); UIUtils.styleScrollPane(descScroll);

        descCard.add(descHdr,    BorderLayout.NORTH);
        descCard.add(descScroll, BorderLayout.CENTER);

        // â”€â”€ BOTTOM: Action buttons â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        JPanel actionRow = new JPanel(new GridLayout(1, (!free && inStock) ? 3 : 1, 10, 0));
        actionRow.setBackground(UIUtils.DARK_NAVY);

        UIUtils.RoundedButton closeBtn = new UIUtils.RoundedButton("Close", new Color(60,80,120));
        closeBtn.setFont(UIUtils.uiFont(Font.BOLD,12));
        closeBtn.setPreferredSize(new Dimension(0,40));
        closeBtn.addActionListener(e -> dlg.dispose());

        if (!free && inStock) {
            UIUtils.RoundedButton addCartBtn = new UIUtils.RoundedButton(
                    "ðŸ›’  Add to Cart", new Color(50,90,170));
            addCartBtn.setFont(UIUtils.uiFont(Font.BOLD,12));
            addCartBtn.setPreferredSize(new Dimension(0,40));
            addCartBtn.addActionListener(e -> {
                cartItems.merge(cp.getPartId(), qty[0], Integer::sum);
                cartBadgeRefresh.run();
                JOptionPane.showMessageDialog(dlg,
                        String.format("âœ… Added %dÃ— %s to cart!", qty[0], cp.getName()),
                        "Added to Cart", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
            });

            UIUtils.RoundedButton buyNowBtn = new UIUtils.RoundedButton(
                    "âš¡  Buy Now", UIUtils.SUCCESS_GREEN);
            buyNowBtn.setFont(UIUtils.uiFont(Font.BOLD,12));
            buyNowBtn.setPreferredSize(new Dimension(0,40));
            buyNowBtn.addActionListener(e -> {
                dlg.dispose();
                showDirectBuyDialog(cp, qty[0]);
            });

            actionRow.add(addCartBtn);
            actionRow.add(buyNowBtn);
            actionRow.add(closeBtn);

        } else if (!inStock) {
            JPanel wrap = new JPanel(new BorderLayout(0,8));
            wrap.setBackground(UIUtils.DARK_NAVY);
            JLabel outLbl = new JLabel("âŒ This item is currently out of stock", JLabel.CENTER);
            outLbl.setFont(UIUtils.uiFont(Font.BOLD,12));
            outLbl.setForeground(new Color(220,80,80));
            wrap.add(outLbl,   BorderLayout.NORTH);
            wrap.add(closeBtn, BorderLayout.SOUTH);
            outer.add(topCard,  BorderLayout.NORTH);
            outer.add(descCard, BorderLayout.CENTER);
            outer.add(wrap,     BorderLayout.SOUTH);
            dlg.setContentPane(outer);
            dlg.setVisible(true);
            return;
        } else {
            actionRow.add(closeBtn);
        }

        outer.add(topCard,   BorderLayout.NORTH);
        outer.add(descCard,  BorderLayout.CENTER);
        outer.add(actionRow, BorderLayout.SOUTH);
        dlg.setContentPane(outer);
        dlg.setVisible(true);
    }

    // â”€â”€ Direct Buy dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    void showDirectBuyDialog(CarPart cp, int qty) {
        JDialog dlg = new JDialog((Dialog) null, "Buy Now â€” "+cp.getName(), true);
        dlg.setSize(440, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel outer = new JPanel(new BorderLayout(0,14));
        outer.setBackground(UIUtils.DARK_NAVY);
        outer.setBorder(new EmptyBorder(20,24,20,24));

        // Order summary card
        JPanel summary = new JPanel(new GridBagLayout());
        summary.setBackground(UIUtils.TABLE_BG);
        summary.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE,12,1),
                new EmptyBorder(16,18,16,18)));
        GridBagConstraints sg = new GridBagConstraints();
        sg.fill = GridBagConstraints.HORIZONTAL; sg.insets = new Insets(5,4,5,4);

        // helper to add summary row
        int[] sRow = {0};
        java.util.function.BiConsumer<String,String> addRow = (lbl, val) -> {
            sg.gridy=sRow[0]; sg.gridx=0; sg.weightx=0.5;
            JLabel l=new JLabel(lbl); l.setForeground(UIUtils.DIM_TEXT);
            l.setFont(UIUtils.uiFont(Font.PLAIN,12)); summary.add(l,sg);
            sg.gridx=1;
            JLabel v=new JLabel(val,JLabel.RIGHT);
            v.setFont(UIUtils.uiFont(Font.BOLD,12)); v.setForeground(UIUtils.WHITE);
            summary.add(v,sg);
            sRow[0]++;
        };

        double subtotal = cp.getPrice() * qty;
        addRow.accept("Product",   cp.getName());
        addRow.accept("Unit Price", String.format("RM %.2f", cp.getPrice()));
        addRow.accept("Quantity",  String.valueOf(qty));

        sg.gridy=sRow[0]; sg.gridx=0; sg.gridwidth=2;
        JSeparator sdiv=new JSeparator(); sdiv.setForeground(new Color(60,90,150));
        summary.add(sdiv,sg); sg.gridwidth=1; sRow[0]++;

        sg.gridy=sRow[0]; sg.gridx=0;
        JLabel tKey=new JLabel("Total"); tKey.setFont(UIUtils.uiFont(Font.BOLD,14));
        tKey.setForeground(UIUtils.ACCENT_BLUE); summary.add(tKey,sg);
        sg.gridx=1;
        JLabel tVal=new JLabel(String.format("RM %.2f",subtotal),JLabel.RIGHT);
        tVal.setFont(UIUtils.uiFont(Font.BOLD,18)); tVal.setForeground(CustomerDashboard.ACCENT_YELLOW);
        summary.add(tVal,sg);

        // Payment method
        JPanel methodRow=new JPanel(new BorderLayout(10,0));
        methodRow.setBackground(UIUtils.DARK_NAVY);
        JLabel mLbl=new JLabel("Payment Method:");
        mLbl.setFont(UIUtils.uiFont(Font.BOLD,12)); mLbl.setForeground(UIUtils.WHITE);
        JComboBox<String> methodCb=new JComboBox<>(
                new String[]{"Cash","Debit Card","Credit Card","E-Wallet","Online Transfer"});
        UIUtils.styleCombo(methodCb);
        methodRow.add(mLbl,   BorderLayout.WEST);
        methodRow.add(methodCb,BorderLayout.CENTER);

        // Buttons
        JPanel btnRow=new JPanel(new GridLayout(1,2,10,0));
        btnRow.setBackground(UIUtils.DARK_NAVY);
        UIUtils.RoundedButton cancelBtn=new UIUtils.RoundedButton("Cancel",new Color(60,80,120));
        cancelBtn.setFont(UIUtils.uiFont(Font.BOLD,12));
        cancelBtn.setPreferredSize(new Dimension(0,40));
        cancelBtn.addActionListener(e->dlg.dispose());
        UIUtils.RoundedButton confirmBtn=new UIUtils.RoundedButton(
                "âœ”  Confirm Purchase",UIUtils.SUCCESS_GREEN);
        confirmBtn.setFont(UIUtils.uiFont(Font.BOLD,12));
        confirmBtn.setPreferredSize(new Dimension(0,40));
        confirmBtn.addActionListener(e->{
            int ch=JOptionPane.showConfirmDialog(dlg,
                    String.format("Confirm %dÃ— %s for RM %.2f via %s?",
                            qty,cp.getName(),subtotal,methodCb.getSelectedItem()),
                    "Confirm",JOptionPane.YES_NO_OPTION);
            if(ch!=JOptionPane.YES_OPTION) return;
            Payment pay=FileManager.processCustomerTopup(
                    main.currentCustomer.getUserId(),subtotal,
                    methodCb.getSelectedItem().toString());
            JOptionPane.showMessageDialog(dlg,
                    String.format("âœ… Purchase successful!\nReceipt: %s\nTotal: RM %.2f",
                            pay.getReceiptNumber(),subtotal),
                    "Success",JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });
        btnRow.add(cancelBtn); btnRow.add(confirmBtn);

        outer.add(summary,   BorderLayout.NORTH);
        outer.add(methodRow, BorderLayout.CENTER);
        outer.add(btnRow,    BorderLayout.SOUTH);
        dlg.setContentPane(outer);
        dlg.setVisible(true);
    }

    // â”€â”€ Cart dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    void showCartDialog() {
        JDialog dlg = new JDialog((Dialog) null, "My Cart", true);
        dlg.setSize(580,520);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        java.util.Map<String,CarPart> partMap=new java.util.LinkedHashMap<>();
        for(CarPart p:FileManager.readAllCarParts()) partMap.put(p.getPartId(),p);

        JPanel outer=new JPanel(new BorderLayout(0,12));
        outer.setBackground(UIUtils.DARK_NAVY);
        outer.setBorder(new EmptyBorder(16,18,16,18));

        JLabel titleLbl=new JLabel("ðŸ›’  Shopping Cart");
        titleLbl.setFont(UIUtils.uiFont(Font.BOLD,16));
        titleLbl.setForeground(UIUtils.ACCENT_BLUE);
        titleLbl.setBorder(new EmptyBorder(0,0,4,0));

        JPanel itemsPanel=new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel,BoxLayout.Y_AXIS));
        itemsPanel.setBackground(UIUtils.DARK_NAVY);

        double[] totalRef={0.0};
        JLabel[] totalLblRef={null};
        JLabel[] countLblRef={null};
        Runnable[] rebuildCart={null};

        rebuildCart[0] = () -> {
            itemsPanel.removeAll();
            totalRef[0]=0;

            if (cartItems.isEmpty()) {
                JPanel ep=new JPanel(new GridBagLayout());
                ep.setBackground(UIUtils.DARK_NAVY);
                JPanel ei=new JPanel(); ei.setLayout(new BoxLayout(ei,BoxLayout.Y_AXIS));
                ei.setBackground(UIUtils.DARK_NAVY);
                JLabel ic2=new JLabel("ðŸ›’",JLabel.CENTER); ic2.setFont(new Font("Dialog",Font.PLAIN,48));
                ic2.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel t2=new JLabel("Your cart is empty",JLabel.CENTER);
                t2.setFont(UIUtils.uiFont(Font.BOLD,14)); t2.setForeground(UIUtils.DIM_TEXT);
                t2.setAlignmentX(Component.CENTER_ALIGNMENT);
                JLabel h2=new JLabel("Browse products and click \"Add to Cart\"",JLabel.CENTER);
                h2.setFont(UIUtils.uiFont(Font.PLAIN,11)); h2.setForeground(new Color(100,130,175));
                h2.setAlignmentX(Component.CENTER_ALIGNMENT);
                ei.add(Box.createVerticalStrut(20)); ei.add(ic2);
                ei.add(Box.createVerticalStrut(10)); ei.add(t2);
                ei.add(Box.createVerticalStrut(6));  ei.add(h2);
                ep.add(ei); itemsPanel.add(ep);
            } else {
                for (Map.Entry<String,Integer> entry : cartItems.entrySet()) {
                    CarPart cp=partMap.get(entry.getKey());
                    if(cp==null) continue;
                    int q=entry.getValue();
                    double lt=cp.getPrice()*q;
                    totalRef[0]+=lt;

                    JPanel row=new JPanel(new BorderLayout(10,0));
                    row.setBackground(UIUtils.TABLE_BG);
                    row.setBorder(BorderFactory.createCompoundBorder(
                            new UIUtils.RoundedBorder(new Color(50,80,130),10,1),
                            new EmptyBorder(10,12,10,12)));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE,70));
                    row.setAlignmentX(Component.LEFT_ALIGNMENT);

                    // Thumbnail
                    final Image tImg2 = (cp.getImageUrl()!=null&&!cp.getImageUrl().isEmpty()
                            &&new File(cp.getImageUrl()).exists())
                            ? new ImageIcon(cp.getImageUrl()).getImage() : null;
                    JPanel thumb=new JPanel(){
                        @Override protected void paintComponent(Graphics g){
                            Graphics2D g2=(Graphics2D)g.create();
                            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                            int w=getWidth(),h=getHeight();
                            g2.setColor(new Color(22,36,70)); g2.fillRoundRect(0,0,w,h,8,8);
                            if(tImg2!=null){
                                int iw=tImg2.getWidth(null),ih=tImg2.getHeight(null);
                                if(iw>0&&ih>0){
                                    double sc=Math.max((double)w/iw,(double)h/ih);
                                    int dw=(int)(iw*sc),dh=(int)(ih*sc);
                                    g2.setClip(0,0,w,h);
                                    g2.drawImage(tImg2,(w-dw)/2,(h-dh)/2,dw,dh,null);
                                }
                            } else {
                                g2.setFont(UIUtils.uiFont(Font.BOLD,16));
                                g2.setColor(new Color(60,100,160,100));
                                String lt2=cp.getName().isEmpty()?"?":
                                        String.valueOf(Character.toUpperCase(cp.getName().charAt(0)));
                                FontMetrics fm6=g2.getFontMetrics();
                                g2.drawString(lt2,(w-fm6.stringWidth(lt2))/2,
                                        (h+fm6.getAscent()-fm6.getDescent())/2);
                            }
                            g2.dispose();
                        }
                    };
                    thumb.setPreferredSize(new Dimension(48,48));
                    thumb.setOpaque(false);

                    // Centre info
                    JPanel info=new JPanel();
                    info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
                    info.setBackground(UIUtils.TABLE_BG);
                    JLabel nL=new JLabel(cp.getName());
                    nL.setFont(UIUtils.uiFont(Font.BOLD,12)); nL.setForeground(UIUtils.WHITE);
                    JLabel cL=new JLabel(cp.getCategory());
                    cL.setFont(UIUtils.uiFont(Font.PLAIN,10)); cL.setForeground(UIUtils.DIM_TEXT);
                    JLabel pL=new JLabel(String.format("RM %.2f each",cp.getPrice()));
                    pL.setFont(UIUtils.uiFont(Font.PLAIN,11)); pL.setForeground(UIUtils.DIM_TEXT);
                    info.add(nL); info.add(Box.createVerticalStrut(2));
                    info.add(cL); info.add(pL);

                    // Right: line total + qty controls
                    JPanel right=new JPanel(new GridLayout(2,1,0,4));
                    right.setBackground(UIUtils.TABLE_BG);

                    JLabel lineLbl=new JLabel(String.format("RM %.2f",lt),JLabel.RIGHT);
                    lineLbl.setFont(UIUtils.uiFont(Font.BOLD,13)); lineLbl.setForeground(CustomerDashboard.ACCENT_YELLOW);

                    JPanel qCtrl=new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));
                    qCtrl.setBackground(UIUtils.TABLE_BG);

                    UIUtils.RoundedButton mb=new UIUtils.RoundedButton("-",new Color(50,75,130));
                    UIUtils.RoundedButton pb=new UIUtils.RoundedButton("+",new Color(50,75,130));
                    UIUtils.RoundedButton rb=new UIUtils.RoundedButton("âœ•",new Color(160,40,40));
                    for(UIUtils.RoundedButton qb3:new UIUtils.RoundedButton[]{mb,pb,rb}){
                        qb3.setFont(UIUtils.uiFont(Font.BOLD,11));
                        qb3.setPreferredSize(new Dimension(26,22));
                    }
                    JLabel qL2=new JLabel("x"+q,JLabel.CENTER);
                    qL2.setFont(UIUtils.uiFont(Font.BOLD,12)); qL2.setForeground(UIUtils.WHITE);
                    qL2.setPreferredSize(new Dimension(28,22));

                    String pid=cp.getPartId();
                    mb.addActionListener(ev->{
                        int cur=cartItems.getOrDefault(pid,0);
                        if(cur>1) cartItems.put(pid,cur-1); else cartItems.remove(pid);
                        cartBadgeRefresh.run(); rebuildCart[0].run();
                    });
                    pb.addActionListener(ev->{
                        cartItems.merge(pid,1,Integer::sum);
                        cartBadgeRefresh.run(); rebuildCart[0].run();
                    });
                    rb.addActionListener(ev->{
                        cartItems.remove(pid);
                        cartBadgeRefresh.run(); rebuildCart[0].run();
                    });

                    qCtrl.add(mb); qCtrl.add(qL2); qCtrl.add(pb);
                    qCtrl.add(Box.createHorizontalStrut(6)); qCtrl.add(rb);
                    right.add(lineLbl); right.add(qCtrl);

                    row.add(thumb,BorderLayout.WEST);
                    row.add(info, BorderLayout.CENTER);
                    row.add(right,BorderLayout.EAST);
                    itemsPanel.add(row);
                    itemsPanel.add(Box.createVerticalStrut(8));
                }
            }
            if(totalLblRef[0]!=null)
                totalLblRef[0].setText(String.format("Total: RM %.2f",totalRef[0]));
            int ic3=cartItems.values().stream().mapToInt(Integer::intValue).sum();
            if(countLblRef[0]!=null)
                countLblRef[0].setText(ic3+" item"+(ic3!=1?"s":"")+" in cart");
            itemsPanel.revalidate(); itemsPanel.repaint();
        };

        JScrollPane scroll=new JScrollPane(itemsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        UIUtils.styleScrollPane(scroll); scroll.setBorder(null);

        // Footer
        JPanel footer=new JPanel(new BorderLayout(0,10)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.TABLE_BG); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.dispose();
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.ACCENT_BLUE,12,1),
                new EmptyBorder(12,16,12,16)));

        JPanel footerTop=new JPanel(new BorderLayout());
        footerTop.setBackground(UIUtils.TABLE_BG);
        JLabel countLbl3=new JLabel("0 items in cart");
        countLbl3.setFont(UIUtils.uiFont(Font.PLAIN,11)); countLbl3.setForeground(UIUtils.DIM_TEXT);
        countLblRef[0]=countLbl3;
        JLabel totalLbl3=new JLabel("Total: RM 0.00");
        totalLbl3.setFont(UIUtils.uiFont(Font.BOLD,16)); totalLbl3.setForeground(CustomerDashboard.ACCENT_YELLOW);
        totalLblRef[0]=totalLbl3;
        footerTop.add(countLbl3,BorderLayout.WEST);
        footerTop.add(totalLbl3,BorderLayout.EAST);

        JPanel footerBtns=new JPanel(new GridLayout(1,3,10,0));
        footerBtns.setBackground(UIUtils.TABLE_BG);

        UIUtils.RoundedButton clearBtn=new UIUtils.RoundedButton("ðŸ—‘  Clear",new Color(150,40,40));
        clearBtn.setFont(UIUtils.uiFont(Font.BOLD,11));
        clearBtn.addActionListener(e->{
            if(cartItems.isEmpty()) return;
            if(JOptionPane.showConfirmDialog(dlg,"Remove all items?",
                    "Clear Cart",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
                cartItems.clear(); cartBadgeRefresh.run(); rebuildCart[0].run();
            }
        });

        UIUtils.RoundedButton contBtn=new UIUtils.RoundedButton("Continue Shopping",new Color(50,75,130));
        contBtn.setFont(UIUtils.uiFont(Font.BOLD,11));
        contBtn.addActionListener(e->dlg.dispose());

        UIUtils.RoundedButton chkBtn=new UIUtils.RoundedButton("âœ”  Checkout",UIUtils.SUCCESS_GREEN);
        chkBtn.setFont(UIUtils.uiFont(Font.BOLD,12));
        chkBtn.addActionListener(e->{
            if(cartItems.isEmpty()){
                JOptionPane.showMessageDialog(dlg,"Your cart is empty!",
                        "Empty Cart",JOptionPane.WARNING_MESSAGE); return;
            }
            dlg.dispose();
            // TODO: implement showCheckoutDialog(partMap);
            JOptionPane.showMessageDialog(this, "Checkout feature coming soon!", "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        });

        footerBtns.add(clearBtn); footerBtns.add(contBtn); footerBtns.add(chkBtn);
        footer.add(footerTop, BorderLayout.NORTH);
        footer.add(footerBtns,BorderLayout.SOUTH);

        outer.add(titleLbl,BorderLayout.NORTH);
        outer.add(scroll,  BorderLayout.CENTER);
        outer.add(footer,  BorderLayout.SOUTH);
        rebuildCart[0].run();
        dlg.setContentPane(outer);
        dlg.setVisible(true);
    }


}
