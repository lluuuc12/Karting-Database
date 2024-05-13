package com.hibernate.gui;

import java.awt.EventQueue;
import java.awt.Image;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import com.hibernate.dao.DriverDAO;
import com.hibernate.dao.KartDAO;
import com.hibernate.dao.LapDAO;
import com.hibernate.dao.TeamDAO;
import com.hibernate.model.Driver;
import com.hibernate.model.Kart;
import com.hibernate.model.Lap;
import com.hibernate.model.Team;
import com.hibernate.util.LapTimer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import javax.swing.JComboBox;
import java.awt.Color;

class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
	private String datePattern = "yyyy-MM-dd";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

	@Override
	public Object stringToValue(String text) throws ParseException {
		return dateFormatter.parseObject(text);
	}

	@Override
	public String valueToString(Object value) throws ParseException {
		if (value != null) {
			Calendar cal = (Calendar) value;
			return dateFormatter.format(cal.getTime());
		}
		return "";
	}
}

public class App {

	// swing variables
	private JFrame frmKartingdatabase;
	private JTextField textFieldDriverName;
	private JTextField textFieldLaps;
	private JTextField textFieldRaces;
	private JTextField textFieldPodiums;
	private JTextField textFieldWins;
	private JTextField textFieldDriverImageText;
	private JLabel lblDriverImg;
	private JTextField textFieldTeamName;
	private JTextField textFieldTeamImageText;
	private JLabel lblTeamImg;

	// tables variables
	private DefaultTableModel driverModel;
	private JTable driversTable;

	private DefaultTableModel teamModel;
	private JTable teamTable;

	private DefaultTableModel kartModel;
	private JTable kartTable;
	
	private DefaultTableModel lapModel;
	private JTable lapTable;

	// comboBox to add or remove driver from team
	private JComboBox comboBoxAddDriver;
	private JComboBox comboBoxAddTeam;
	private JComboBox comboBoxRemoveDriver;
	private JLabel lblRemoveTeam;

	// comboBox to assign or unassign karts
	private JComboBox comboBoxAssignDriver;
	private JComboBox comboBoxAssignKart;
	private JComboBox comboBoxUnassignDriver;
	
	// comboBox laps
	private JComboBox comboBoxDriverLap;
	private JLabel lblKart;

	// datePicker variables
	private UtilDateModel modelDatePicker;
	private JDatePickerImpl datePicker;

	// driver variables
	private Driver driver;
	private int driver_id = 0;

	// team variables
	private Team team;
	private int team_id = 0;

	// kart variables
	private Kart kart;
	private int kart_id = 0;
	
	// lap variables
	private Lap lap;
	private int lap_id = 0;
	private LapTimer lapTimer = new LapTimer();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App window = new App();
					window.frmKartingdatabase.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public App() {
		initialize();
	}

	public void refreshDriverTable() {
		driverModel.setRowCount(0);
		List<Driver> driverList = DriverDAO.selectAllDrivers();
		if (driverList != null)
			driverList.forEach(d -> {
				Object[] row = new Object[9];
				row[0] = d.getDriver_id();
				row[1] = d.getName();
				row[2] = d.getAge();
				row[3] = d.getLaps();
				row[4] = d.getRaces();
				row[5] = d.getPodiums();
				row[6] = d.getWins();
				row[7] = d.getTeam();
				row[8] = d.getKart();
				driverModel.addRow(row);
			});
	}

	public void refreshTeamTable() {
		teamModel.setRowCount(0);
		List<Team> teamList = TeamDAO.selectAllTeams();
		if (teamList != null)
			teamList.forEach(t -> {
				Object[] row = new Object[4];
				row[0] = t.getTeam_id();
				row[1] = t.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
				row[2] = t.getName();
				List<Driver> driversList = t.getDrivers();
				StringBuilder driversNames = new StringBuilder();
				for (Driver d : driversList) {
					driversNames.append(d.getName() + ", ");
				}
				if (driversNames.length() >= 2) {
					driversNames.delete(driversNames.length() - 2, driversNames.length() - 1);
				}
				row[3] = driversNames;
				teamModel.addRow(row);
			});
	}

	public void refreshComboBoxTeams() {
		comboBoxAddDriver.removeAllItems();
		comboBoxAddTeam.removeAllItems();
		comboBoxRemoveDriver.removeAllItems();
		List<Driver> driverAddList = DriverDAO.selectDriversWithoutTeam();
		List<Driver> driverRemoveList = DriverDAO.selectDriversWithTeam();
		List<Team> teamAddList = TeamDAO.selectAllTeams();
		if (driverAddList != null)
			driverAddList.forEach(d -> {
				comboBoxAddDriver.addItem(d.getName());
			});
		if (teamAddList != null)
			teamAddList.forEach(t -> {
				comboBoxAddTeam.addItem(t.getName());
			});
		if (driverRemoveList != null)
			driverRemoveList.forEach(d -> {
				comboBoxRemoveDriver.addItem(d.getName());
			});
	}

	public void refreshKartTable() {
		kartModel.setRowCount(0);
		List<Kart> kartList = KartDAO.selectAllKarts();
		if (kartList != null)
			kartList.forEach(k -> {
				Object[] row = new Object[2];
				row[0] = k.getKart_id();
				row[1] = k.isAvailable();
				kartModel.addRow(row);
			});
	}

	public void refreshComboBoxKarts() {
		comboBoxAssignKart.removeAllItems();
		comboBoxAssignDriver.removeAllItems();
		comboBoxUnassignDriver.removeAllItems();
		List<Driver> driverAddList = DriverDAO.selectDriversWithoutKart();
		List<Driver> driverRemoveList = DriverDAO.selectDriversWithKart();
		List<Kart> kartList = KartDAO.selectAvailableKarts();
		if (driverAddList != null)
			driverAddList.forEach(d -> {
				comboBoxAssignDriver.addItem(d.getName());
			});
		if (kartList != null)
			kartList.forEach(t -> {
				comboBoxAssignKart.addItem(t.getKart_id());
			});
		if (driverRemoveList != null)
			driverRemoveList.forEach(d -> {
				comboBoxUnassignDriver.addItem(d.getName());
			});
	}
	
	public void refreshLapTable() {
		lapModel.setRowCount(0);
		List<Lap> lapList = LapDAO.selectAllLaps();
		if (lapList != null)
			lapList.forEach(l -> {
				Object[] row = new Object[4];
				row[0] = l.getLap_id();
				row[1] = l.getDriver_id();
				row[2] = l.getTime();
				row[3] = l.getDate();
				lapModel.addRow(row);
			});
	}
	
	public void refreshComboBoxLaps() {
		comboBoxDriverLap.removeAllItems();
		List<Driver> driverList = DriverDAO.selectAllDrivers();
		if (driverList != null)
			driverList.forEach(d -> {
				comboBoxDriverLap.addItem(d.getName());
			});
	}

	public void refreshAll() {
		refreshDriverTable();
		refreshTeamTable();
		refreshComboBoxTeams();
		refreshKartTable();
		refreshComboBoxKarts();
		refreshLapTable();
		refreshComboBoxLaps();
	}

	private int parseTextFieldToInt(JTextField textField) {
		String text = textField.getText();
		if (!text.isEmpty() && text.matches("\\d+")) {
			return Integer.parseInt(text);
		} else {
			return 0;
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmKartingdatabase = new JFrame();
		frmKartingdatabase.setTitle("Karting Database");
		frmKartingdatabase.setBounds(100, 100, 1200, 850);
		frmKartingdatabase.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmKartingdatabase.getContentPane().setLayout(null);

		// tabbed pane with different panels
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(12, 12, 1166, 782);
		frmKartingdatabase.getContentPane().add(tabbedPane);

		JPanel driverPanel = new JPanel();
		tabbedPane.addTab("Drivers", null, driverPanel, null);
		driverPanel.setLayout(null);

		JPanel teamPanel = new JPanel();
		tabbedPane.addTab("Teams", null, teamPanel, null);
		teamPanel.setLayout(null);

		JPanel kartPanel = new JPanel();
		tabbedPane.addTab("Karts", null, kartPanel, null);
		
		JPanel lapPanel = new JPanel();
		tabbedPane.addTab("Laps", null, lapPanel, null);
		lapPanel.setLayout(null);
		
		JPanel racePanel = new JPanel();
		tabbedPane.addTab("Race", null, racePanel, null);
		racePanel.setLayout(null);

		// table models and tables
		// divers table
		driverModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		driverModel.addColumn("ID");
		driverModel.addColumn("Name");
		driverModel.addColumn("Age");
		driverModel.addColumn("Laps");
		driverModel.addColumn("Races");
		driverModel.addColumn("Podiums");
		driverModel.addColumn("Wins");
		driverModel.addColumn("Team");
		driverModel.addColumn("Kart");

		driversTable = new JTable(driverModel);
		driversTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = driversTable.getSelectedRow();
				TableModel model = driversTable.getModel();
				driver_id = (int) model.getValueAt(i, 0);
				driver = DriverDAO.selectDriver(driver_id);
				textFieldDriverName.setText(driver.getName());
				modelDatePicker.setValue(Date.from(driver.getDob().atStartOfDay(ZoneId.systemDefault()).toInstant()));
				textFieldLaps.setText(String.valueOf(driver.getLaps()));
				textFieldRaces.setText(String.valueOf(driver.getRaces()));
				textFieldPodiums.setText(String.valueOf(driver.getPodiums()));
				textFieldWins.setText(String.valueOf(driver.getWins()));
				Blob img = driver.getImg();
				if (img != null) {
					try {
						byte[] imageBytes = img.getBytes(1, (int) img.length());
						ImageIcon imageIcon = new ImageIcon(imageBytes);
						Image image = imageIcon.getImage();
						image.getScaledInstance(lblDriverImg.getWidth(), lblDriverImg.getHeight(), Image.SCALE_SMOOTH);
						ImageIcon resizedImage = new ImageIcon(image);
						lblDriverImg.setIcon(resizedImage);
					} catch (Exception imgException) {

					}
				} else {
					lblDriverImg.setIcon(null);
				}
			}
		});
		driversTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JScrollPane scrollPaneDrivers = new JScrollPane(driversTable);
		scrollPaneDrivers.setBounds(12, 12, 1137, 350);
		driverPanel.add(scrollPaneDrivers);

		// teams table
		teamModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		teamModel.addColumn("ID");
		teamModel.addColumn("Date");
		teamModel.addColumn("Name");
		teamModel.addColumn("Drivers");

		teamTable = new JTable(teamModel);
		teamTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = teamTable.getSelectedRow();
				team_id = (int) teamTable.getModel().getValueAt(i, 0);
				team = TeamDAO.selectTeam(team_id);
				textFieldTeamName.setText(team.getName());
				Blob img = team.getImg();
				if (img != null) {
					try {
						byte[] imageBytes = img.getBytes(1, (int) img.length());
						ImageIcon imageIcon = new ImageIcon(imageBytes);
						Image image = imageIcon.getImage();
						image.getScaledInstance(lblTeamImg.getWidth(), lblTeamImg.getHeight(), Image.SCALE_SMOOTH);
						ImageIcon resizedImage = new ImageIcon(image);
						lblTeamImg.setIcon(resizedImage);
					} catch (Exception imgException) {

					}
				} else {
					lblDriverImg.setIcon(null);
				}

			}
		});
		teamTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

		JScrollPane scrollPaneTeams = new JScrollPane(teamTable);
		scrollPaneTeams.setBounds(12, 12, 670, 450);
		teamPanel.add(scrollPaneTeams);

		// karts table
		kartModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		kartModel.addColumn("ID");
		kartModel.addColumn("available");

		kartTable = new JTable(kartModel);
		kartTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = kartTable.getSelectedRow();
				kart_id = (int) kartTable.getModel().getValueAt(i, 0);
				kart = KartDAO.selectKart(kart_id);
			}
		});
		kartPanel.setLayout(null);
		kartTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

		JScrollPane scrollPaneKarts = new JScrollPane(kartTable);
		scrollPaneKarts.setBounds(12, 12, 453, 403);
		kartPanel.add(scrollPaneKarts);
		
		// laps table
		lapModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		lapModel.addColumn("ID");
		lapModel.addColumn("Driver");
		lapModel.addColumn("Time");
		lapModel.addColumn("Date");
		
		lapTable = new JTable(lapModel);
		lapTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int i = lapTable.getSelectedRow();
				lap_id = (int) lapTable.getModel().getValueAt(i, 0);
				lap = LapDAO.selectLap(lap_id);
			}
		});
		lapTable.setLayout(null);
		lapTable.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		
		JScrollPane scrollPaneLaps = new JScrollPane(lapTable);
		scrollPaneLaps.setBounds(12, 12, 562, 500);
		lapPanel.add(scrollPaneLaps);
		
		

		JLabel lblTeamName = new JLabel("Team name:");
		lblTeamName.setBounds(12, 498, 101, 15);
		teamPanel.add(lblTeamName);

		textFieldTeamName = new JTextField();
		textFieldTeamName.setBounds(131, 493, 150, 25);
		teamPanel.add(textFieldTeamName);
		textFieldTeamName.setColumns(10);

		JLabel lblImage_1 = new JLabel("Select Image:");
		lblImage_1.setBounds(12, 535, 101, 14);
		teamPanel.add(lblImage_1);

		textFieldTeamImageText = new JTextField();
		textFieldTeamImageText.setEditable(false);
		textFieldTeamImageText.setBounds(131, 533, 150, 20);
		teamPanel.add(textFieldTeamImageText);
		textFieldTeamImageText.setColumns(10);

		JLabel lblName = new JLabel("Name:");
		lblName.setBounds(90, 430, 70, 15);
		driverPanel.add(lblName);

		JLabel lblBirth = new JLabel("Birth:");
		lblBirth.setBounds(348, 435, 70, 15);
		driverPanel.add(lblBirth);

		JLabel lblLaps = new JLabel("Laps:");
		lblLaps.setBounds(90, 475, 70, 15);
		driverPanel.add(lblLaps);

		JLabel lblRaces = new JLabel("Races");
		lblRaces.setBounds(90, 522, 70, 15);
		driverPanel.add(lblRaces);

		JLabel lblPodiums = new JLabel("Podiums:");
		lblPodiums.setBounds(90, 570, 70, 15);
		driverPanel.add(lblPodiums);

		JLabel lblWins = new JLabel("Wins:");
		lblWins.setBounds(90, 618, 70, 15);
		driverPanel.add(lblWins);

		modelDatePicker = new UtilDateModel();
		Properties properties = new Properties();
		properties.put("text.today", "Today");
		properties.put("text.month", "Month");
		properties.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(modelDatePicker, properties);

		datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		datePicker.setBounds(439, 425, 150, 25);
		driverPanel.add(datePicker);

		textFieldDriverName = new JTextField();
		textFieldDriverName.setBounds(163, 425, 150, 25);
		driverPanel.add(textFieldDriverName);
		textFieldDriverName.setColumns(10);

		textFieldLaps = new JTextField();
		textFieldLaps.setColumns(10);
		textFieldLaps.setBounds(163, 473, 150, 25);
		driverPanel.add(textFieldLaps);

		textFieldRaces = new JTextField();
		textFieldRaces.setColumns(10);
		textFieldRaces.setBounds(163, 520, 150, 25);
		driverPanel.add(textFieldRaces);

		textFieldPodiums = new JTextField();
		textFieldPodiums.setColumns(10);
		textFieldPodiums.setBounds(163, 568, 150, 25);
		driverPanel.add(textFieldPodiums);

		textFieldWins = new JTextField();
		textFieldWins.setColumns(10);
		textFieldWins.setBounds(163, 616, 150, 25);
		driverPanel.add(textFieldWins);

		JButton btnAddDriver = new JButton("Add");
		btnAddDriver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String name = textFieldDriverName.getText();
					if (name.isEmpty()) {
						throw new IllegalArgumentException("Name cannot be empty");
					} else if (DriverDAO.selectDriver(name) != null) {
						throw new IllegalArgumentException("Duplicated name");
					}

					LocalDate dob = null;
					int age = 0;
					if ((java.util.Date) datePicker.getModel().getValue() != null) {
						Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
						dob = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						LocalDate today = LocalDate.now();
						age = Period.between(dob, today).getYears();
					} else {
						throw new IllegalArgumentException("Birth cannot be empty");
					}
					int laps = parseTextFieldToInt(textFieldLaps);
					int races = parseTextFieldToInt(textFieldRaces);
					int podiums = parseTextFieldToInt(textFieldPodiums);
					int wins = parseTextFieldToInt(textFieldWins);
					Blob img = null;
					if (!textFieldDriverImageText.getText().isEmpty()) {
						try {
							String imagePath = textFieldDriverImageText.getText();
							byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
							img = new com.mysql.cj.jdbc.Blob(imageBytes, null);
						} catch (Exception imgException) {

						}
					}
					driver = new Driver(name, dob, age, laps, races, podiums, wins, img);
					DriverDAO.insertDriver(driver);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Driver inserted successfully");
					refreshAll();
				} catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(frmKartingdatabase, iae.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnAddDriver.setBounds(202, 706, 117, 25);
		driverPanel.add(btnAddDriver);

		JButton btnUpdateDriver = new JButton("Update");
		btnUpdateDriver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (driver_id == 0) {
					JOptionPane.showMessageDialog(frmKartingdatabase, "Not driver selected", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					driver = DriverDAO.selectDriver(driver_id);
					String name = textFieldDriverName.getText();
					if (name.isEmpty()) {
						throw new IllegalArgumentException("Name cannot be empty");
					}
					Date selectedDate = (java.util.Date) datePicker.getModel().getValue();
					LocalDate dob = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
					LocalDate today = LocalDate.now();
					int age = Period.between(dob, today).getYears();

					int laps = parseTextFieldToInt(textFieldLaps);
					int races = parseTextFieldToInt(textFieldRaces);
					int podiums = parseTextFieldToInt(textFieldPodiums);
					int wins = parseTextFieldToInt(textFieldWins);
					int team = 0;
					int kart = 0;
					Blob img = null;
					if (!textFieldDriverImageText.getText().isEmpty()) {
						try {
							String imagePath = textFieldDriverImageText.getText();
							byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
							img = new com.mysql.cj.jdbc.Blob(imageBytes, null);
						} catch (Exception imgException) {

						}
					}
					// without team and kart
					DriverDAO.updateDriver(driver, name, dob, age, laps, races, podiums, wins, team, kart, img);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Driver updated successfully");
					refreshAll();
				}
			}
		});
		btnUpdateDriver.setBounds(521, 706, 117, 25);
		driverPanel.add(btnUpdateDriver);

		JButton btnDeleteDriver = new JButton("Delete");
		btnDeleteDriver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (driver_id == 0) {
					JOptionPane.showMessageDialog(frmKartingdatabase, "No driver selected", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					driver = DriverDAO.selectDriver(driver_id);
					team = TeamDAO.selectTeam(driver.getTeam());
					if (team != null) {
						TeamDAO.updateTeamRemoveDriver(team, driver_id);
					}
					kart = KartDAO.selectKart(driver.getKart());
					if (kart != null) {
						KartDAO.updateKart(kart, true);
					}
					DriverDAO.deleteDriver(driver_id);
					lblDriverImg.setIcon(null);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Driver deleted successfully");
					refreshAll();
					driver_id = 0;
				}
			}
		});
		btnDeleteDriver.setBounds(840, 706, 117, 25);
		driverPanel.add(btnDeleteDriver);

		lblDriverImg = new JLabel();
		lblDriverImg.setBounds(808, 384, 300, 300);
		driverPanel.add(lblDriverImg);

		lblTeamImg = new JLabel("");
		lblTeamImg.setBounds(700, 12, 449, 450);
		teamPanel.add(lblTeamImg);

		JLabel lblImage = new JLabel("Select Image:");
		lblImage.setBounds(348, 478, 92, 14);
		driverPanel.add(lblImage);

		textFieldDriverImageText = new JTextField();
		textFieldDriverImageText.setEditable(false);
		textFieldDriverImageText.setBounds(439, 472, 150, 20);
		driverPanel.add(textFieldDriverImageText);
		textFieldDriverImageText.setColumns(10);

		JButton btnSelectDriverImage = new JButton("Select Image");
		btnSelectDriverImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				if (chooser.getSelectedFile() != null) {
					File f = chooser.getSelectedFile();
					String fileName = f.getAbsolutePath();
					textFieldDriverImageText.setText(fileName);
				}
			}
		});
		btnSelectDriverImage.setBounds(609, 470, 132, 25);
		driverPanel.add(btnSelectDriverImage);

		JButton btnSelectTeamImage = new JButton("Select Image");
		btnSelectTeamImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.showOpenDialog(null);
				if (chooser.getSelectedFile() != null) {
					File f = chooser.getSelectedFile();
					String fileName = f.getAbsolutePath();
					textFieldTeamImageText.setText(fileName);
				}
			}
		});
		btnSelectTeamImage.setBounds(293, 530, 132, 25);
		teamPanel.add(btnSelectTeamImage);

		JButton btnAddTeam = new JButton("Add");
		btnAddTeam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					String name = textFieldTeamName.getText();
					if (name.isEmpty()) {
						throw new IllegalArgumentException("Name cannot be empty");
					} else if (TeamDAO.selectTeam(name) != null && TeamDAO.selectTeam(name).getName().equals(name)) {
						throw new IllegalArgumentException("Duplicated name");
					}
					LocalDate date = LocalDate.now();
					Blob img = null;
					if (!textFieldTeamImageText.getText().isEmpty()) {
						try {
							String imagePath = textFieldTeamImageText.getText();
							byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
							img = new com.mysql.cj.jdbc.Blob(imageBytes, null);
						} catch (Exception imgException) {

						}
					}
					team = new Team(name, date, img);
					TeamDAO.insertTeam(team);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Team inserted successfully");
					refreshAll();
				} catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(frmKartingdatabase, iae.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnAddTeam.setBounds(31, 579, 82, 25);
		teamPanel.add(btnAddTeam);

		JButton btnUpdateTeam = new JButton("Upd");
		btnUpdateTeam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (team_id == 0) {
					JOptionPane.showMessageDialog(frmKartingdatabase, "No team selected", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					team = TeamDAO.selectTeam(team_id);
					String name = textFieldTeamName.getText();
					if (name.isEmpty()) {
						throw new IllegalArgumentException("Name cannot be empty");
					} else if (TeamDAO.selectTeam(name) != null && TeamDAO.selectTeam(name).getName().equals(name)) {
						throw new IllegalArgumentException("Duplicated name");
					}
					Blob img = null;
					if (!textFieldTeamImageText.getText().isEmpty()) {
						try {
							String imagePath = textFieldTeamImageText.getText();
							byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
							img = new com.mysql.cj.jdbc.Blob(imageBytes, null);
						} catch (Exception imgException) {

						}
					}
					TeamDAO.updateTeam(team, name, img);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Team updated successfully");
					lblTeamImg.setIcon(null);
					refreshAll();
				}
			}
		});
		btnUpdateTeam.setBounds(165, 579, 82, 25);
		teamPanel.add(btnUpdateTeam);

		JButton btnDeleteTeam = new JButton("Del");
		btnDeleteTeam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (team_id == 0) {
					JOptionPane.showMessageDialog(frmKartingdatabase, "No team selected", "Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					team = TeamDAO.selectTeam(team_id);
					if (team.getDrivers() != null) {
						for (Driver d : team.getDrivers()) {
							DriverDAO.updateDriverTeam(d, 0);
						}
					}
					TeamDAO.deleteTeam(team_id);
					lblTeamImg.setIcon(null);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Team deleted successfully");
					refreshAll();
				}
			}
		});
		btnDeleteTeam.setBounds(293, 579, 82, 25);
		teamPanel.add(btnDeleteTeam);

		JLabel lblAddDriverToTeam = new JLabel("Add driver to team");
		lblAddDriverToTeam.setBounds(542, 498, 144, 15);
		teamPanel.add(lblAddDriverToTeam);

		JLabel lblRemoveDriverFrom = new JLabel("Remove driver from team");
		lblRemoveDriverFrom.setBounds(806, 498, 183, 15);
		teamPanel.add(lblRemoveDriverFrom);

		JLabel lblNewLabel = new JLabel("Driver");
		lblNewLabel.setBounds(552, 535, 70, 15);
		teamPanel.add(lblNewLabel);

		JLabel lblTeam = new JLabel("Team");
		lblTeam.setBounds(552, 584, 70, 15);
		teamPanel.add(lblTeam);

		comboBoxAddDriver = new JComboBox();
		comboBoxAddDriver.setBounds(640, 530, 144, 25);
		teamPanel.add(comboBoxAddDriver);

		comboBoxAddTeam = new JComboBox();
		comboBoxAddTeam.setBounds(640, 579, 144, 25);
		teamPanel.add(comboBoxAddTeam);

		lblRemoveTeam = new JLabel();
		lblRemoveTeam.setBounds(866, 579, 144, 25);
		teamPanel.add(lblRemoveTeam);

		comboBoxRemoveDriver = new JComboBox();
		comboBoxRemoveDriver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (comboBoxRemoveDriver.getSelectedItem() != null) {
					String driverName = (String) comboBoxRemoveDriver.getSelectedItem();
					Driver driver = DriverDAO.selectDriver(driverName);
					Team team = TeamDAO.selectTeam(driver.getTeam());
					lblRemoveTeam.setText(team.getName());
				} else {
					lblRemoveTeam.setText(null);
				}
			}
		});
		comboBoxRemoveDriver.setBounds(866, 530, 144, 25);
		teamPanel.add(comboBoxRemoveDriver);

		JButton btnAddDriverToTeam = new JButton("Add");
		btnAddDriverToTeam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxAddDriver.getSelectedItem();
				String teamName = (String) comboBoxAddTeam.getSelectedItem();
				if (driverName == null || teamName == null) {
					JOptionPane.showMessageDialog(frmKartingdatabase, "You must select driver and team", "Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					Driver driver = DriverDAO.selectDriver(driverName);
					Team team = TeamDAO.selectTeam(teamName);
					TeamDAO.updateTeamAddDriver(team, driver);
					DriverDAO.updateDriverTeam(driver, team.getTeam_id());
					JOptionPane.showMessageDialog(frmKartingdatabase, "Driver added to team successfully");
					refreshAll();
				}
			}
		});
		btnAddDriverToTeam.setBounds(665, 631, 90, 25);
		teamPanel.add(btnAddDriverToTeam);

		JButton btnRemoveDriverFromTeam = new JButton("Remove");
		btnRemoveDriverFromTeam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxRemoveDriver.getSelectedItem();
				if (driverName == null) {
					JOptionPane.showMessageDialog(frmKartingdatabase, "You must select driver", "Error",
							JOptionPane.ERROR_MESSAGE);
				} else {
					Driver driver = DriverDAO.selectDriver(driverName);
					Team team = TeamDAO.selectTeam(driver.getTeam());
					TeamDAO.updateTeamRemoveDriver(team, driver.getDriver_id());
					DriverDAO.updateDriverTeam(driver, 0);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Driver removed from team successfully");
					refreshAll();
				}
			}
		});
		btnRemoveDriverFromTeam.setBounds(891, 631, 90, 25);
		teamPanel.add(btnRemoveDriverFromTeam);

		JButton btnAddKart = new JButton("Add");
		btnAddKart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				kart = new Kart(true);
				KartDAO.insertKart(kart);
				JOptionPane.showMessageDialog(frmKartingdatabase, "Kart added successfully");
				refreshAll();
			}
		});
		btnAddKart.setBounds(64, 465, 117, 25);
		kartPanel.add(btnAddKart);

		JButton btnDeleteKart = new JButton("Delete");
		btnDeleteKart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (kart_id == 0) {
					JOptionPane.showMessageDialog(null, "No kart selected", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					Driver driver = DriverDAO.selectDriverByKart(kart_id);
					DriverDAO.updateDriverKart(driver, 0);
					KartDAO.deleteKart(kart_id);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Kart removed successfully");
					refreshAll();
					driver_id = 0;
				}
			}
		});
		btnDeleteKart.setBounds(275, 465, 117, 25);
		kartPanel.add(btnDeleteKart);

		JLabel lblAssignKart = new JLabel("Assign kart");
		lblAssignKart.setBounds(523, 76, 100, 15);
		kartPanel.add(lblAssignKart);

		JLabel lblDriver = new JLabel("Driver:");
		lblDriver.setBounds(533, 103, 70, 15);
		kartPanel.add(lblDriver);

		JLabel lblKart = new JLabel("Kart:");
		lblKart.setBounds(533, 144, 70, 15);
		kartPanel.add(lblKart);

		comboBoxAssignDriver = new JComboBox();
		comboBoxAssignDriver.setBounds(621, 103, 136, 25);
		kartPanel.add(comboBoxAssignDriver);

		comboBoxAssignKart = new JComboBox();
		comboBoxAssignKart.setBounds(621, 139, 136, 25);
		kartPanel.add(comboBoxAssignKart);

		JLabel lblUnassignKart = new JLabel("");
		lblUnassignKart.setBounds(830, 144, 100, 15);
		kartPanel.add(lblUnassignKart);

		comboBoxUnassignDriver = new JComboBox();
		comboBoxUnassignDriver.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxUnassignDriver.getSelectedItem();
				if (driverName == null) {
					lblUnassignKart.setText(null);
				} else {
					Driver driver = DriverDAO.selectDriver(driverName);
					lblUnassignKart.setText(String.valueOf(driver.getKart()));
				}
			}
		});
		comboBoxUnassignDriver.setBounds(830, 98, 136, 25);
		kartPanel.add(comboBoxUnassignDriver);

		JLabel lblTxt = new JLabel("Unassign Kart");
		lblTxt.setBounds(802, 76, 100, 15);
		kartPanel.add(lblTxt);

		JButton btnAssign = new JButton("Assign");
		btnAssign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxAssignDriver.getSelectedItem();
				if (driverName == null || comboBoxAssignKart.getSelectedItem() == null) {
					JOptionPane.showMessageDialog(null, "You must select driver and kart", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					int kartId = (int) comboBoxAssignKart.getSelectedItem();
					Driver driver = DriverDAO.selectDriver(driverName);
					Kart kart = KartDAO.selectKart(kartId);
					DriverDAO.updateDriverKart(driver, kart.getKart_id());
					KartDAO.updateKart(kart, false);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Kart assigned successfully");
					refreshAll();
				}
			}
		});
		btnAssign.setBounds(593, 196, 117, 25);
		kartPanel.add(btnAssign);
		
		JLabel lblDriver_1 = new JLabel("Driver:");
		lblDriver_1.setBounds(605, 76, 70, 15);
		lapPanel.add(lblDriver_1);
		
		JLabel lblKart_1 = new JLabel("Kart:");
		lblKart_1.setBounds(784, 76, 70, 15);
		lapPanel.add(lblKart_1);
		
		JLabel lblKartLap = new JLabel("");
		lblKartLap.setBounds(804, 108, 70, 15);
		lapPanel.add(lblKartLap);
		
		JLabel lblOnLap = new JLabel("");
		lblOnLap.setForeground(Color.RED);
		lblOnLap.setBounds(958, 108, 46, 14);
		lapPanel.add(lblOnLap);

		JButton btnUnassign = new JButton("Unassign");
		btnUnassign.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxUnassignDriver.getSelectedItem();
				if (driverName == null) {
					JOptionPane.showMessageDialog(null, "You must select driver", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					Driver driver = DriverDAO.selectDriver(driverName);
					Kart kart = KartDAO.selectKart(driver.getKart());
					KartDAO.updateKart(kart, true);
					DriverDAO.updateDriverKart(driver, 0);
					JOptionPane.showMessageDialog(frmKartingdatabase, "Kart unassigned successfully");
					refreshAll();
				}
			}
		});
		btnUnassign.setBounds(838, 196, 117, 25);
		kartPanel.add(btnUnassign);
		
		comboBoxDriverLap = new JComboBox();
		comboBoxDriverLap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxDriverLap.getSelectedItem();
				if (driverName != null) {
					Driver driver = DriverDAO.selectDriver(driverName);
					lblKartLap.setText(String.valueOf(driver.getKart()));
				} else {
					lblKartLap.setText(null);
				}
			}
		});
		comboBoxDriverLap.setBounds(621, 103, 145, 25);
		lapPanel.add(comboBoxDriverLap);
		
		JButton btnStartLap = new JButton("Start lap");
		btnStartLap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String driverName = (String) comboBoxDriverLap.getSelectedItem();
				String kartId = lblKartLap.getText();
				if(driverName == null || kartId == null) {
					JOptionPane.showMessageDialog(null, "You must select driver with assigned kart", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					try {
	                    lapTimer.startLap();
	                    lblOnLap.setText("On Lap");
	                    comboBoxDriverLap.setEnabled(false);
	                } catch (IllegalStateException e) {
	                    JOptionPane.showMessageDialog(frmKartingdatabase, e.getMessage());
	                }
				}
			}
		});
		btnStartLap.setBounds(637, 165, 117, 25);
		lapPanel.add(btnStartLap);
		
		JButton btnFinishLap = new JButton("Finish lap");
        btnFinishLap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	try {
                    LocalTime lapTime = lapTimer.finishLap();
                    lblOnLap.setText(null);
                    comboBoxDriverLap.setEnabled(true);
    				String driverName = (String) comboBoxDriverLap.getSelectedItem();
                    Driver driver = DriverDAO.selectDriver(driverName);
                    lap = new Lap(driver.getDriver_id(), driver.getKart(), lapTime, LocalDate.now());
                    LapDAO.insertLap(lap);
                    refreshAll();
                } catch (IllegalStateException e) {
                    JOptionPane.showMessageDialog(frmKartingdatabase, e.getMessage());
                }
            }
        });
        btnFinishLap.setBounds(804, 165, 117, 25);
		lapPanel.add(btnFinishLap);
		
		JButton btnDeleteLap = new JButton("Delete lap");
		btnDeleteLap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(lap_id != 0) {
					LapDAO.deleteLap(lap_id);
                    JOptionPane.showMessageDialog(frmKartingdatabase, "Lap deleted successfully");
					lap_id = 0;
					refreshAll();
				} else {
                    JOptionPane.showMessageDialog(frmKartingdatabase, "No lap selected");
				}
			}
		});
		btnDeleteLap.setBounds(960, 166, 117, 25);
		lapPanel.add(btnDeleteLap);

		refreshAll();
	}
}
