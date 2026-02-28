import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

// ─────────────────────────────────────────────
//  Student class: holds one student's data
// ─────────────────────────────────────────────
class Student {
    private String name;
    private ArrayList<Double> scores;

    public Student(String name) {
        this.name = name;
        this.scores = new ArrayList<>();
    }

    public void addScore(double score) {
        scores.add(score);
    }

    public String getName() { return name; }

    public double getAverage() {
        if (scores.isEmpty()) return 0;
        double sum = 0;
        for (double s : scores) sum += s;
        return sum / scores.size();
    }

    public double getHighest() {
        return scores.isEmpty() ? 0 : Collections.max(scores);
    }

    public double getLowest() {
        return scores.isEmpty() ? 0 : Collections.min(scores);
    }

    public String getLetterGrade() {
        double avg = getAverage();
        if (avg >= 90) return "A";
        if (avg >= 80) return "B";
        if (avg >= 70) return "C";
        if (avg >= 60) return "D";
        return "F";
    }

    public ArrayList<Double> getScores() { return scores; }
}

// ─────────────────────────────────────────────
//  Main Program
// ─────────────────────────────────────────────
public class StudentGradeTracker {

    static ArrayList<Student> students = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("  Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> addStudent();
                case "2" -> viewStudent();
                case "3" -> printSummaryReport();
                case "4" -> { System.out.println("\n  Goodbye! 👋\n"); running = false; }
                default  -> System.out.println("\n  ⚠  Invalid choice. Please enter 1–4.\n");
            }
        }
    }

    // ── Add a new student ──────────────────────
    static void addStudent() {
        System.out.println("\n─── Add Student ───────────────────────");
        System.out.print("  Student name: ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("  ⚠  Name cannot be empty.\n");
            return;
        }

        Student student = new Student(name);

        System.out.println("  Enter scores one by one.");
        System.out.println("  Type 'done' when finished.\n");

        while (true) {
            System.out.print("  Score: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("done")) break;

            try {
                double score = Double.parseDouble(input);
                if (score < 0 || score > 100) {
                    System.out.println("  ⚠  Score must be between 0 and 100.");
                } else {
                    student.addScore(score);
                    System.out.printf("  ✓ Added %.1f%n", score);
                }
            } catch (NumberFormatException e) {
                System.out.println("  ⚠  Invalid number. Try again.");
            }
        }

        if (student.getScores().isEmpty()) {
            System.out.println("  ⚠  No scores added. Student not saved.\n");
            return;
        }

        students.add(student);
        System.out.printf("%n  ✅ %s added! Average: %.1f (%s)%n%n",
            name, student.getAverage(), student.getLetterGrade());
    }

    // ── View a specific student ────────────────
    static void viewStudent() {
        if (students.isEmpty()) {
            System.out.println("\n  No students found. Add one first.\n");
            return;
        }

        System.out.println("\n─── Select Student ────────────────────");
        for (int i = 0; i < students.size(); i++) {
            System.out.printf("  [%d] %s%n", i + 1, students.get(i).getName());
        }

        System.out.print("\n  Enter number: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= students.size()) {
                System.out.println("  ⚠  Invalid selection.\n");
                return;
            }
            printStudentDetail(students.get(idx));
        } catch (NumberFormatException e) {
            System.out.println("  ⚠  Invalid input.\n");
        }
    }

    // ── Print one student's detail ─────────────
    static void printStudentDetail(Student s) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.printf( "║  %-36s║%n", s.getName());
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf( "║  Scores:  %-27s║%n", s.getScores().toString());
        System.out.printf( "║  Average: %-5.1f                         ║%n", s.getAverage());
        System.out.printf( "║  Highest: %-5.1f                         ║%n", s.getHighest());
        System.out.printf( "║  Lowest:  %-5.1f                         ║%n", s.getLowest());
        System.out.printf( "║  Grade:   %-3s                           ║%n", s.getLetterGrade());
        System.out.println("╚══════════════════════════════════════╝\n");
    }

    // ── Summary report for all students ───────
    static void printSummaryReport() {
        if (students.isEmpty()) {
            System.out.println("\n  No students to report. Add some first.\n");
            return;
        }

        // Compute class-wide stats
        double classHigh = Double.MIN_VALUE;
        double classLow  = Double.MAX_VALUE;
        double classSum  = 0;
        int    totalScores = 0;
        Student topStudent = null;

        for (Student s : students) {
            double avg = s.getAverage();
            classSum  += s.getAverage() * s.getScores().size();
            totalScores += s.getScores().size();
            if (avg > classHigh) { classHigh = avg; topStudent = s; }
            if (s.getLowest() < classLow) classLow = s.getLowest();
        }
        double classAvg = totalScores > 0 ? classSum / totalScores : 0;

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         STUDENT GRADE SUMMARY REPORT                ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf( "║  %-20s %8s %8s %8s %6s ║%n",
            "Name", "Avg", "High", "Low", "Grade");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        for (Student s : students) {
            System.out.printf("║  %-20s %8.1f %8.1f %8.1f %6s ║%n",
                truncate(s.getName(), 20),
                s.getAverage(),
                s.getHighest(),
                s.getLowest(),
                s.getLetterGrade());
        }

        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.printf( "║  CLASS AVERAGE: %-4.1f                                  ║%n", classAvg);
        System.out.printf( "║  CLASS HIGHEST: %-4.1f  |  CLASS LOWEST: %-4.1f          ║%n", classHigh, classLow);
        if (topStudent != null)
            System.out.printf("║  TOP STUDENT:  %-36s ║%n", topStudent.getName());
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }

    // ── Helpers ────────────────────────────────
    static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    static void printMenu() {
        System.out.println("┌──────────────────────────────────┐");
        System.out.println("│   GRADE TRACKER MENU             │");
        System.out.println("├──────────────────────────────────┤");
        System.out.println("│  1. Add Student                  │");
        System.out.println("│  2. View Student Detail          │");
        System.out.println("│  3. Print Summary Report         │");
        System.out.println("│  4. Exit                         │");
        System.out.println("└──────────────────────────────────┘");
    }

    static void printBanner() {
        System.out.println();
        System.out.println("  ╔═══════════════════════════════╗");
        System.out.println("  ║   STUDENT GRADE TRACKER v1.0  ║");
        System.out.println("  ╚═══════════════════════════════╝");
        System.out.println();
    }
}