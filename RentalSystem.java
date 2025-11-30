import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class RentalSystem {
    private static RentalSystem instance;

    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();

    private RentalSystem() {
        loadData();
    }

    public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }

    public boolean addVehicle(Vehicle vehicle) {
        if (findVehicleByPlate(vehicle.getLicensePlate()) != null) {
            System.out.println("A vehicle with this license plate already exists.");
            return false;
        }
        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;
    }

    public boolean addCustomer(Customer customer) {
        if (findCustomerById(customer.getCustomerId()) != null) {
            System.out.println("A customer with this ID already exists.");
            return false;
        }
        customers.add(customer);
        saveCustomer(customer);
        return true;
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Available) {
            vehicle.setStatus(Vehicle.VehicleStatus.Rented);
            RentalRecord record = new RentalRecord(vehicle, customer, date, amount, "RENT");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle rented to " + customer.getCustomerName());
        } else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Rented) {
            vehicle.setStatus(Vehicle.VehicleStatus.Available);
            RentalRecord record = new RentalRecord(vehicle, customer, date, extraFees, "RETURN");
            rentalHistory.addRecord(record);
            saveRecord(record);
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        } else {
            System.out.println("Vehicle is not rented.");
        }
    }

    public void displayVehicles(Vehicle.VehicleStatus status) {
        if (status == null) {
            System.out.println("\n=== All Vehicles ===");
        } else {
            System.out.println("\n=== " + status + " Vehicles ===");
        }

        System.out.printf("|%-16s | %-12s | %-12s | %-12s | %-6s | %-18s |%n",
                " Type", "Plate", "Make", "Model", "Year", "Status");
        System.out.println("|--------------------------------------------------------------------------------------------|");

        boolean found = false;
        for (Vehicle vehicle : vehicles) {
            if (status == null || vehicle.getStatus() == status) {
                found = true;
                String vehicleType;
                if (vehicle instanceof Car) {
                    vehicleType = "Car";
                } else if (vehicle instanceof Minibus) {
                    vehicleType = "Minibus";
                } else if (vehicle instanceof PickupTruck) {
                    vehicleType = "Pickup Truck";
                } else {
                    vehicleType = "Unknown";
                }
                System.out.printf("| %-15s | %-12s | %-12s | %-12s | %-6d | %-18s |%n",
                        vehicleType, vehicle.getLicensePlate(), vehicle.getMake(),
                        vehicle.getModel(), vehicle.getYear(), vehicle.getStatus().toString());
            }
        }
        if (!found) {
            if (status == null) {
                System.out.println("  No Vehicles found.");
            } else {
                System.out.println("  No vehicles with Status: " + status);
            }
        }
        System.out.println();
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }

    public void displayRentalHistory() {
        if (rentalHistory.getRentalHistory().isEmpty()) {
            System.out.println("  No rental history found.");
        } else {
            System.out.printf("|%-10s | %-12s | %-20s | %-12s | %-12s |%n",
                    " Type", "Plate", "Customer", "Date", "Amount");
            System.out.println("|-------------------------------------------------------------------------------|");

            for (RentalRecord record : rentalHistory.getRentalHistory()) {
                System.out.printf("| %-9s | %-12s | %-20s | %-12s | $%-11.2f |%n",
                        record.getRecordType(),
                        record.getVehicle().getLicensePlate(),
                        record.getCustomer().getCustomerName(),
                        record.getRecordDate().toString(),
                        record.getTotalAmount());
            }
            System.out.println();
        }
    }

    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }

    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }

    public void saveVehicle(Vehicle vehicle) {
        try (FileWriter fw = new FileWriter("vehicles.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {

            String type;
            if (vehicle instanceof Car) {
                type = "Car";
            } else if (vehicle instanceof Minibus) {
                type = "Minibus";
            } else if (vehicle instanceof PickupTruck) {
                type = "PickupTruck";
            } else {
                type = "Unknown";
            }

            pw.println(type + "," + vehicle.getLicensePlate() + "," +
                    vehicle.getMake() + "," + vehicle.getModel() + "," +
                    vehicle.getYear());
        } catch (IOException e) {
            System.out.println("Error saving vehicle.");
        }
    }

    public void saveCustomer(Customer customer) {
        try (FileWriter fw = new FileWriter("customers.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(customer.getCustomerId() + "," + customer.getCustomerName());
        } catch (IOException e) {
            System.out.println("Error saving customer.");
        }
    }

    public void saveRecord(RentalRecord record) {
        try (FileWriter fw = new FileWriter("rental_records.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println(record.getRecordType() + "," +
                    record.getVehicle().getLicensePlate() + "," +
                    record.getCustomer().getCustomerId() + "," +
                    record.getRecordDate() + "," +
                    record.getTotalAmount());
        } catch (IOException e) {
            System.out.println("Error saving rental record.");
        }
    }

    private void loadData() {
        File vehiclesFile = new File("vehicles.txt");
        if (vehiclesFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(vehiclesFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String type = parts[0];
                        String plate = parts[1];
                        String make = parts[2];
                        String model = parts[3];
                        int year = Integer.parseInt(parts[4]);

                        Vehicle v = null;
                        if (type.equalsIgnoreCase("Car")) {
                            v = new Car(make, model, year, 0);
                        } else if (type.equalsIgnoreCase("Minibus")) {
                            v = new Minibus(make, model, year, false);
                        } else if (type.equalsIgnoreCase("PickupTruck")) {
                            v = new PickupTruck(make, model, year, 0.0, false);
                        }
                        if (v != null) {
                            v.setLicensePlate(plate);
                            vehicles.add(v);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading vehicles.");
            }
        }

        File customersFile = new File("customers.txt");
        if (customersFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(customersFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        customers.add(new Customer(id, name));
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading customers.");
            }
        }

        File recordsFile = new File("rental_records.txt");
        if (recordsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(recordsFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String recordType = parts[0];
                        String plate = parts[1];
                        int customerId = Integer.parseInt(parts[2]);
                        LocalDate date = LocalDate.parse(parts[3]);
                        double amount = Double.parseDouble(parts[4]);

                        Vehicle v = findVehicleByPlate(plate);
                        Customer c = findCustomerById(customerId);

                        if (v != null && c != null) {
                            RentalRecord record =
                                    new RentalRecord(v, c, date, amount, recordType);
                            rentalHistory.addRecord(record);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error loading rental records.");
            }
        }
    }
}
