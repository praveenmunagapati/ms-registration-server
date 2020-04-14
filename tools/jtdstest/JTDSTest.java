/*

Modified from sample code posted by Denis Popov at http://www.sql.ru/forum/actualthread.aspx?tid=180792

*/

import java.sql.*;

public class JTDSTest
{
	public static void main(String[] args) throws SQLException
	{
		DriverManager.registerDriver(new net.sourceforge.jtds.jdbc.Driver());

		try (Connection conn = getConnection(args))
        {
            try (Statement stmt = conn.createStatement())
            {
                try (ResultSet rs = stmt.executeQuery("SELECT 'Hello " + JTDSTest.class.getName() + "'"))
                {
                    while (rs.next())
                        System.out.println(rs.getString(1));
                }

//                testInfinity(conn);
            }
        }
	}

	private static Connection getConnection(String[] args) throws SQLException
    {
        return (3 == args.length ?
            DriverManager.getConnection(args[0], args[1], args[2]) :
            DriverManager.getConnection("jdbc:jtds:sqlserver://" + args[0] + ":" + args[1] + "/" + args[2], args[3], args[4]));
    }

	private static void testInfinity(Connection conn) throws SQLException
    {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO test.TestTable (IntNotNull, DatetimeNotNull, RealNull, BitNotNull) VALUES (?, ?, ?, ?)"))
        {
            ps.setInt(1, 1);
            ps.setDate(2, new Date(new java.util.Date().getTime()));
            ps.setDouble(3, 10.0);
            ps.setBoolean(4, true);
            ps.execute();
            ps.setDouble(1, Double.NEGATIVE_INFINITY);
            ps.execute();
        }
    }
}
