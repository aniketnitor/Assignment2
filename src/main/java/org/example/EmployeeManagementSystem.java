package org.example;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

class Person implements Serializable {
    private String name;
    private int age;
    private String address;

    public Person(String name, int age, String address) {
        this.name = name;
        this.age = age;
        this.address = address;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}

class Employee extends Person implements Serializable {
    private Integer employeeId;
    private String department;
    private String designation;
    private Double salary;
    private LocalDate joiningDate;

    public Employee(String name, int age, String address,
                    Integer employeeId, String department,
                    String designation, Double salary,
                    LocalDate joiningDate) {
        super(name, age, address);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.salary = salary;
        this.joiningDate = joiningDate;
    }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public long getTenure() {
        return ChronoUnit.YEARS.between(joiningDate, LocalDate.now());
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', department='%s', designation='%s', salary=%.2f, joiningDate=%s, tenure=%d years}",
                employeeId, getName(), department, designation, salary,
                joiningDate.format(DateTimeFormatter.ISO_LOCAL_DATE), getTenure());
    }
}

public class EmployeeManagementSystem {
    private Map<Integer, Employee> employeeMap;
    private static final String FILE_PATH = "employees.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Scanner scanner;

    public EmployeeManagementSystem() {
        employeeMap = new HashMap<>();
        scanner = new Scanner(System.in);
        loadEmployees();
    }

    public void addEmployee(Employee employee) {
        Predicate<Employee> validateEmployee = emp ->
                emp.getName() != null && !emp.getName().isEmpty() &&
                        emp.getEmployeeId() != null && emp.getEmployeeId() > 0 &&
                        emp.getSalary() != null && emp.getSalary() > 0;

        if (validateEmployee.test(employee)) {
            employeeMap.put(employee.getEmployeeId(), employee);
            System.out.println("Employee added successfully: " + employee.getName());
        } else {
            System.out.println("Invalid employee data. Cannot add employee.");
        }
    }

    public void viewEmployees(Comparator<Employee> comparator) {
        employeeMap.values().stream()
                .sorted(comparator)
                .forEach(System.out::println);
    }

    public Optional<Employee> findEmployeeById(Integer id) {
        return Optional.ofNullable(employeeMap.get(id));
    }

    public List<Employee> filterEmployeesByJoiningDateRange(LocalDate start, LocalDate end) {
        return employeeMap.values().stream()
                .filter(emp -> !emp.getJoiningDate().isBefore(start) && !emp.getJoiningDate().isAfter(end))
                .collect(Collectors.toList());
    }

    public void updateEmployee(Integer id, Consumer<Employee> updateFunction) {
        findEmployeeById(id).ifPresentOrElse(
                employee -> {
                    updateFunction.accept(employee);
                    System.out.println("Employee updated successfully: " + employee.getName());
                },
                () -> System.out.println("Employee not found with ID: " + id)
        );
    }

    public void removeEmployee(Integer id) {
        if (employeeMap.remove(id) != null) {
            System.out.println("Employee removed successfully.");
        } else {
            System.out.println("Employee not found.");
        }
    }

    public void saveEmployees() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(new ArrayList<>(employeeMap.values()));
            System.out.println("Employees saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving employees: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadEmployees() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            List<Employee> employees = (List<Employee>) ois.readObject();
            employees.forEach(emp -> employeeMap.put(emp.getEmployeeId(), emp));
            System.out.println("Employees loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No existing employee data found. Starting with empty database.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading employees: " + e.getMessage());
        }
    }

    public void interactiveAddEmployee() {
        try {
            System.out.println("\n--- Add New Employee ---");

            System.out.print("Enter Employee ID: ");
            Integer employeeId = Integer.parseInt(scanner.nextLine());

            if (employeeMap.containsKey(employeeId)) {
                System.out.println("Employee ID already exists. Please use a unique ID.");
                return;
            }

            System.out.print("Enter Name: ");
            String name = scanner.nextLine();

            System.out.print("Enter Age: ");
            int age = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter Address: ");
            String address = scanner.nextLine();

            System.out.print("Enter Department: ");
            String department = scanner.nextLine();

            System.out.print("Enter Designation: ");
            String designation = scanner.nextLine();

            System.out.print("Enter Salary: ");
            Double salary = Double.parseDouble(scanner.nextLine());

            System.out.print("Enter Joining Date (yyyy-MM-dd): ");
            LocalDate joiningDate = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);

            Employee newEmployee = new Employee(name, age, address,
                    employeeId, department, designation, salary, joiningDate);

            addEmployee(newEmployee);
        } catch (NumberFormatException | DateTimeParseException e) {
            System.out.println("Invalid input. Employee not added.");
        }
    }

    public void interactiveViewEmployees() {
        System.out.println("\n--- View Employees ---");
        System.out.println("Sort By:");
        System.out.println("1. Name");
        System.out.println("2. Salary");
        System.out.println("3. Joining Date");
        System.out.print("Enter your choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());

            Comparator<Employee> comparator = switch (choice) {
                case 1 -> Comparator.comparing(Employee::getName);
                case 2 -> Comparator.comparing(Employee::getSalary).reversed();
                case 3 -> Comparator.comparing(Employee::getJoiningDate);
                default -> throw new IllegalArgumentException("Invalid choice");
            };

            viewEmployees(comparator);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid selection. Showing default (by name).");
            viewEmployees(Comparator.comparing(Employee::getName));
        }
    }

    public void interactiveSearchEmployee() {
        System.out.println("\n--- Search Employee ---");
        System.out.print("Enter Employee ID to search: ");

        try {
            Integer id = Integer.parseInt(scanner.nextLine());

            findEmployeeById(id)
                    .ifPresentOrElse(
                            System.out::println,
                            () -> System.out.println("Employee not found with ID: " + id)
                    );
        } catch (NumberFormatException e) {
            System.out.println("Invalid Employee ID.");
        }
    }

    public void interactiveUpdateEmployee() {
        System.out.println("\n--- Update Employee ---");
        System.out.print("Enter Employee ID to update: ");

        try {
            Integer id = Integer.parseInt(scanner.nextLine());

            findEmployeeById(id).ifPresentOrElse(
                    employee -> {
                        System.out.println("Current Employee Details: " + employee);
                        System.out.println("Select attribute to update:");
                        System.out.println("1. Name");
                        System.out.println("2. Age");
                        System.out.println("3. Address");
                        System.out.println("4. Department");
                        System.out.println("5. Designation");
                        System.out.println("6. Salary");
                        System.out.print("Enter your choice: ");

                        try {
                            int choice = Integer.parseInt(scanner.nextLine());

                            switch (choice) {
                                case 1 -> {
                                    System.out.print("Enter new Name: ");
                                    employee.setName(scanner.nextLine());
                                }
                                case 2 -> {
                                    System.out.print("Enter new Age: ");
                                    employee.setAge(Integer.parseInt(scanner.nextLine()));
                                }
                                case 3 -> {
                                    System.out.print("Enter new Address: ");
                                    employee.setAddress(scanner.nextLine());
                                }
                                case 4 -> {
                                    System.out.print("Enter new Department: ");
                                    employee.setDepartment(scanner.nextLine());
                                }
                                case 5 -> {
                                    System.out.print("Enter new Designation: ");
                                    employee.setDesignation(scanner.nextLine());
                                }
                                case 6 -> {
                                    System.out.print("Enter new Salary: ");
                                    employee.setSalary(Double.parseDouble(scanner.nextLine()));
                                }
                                default -> {
                                    System.out.println("Invalid choice.");
                                    return;
                                }
                            }

                            System.out.println("Employee updated successfully.");
                            System.out.println("Updated Employee: " + employee);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input.");
                        }
                    },
                    () -> System.out.println("Employee not found with ID: " + id)
            );
        } catch (NumberFormatException e) {
            System.out.println("Invalid Employee ID.");
        }
    }

    public void interactiveRemoveEmployee() {
        System.out.println("\n--- Remove Employee ---");
        System.out.print("Enter Employee ID to remove: ");

        try {
            Integer id = Integer.parseInt(scanner.nextLine());
            removeEmployee(id);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Employee ID.");
        }
    }

    public void interactiveFilterByJoiningDate() {
        System.out.println("\n--- Filter Employees by Joining Date ---");

        try {
            System.out.print("Enter Start Date (yyyy-MM-dd): ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);

            System.out.print("Enter End Date (yyyy-MM-dd): ");
            LocalDate endDate = LocalDate.parse(scanner.nextLine(), DATE_FORMATTER);

            List<Employee> filteredEmployees = filterEmployeesByJoiningDateRange(startDate, endDate);

            if (filteredEmployees.isEmpty()) {
                System.out.println("No employees found in the specified date range.");
            } else {
                System.out.println("Employees joining between " + startDate + " and " + endDate + ":");
                filteredEmployees.forEach(System.out::println);
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        }
    }

    public void displayMenu() {
        while (true) {
            System.out.println("\n--- Employee Management System ---");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Search Employee");
            System.out.println("4. Update Employee");
            System.out.println("5. Remove Employee");
            System.out.println("6. Filter Employees by Joining Date");
            System.out.println("7. Save and Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> interactiveAddEmployee();
                    case 2 -> interactiveViewEmployees();
                    case 3 -> interactiveSearchEmployee();
                    case 4 -> interactiveUpdateEmployee();
                    case 5 -> interactiveRemoveEmployee();
                    case 6 -> interactiveFilterByJoiningDate();
                    case 7 -> {
                        saveEmployees();
                        System.out.println("Exiting Employee Management System. Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    public static void main(String[] args) {
        EmployeeManagementSystem ems = new EmployeeManagementSystem();
        ems.displayMenu();
    }
}