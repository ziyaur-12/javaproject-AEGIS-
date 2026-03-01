

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.Timer;
import java.util.concurrent.CopyOnWriteArrayList;

// ==========================================
// 1. MAIN LAUNCHER
// ==========================================
public class AegisNetworkComplete {
    public static void main(String[] args) {
        // Enable Antialiasing for Text globally
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            AegisServer server = new AegisServer();
            server.setVisible(true);

            AegisClient client = new AegisClient(server);
            client.setVisible(true);
        });
    }
}

// ==========================================
// CUSTOM UI COMPONENTS (THE "GLOW UP")
// ==========================================
class ModernButton extends JButton {
    private Color hoverColor;
    private Color normalColor;
    private boolean isHovered = false;

    public ModernButton(String text, Color bg) {
        super(text);
        this.normalColor = bg;
        this.hoverColor = bg.brighter();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorder(new EmptyBorder(10, 20, 10, 20));
        setForeground(Color.WHITE);
        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { isHovered = true; repaint(); }
            public void mouseExited(MouseEvent e) { isHovered = false; repaint(); }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isHovered) {
            g2.setColor(hoverColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            // Glow effect
            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
        } else {
            g2.setColor(normalColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
        }
        super.paintComponent(g);
    }
}

class ModernPanel extends JPanel {
    private Color c1, c2;
    public ModernPanel(Color start, Color end) {
        this.c1 = start; this.c2 = end;
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}

// ==========================================
// 2. CLIENT WINDOW (FIELD APP)
// ==========================================
class AegisClient extends JFrame {
    private AegisServer serverRef;

    public AegisClient(AegisServer server) {
        this.serverRef = server;
        setTitle("FIELD REPORT APP (CLIENT)");
        setSize(450, 700);
        setLocation(50, 100); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Custom Dark Theme for Client too
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 250));
        
        JPanel header = new ModernPanel(new Color(220, 50, 50), new Color(180, 20, 20));
        header.setBorder(new EmptyBorder(20,10,20,10));
        JLabel title = new JLabel("EMERGENCY REPORTER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title);

        JPanel form = new JPanel(new GridLayout(6, 1, 15, 15));
        form.setBorder(new EmptyBorder(30, 30, 30, 30));
        form.setBackground(Color.WHITE);
        
        String[] cities = server.getCityList(); 
        JComboBox<String> cmbCity = new JComboBox<>(cities);
        styleCombo(cmbCity, "Select Affected City");

        JTextField txtPrecise = new JTextField();
        styleField(txtPrecise, "Precise Location (Sector/Area)");

        String[] disasters = {"Flood", "Earthquake", "Fire", "Medical", "Cyclone", "Landslide", "Chemical Leak"};
        JComboBox<String> cmbDisaster = new JComboBox<>(disasters);
        styleCombo(cmbDisaster, "Disaster Type");

        String[] items = {"Food Packs", "Med Kits", "Tents", "Boats", "Water", "Oxygen Cylinders", "Generators"};
        JComboBox<String> cmbItem = new JComboBox<>(items);
        styleCombo(cmbItem, "Required Item");

        JTextField txtQty = new JTextField("500");
        styleField(txtQty, "Quantity Needed");

        form.add(cmbCity); form.add(txtPrecise); form.add(cmbDisaster); form.add(cmbItem); form.add(txtQty);

        ModernButton btnSend = new ModernButton("TRANSMIT SOS SIGNAL 📡", new Color(0, 120, 60));
        btnSend.setPreferredSize(new Dimension(100, 50));
        
        btnSend.addActionListener(e -> {
            try {
                String city = (String) cmbCity.getSelectedItem();
                String area = txtPrecise.getText();
                String type = (String) cmbDisaster.getSelectedItem();
                String item = (String) cmbItem.getSelectedItem();
                int qty = Integer.parseInt(txtQty.getText());

                server.receiveRequest(city, area, type, item, qty);
                JOptionPane.showMessageDialog(this, "Signal Transmitted to HQ Successfully!");
                txtPrecise.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Data!");
            }
        });

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnSend);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    private void styleField(JTextField f, String title) {
        f.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), title, 0, 0, new Font("Segoe UI", Font.BOLD, 12)));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }
    private void styleCombo(JComboBox b, String title) {
        b.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY), title, 0, 0, new Font("Segoe UI", Font.BOLD, 12)));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBackground(Color.WHITE);
    }
}

// ==========================================
// 3. SERVER WINDOW (HQ COMMAND CENTER)
// ==========================================
class AegisServer extends JFrame {

    // --- COLORS ---
    final Color BG_DARK = new Color(15, 18, 28);
    final Color PANEL_BG = new Color(25, 30, 45);
    final Color SIDEBAR_BG = new Color(18, 22, 33);
    final Color ACCENT_RED = new Color(255, 80, 80);
    final Color ACCENT_CYAN = new Color(0, 220, 255);
    final Color ACCENT_GREEN = new Color(50, 220, 100);
    final Color ACCENT_YELLOW = new Color(255, 200, 0); 
    final Color ACCENT_PURPLE = new Color(180, 80, 255);
    final Color TEXT_WHITE = new Color(240, 240, 255);
    
    static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 22);
    static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
    
    // --- PHYSICS ---
    final double PIXELS_TO_KM = 4.0;
    final double TRUCK_SPEED = 60.0;
    final double HELI_SPEED = 220.0;
    final double DRONE_SPEED = 150.0;
    final int SIMULATION_TICK_MS = 100; 
    
    double timeMultiplier = 500.0; 
    double radarAngle = 0; // For Radar Effect

    // --- STATE ---
    int availableTrucks = 50, availableHelis = 15, availableDrones = 120; 

    // --- MODELS ---
    DefaultTableModel requestModel, dispatchModel, inventoryModel, hospitalModel, blockchainModel;
    List<NodeEntity> cityNodes = new ArrayList<>();
    List<NodeEntity> warehouseNodes = new ArrayList<>();
    List<VehicleEntity> activeVehicles = new CopyOnWriteArrayList<>();
    
    // --- UI COMPONENTS ---
    CardLayout cardLayout = new CardLayout();
    JPanel centerContentPanel = new JPanel(cardLayout);
    MapPanel mapContainer;
    JTextArea liveLogConsole;
    JLabel lblFleetStatus;
    JSlider speedSlider;
    
    ModernButton btnMonitor, btnLogistics, btnInventory, btnHospital, btnAnalytics, btnBlockchain;

    public AegisServer() {
        setTitle("A.E.G.I.S. HQ SERVER - ULTIMATE MAX EDITION");
        setSize(1450, 950);
        setLocation(500, 50); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initData();
        initTables();

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        centerContentPanel.setBackground(BG_DARK);
        add(centerContentPanel, BorderLayout.CENTER);

        // Add Screens
        centerContentPanel.add(createMonitorScreen(), "MONITOR");
        centerContentPanel.add(createLogisticsScreen(), "LOGISTICS");
        centerContentPanel.add(createInventoryScreen(), "INVENTORY");
        centerContentPanel.add(createHospitalScreen(), "HOSPITALS");
        centerContentPanel.add(createAnalyticsScreen(), "ANALYTICS");
        centerContentPanel.add(createBlockchainScreen(), "BLOCKCHAIN");

        new Timer(SIMULATION_TICK_MS, e -> updateSimulation()).start();
        updateStatsLabel();
    }

    public String[] getCityList() {
        return cityNodes.stream().map(n -> n.name).toArray(String[]::new);
    }

    public void receiveRequest(String city, String area, String type, String item, int qty) {
        String reqID = "RQ-" + System.currentTimeMillis() % 1000;
        String qStr = qty + " " + item;
        
        requestModel.addRow(new Object[]{reqID, city, area, type, qStr, "PENDING"});
        triggerAlert(city); 
        
        liveLogConsole.append(">> [SOS] " + qStr + " needed at " + city + " (" + type + ")\n");
        addBlockchainBlock("REMOTE_REQ", "Incident at " + city);
        cardLayout.show(centerContentPanel, "MONITOR");
    }

    // --- DATA INIT ---
    private void initData() {
        warehouseNodes.add(new NodeEntity("Delhi Hub", 380, 180, false));
        warehouseNodes.add(new NodeEntity("Mumbai Port", 180, 450, false));
        warehouseNodes.add(new NodeEntity("Kolkata Depot", 750, 350, false));
        warehouseNodes.add(new NodeEntity("Chennai Hub", 420, 650, false));
        warehouseNodes.add(new NodeEntity("Nagpur Center", 400, 400, false));
        warehouseNodes.add(new NodeEntity("Guwahati Base", 800, 220, false));

        // 55 Major Cities (Sorted)
        cityNodes.add(new NodeEntity("Srinagar", 320, 80, false));
        cityNodes.add(new NodeEntity("Jammu", 330, 100, false));
        cityNodes.add(new NodeEntity("Shimla", 360, 110, false));
        cityNodes.add(new NodeEntity("Chandigarh", 350, 140, false));
        cityNodes.add(new NodeEntity("Dehradun", 390, 130, false));
        cityNodes.add(new NodeEntity("Delhi", 380, 180, false));
        cityNodes.add(new NodeEntity("Jaipur", 300, 220, false));
        cityNodes.add(new NodeEntity("Lucknow", 480, 220, false));
        cityNodes.add(new NodeEntity("Kanpur", 490, 240, false));
        cityNodes.add(new NodeEntity("Varanasi", 550, 260, false));
        cityNodes.add(new NodeEntity("Agra", 400, 210, false));
        cityNodes.add(new NodeEntity("Patna", 600, 260, false));
        cityNodes.add(new NodeEntity("Guwahati", 800, 220, true)); // Flooded
        cityNodes.add(new NodeEntity("Shillong", 810, 230, false));
        cityNodes.add(new NodeEntity("Imphal", 850, 250, true));
        cityNodes.add(new NodeEntity("Gangtok", 700, 180, false));
        cityNodes.add(new NodeEntity("Ahmedabad", 200, 320, false));
        cityNodes.add(new NodeEntity("Surat", 200, 390, false));
        cityNodes.add(new NodeEntity("Bhopal", 380, 350, false));
        cityNodes.add(new NodeEntity("Indore", 340, 360, false));
        cityNodes.add(new NodeEntity("Ranchi", 620, 320, false));
        cityNodes.add(new NodeEntity("Jamshedpur", 640, 340, false));
        cityNodes.add(new NodeEntity("Kolkata", 750, 350, false));
        cityNodes.add(new NodeEntity("Mumbai", 180, 450, false));
        cityNodes.add(new NodeEntity("Pune", 220, 480, false));
        cityNodes.add(new NodeEntity("Nashik", 230, 430, false));
        cityNodes.add(new NodeEntity("Aurangabad", 260, 440, false));
        cityNodes.add(new NodeEntity("Nagpur", 400, 400, false));
        cityNodes.add(new NodeEntity("Raipur", 500, 380, false));
        cityNodes.add(new NodeEntity("Bhubaneswar", 650, 400, false));
        cityNodes.add(new NodeEntity("Hyderabad", 400, 500, false));
        cityNodes.add(new NodeEntity("Warangal", 420, 480, false));
        cityNodes.add(new NodeEntity("Visakhapatnam", 550, 500, false));
        cityNodes.add(new NodeEntity("Vijayawada", 480, 530, false));
        cityNodes.add(new NodeEntity("Goa", 220, 580, false));
        cityNodes.add(new NodeEntity("Bangalore", 380, 600, false));
        cityNodes.add(new NodeEntity("Mangalore", 320, 610, false));
        cityNodes.add(new NodeEntity("Mysore", 360, 620, false));
        cityNodes.add(new NodeEntity("Chennai", 420, 650, false));
        cityNodes.add(new NodeEntity("Coimbatore", 370, 660, false));
        cityNodes.add(new NodeEntity("Madurai", 390, 700, false));
        cityNodes.add(new NodeEntity("Kochi", 340, 700, false));
        cityNodes.add(new NodeEntity("Trivandrum", 360, 750, false));
        cityNodes.add(new NodeEntity("Kozhikode", 330, 660, false));
    }

    private void initTables() {
        requestModel = new DefaultTableModel(new String[]{"Req ID", "Location", "Precise Area", "Disaster", "Items Needed", "Status"}, 0);
        dispatchModel = new DefaultTableModel(new String[]{"ID", "Vehicle", "Target", "Payload", "ETA", "Status"}, 0);
        blockchainModel = new DefaultTableModel(new String[]{"Block Hash", "Timestamp", "Action", "Details", "Validator"}, 0);
        
        inventoryModel = new DefaultTableModel(new String[]{"Hub Location", "Item Name", "Stock Level", "Category", "Status"}, 0);
        String[][] baseItems = {{"Food Packs", "50000", "Food"}, {"Medical Kits", "20000", "Medical"}, {"Tents", "8000", "Shelter"}, {"Rescue Boats", "200", "Rescue"}};
        for(NodeEntity w : warehouseNodes) for(String[] item : baseItems) inventoryModel.addRow(new Object[]{w.name, item[0], item[1], item[2]});

        hospitalModel = new DefaultTableModel(new String[]{"City", "Hospital Name", "Status", "ICU Beds", "Oxygen %", "Ambulances"}, 0);
        for(NodeEntity c : cityNodes) {
            String status = (c.name.length() % 2 == 0) ? "OPERATIONAL" : "BUSY";
            hospitalModel.addRow(new Object[]{c.name, "City General", status, "45", "98%", "10"});
        }
    }

    private void addBlockchainBlock(String action, String details) {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String hash = Integer.toHexString((action + details + time).hashCode()).toUpperCase();
        while(hash.length() < 16) hash += "X"; 
        blockchainModel.insertRow(0, new Object[]{"0x" + hash, time, action, details, "NODE-A1"});
    }

    // --- PHYSICS & SIMULATION ---
    private void updateSimulation() {
        double hoursPassed = (SIMULATION_TICK_MS / 3600000.0) * timeMultiplier;
        radarAngle += 0.1; // Radar rotation

        for(VehicleEntity v : activeVehicles) {
            double speed = v.type.equals("DRONE") ? DRONE_SPEED : (v.type.equals("HELICOPTER") ? HELI_SPEED : TRUCK_SPEED);
            double distCovered = speed * hoursPassed;
            
            v.traveledKm += distCovered;
            v.progress = v.traveledKm / v.totalDistanceKm;
            double remainingHours = (v.totalDistanceKm - v.traveledKm) / speed;
            v.etaString = formatRealTime(remainingHours);

            if(v.progress >= 1.0) {
                v.progress = 1.0;
                v.etaString = "ARRIVED";
                if(!v.delivered) { v.delivered = true; handleDelivery(v); }
            }
        }
        activeVehicles.removeIf(v -> v.delivered && v.progress >= 1.0);
        
        for(int i=0; i<dispatchModel.getRowCount(); i++) {
            String dId = (String) dispatchModel.getValueAt(i, 0);
            for(VehicleEntity v : activeVehicles) {
                if(v.dispatchId.equals(dId)) dispatchModel.setValueAt(v.etaString, i, 4);
            }
        }
        if(mapContainer.isVisible()) mapContainer.repaint();
    }

    private String formatRealTime(double hours) {
        if(hours <= 0) return "Arriving...";
        int d = (int) (hours / 24); int h = (int) (hours % 24); int m = (int) ((hours - (int)hours) * 60);
        if(d > 0) return d + "d " + h + "h " + m + "m";
        return h + "h " + m + "m";
    }

    private void handleDelivery(VehicleEntity v) {
        liveLogConsole.append(">> " + v.type + " REACHED " + v.target.name + "\n");
        if(v.type.equals("TRUCK")) availableTrucks++; else if(v.type.equals("HELICOPTER")) availableHelis++; else availableDrones++;
        updateStatsLabel();
        for(int i=0; i<requestModel.getRowCount(); i++) if(requestModel.getValueAt(i, 0).equals(v.reqId)) requestModel.setValueAt("FULFILLED", i, 5);
        for(int i=0; i<dispatchModel.getRowCount(); i++) if(dispatchModel.getValueAt(i, 0).equals(v.dispatchId)) dispatchModel.setValueAt("COMPLETED", i, 5);
        addBlockchainBlock("DELIVERY", "Confirmed at " + v.target.name);
        JOptionPane.showMessageDialog(this, "DELIVERY SUCCESSFUL!\nVehicle: " + v.type + "\nTarget: " + v.target.name);
    }

    // --- SCREENS ---
    private JPanel createMonitorScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        mapContainer = new MapPanel();
        JPanel right = new JPanel(new BorderLayout()); right.setPreferredSize(new Dimension(450,0)); right.setBackground(PANEL_BG);
        right.setBorder(new EmptyBorder(15,15,15,15));

        liveLogConsole = new JTextArea(); liveLogConsole.setBackground(new Color(15,15,20)); liveLogConsole.setForeground(ACCENT_GREEN); liveLogConsole.setFont(FONT_MONO);
        liveLogConsole.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        
        // SLIDER CONTROL
        JPanel controlPanel = new JPanel(new BorderLayout()); controlPanel.setBackground(PANEL_BG);
        controlPanel.setBorder(BorderFactory.createTitledBorder(null, "TIME DILATION CONTROL", 0, 0, FONT_MONO, ACCENT_YELLOW));
        speedSlider = new JSlider(1, 1000, 500); speedSlider.setBackground(PANEL_BG); speedSlider.setForeground(ACCENT_CYAN);
        JLabel lblTimeScale = new JLabel("Sim Speed: 500x"); lblTimeScale.setForeground(TEXT_WHITE); lblTimeScale.setHorizontalAlignment(SwingConstants.CENTER);
        speedSlider.addChangeListener(e -> {
            timeMultiplier = speedSlider.getValue();
            lblTimeScale.setText("Sim Speed: " + (int)timeMultiplier + "x");
        });
        controlPanel.add(lblTimeScale, BorderLayout.NORTH); controlPanel.add(speedSlider, BorderLayout.CENTER);

        ModernButton btnDrone = new ModernButton("👁 DRONE VISION SCAN", ACCENT_CYAN);
        btnDrone.addActionListener(e -> simulateDroneVision());

        JPanel bot = new JPanel(new BorderLayout());
        bot.setBackground(PANEL_BG);
        bot.add(btnDrone, BorderLayout.CENTER);

        right.add(controlPanel, BorderLayout.NORTH);
        right.add(new JScrollPane(liveLogConsole), BorderLayout.CENTER);
        right.add(bot, BorderLayout.SOUTH);

        panel.add(mapContainer, BorderLayout.CENTER); panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private void simulateDroneVision() {
        JDialog d = new JDialog(this, "DRONE FEED", true); d.setSize(500, 400); d.setLocationRelativeTo(this);
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); g.setColor(Color.BLACK); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(ACCENT_GREEN); g.drawRect(50,50,getWidth()-100,getHeight()-100);
                g.drawString("SCANNING SECTOR 7...", 60, 40); g.drawString("DAMAGE: CRITICAL (85%)", 60, getHeight()-20);
                g.setColor(Color.RED); g.drawString("OBSTACLE DETECTED", 200, 200); g.drawRect(180, 180, 100, 40);
            }
        };
        d.add(p); d.setVisible(true);
    }

    private JPanel createLogisticsScreen() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        panel.setBackground(BG_DARK);
        
        JPanel p1 = new JPanel(new BorderLayout()); p1.setBackground(PANEL_BG);
        p1.setBorder(BorderFactory.createTitledBorder(null, "PENDING REQUESTS", 0, 0, FONT_HEADER, ACCENT_RED));
        JTable reqTable = createDarkTable(requestModel);
        p1.add(new JScrollPane(reqTable), BorderLayout.CENTER);
        
        ModernButton btnDispatch = new ModernButton("ASSESS ROUTE & DISPATCH", ACCENT_GREEN);
        
        btnDispatch.addActionListener(e -> {
            int row = reqTable.getSelectedRow();
            if(row == -1) { JOptionPane.showMessageDialog(this, "Select a Request!"); return; }
            String city = requestModel.getValueAt(row, 1).toString();
            String reqId = requestModel.getValueAt(row, 0).toString();
            String item = requestModel.getValueAt(row, 4).toString();
            NodeEntity target = findNode(city, cityNodes);
            
            // Calculate Nearest Hub
            double minDist = Double.MAX_VALUE;
            NodeEntity tempHub = warehouseNodes.get(0);
            for(NodeEntity w : warehouseNodes) {
                double d = w.p.distance(target.p);
                if(d < minDist) { minDist=d; tempHub=w; }
            }
            final NodeEntity finalHub = tempHub; 
            final double finalDist = minDist;

            JDialog d = new JDialog(this, "FLEET SELECTOR", true); d.setSize(350, 250); d.setLayout(new GridLayout(4,1));
            JComboBox<String> cmbV = new JComboBox<>(new String[]{"TRUCK","HELICOPTER","DRONE"});
            ModernButton b = new ModernButton("LAUNCH MISSION", ACCENT_RED);
            
            b.addActionListener(ev -> {
                String vType = (String)cmbV.getSelectedItem();
                if(target.isFlooded && vType.equals("TRUCK")) { JOptionPane.showMessageDialog(d, "TARGET FLOODED! USE AIR UNIT."); return; }
                
                if(vType.equals("TRUCK")) availableTrucks--; else if(vType.equals("HELICOPTER")) availableHelis--; else availableDrones--;
                updateStatsLabel();
                
                target.isAlert = false; // STOP RED BLINK HERE
                mapContainer.repaint();
                
                double realDist = finalDist * PIXELS_TO_KM;
                String did = "DSP-"+System.currentTimeMillis()%100;
                VehicleEntity v = new VehicleEntity(finalHub, target, item, did, realDist, vType, reqId);
                activeVehicles.add(v);
                
                dispatchModel.addRow(new Object[]{v.dispatchId, vType, city, item, "Calc...", "IN TRANSIT"});
                addBlockchainBlock("DISPATCH", vType + " to " + city);
                d.dispose();
                JOptionPane.showMessageDialog(this, "DISPATCH CONFIRMED: " + vType + " -> " + city);
            });
            d.add(new JLabel(" Nearest Hub: " + finalHub.name)); 
            d.add(new JLabel(" Dist: " + (int)(finalDist*PIXELS_TO_KM) + " KM"));
            d.add(cmbV); d.add(b); d.setVisible(true);
        });
        p1.add(btnDispatch, BorderLayout.SOUTH);
        
        JPanel p2 = new JPanel(new BorderLayout()); p2.setBackground(PANEL_BG);
        p2.setBorder(BorderFactory.createTitledBorder(null, "ACTIVE FLEET (REAL-TIME ETA)", 0, 0, FONT_HEADER, ACCENT_CYAN));
        p2.add(new JScrollPane(createDarkTable(dispatchModel)), BorderLayout.CENTER);
        panel.add(p1); panel.add(p2); return panel;
    }

    private JPanel createAnalyticsScreen() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG_DARK); p.setBorder(new EmptyBorder(20,20,20,20));
        JLabel title = new JLabel("AI DEMAND PREDICTION (LSTM)"); title.setForeground(ACCENT_PURPLE); title.setFont(FONT_HEADER);
        JPanel graph = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(50,50,60)); g2.drawLine(50, h-50, w-50, h-50);
                int[] data = {50, 80, 40, 120, 90, 150, 200};
                for(int i=0; i<data.length; i++) {
                    g2.setColor(i>4 ? ACCENT_RED : ACCENT_CYAN);
                    g2.fillRect(50 + i*100, h-50-data[i], 50, data[i]);
                }
            }
        };
        graph.setBackground(PANEL_BG); graph.setBorder(new LineBorder(ACCENT_PURPLE));
        p.add(title, BorderLayout.NORTH); p.add(graph, BorderLayout.CENTER); return p;
    }

    private JPanel createBlockchainScreen() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(10,10,10,10));
        JTable t = createDarkTable(blockchainModel); 
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createTitledBorder(null, "IMMUTABLE AUDIT LEDGER", 0, 0, FONT_HEADER, ACCENT_YELLOW));
        p.add(sp, BorderLayout.CENTER); return p;
    }
    private JPanel createInventoryScreen() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(10,10,10,10));
        JTable t = createDarkTable(inventoryModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER); return p;
    }
    private JPanel createHospitalScreen() {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(BG_DARK);
        p.setBorder(new EmptyBorder(10,10,10,10));
        JTable t = createDarkTable(hospitalModel);
        p.add(new JScrollPane(t), BorderLayout.CENTER); return p;
    }

    // --- VISUALS & HELPERS ---
    class MapPanel extends JPanel {
        public MapPanel() { setBackground(new Color(10, 14, 22)); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Grid
            g2.setColor(new Color(255,255,255,5));
            for(int i=0; i<getWidth(); i+=50) g2.drawLine(i,0,i,getHeight());
            for(int i=0; i<getHeight(); i+=50) g2.drawLine(0,i,getWidth(),i);
            
            // Radar Scan Effect
            int cx = getWidth()/2, cy = getHeight()/2;
            g2.setColor(new Color(0, 255, 255, 10));
            g2.fillArc(cx-400, cy-400, 800, 800, (int)Math.toDegrees(radarAngle), 40);

            g2.setStroke(new BasicStroke(1));
            for(NodeEntity w : warehouseNodes) for(NodeEntity c : cityNodes) if(w.p.distance(c.p)<250) { g2.setColor(new Color(0,255,255,5)); g2.drawLine(w.p.x, w.p.y, c.p.x, c.p.y); }
            
            for(NodeEntity n : cityNodes) {
                if(n.isFlooded) { g2.setColor(new Color(0,100,255,100)); g2.fillOval(n.p.x-10, n.p.y-10, 20, 20); }
                if(n.isAlert) { 
                    g2.setColor(ACCENT_RED); 
                    g2.drawOval(n.p.x-15, n.p.y-15, 30, 30); 
                    g2.setColor(new Color(255, 80, 80, 50)); // Glow
                    g2.fillOval(n.p.x-15, n.p.y-15, 30, 30);
                }
                g2.setColor(n.isFlooded ? ACCENT_CYAN : Color.GRAY); g2.fillOval(n.p.x-3, n.p.y-3, 6, 6);
                g2.setColor(TEXT_WHITE); g2.setFont(new Font("SansSerif", Font.PLAIN, 9)); g2.drawString(n.name, n.p.x+5, n.p.y);
            }
            for(NodeEntity w : warehouseNodes) { g2.setColor(ACCENT_GREEN); g2.fillRect(w.p.x-5, w.p.y-5, 10, 10); }
            for(VehicleEntity v : activeVehicles) { g2.setColor(ACCENT_YELLOW); Point p = v.getCurrentPos(); g2.fillOval(p.x-4, p.y-4, 8, 8); }
        }
    }

    class NodeEntity { String name; Point p; boolean isAlert=false; boolean isFlooded; public NodeEntity(String n, int x, int y, boolean f) { name=n; p=new Point(x,y); isFlooded=f;} }
    class VehicleEntity {
        NodeEntity source, target; double progress=0.0, traveledKm=0.0, totalDistanceKm; String payload, dispatchId, etaString, type, reqId; boolean delivered=false;
        public VehicleEntity(NodeEntity s, NodeEntity t, String p, String did, double dist, String vType, String rid) { source=s; target=t; payload=p; dispatchId=did; totalDistanceKm=dist; type=vType; reqId=rid; }
        public Point getCurrentPos() { int x = (int)(source.p.x + (target.p.x - source.p.x)*progress); int y = (int)(source.p.y + (target.p.y - source.p.y)*progress); return new Point(x,y); }
    }
    private String formatTime(double hours) { int h=(int)hours; int m=(int)((hours-h)*60); return h+"h "+m+"m"; }
    private NodeEntity findNode(String name, List<NodeEntity> list) { return list.stream().filter(n->n.name.equalsIgnoreCase(name)).findFirst().orElse(null); }
    private void triggerAlert(String city) { NodeEntity n=findNode(city, cityNodes); if(n!=null) { n.isAlert=true; } } 
    private void updateStatsLabel() { lblFleetStatus.setText("TRUCKS: " + availableTrucks + " | HELIS: " + availableHelis + " | DRONES: " + availableDrones); }
    
    private JPanel createHeader() {
        ModernPanel p = new ModernPanel(new Color(25, 30, 45), new Color(15, 20, 30));
        p.setLayout(new BorderLayout()); p.setBorder(new EmptyBorder(15,20,15,20));
        JLabel l = new JLabel("A.E.G.I.S. HQ SERVER"); l.setForeground(TEXT_WHITE); l.setFont(FONT_HEADER);
        lblFleetStatus = new JLabel("LOADING..."); lblFleetStatus.setForeground(ACCENT_YELLOW); lblFleetStatus.setFont(new Font("Consolas", Font.BOLD, 14));
        p.add(l, BorderLayout.WEST); p.add(lblFleetStatus, BorderLayout.EAST); return p;
    }
    
    private JPanel createSidebar() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(SIDEBAR_BG); p.setPreferredSize(new Dimension(220, 0));
        p.setBorder(new EmptyBorder(20, 10, 20, 10));
        
        btnMonitor = createNavBtn("MONITOR", "MONITOR");
        btnLogistics = createNavBtn("LOGISTICS", "LOGISTICS");
        btnInventory = createNavBtn("INVENTORY", "INVENTORY");
        btnHospital = createNavBtn("HOSPITALS", "HOSPITALS");
        btnAnalytics = createNavBtn("ANALYTICS", "ANALYTICS");
        btnBlockchain = createNavBtn("BLOCKCHAIN", "BLOCKCHAIN");
        
        p.add(btnMonitor); p.add(Box.createVerticalStrut(10));
        p.add(btnLogistics); p.add(Box.createVerticalStrut(10));
        p.add(btnInventory); p.add(Box.createVerticalStrut(10));
        p.add(btnHospital); p.add(Box.createVerticalStrut(10));
        p.add(btnAnalytics); p.add(Box.createVerticalStrut(10));
        p.add(btnBlockchain);
        return p;
    }
    
    private ModernButton createNavBtn(String t, String c) {
        ModernButton b = new ModernButton(t, new Color(40, 50, 70));
        b.setMaximumSize(new Dimension(200, 45));
        b.addActionListener(e -> cardLayout.show(centerContentPanel, c)); return b;
    }
    
    private JTable createDarkTable(DefaultTableModel m) {
        JTable t = new JTable(m); 
        t.setBackground(PANEL_BG); 
        t.setForeground(TEXT_WHITE); 
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.getTableHeader().setBackground(new Color(30, 35, 50)); 
        t.getTableHeader().setForeground(ACCENT_CYAN);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        return t;
    }
}