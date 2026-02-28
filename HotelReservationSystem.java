import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

// ─────────────────────────────────────────────────────────────────────────────
//  ROOM
// ─────────────────────────────────────────────────────────────────────────────
class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    enum Category {
        STANDARD("Standard", 2500),
        DELUXE("Deluxe",     4800),
        SUITE("Suite",       9500);

        final String label;
        final double price;
        Category(String label, double price) { this.label = label; this.price = price; }
    }

    private final int      roomNumber;
    private final Category category;
    private final String   description;
    private       boolean  available = true;

    Room(int roomNumber, Category category, String description) {
        this.roomNumber  = roomNumber;
        this.category    = category;
        this.description = description;
    }

    int      getRoomNumber()  { return roomNumber; }
    Category getCategory()    { return category; }
    String   getDescription() { return description; }
    boolean  isAvailable()    { return available; }
    void     setAvailable(boolean v) { this.available = v; }

    @Override
    public String toString() {
        return String.format("  Room %03d | %-8s | %-20s | P%.0f/night | %s",
            roomNumber, category.label, description,
            category.price, available ? "[Available]" : "[Occupied] ");
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  RESERVATION
// ─────────────────────────────────────────────────────────────────────────────
class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");

    enum Status { CONFIRMED, CANCELLED }

    private final String         bookingId;
    private final String         guestName;
    private final String         guestEmail;
    private final int            roomNumber;
    private final Room.Category  roomCategory;
    private final LocalDate      checkIn;
    private final LocalDate      checkOut;
    private final double         totalAmount;
    private final String         paymentMethod;
    private       Status         status = Status.CONFIRMED;

    Reservation(String bookingId, String guestName, String guestEmail,
                int roomNumber, Room.Category roomCategory,
                LocalDate checkIn, LocalDate checkOut, String paymentMethod) {
        this.bookingId     = bookingId;
        this.guestName     = guestName;
        this.guestEmail    = guestEmail;
        this.roomNumber    = roomNumber;
        this.roomCategory  = roomCategory;
        this.checkIn       = checkIn;
        this.checkOut      = checkOut;
        this.paymentMethod = paymentMethod;
        long nights        = ChronoUnit.DAYS.between(checkIn, checkOut);
        this.totalAmount   = nights * roomCategory.price;
    }

    String        getBookingId()    { return bookingId; }
    String        getGuestName()    { return guestName; }
    int           getRoomNumber()   { return roomNumber; }
    Room.Category getRoomCategory() { return roomCategory; }
    LocalDate     getCheckIn()      { return checkIn; }
    LocalDate     getCheckOut()     { return checkOut; }
    double        getTotalAmount()  { return totalAmount; }
    Status        getStatus()       { return status; }
    long          getNights()       { return ChronoUnit.DAYS.between(checkIn, checkOut); }

    void cancel() { this.status = Status.CANCELLED; }

    void printDetails() {
        System.out.println();
        System.out.println("  +--------------------------------------------------+");
        System.out.println("  |            BOOKING CONFIRMATION                  |");
        System.out.println("  +--------------------------------------------------+");
        System.out.printf( "  | Booking ID   : %-33s|%n", bookingId);
        System.out.printf( "  | Status       : %-33s|%n", status);
        System.out.println("  +--------------------------------------------------+");
        System.out.printf( "  | Guest Name   : %-33s|%n", guestName);
        System.out.printf( "  | Email        : %-33s|%n", guestEmail);
        System.out.println("  +--------------------------------------------------+");
        System.out.printf( "  | Room         : %03d %-30s|%n", roomNumber, "(" + roomCategory.label + ")");
        System.out.printf( "  | Check-In     : %-33s|%n", checkIn.format(FMT));
        System.out.printf( "  | Check-Out    : %-33s|%n", checkOut.format(FMT));
        System.out.printf( "  | Nights       : %-33d|%n", getNights());
        System.out.println("  +--------------------------------------------------+");
        System.out.printf( "  | Rate/Night   : P%-32.2f|%n", roomCategory.price);
        System.out.printf( "  | TOTAL AMOUNT : P%-32.2f|%n", totalAmount);
        System.out.printf( "  | Payment      : %-33s|%n", paymentMethod);
        System.out.println("  +--------------------------------------------------+");
        System.out.println();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  FILE STORAGE
// ─────────────────────────────────────────────────────────────────────────────
class FileStorage {
    private static final String FILE = "reservations.dat";

    @SuppressWarnings("unchecked")
    static List<Reservation> load() {
        File f = new File(FILE);
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
            List<Reservation> list = (List<Reservation>) in.readObject();
            System.out.println("  Loaded " + list.size() + " reservation(s) from file.");
            return list;
        } catch (Exception e) {
            System.out.println("  Could not load saved data: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    static void save(List<Reservation> reservations) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(new ArrayList<>(reservations));
        } catch (IOException e) {
            System.out.println("  Could not save data: " + e.getMessage());
        }
    }

    static void exportTxt(List<Reservation> reservations) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("bookings_report.txt"))) {
            pw.println("HOTEL RESERVATION REPORT");
            pw.println("=".repeat(70));
            pw.printf("%-10s %-18s %-6s %-12s %-12s %-12s %-10s%n",
                "ID", "Guest", "Room", "Check-In", "Check-Out", "Total", "Status");
            pw.println("-".repeat(70));
            for (Reservation r : reservations) {
                pw.printf("%-10s %-18s %-6d %-12s %-12s P%-11.2f %-10s%n",
                    r.getBookingId(), r.getGuestName(), r.getRoomNumber(),
                    r.getCheckIn(), r.getCheckOut(), r.getTotalAmount(), r.getStatus());
            }
            pw.println("=".repeat(70));
            System.out.println("  Report exported to bookings_report.txt");
        } catch (IOException e) {
            System.out.println("  Export failed: " + e.getMessage());
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HOTEL MANAGER
// ─────────────────────────────────────────────────────────────────────────────
class HotelManager {
    private final Map<Integer, Room>       rooms        = new LinkedHashMap<>();
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private int counter = 1000;

    HotelManager() {
        // Standard: 101-105
        String[] std = {"Garden View","Pool View","City View","Garden View","Pool View"};
        for (int i = 0; i < 5; i++) rooms.put(101+i, new Room(101+i, Room.Category.STANDARD, std[i]));
        // Deluxe: 201-205
        String[] dlx = {"Ocean View","Mountain View","Balcony","Skyline View","Sea Breeze"};
        for (int i = 0; i < 5; i++) rooms.put(201+i, new Room(201+i, Room.Category.DELUXE, dlx[i]));
        // Suite: 301-303
        String[] ste = {"Presidential","Honeymoon Suite","Penthouse"};
        for (int i = 0; i < 3; i++) rooms.put(301+i, new Room(301+i, Room.Category.SUITE, ste[i]));
    }

    void loadFromFile() {
        List<Reservation> saved = FileStorage.load();
        for (Reservation r : saved) {
            reservations.put(r.getBookingId(), r);
            if (r.getStatus() == Reservation.Status.CONFIRMED) {
                Room room = rooms.get(r.getRoomNumber());
                if (room != null) room.setAvailable(false);
            }
        }
        if (!saved.isEmpty()) counter = 1000 + saved.size();
    }

    List<Room> searchRooms(Room.Category filter) {
        return rooms.values().stream()
            .filter(r -> r.isAvailable() && (filter == null || r.getCategory() == filter))
            .collect(Collectors.toList());
    }

    Reservation book(String name, String email, int roomNum,
                     LocalDate in, LocalDate out, String payment) {
        Room room = rooms.get(roomNum);
        if (room == null)        throw new IllegalArgumentException("Room " + roomNum + " does not exist.");
        if (!room.isAvailable()) throw new IllegalStateException("Room " + roomNum + " is already occupied.");
        if (!out.isAfter(in))    throw new IllegalArgumentException("Check-out must be after check-in.");

        String id = "BK" + (++counter);
        Reservation r = new Reservation(id, name, email, roomNum, room.getCategory(), in, out, payment);
        room.setAvailable(false);
        reservations.put(id, r);
        FileStorage.save(new ArrayList<>(reservations.values()));
        return r;
    }

    boolean cancel(String id) {
        Reservation r = reservations.get(id);
        if (r == null || r.getStatus() == Reservation.Status.CANCELLED) return false;
        r.cancel();
        Room room = rooms.get(r.getRoomNumber());
        if (room != null) room.setAvailable(true);
        FileStorage.save(new ArrayList<>(reservations.values()));
        return true;
    }

    Reservation find(String id) { return reservations.get(id); }

    List<Reservation> allReservations() { return new ArrayList<>(reservations.values()); }

    void simulatePayment(String method, double amount) {
        System.out.println("\n  Processing payment...");
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        switch (method) {
            case "1" -> System.out.printf("  Payment of P%.2f charged to card. APPROVED.%n%n", amount);
            case "2" -> System.out.printf("  GCash payment of P%.2f confirmed.%n%n", amount);
            default  -> System.out.printf("  Cash payment of P%.2f accepted.%n%n", amount);
        }
    }

    void printStats() {
        long confirmed = reservations.values().stream().filter(r -> r.getStatus() == Reservation.Status.CONFIRMED).count();
        double revenue = reservations.values().stream()
            .filter(r -> r.getStatus() == Reservation.Status.CONFIRMED)
            .mapToDouble(Reservation::getTotalAmount).sum();
        long avail = rooms.values().stream().filter(Room::isAvailable).count();

        System.out.println("\n  +---------------------------------+");
        System.out.println("  |       HOTEL STATISTICS          |");
        System.out.println("  +---------------------------------+");
        System.out.printf( "  | Total Rooms     : %-12d  |%n", rooms.size());
        System.out.printf( "  | Available       : %-12d  |%n", avail);
        System.out.printf( "  | Occupied        : %-12d  |%n", rooms.size() - avail);
        System.out.printf( "  | Total Bookings  : %-12d  |%n", reservations.size());
        System.out.printf( "  | Confirmed       : %-12d  |%n", confirmed);
        System.out.printf( "  | Total Revenue   : P%-11.2f  |%n", revenue);
        System.out.println("  +---------------------------------+\n");
    }

    void exportReport() { FileStorage.exportTxt(new ArrayList<>(reservations.values())); }
}

// ─────────────────────────────────────────────────────────────────────────────
//  MAIN — Console UI
//  Compile:  javac HotelReservationSystem.java
//  Run:      java HotelReservationSystem
// ─────────────────────────────────────────────────────────────────────────────
public class HotelReservationSystem {

    static final HotelManager      manager = new HotelManager();
    static final Scanner           sc      = new Scanner(System.in);
    static final DateTimeFormatter DATE    = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        banner();
        manager.loadFromFile();

        boolean run = true;
        while (run) {
            menu();
            switch (input("Choice")) {
                case "1" -> searchRooms();
                case "2" -> makeBooking();
                case "3" -> viewBooking();
                case "4" -> cancelBooking();
                case "5" -> listBookings();
                case "6" -> manager.printStats();
                case "7" -> manager.exportReport();
                case "0" -> { System.out.println("\n  Goodbye!\n"); run = false; }
                default  -> System.out.println("\n  Invalid choice.\n");
            }
        }
    }

    // 1. Search rooms
    static void searchRooms() {
        System.out.println("\n  Filter: [1] All  [2] Standard  [3] Deluxe  [4] Suite");
        Room.Category filter = switch (input("Filter")) {
            case "2" -> Room.Category.STANDARD;
            case "3" -> Room.Category.DELUXE;
            case "4" -> Room.Category.SUITE;
            default  -> null;
        };
        List<Room> list = manager.searchRooms(filter);
        System.out.println();
        if (list.isEmpty()) { System.out.println("  No available rooms.\n"); return; }
        list.forEach(System.out::println);
        System.out.printf("%n  %d room(s) available.%n%n", list.size());
    }

    // 2. Make booking
    static void makeBooking() {
        System.out.println("\n--- New Reservation ---");
        String name = input("Guest name");
        if (name.isBlank()) { System.out.println("  Name required.\n"); return; }
        String email = input("Email");

        searchRooms();

        int roomNum;
        try { roomNum = Integer.parseInt(input("Room number").trim()); }
        catch (NumberFormatException e) { System.out.println("  Invalid room number.\n"); return; }

        LocalDate in  = readDate("Check-in  (yyyy-MM-dd)");
        LocalDate out = readDate("Check-out (yyyy-MM-dd)");
        if (in == null || out == null) return;

        System.out.println("  Payment: [1] Card  [2] GCash  [3] Cash");
        String pm = input("Choose");

        try {
            Reservation r = manager.book(name, email, roomNum, in, out, pm);
            manager.simulatePayment(pm, r.getTotalAmount());
            r.printDetails();
        } catch (Exception e) {
            System.out.println("\n  Booking failed: " + e.getMessage() + "\n");
        }
    }

    // 3. View booking
    static void viewBooking() {
        Reservation r = manager.find(input("Booking ID").trim().toUpperCase());
        if (r == null) System.out.println("\n  Booking not found.\n");
        else r.printDetails();
    }

    // 4. Cancel booking
    static void cancelBooking() {
        String id = input("Booking ID to cancel").trim().toUpperCase();
        Reservation r = manager.find(id);
        if (r == null) { System.out.println("\n  Booking not found.\n"); return; }
        if (r.getStatus() == Reservation.Status.CANCELLED) { System.out.println("\n  Already cancelled.\n"); return; }

        System.out.printf("%n  Cancel booking for %s (Room %d)? Type YES to confirm: ", r.getGuestName(), r.getRoomNumber());
        if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
            manager.cancel(id);
            System.out.println("  Booking " + id + " cancelled. Room is now available.\n");
        } else {
            System.out.println("  Cancellation aborted.\n");
        }
    }

    // 5. List all bookings
    static void listBookings() {
        List<Reservation> all = manager.allReservations();
        if (all.isEmpty()) { System.out.println("\n  No reservations found.\n"); return; }

        System.out.println();
        System.out.printf("  %-10s %-18s %-6s %-12s %-12s %-12s %-10s%n",
            "ID", "Guest", "Room", "Check-In", "Check-Out", "Total", "Status");
        System.out.println("  " + "-".repeat(82));
        for (Reservation r : all) {
            System.out.printf("  %-10s %-18s %-6d %-12s %-12s P%-11.2f %-10s%n",
                r.getBookingId(),
                r.getGuestName().length() > 18 ? r.getGuestName().substring(0,17)+"…" : r.getGuestName(),
                r.getRoomNumber(),
                r.getCheckIn(), r.getCheckOut(),
                r.getTotalAmount(), r.getStatus());
        }
        System.out.printf("%n  Total: %d booking(s)%n%n", all.size());
    }

    // Helpers
    static String input(String label) {
        System.out.printf("  > %s: ", label);
        return sc.nextLine();
    }

    static LocalDate readDate(String label) {
        try { return LocalDate.parse(input(label).trim(), DATE); }
        catch (DateTimeParseException e) {
            System.out.println("  Invalid date. Use yyyy-MM-dd (e.g. 2025-12-25)\n");
            return null;
        }
    }

    static void menu() {
        System.out.println("+-------------------------------------+");
        System.out.println("|      GRAND AZURE HOTEL SYSTEM       |");
        System.out.println("+-------------------------------------+");
        System.out.println("|  1. Search Available Rooms          |");
        System.out.println("|  2. Make a Reservation              |");
        System.out.println("|  3. View Booking Details            |");
        System.out.println("|  4. Cancel Reservation              |");
        System.out.println("|  5. List All Bookings               |");
        System.out.println("|  6. Hotel Statistics                |");
        System.out.println("|  7. Export Report to File           |");
        System.out.println("|  0. Exit                            |");
        System.out.println("+-------------------------------------+");
    }

    static void banner() {
        System.out.println("\n  ==========================================");
        System.out.println("       GRAND AZURE HOTEL SYSTEM v1.0      ");
        System.out.println("  ==========================================\n");
    }
}
