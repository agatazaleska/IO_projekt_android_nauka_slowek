import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists glossary");
            statement.executeUpdate("create table glossary (term string, definition string)");

            // przykładowe 10 pierwszych z https://en.wikipedia.org/wiki/Glossary_of_computer_science
            statement.executeUpdate("insert into glossary values('abstract data type', 'A mathematical model for data types in which a data type is defined by its behavior (semantics) from the point of view of a user of the data, specifically in terms of possible values, possible operations on data of this type, and the behavior of these operations.')");
            statement.executeUpdate("insert into glossary values('abstract method', 'One with only a signature and no implementation body.')");
            statement.executeUpdate("insert into glossary values('abstraction', 'In software engineering and computer science, the process of removing physical, spatial, or temporal details or attributes in the study of objects or systems in order to more closely attend to other details of interest; it is also very similar in nature to the process of generalization.')");
            statement.executeUpdate("insert into glossary values('agent architecture', 'A blueprint for software agents and intelligent control systems depicting the arrangement of components.')");
            statement.executeUpdate("insert into glossary values('agent-based model', 'A class of computational models for simulating the actions and interactions of autonomous agents (both individual or collective entities such as organizations or groups) with a view to assessing their effects on the system as a whole.')");
            statement.executeUpdate("insert into glossary values('aggregate function', 'In database management, a function in which the values of multiple rows are grouped together to form a single value of more significant meaning or measurement, such as a sum, count, or max.')");
            statement.executeUpdate("insert into glossary values('agile software development', 'An approach to software development under which requirements and solutions evolve through the collaborative effort of self-organizing and cross-functional teams and their customer(s)/end user(s).')");
            statement.executeUpdate("insert into glossary values('algorithm', 'An unambiguous specification of how to solve a class of problems.')");
            statement.executeUpdate("insert into glossary values('algorithm design', 'A method or mathematical process for problem-solving and for engineering algorithms.')");
            statement.executeUpdate("insert into glossary values('algorithmic efficiency', 'A property of an algorithm which relates to the number of computational resources used by the algorithm.')");

            ResultSet rs = statement.executeQuery("select * from glossary");

            int i = 1;
            while (rs.next()) {
                // read the result set
                System.out.print(i + ". term = " + rs.getString("term"));
                System.out.println("definition = " + rs.getString("definition") + "\n");
                i++;
            }

            new MemoryGame();
        } catch(SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch(SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }
}