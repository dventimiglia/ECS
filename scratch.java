import com.davidaventimiglia.mdaec.model.*;
import com.davidaventimiglia.mdaec.renderers.*;
import java.util.*;
import java.sql.*;
AbstractSQLBasedElevatorControlSystem ecs = new AbstractSQLBasedElevatorControlSystem(2, 5, new FancyRenderer()){};
// Connection c = ecs.getConnection();
// Statement s = c.createStatement();
// DatabaseMetaData m = c.getMetaData();
// ResultSet r = m.getTables(null, null, null, null);
// while (r.next()) System.out.println(r.getString(3));