package asc.gui.shared;

import asc.model.Customer;
import asc.util.*;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Registration dialog – Customer self-registration only.
 * Account is created immediately (no Manager approval needed).
 * Other roles (Manager, Counter Staff, Technician) are created by the Manager.
 */
public class RegisterDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private JTextField     uF, nF, eF, phF;
    private JPasswordField pwF, cpwF;
    private JComboBox<String> genderBox;

    private File   chosenAvatarFile = null;
    private JLabel avatarPreviewLabel;

    public RegisterDialog(Frame parent) {
        super(parent, "Registration", true);
        initComponents();
    }

    private void initComponents() {
        setSize(500, 720);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.DARK_NAVY);

        // ── Top: avatar + title ──────────────────────────────────────────────
        JPanel avatarPanel = new JPanel();
        avatarPanel.setBackground(UIUtils.MEDIUM_NAVY);
        avatarPanel.setBorder(new EmptyBorder(18, 10, 14, 10));
        avatarPanel.setLayout(new BoxLayout(avatarPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("APU-ASC Customer Registration", JLabel.CENTER);
        titleLbl.setFont(UIUtils.uiFont(Font.BOLD, 16));
        titleLbl.setForeground(UIUtils.WHITE);
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Your account will be ready immediately after registration", JLabel.CENTER);
        subLbl.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        subLbl.setForeground(UIUtils.GREEN_OK);
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        avatarPreviewLabel = new JLabel(UIUtils.loadCircularAvatar(null, "", 72));
        avatarPreviewLabel.setAlignmentX(CENTER_ALIGNMENT);
        avatarPreviewLabel.setToolTipText("Click to choose a profile picture");
        avatarPreviewLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarPreviewLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { pickAvatar(); }
        });

        JLabel hintLbl = new JLabel("Click avatar to upload photo (optional)", JLabel.CENTER);
        hintLbl.setFont(UIUtils.uiFont(Font.ITALIC, 11));
        hintLbl.setForeground(UIUtils.LIGHT_GRAY);
        hintLbl.setAlignmentX(CENTER_ALIGNMENT);

        avatarPanel.add(titleLbl);
        avatarPanel.add(Box.createVerticalStrut(4));
        avatarPanel.add(subLbl);
        avatarPanel.add(Box.createVerticalStrut(8));
        avatarPanel.add(avatarPreviewLabel);
        avatarPanel.add(Box.createVerticalStrut(4));
        avatarPanel.add(hintLbl);

        // ── Centre: form fields ──────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIUtils.DARK_NAVY);
        form.setBorder(new EmptyBorder(12, 32, 10, 32));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        uF   = new JTextField();     UIUtils.styleTextField(uF);
        pwF  = new JPasswordField(); UIUtils.styleTextField(pwF);
        cpwF = new JPasswordField(); UIUtils.styleTextField(cpwF);
        nF   = new JTextField();     UIUtils.styleTextField(nF);
        eF   = new JTextField();     UIUtils.styleTextField(eF);
        phF  = new JTextField();     UIUtils.styleTextField(phF);

        genderBox = UIUtils.createGenderCombo("");
        genderBox.addActionListener(e -> {
            if (chosenAvatarFile == null) {
                String g = UIUtils.resolveGender(genderBox);
                avatarPreviewLabel.setIcon(UIUtils.loadCircularAvatar(null, g, 72));
            }
        });

        // Role is fixed to CUSTOMER — other roles are created by Manager only
        JLabel roleValueLbl = new JLabel("CUSTOMER");
        roleValueLbl.setForeground(UIUtils.ACCENT_BLUE);
        roleValueLbl.setFont(UIUtils.uiFont(Font.BOLD, 13));

        addFormRow(form, gbc, 0, "Role:",          roleValueLbl);
        addFormRow(form, gbc, 1, "Username:",      uF);
        addFormRow(form, gbc, 2, "Password:",      pwF);
        addFormRow(form, gbc, 3, "Confirm Pass:",  cpwF);
        addFormRow(form, gbc, 4, "Full Name:",     nF);
        addFormRow(form, gbc, 5, "Gender:",        genderBox);
        addFormRow(form, gbc, 6, "Email:",         eF);
        addFormRow(form, gbc, 7, "Phone:",         phF);

        // ── Bottom: register button ──────────────────────────────────────────
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(UIUtils.DARK_NAVY);
        btnPanel.setBorder(new EmptyBorder(5, 32, 16, 32));

        JButton regBtn = new UIUtils.RoundedButton("Submit Registration", UIUtils.ACCENT_BLUE);
        regBtn.setPreferredSize(new Dimension(220, 38));
        regBtn.addActionListener(e -> doRegister());
        btnPanel.add(regBtn);

        root.add(avatarPanel, BorderLayout.NORTH);
        root.add(form,        BorderLayout.CENTER);
        root.add(btnPanel,    BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void pickAvatar() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose Profile Picture");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image Files (*.png,*.jpg,*.jpeg)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chosenAvatarFile = chooser.getSelectedFile();
            try {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(chosenAvatarFile);
                if (img != null) {
                    int d = 72;
                    java.awt.image.BufferedImage circle =
                            new java.awt.image.BufferedImage(d, d, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = circle.createGraphics();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, d, d));
                    g2.drawImage(img, 0, 0, d, d, null);
                    g2.dispose();
                    avatarPreviewLabel.setIcon(new ImageIcon(circle));
                }
            } catch (Exception ignored) {}
        }
    }

    private void doRegister() {
        String username = uF.getText().trim();
        String password = new String(pwF.getPassword());
        String confirm  = new String(cpwF.getPassword());
        String name     = nF.getText().trim();
        String gender   = UIUtils.resolveGender(genderBox);
        String email    = eF.getText().trim();
        String phone    = phF.getText().trim();

        // Validation
        if (username.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            msg("All fields are required.", JOptionPane.WARNING_MESSAGE); return;
        }
        if (password.isEmpty()) {
            msg("Password is required.", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!password.equals(confirm)) {
            msg("Passwords do not match.", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            msg("Invalid email format.", JOptionPane.WARNING_MESSAGE); return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            msg("Phone must be 10\u201311 digits.", JOptionPane.WARNING_MESSAGE); return;
        }
        if (FileManager.isUsernameExists(username)) {
            msg("Username already taken.", JOptionPane.WARNING_MESSAGE); return;
        }

        // Create customer account immediately — no approval needed, vehicle added later
        String custId = FileManager.generateCustomerId();
        Customer newCustomer = new Customer(
                custId, username, password, name, email, phone, "", gender);
        FileManager.saveCustomer(newCustomer);

        // Save avatar if chosen
        if (chosenAvatarFile != null) UIUtils.saveAvatar(custId, chosenAvatarFile);

        JOptionPane.showMessageDialog(this,
                "Registration successful!\n\nWelcome to APU-ASC, " + name + "!\n" +
                "Your Customer ID: " + custId + "\nYou can log in now.",
                "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void msg(String text, int type) {
        JOptionPane.showMessageDialog(this, text, "Validation", type);
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0.38;
        JLabel l = new JLabel(label);
        l.setForeground(UIUtils.WHITE);
        l.setFont(UIUtils.uiFont(Font.BOLD, 12));
        p.add(l, gbc);
        gbc.gridx = 1; gbc.weightx = 0.62;
        p.add(comp, gbc);
    }
}
