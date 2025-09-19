import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MySQLConnectExample {
    public class Student_Count {
    // New class to represent a Student
    public static class Student { // Make the nested class public and static for access
        String name;
        int yearLevel;
        String course;

        public Student( String name, int year_level, String course) {
            this.name = name;
            this.yearLevel = year_level;
            this.course = course;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // This method now correctly references the Student class from its own file
    public static void printStudentDetails(Map<String, Map<String, List<Student_Count.Student>>> studentGroups) {
        System.out.println("--- All Student Details ---");
        for (Map.Entry<String, Map<String, List<Student_Count.Student>>> groupEntry : studentGroups.entrySet()) {
            String groupName = groupEntry.getKey();
            Map<String, List<Student_Count.Student>> sections = groupEntry.getValue();

            for (Map.Entry<String, List<Student_Count.Student>> sectionEntry : sections.entrySet()) {
                String sectionName = sectionEntry.getKey();
                List<Student_Count.Student> students = sectionEntry.getValue();

                System.out.println("\n" + groupName + " - " + sectionName + ":");
                for (Student_Count.Student student : students) {
                    System.out.println("  - " + student.name + " (" + student.yearLevel + "th Year, " + student.course + ")");
                }
            }
        }
        System.out.println("--- End of Student Details ---");
    }

    // Group students by year level and course
    // New map structure to handle sections
    // ... (rest of the code remains the same)

    static Map<String, Map<String, List<Student_Count.Student>>> groupStudentsByCourseAndYear(int totalStudents, String[] courses, int numYearLevels, int maxStudentsPerSection) {
        Map<String, Map<String, List<Student_Count.Student>>> groups = new LinkedHashMap<>();
        Map<String, Character> sectionTrackers = new HashMap<>();
        int studentId = 1;
        Random rand = new Random();

        // Step 1: Initial grouping (as in the original code)
        while (studentId <= totalStudents) {
            String course = courses[rand.nextInt(courses.length)];
            int yearLevel = rand.nextInt(numYearLevels) + 1;

            String groupName = yearLevel + "th Year - " + course;
            groups.putIfAbsent(groupName, new LinkedHashMap<>());
            char currentSectionLetter = sectionTrackers.getOrDefault(groupName, 'A');
            String currentSectionName = course.substring(0, Math.min(course.length(), 2)).toUpperCase() + "-" + currentSectionLetter;

            Map<String, List<Student_Count.Student>> sections = groups.get(groupName);
            sections.putIfAbsent(currentSectionName, new ArrayList<>());
            List<Student_Count.Student> currentSection = sections.get(currentSectionName);

            if (currentSection.size() >= maxStudentsPerSection) {
                currentSectionLetter++;
                sectionTrackers.put(groupName, currentSectionLetter);
                currentSectionName = course.substring(0, Math.min(course.length(), 2)).toUpperCase() + "-" + currentSectionLetter;
                sections.put(currentSectionName, new ArrayList<>());
                currentSection = sections.get(currentSectionName);
            }

            currentSection.add(new Student_Count.Student("Student" + studentId, yearLevel, course));
            studentId++;
        }

        // Step 2: Optimization for small sections
        int minStudentsPerSection = 5;

        // Iterate through each main group (e.g., "1st Year - BSCS")
        for (Map.Entry<String, Map<String, List<Student_Count.Student>>> groupEntry : groups.entrySet()) {
            Map<String, List<Student_Count.Student>> sections = groupEntry.getValue();
            List<String> smallSections = new ArrayList<>();
            List<String> largeSections = new ArrayList<>();

            // Identify small and large sections
            for (Map.Entry<String, List<Student_Count.Student>> sectionEntry : sections.entrySet()) {
                if (sectionEntry.getValue().size() < minStudentsPerSection && sectionEntry.getValue().size() > 0) {
                    smallSections.add(sectionEntry.getKey());
                } else if (sectionEntry.getValue().size() >= minStudentsPerSection) {
                    largeSections.add(sectionEntry.getKey());
                }
            }

            // Redistribute students from small sections
            if (!smallSections.isEmpty() && !largeSections.isEmpty()) {
                int largeSectionIndex = 0;
                for (String smallSectionName : smallSections) {
                    List<Student_Count.Student> smallSectionStudents = sections.get(smallSectionName);
                    if (smallSectionStudents != null) {
                        for (Student_Count.Student student : smallSectionStudents) {
                            String targetSectionName = largeSections.get(largeSectionIndex);
                            sections.get(targetSectionName).add(student);
                            largeSectionIndex = (largeSectionIndex + 1) % largeSections.size();
                        }
                    }
                    // Step 3: Remove the small, now-empty section
                    sections.remove(smallSectionName);
                }
            }
        }

        return groups;
    }

    }


    public static void main(String[] args) {
        String url = "jdbc:mysql://127.0.0.1:3306/algocure";
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to MySQL!");

            // Print all details from courses table
            String query = "SELECT * FROM courses";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(meta.getColumnName(i) + ": " + rs.getString(i) + "  ");
                }
                System.out.println();
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

         int totalStudents = 1200;
        String[] courses = {"BSCS", "BSIT", "BSIS"};
        int numYearLevels = 4;
        int maxStudentsPerSection = 40;

        // Group students
        Map<String, Map<String, List<Student_Count.Student>>> studentGroups =
            Student_Count.groupStudentsByCourseAndYear(totalStudents, courses, numYearLevels, maxStudentsPerSection);

        // Print all student details
        Student_Count.printStudentDetails(studentGroups);

    }
}
